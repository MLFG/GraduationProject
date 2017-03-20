package cn.edu.lin.graduationproject.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import cn.bmob.im.bean.BmobInvitation;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.adapter.InvitationAdapter;
import cn.edu.lin.graduationproject.constant.Constants;

/**
 * 呈现当前登录用户所收到的 添加好友申请
 */
public class NewFriendActivity extends BaseActivity {

    @Bind(R.id.lv_newfriend_listview)
    ListView listView;
    List<BmobInvitation> invitations;
    InvitationAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setMyContentView() {
        setContentView(R.layout.activity_new_friend);
    }

    @Override
    public void init() {
        super.init();
        initHeaderView();
        initListView();
    }

    private void initHeaderView(){
        setHeaderTitle("添加好友", Constants.Position.CENTER);
        setHeaderImage(Constants.Position.LEFT, R.drawable.back_arrow_2, true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initListView(){
        invitations = new ArrayList<>();
        adapter = new InvitationAdapter(this,invitations);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh(){
        // 从本地数据库的 tab_new_contacts 数据表中取出数据
        // 作为 ListView 的数据源
        List<BmobInvitation> list = bmobDB.queryBmobInviteList();
        Set<String> nameSet = new HashSet<>();
        // 对 list 进行数据的筛选
        List<BmobInvitation> filterList = new ArrayList<>();
        for(BmobInvitation bi : list){
            if(bi.getStatus() == 2 && !nameSet.contains(bi.getFromname())){
                filterList.add(bi);
                nameSet.add(bi.getFromname());
            }
        }
        adapter.addAll(filterList,true);
    }
}
