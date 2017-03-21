package cn.edu.lin.graduationproject.ui;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.dd.CircularProgressButton;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.listener.SaveListener;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.app.MyApp;
import cn.edu.lin.graduationproject.bean.MyUser;
import cn.edu.lin.graduationproject.constant.Constants;
import cn.edu.lin.graduationproject.util.NetUtil;
import cn.edu.lin.graduationproject.util.PinYinUtil;

public class RegistActivity extends BaseActivity {

    private static final String TAG = "RegistActivity";

    @BindView(R.id.et_regist_username)
    EditText etUsername;
    @BindView(R.id.et_regist_password)
    EditText etPassword;
    @BindView(R.id.et_regist_repassword)
    EditText etRePassword;
    @BindView(R.id.rg_regist_gender)
    RadioGroup rgGender;
    @BindView(R.id.btn_regist_regist)
    CircularProgressButton btnRegist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setMyContentView() {
        setContentView(R.layout.activity_regist);
    }

    @Override
    public void init() {
        super.init();
        initHeaderView();
    }

    private void initHeaderView(){
        setHeaderTitle("欢迎注册");
        setHeaderImage(Constants.Position.LEFT, R.drawable.back_arrow_2, true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpTo(LoginActivity.class,true);
            }
        });
    }

    /**
     * 单击 注册 按钮，注册新用户
     * @param view
     */
    @OnClick(R.id.btn_regist_regist)
    public void regist(View view){
        // 1.判空
        if(isEmpty(etUsername,etPassword,etRePassword)){
            return;
        }
        // 2.判断两次密码输入是否一致
        String password = etPassword.getText().toString();
        String rePassword = etRePassword.getText().toString();
        if(!password.equals(rePassword)){
            toast("两次密码不一致");
            etPassword.setText("");
            etRePassword.setText("");
            return;
        }
        // 3.判断是否有网络
        if(!NetUtil.isNetworkAvailable(this)){
            toast("当前网络不给力");
            return;
        }
        // 4.构建实体类，并进行注册
        final MyUser user = new MyUser();
        user.setUsername(etUsername.getText().toString());
        user.setPassword(etPassword.getText().toString());
        boolean gender = true;
        if(rgGender.getCheckedRadioButtonId() == R.id.rb_regist_girl){
            gender = false;
        }
        user.setGender(gender);
        // 设置用户的位置
        user.setLocation(MyApp.lastPoint);
        // 设置用户的拼音名字
        user.setPyname(PinYinUtil.getPinYin(etUsername.getText().toString()));
        // 设置用户的首字母
        user.setLetter(PinYinUtil.getFirstLetter(etUsername.getText().toString()));
        // 设置用户注册时所使用的设备 ID
        user.setInstallId(BmobInstallation.getInstallationId(this));
        // 设置用户注册时所使用的设备的类型
        user.setDeviceType("android");
        // CircularProgressButton 进入工作状态
        btnRegist.setIndeterminateProgressMode(true);
        btnRegist.setProgress(50);
        // 提交用户信息（signUp方法继承自BmobUser）
        user.signUp(this, new SaveListener() {
            @Override
            public void onSuccess() {
                btnRegist.setProgress(100);
                // 更新 _installation 数据表中
                // 当前设备所对应的数据记录的 uid 字段的值
                // 将其值改为刚刚注册并登录成功的用户的 username
                userManager.bindInstallationForRegister(user.getUsername());
                // 登录成功后，界面跳转到主界面
                jumpTo(MainActivity.class,true);
                // 让用户马上进行登录操作
                user.login(RegistActivity.this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        // 更新 _installation 数据表中
                        // 当前设备所对应的数据记录的 uid 字段的值
                        // 将其值改为刚刚注册并登录成功的用户的 username
                        userManager.bindInstallationForRegister(user.getUsername());
                        Log.d(TAG, "onSuccess: ");
                        // 登录成功后，界面跳转到主界面
                        jumpTo(MainActivity.class,true);
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        toastAndLog("登录失败",i,s);
                    }
                });
            }

            @Override
            public void onFailure(int i, String s) {
                btnRegist.setProgress(-1);
                switch (i){
                    case 202:
                        toast("用户名重复");
                        break;
                    default:
                        toastAndLog("注册失败",i,s);
                        break;
                }
                // 让 btnRegist 从错误状态恢复到正常状态
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnRegist.setProgress(0);
                    }
                },1500);
            }
        });
    }
}
