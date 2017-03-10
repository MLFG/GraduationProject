package cn.edu.lin.graduationproject.ui;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;

import com.dd.CircularProgressButton;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.constant.Constants;
import cn.edu.lin.graduationproject.util.NetUtil;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.et_login_username)
    EditText etUsername;
    @BindView(R.id.et_login_password)
    EditText etPassword;
    @BindView(R.id.btn_login_login)
    CircularProgressButton btnLogin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setMyContentView() {
        setContentView(R.layout.activity_login);
    }

    @Override
    public void init() {
        super.init();
        initHeaderView();
    }

    private void initHeaderView(){
        setHeaderTitle("欢迎使用");
        setHeaderImage(Constants.Position.LEFT,R.drawable.ic_launcher,false,null);
    }

    @OnClick(R.id.tv_login_regist)
    public void regist(View view){
        jumpTo(RegistActivity.class,true);
    }

    @OnClick(R.id.btn_login_login)
    public void login(View view){
        // 1.判空
        if(isEmpty(etUsername,etPassword)){
            return;
        }
        // 2.判网
        if(!NetUtil.isNetworkAvailable(this)){
            toast("当前网络不给力");
            return;
        }
        // 3.登录操作
        BmobChatUser user = new BmobChatUser();
        user.setUsername(etUsername.getText().toString());
        user.setPassword(etPassword.getText().toString());

        btnLogin.setIndeterminateProgressMode(true);
        btnLogin.setProgress(50);

        /** 登录成功后，一次调用了如下两个方法
         *  checkAndBindInstallation
         *  当用户在当前设备登录后，会去 _installation 数据表中检查
         *  该用户是否在其他设备上依然保持这登录状态
         *  如果是：则从当前设备向其余设备发送一条消息{"tag":"offline"}
         *  其余设备在收到该消息时，荧光做让当前登录用户强行下线的处理
         *  当前设备在发送消息完毕后，更新当前设备在 _installation 数据表中对应数据记录的 uid 字段值
         *  更新为当前登录用户的用户名
         *  updateInstallIdForUser
         *  更新当前登录用户在 _user 表中对应的数据记录的 installId 和 deviceType 两个字段值
         *  更新为当前所使用设备的设备 ID，deviceType 改为 android
         */
        userManager.login(user, new SaveListener() {
            @Override
            public void onSuccess() {
                btnLogin.setProgress(100);
                // 更新登录用户的位置
                updateUserLocation(new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        getMyFriends(new FindListener<BmobChatUser>() {
                            @Override
                            public void onSuccess(List<BmobChatUser> list) {
                                jumpTo(MainActivity.class,true);
                            }

                            @Override
                            public void onError(int i, String s) {
                                switch (i){
                                    case 1000:
                                        jumpTo(MainActivity.class,true);
                                        break;
                                    default:
                                        toastAndLog("查询当前登录用户好友列表失败",i,s);
                                        break;
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        toastAndLog("登录时，更新位置出错",i,s);
                    }
                });
            }

            @Override
            public void onFailure(int i, String s) {
                btnLogin.setProgress(-1);
                switch (i){
                    case 101:
                        toast("用户名或密码错误，请重试");
                        etPassword.setText("");
                        break;
                    default:
                        toastAndLog("登录失败，稍后重试",i,s);
                        break;
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnLogin.setProgress(0);
                    }
                },1500);
            }
        });
    }
}
