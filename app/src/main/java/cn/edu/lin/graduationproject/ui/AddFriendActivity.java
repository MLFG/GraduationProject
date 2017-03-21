package cn.edu.lin.graduationproject.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.adapter.AddFriendAdapter;
import cn.edu.lin.graduationproject.constant.Constants;
import cn.edu.lin.graduationproject.listener.OnDatasLoadFinishListener;

public class AddFriendActivity extends BaseActivity {

    private static final String TAG = "AddFriendActivity";

    @BindView(R.id.et_addfriend_username)
    EditText etUsername;
    @BindView(R.id.lv_addfriend_listview)
    PullToRefreshListView ptrListView;

    ListView listView;
    List<BmobChatUser> users;
    AddFriendAdapter adapter;

    List<BmobChatUser> friendList; // 当前登录用户的所有好友列表
    int page; // 当前查询第几页的用户
    private static final int PAGE_LIMIT = 5; // 一页查询几个用户

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setMyContentView() {
        setContentView(R.layout.activity_add_friend);
    }

    @Override
    public void init() {
        super.init();
        initHeaderView();
        initListView();
    }

    private void initListView() {
        listView = ptrListView.getRefreshableView();
        users = new ArrayList<>();
        adapter = new AddFriendAdapter(this,users);
        listView.setAdapter(adapter);

        ptrListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                page += 1;
                queryUserByPage(page, etUsername.getText().toString(), new OnDatasLoadFinishListener<BmobChatUser>() {
                    @Override
                    public void onLoadFinish(List<BmobChatUser> datas) {
                        ptrListView.onRefreshComplete();
                        if(datas == null){
                            toast("没有更多数据了");
                            ptrListView.setMode(PullToRefreshBase.Mode.DISABLED);
                        }else if(datas.size() == 0){
                            page += 1;
                            queryUserByPage(page,etUsername.getText().toString(),this);
                        }else{
                            adapter.addAll(datas,false);
                        }
                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AddFriendActivity.this,UserInfoActivity.class);
                intent.putExtra("from","stranger");
                intent.putExtra("name",adapter.getItem(position-1).getUsername());
                jumpTo(intent,false);
            }
        });
    }

    private void initHeaderView() {
        setHeaderTitle("搜索好友");
        setHeaderImage(Constants.Position.LEFT, R.drawable.back_arrow_2, true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 根据用户输入的名字进行精确搜索
     * @param view
     */
    @OnClick(R.id.btn_addfriend_search)
    public void search(View view){
        // 关闭 PullToRefreshListView 的刷新功能
        ptrListView.setMode(PullToRefreshBase.Mode.DISABLED);
        String username = etUsername.getText().toString();
        if(TextUtils.isEmpty(username)){
            // 用户未输入任何要搜索的内容
            return;
        }
        if(username.equals(userManager.getCurrentUserName())){
            // 用户输入的搜索名字与当前登录用户本身的用户名一致
            return;
        }
        if(isFriend(username)){
            // 如果要搜索的名字已经是当前登录用户的好友了
            toast(username + "已经是您的好友了");
            return;
        }
        queryUserByName(username);
    }

    private void queryUserByName(final String username) {
        BmobQuery<BmobChatUser> query = new BmobQuery<>();
        query.addWhereEqualTo("username",username);
        query.findObjects(this, new FindListener<BmobChatUser>() {
            @Override
            public void onSuccess(List<BmobChatUser> list) {
                if( list.size() > 0 ){
                    // 在 _user 表中找到了用户名为 username 的用户
                    Log.d(TAG, "onSuccess: list.size() = " + list.size());
                    adapter.addAll(list,true);
                }else{
                    toast("没有用户名为" + username + "的用户");
                }
            }

            @Override
            public void onError(int i, String s) {
                toastAndLog("查询用户失败，稍后重试",i,s);
            }
        });
    }

    /**
     * 判断 username 所对应的用户是否已经是当前登录用户的好友
     * @param username  用户名
     * @return  true    username 所对应的用户已经是当前登录用户好友
     *          false   username 所对应的用户还不是当前登录用户好友
     */
    private boolean isFriend(String username) {
        // 从本地数据库好友列表中获取当恰你等录用户所有好友列表
        if(friendList == null){
            friendList = bmobDB.getAllContactList();
        }
        for(BmobChatUser bcu : friendList){
            if(bcu.getUsername().equals(username)){
                return true;
            }
        }
        return false;
    }

    @OnClick(R.id.btn_addfriend_searchmore)
    public void searchMore(View view){
        // 让 PullToRefreshListView 可以上推
        ptrListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        final String username = etUsername.getText().toString();
        if(TextUtils.isEmpty(username)){
            return;
        }
        // 如果 username 与当前登录用户的用户名一致，也应该允许用户继续发起模糊搜索
        // eg：当前登录用户是 abc ，输入的 username 是 abc 那么可以搜索用户表中用户名包含 abc 的这些用户
        // 筛选当前登录用户的好友，也需要在获得查询结果后再做处理
        page = 0;
        queryUserByPage(page, username, new OnDatasLoadFinishListener<BmobChatUser>() {
            @Override
            public void onLoadFinish(List<BmobChatUser> datas) {
                if(datas == null){
                    toast("没有包含"+ username + "的用户");
                }else if(datas.size() == 0){
                    // 发起下一次的查询
                    page += 1;
                    queryUserByPage(page,username,this);
                }else{
                    adapter.addAll(datas,true);
                }
            }
        });
    }

    /**
     * 分页查询
     * 查询 _user 表中，用户名包含 username 的用户
     * @param page      页码
     * @param username  要查询被包含的用户名
     * @param listener  处理查询返回结果
     */
    private void queryUserByPage(int page, final String username, final OnDatasLoadFinishListener<BmobChatUser> listener){
        BmobQuery<BmobChatUser> query = new BmobQuery<>();
        // 设定查询条件
        query.addWhereNotEqualTo("username",userManager.getCurrentUserName());
        // 关于分页的设定
        // 忽略查询结果的前 PAGE——LIMIT * page 个数据
        query.setSkip(PAGE_LIMIT * page);
        // 设定一次最多返回多少个数据
        query.setLimit(PAGE_LIMIT);
        query.findObjects(this, new FindListener<BmobChatUser>() {
            @Override
            public void onSuccess(List<BmobChatUser> list) {
                // 1.list.size() == 0 _user 数据表中确实没有包含 username 的用户
                // 2.list.size() > 0
                //   2.1 有包含 username 的用户，这批用户全部都是当前登录用户的好友
                //   2.2 通过删选后还剩下数据，可以放到 ListView 中呈现
                if(list.size()>0){
                    // 将 list 不是当前登录用户好友且用户名中包含 username 的放入 list
                    List<BmobChatUser> listUser = new ArrayList<BmobChatUser>();
                    // 数据删选
                    for(BmobChatUser bcu : list){
                        if(!isFriend(bcu.getUsername()) && bcu.getUsername().contains(username)){
                            listUser.add(bcu);
                        }
                    }
                    // list 的两种情况
                    // 1. list.size() == 0 从服务器返回的这批数据，没有一个通过删选
                    // 2. list.size() > 0 从服务器返回的这批数据有数据通过删选
                    listener.onLoadFinish(list);
                }else{
                    listener.onLoadFinish(null);
                }
            }

            @Override
            public void onError(int i, String s) {
                toastAndLog("查询用户时错误",i,s);
            }
        });
    }
}
