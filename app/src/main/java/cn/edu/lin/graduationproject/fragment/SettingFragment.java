package cn.edu.lin.graduationproject.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Method;

import butterknife.Bind;
import butterknife.OnClick;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.app.MyApp;
import cn.edu.lin.graduationproject.constant.Constants;
import cn.edu.lin.graduationproject.ui.UserInfoActivity;
import cn.edu.lin.graduationproject.util.SPUtil;

/**
 * Created by liminglin on 17-3-1.
 */

public class SettingFragment extends BaseFragment {

    private static final String NOTIFICATION = "notification";
    private static final String SOUND = "sound";
    private static final String VIBRATE = "vibrate";

    private static final int SWITCH_ON = 0;
    private static final int SWITCH_OFF = 1;

    @Bind(R.id.tv_setting_username)
    TextView tvUsername;
    @Bind(R.id.tv_setting_notification)
    TextView tvNotification;
    @Bind(R.id.tv_setting_sound)
    TextView tvSound;
    @Bind(R.id.tv_setting_vibrate)
    TextView tvVibrate;
    @Bind(R.id.iv_setting_editornotification)
    ImageView ivNotification;
    @Bind(R.id.iv_setting_editorsound)
    ImageView ivSound;
    @Bind(R.id.iv_setting_editorvibrate)
    ImageView ivVibrate;

    SPUtil spUtil;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting,container,false);
        return view;
    }

    @Override
    public void init() {
        super.init();
        spUtil = new SPUtil(getActivity(),userManager.getCurrentUserObjectId());
        initHeaderView();
        initView();
    }

    private void initView() {
        // 当前登录用户的用户名
        tvUsername.setText(userManager.getCurrentUserName());
        // 根据当前登录用户的偏好设置文件 设置 TextView 和 ImageView 的显示
        if (spUtil.isAllowNotification()) {
            switcher(NOTIFICATION,SWITCH_ON);
            ivSound.setClickable(true);
            ivVibrate.setClickable(true);
        }else{
            switcher(NOTIFICATION,SWITCH_OFF);
            ivSound.setClickable(false);
            ivVibrate.setClickable(false);
        }

        if(spUtil.isAllowSound()){
            switcher(SOUND,SWITCH_ON);
        }else{
            switcher(SOUND,SWITCH_OFF);
        }

        if(spUtil.isAllowVibrate()){
            switcher(VIBRATE,SWITCH_ON);
        }else{
            switcher(VIBRATE,SWITCH_OFF);
        }

    }

    /**
     * 设定 ImageView 中的图片
     * TextView 中的文本
     * 偏好设置
     * @param tag
     * @param state
     */
    private void switcher(String tag, int state) {
        try{
            // "notification"
            String ivResName = "iv_setting_editor" + tag;
            // ImageView 对应的 id
            int ivResId = getResources().getIdentifier(ivResName,"id",getActivity().getPackageName());
            // TextView 对应的 id
            String tvResName = "tv_setting_" + tag;
            int tvResId = getResources().getIdentifier(tvResName,"id",getActivity().getPackageName());

            if(ivResId == 0 || tvResId == 0){
                throw new RuntimeException("未能找到正确的视图");
            }
            ImageView iv = (ImageView) getView().findViewById(ivResId);

            if(state == SWITCH_ON){
                iv.setImageResource(R.drawable.ic_switch_on);
            }else{
                iv.setImageResource(R.drawable.ic_switch_off);
            }

            TextView tv = (TextView) getView().findViewById(tvResId);
            tv.setText((state==SWITCH_ON?"允许":"禁止")+(NOTIFICATION.equals(tag)?"通知":(SOUND.equals(tag)?"声音":"振动")));

            // 调用 sputil 中的方法来设定偏好设置文件
            // "notification" -- "setNotification"
            char[] chars = tag.toCharArray();
            chars[0] -= 32;
            String methodName = "set" + new String(chars);
            Method method = spUtil.getClass().getDeclaredMethod(methodName,boolean.class);
            method.invoke(spUtil,state == SWITCH_ON ? true : false);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initHeaderView() {
        setHeaderTitle("设置", Constants.Position.CENTER);
    }

    @OnClick(R.id.iv_setting_editornotification)
    public void setNotification(View view){
        if(!spUtil.isAllowNotification()){
            switcher(NOTIFICATION,SWITCH_ON);
            ivSound.setClickable(true);
            ivVibrate.setClickable(true);
        }else{
            switcher(NOTIFICATION,SWITCH_OFF);
            ivSound.setClickable(false);
            ivVibrate.setClickable(false);

            switcher(SOUND,SWITCH_OFF);
            switcher(VIBRATE,SWITCH_OFF);
        }
    }

    @OnClick(R.id.iv_setting_editorsound)
    public void setSound(View view){
        if(!spUtil.isAllowSound()){
            switcher(SOUND,SWITCH_ON);
        }else{
            switcher(SOUND,SWITCH_OFF);
        }
    }

    @OnClick(R.id.iv_setting_editorvibrate)
    public void setVibrate(View view){
        if(!spUtil.isAllowVibrate()){
            switcher(VIBRATE,SWITCH_ON);
        }else{
            switcher(VIBRATE,SWITCH_OFF);
        }
    }

    @OnClick(R.id.btn_setting_logout)
    public void logout(View view){
        MyApp.logout();
    }

    @OnClick(R.id.iv_setting_editorusername)
    public void editUserInfo(View view){
        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
        intent.putExtra("from","me");
        jumpTo(intent,false);
    }
}
