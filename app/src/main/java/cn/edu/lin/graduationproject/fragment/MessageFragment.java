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

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobRecent;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.adapter.RecentAdapter;
import cn.edu.lin.graduationproject.constant.Constants;
import cn.edu.lin.graduationproject.ui.ChatActivity;
import cn.edu.lin.graduationproject.ui.MainActivity;
import cn.edu.lin.graduationproject.util.DialogUtil;

/**
 * Created by liminglin on 17-3-1.
 */

public class MessageFragment extends BaseFragment {

    @Bind(R.id.lv_message_listview)
    ListView listView;
    List<BmobRecent> recents;
    RecentAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message,container,false);
        return view;
    }

    @Override
    public void init() {
        super.init();
        initHeaderView();
        initListView();
    }

    private void initHeaderView() {
        setHeaderTitle("即时通信", Constants.Position.CENTER);
    }

    private void initListView() {
        recents = new ArrayList<>();
        adapter = new RecentAdapter(getActivity(),recents);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 更新未读消息状态为已读
                String toId = adapter.getItem(position).getTargetid();
                bmobDB.resetUnread(toId);
                // 传递用户点击的绘画目标用户是谁
                String username = adapter.getItem(position).getUserName();
                BmobQuery<BmobChatUser> query = new BmobQuery<BmobChatUser>();
                query.addWhereEqualTo("username",username);
                query.findObjects(getActivity(), new FindListener<BmobChatUser>() {
                    @Override
                    public void onSuccess(List<BmobChatUser> list) {
                        BmobChatUser user = list.get(0);
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("user",user);
                        jumpTo(intent,false);
                    }

                    @Override
                    public void onError(int i, String s) {
                        toastAndLog("查询用户失败",i,s);
                    }
                });
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                DialogUtil.showDialog(getActivity(), "删除通知", "您确实要删除与" + adapter.getItem(position).getUserName() + "之间的所有聊天记录吗？", true, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 删除会话的操作
                        // 从会话表(recent)中删除数据记录
                        bmobDB.deleteRecent(adapter.getItem(position).getTargetid());
                        // 从聊天表(chat)中删除数据记录
                        bmobDB.deleteMessages(adapter.getItem(position).getTargetid());
                        // 从 ListView 的数据源中将该数据删除
                        adapter.remove(adapter.getItem(position));
                        // 更新 MainActivity 中总的未读消息的数量
                        MainActivity mat = (MainActivity) getActivity();
                        mat.setBadgeCount();
                    }
                });
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        // 从当前登录用户对应数据库的 recent 表中
        // 取出所有数据记录放到 ListView 中呈现
        List<BmobRecent> list = bmobDB.queryRecents();
        adapter.addAll(list,true);
    }
}
