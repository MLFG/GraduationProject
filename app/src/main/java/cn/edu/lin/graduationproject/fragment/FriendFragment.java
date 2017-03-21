package cn.edu.lin.graduationproject.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.v3.listener.UpdateListener;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.adapter.FriendAdapter;
import cn.edu.lin.graduationproject.bean.MyUser;
import cn.edu.lin.graduationproject.constant.Constants;
import cn.edu.lin.graduationproject.ui.AddFriendActivity;
import cn.edu.lin.graduationproject.ui.MainActivity;
import cn.edu.lin.graduationproject.ui.NearFriendActivity;
import cn.edu.lin.graduationproject.ui.NewFriendActivity;
import cn.edu.lin.graduationproject.ui.UserInfoActivity;
import cn.edu.lin.graduationproject.util.DialogUtil;
import cn.edu.lin.graduationproject.util.PinYinUtil;
import cn.edu.lin.graduationproject.view.MyLetterView;

/**
 * Created by liminglin on 17-3-1.
 */

public class FriendFragment extends BaseFragment {

    private static final String TAG = "FriendFragment";

    @BindView(R.id.lv_friend_listview)
    ListView listView;
    List<MyUser> users;
    FriendAdapter adapter;
    @BindView(R.id.mlv_friend_letters)
    MyLetterView mlvLetters;
    @BindView(R.id.tv_friend_letter)
    TextView tvLetter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend,container,false);
        return view;
    }

    @Override
    public void init() {
        super.init();
        initHeaderView();
        initListView();
        initView();
    }

    private void initHeaderView() {
        setHeaderTitle("好友", Constants.Position.CENTER);
        setHeaderImage(Constants.Position.RIGHT, R.drawable.ic_add_newfriend, true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击跳转到添加好友界面
                jumpTo(AddFriendActivity.class,false);
            }
        });
    }

    private void initView() {
        mlvLetters.setOnTouchLetterListener(new MyLetterView.OnTouchLetterListener() {
            @Override
            public void onTouchLetter(String letter) {
                listView.setSelection(adapter.getPositionForSection(letter.charAt(0))+1);
                tvLetter.setVisibility(View.VISIBLE);
                tvLetter.setText(letter);
            }

            @Override
            public void onReleaseLetter() {
                tvLetter.setVisibility(View.INVISIBLE);
                tvLetter.setText("");
            }
        });
    }

    private void initListView() {
        users = new ArrayList<>();
        adapter = new FriendAdapter(getActivity(),users);

        View header = LayoutInflater.from(getActivity()).inflate(R.layout.header_listview_friend,listView,false);
        listView.addHeaderView(header,null,false); // false 就是为了不让 headerview 被选中

        // 为 header 中的两个 TextView 添加单击事件监听
        TextView tvNew = (TextView) header.findViewById(R.id.tv_header_newfriend);
        tvNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击跳转到 NewFriendActivity 显示收到的 添加好友申请
                jumpTo(NewFriendActivity.class,false);
            }
        });

        TextView tvNear = (TextView) header.findViewById(R.id.tv_header_nearfriend);

        tvNear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击跳转到 NearFriendActivity 显示附件的陌生人
                jumpTo(NearFriendActivity.class,false);
            }
        });

        listView.setAdapter(adapter);
        // 长按删除好友
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                DialogUtil.showDialog(getActivity(), "删除通知", "您确定要删除您的好友" + adapter.getItem(position).getUsername(), true, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 1.解除好友关系（从服务器端解除，从本地数据库的friends表中解除）
                        // 2.删除聊天记录（删除会话信息，删除所有的聊天信息）
                        userManager.deleteContact(adapter.getItem(position).getObjectId(), new UpdateListener() {
                            @Override
                            public void onSuccess() {
                                // 3.从 FriendFragment 的 listView 中删除掉该好友
                                adapter.remove(adapter.getItem(position-1));
                                // 4.更新 MainActivity 中总的未读消息数量
                                MainActivity mat = (MainActivity) getActivity();
                                mat.refreshMessageFragment();
                            }

                            @Override
                            public void onFailure(int i, String s) {
                                toastAndLog("删除好友失败，请稍后充实",i,s);
                            }
                        });
                    }
                });
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 跳转到 UserInfoActivity 查看好友资料
                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                intent.putExtra("from","friend");
                intent.putExtra("name",adapter.getItem(position-1).getUsername());
                jumpTo(intent,false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        // 获取 ListView 中呈现的数据源
        List<BmobChatUser> contacts = bmobDB.getAllContactList();
        // 根据 List<BmobChatUser> 获得一个 List<MyUser>
        List<MyUser> list = getMyUserList(contacts);
        // 对数据进行排序
        Collections.sort(list, new Comparator<MyUser>() {
            @Override
            public int compare(MyUser o1, MyUser o2) {
                // 提供比较完善的排序方式
                char mychar = '#' + 128;
                String l = o1.getPyname();
                if(l.charAt(0) == '#'){
                    l = l.replace('#',mychar);
                }
                String r = o2.getPyname();
                if(r.charAt(0) == '#'){
                    r = r.replace('#',mychar);
                }
                if(l.charAt(0) == mychar && r.charAt(0) == mychar){
                    return o1.getUsername().compareTo(o2.getUsername());
                }else{
                    return l.compareTo(r);
                }
            }
        });
        // 将排序好的数据放到 listView 中呈现
        adapter.addAll(list,true);
    }

    /**
     * 根据 List<BmobChatUser> 获得一个 List<MyUser>
     * @param contacts
     * @return
     */
    private List<MyUser> getMyUserList(List<BmobChatUser> contacts) {
        List<MyUser> list = new ArrayList<>();
        for(BmobChatUser bcu : contacts){
            MyUser mu = new MyUser();
            mu.setAvatar(bcu.getAvatar());
            mu.setUsername(bcu.getUsername());
            mu.setPyname(PinYinUtil.getPinYin(bcu.getUsername()));
            mu.setLetter(PinYinUtil.getFirstLetter(bcu.getUsername()));
            // 为了删除好友时，必须提供 objectId
            mu.setObjectId(bcu.getObjectId());
            list.add(mu);
        }
        return list;
    }

}