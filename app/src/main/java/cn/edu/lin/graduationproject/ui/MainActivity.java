package cn.edu.lin.graduationproject.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.inteface.EventListener;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.adapter.MyPagerAdapter;
import cn.edu.lin.graduationproject.app.MyApp;
import cn.edu.lin.graduationproject.constant.Constants;
import cn.edu.lin.graduationproject.fragment.FriendFragment;
import cn.edu.lin.graduationproject.fragment.MessageFragment;
import cn.edu.lin.graduationproject.receiver.MyReceiver;
import cn.edu.lin.graduationproject.util.DialogUtil;
import cn.edu.lin.graduationproject.util.SPUtil;
import cn.edu.lin.graduationproject.view.BadgeView;
import cn.edu.lin.graduationproject.view.MyTabIcon;

public class MainActivity extends BaseActivity implements EventListener{
    private static final String TAG = "MainActivity";

    @BindView(R.id.vp_main_viewpager)
    ViewPager viewPager;
    MyPagerAdapter adapter;

    @BindView(R.id.mti_main_message)
    MyTabIcon mtiMessage;
    @BindView(R.id.mti_main_friend)
    MyTabIcon mtiFriend;
    @BindView(R.id.mti_main_find)
    MyTabIcon mtiFind;
    @BindView(R.id.mti_main_setting)
    MyTabIcon mtiSetting;

    MyTabIcon[] tabIcons;

    @BindView(R.id.iv_main_newinvitation)
    ImageView ivNewInvitation;

    SPUtil spUtil;

    @BindView(R.id.bv_main_unread)
    BadgeView bvCount;

    AddFriendReceiver addFriendReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setMyContentView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    public void init() {
        super.init();
        spUtil = new SPUtil(this,userManager.getCurrentUserObjectId());
        initViewPager();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        addFriendReceiver = new AddFriendReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ADD_FRIEND);
        registerReceiver(addFriendReceiver,filter);

        MyReceiver.regist(this);

        // 更新当前登录用户所对应数据库中是否有未处理的添加好友申请来决定 ivNewInation是否可见
        if(bmobDB.hasNewInvite()){
            ivNewInvitation.setVisibility(View.VISIBLE);
        }else{
            ivNewInvitation.setVisibility(View.INVISIBLE);
        }
        setBadgeCount();
    }

    /**
     * 设置未读消息
     */
    public void setBadgeCount() {
        int count = bmobDB.getAllUnReadCount();
        bvCount.setBadgeCount(count);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(addFriendReceiver);
        MyReceiver.unRegist(this);
    }

    private void initView() {
        tabIcons = new MyTabIcon[4];
        tabIcons[0] = mtiMessage;
        tabIcons[1] = mtiFriend;
        tabIcons[2] = mtiFind;
        tabIcons[3] = mtiSetting;
        for(MyTabIcon mti : tabIcons){
            mti.setPaintAlpha(0);
        }
        tabIcons[0].setPaintAlpha(255);
    }

    private void initViewPager() {
        adapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            /**
             * position 页码
             * positionOffset 滑动百分比
             * positionOffsetPixels 挥动的像素值
             * @param position
             * @param positionOffset
             * @param positionOffsetPixels
             */
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position < 3){
                    // 颜色由彩色 -- 灰色变化 255 -- 0
                    tabIcons[position].setPaintAlpha((int) (255*(1-positionOffset)));
                    // 颜色由灰色 -- 彩色变化 0 -- 255
                    tabIcons[position+1].setPaintAlpha((int) (255*positionOffset));
                }
            }

            @Override
            public void onPageSelected(int position) {
                for(MyTabIcon mti : tabIcons){
                    mti.setPaintAlpha(0);
                }
                tabIcons[position].setPaintAlpha(255);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @OnClick({R.id.mti_main_message,R.id.mti_main_friend,R.id.mti_main_find,R.id.mti_main_setting})
    public void setCurrentPage(View view){
        switch (view.getId()){
            case R.id.mti_main_message:
                viewPager.setCurrentItem(0,false);
                break;
            case R.id.mti_main_friend:
                viewPager.setCurrentItem(1,false);
                break;
            case R.id.mti_main_find:
                viewPager.setCurrentItem(2,false);
                break;
            default:
                viewPager.setCurrentItem(3,false);
                break;
        }
    }

    @Override
    public void onMessage(BmobMsg bmobMsg) {

    }

    @Override
    public void onReaded(String s, String s1) {

    }

    @Override
    public void onNetChange(boolean b) {

    }

    @Override
    public void onAddUser(BmobInvitation bmobInvitation) {
        // 当收到了 添加好友申请 时，该方法会被 MyReceiver
        ivNewInvitation.setVisibility(View.VISIBLE);
        if(spUtil.isAllowSound()){
            MyApp.mediaPlayer.start();
        }
    }

    @Override
    public void onOffline() {
        // 当受到下线通知时，该方法会被 MyReceiver 调用
        DialogUtil.showDialog(this, "下线通知", "检测到您的帐号在另一台设备登录，请你重新登录!", false, (dialog, which) -> MyApp.logout());
    }

    public void refreshMessageFragment(){
        // 在 MainActivity 中调用 MessageFragment 的 refresh 方法
        // 以刷新 MessageFragment ListView 中的数据
        MessageFragment mf = (MessageFragment) adapter.getItem(0);
        mf.refresh();
        // 更改 MainActivity bvCount 未读消息的数量
        setBadgeCount();
    }

    public class AddFriendReceiver extends BroadcastReceiver{


        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(Constants.ADD_FRIEND.equals(action)){
                // 刷新好友列表(FriendFragment 的 listView)
                FriendFragment ff = (FriendFragment) adapter.getItem(1);
                ff.refresh();
                // 刷新会话列表( MessageFragment的listView )
                // 刷新 MainActivity 的总的纬度消息数量
                refreshMessageFragment();
            }
        }
    }
}
