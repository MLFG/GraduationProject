package cn.edu.lin.graduationproject.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import butterknife.ButterKnife;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.db.BmobDB;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.app.MyApp;
import cn.edu.lin.graduationproject.bean.MyUser;
import cn.edu.lin.graduationproject.constant.Constants;

/**
 * Created by liminglin on 17-2-28.
 */

public abstract class BaseActivity extends FragmentActivity {
    private static final String TAG = "BaseActivity";

    // 用于管理用户（用户的登录，登出，添加好友，删除好友）
    BmobUserManager userManager;
    // 用于管理聊天内容（聊天内容的创建、发送、删除、存储）
    BmobChatManager chatManager;
    // BmobIMSDK 使用 Sqlite 数据库作为本地缓存
    // 策略是建立很多个数据库每个数据库有固定的四张数据表
    // 只要当前设备上有一个用户登录，就为该用户创建一个数据库
    // 数据库的名字是该登录用户的 objectId
    // 该数据苦就是缓存当前设备的当前登录用户的所有相关数据
    // 用于管理本地数据库
    BmobDB bmobDB;

    Toast toast;
    View headerView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userManager = BmobUserManager.getInstance(MyApp.context);
        chatManager = BmobChatManager.getInstance(MyApp.context);
        // 创建或打开当前设备上当前登录用户所对应的数据（数据库的名字适当恰你等录用户的 objectId）
        // 如果没有处于登录状态的用户，则创建或打开默认的数据（数据库的名字是 bmobchat.db）
        // 另外一种创建数据库的方式：BmobDB.create(context,toId)是创建或打开 toId名字的对应数据库

        bmobDB = BmobDB.create(MyApp.context);
        toast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
        MyApp.activities.add(this);
        initLayout();
        init();
    }

    @Override
    protected void onDestroy() {
        MyApp.activities.remove(this);
        super.onDestroy();
    }

    /**
     * 由子类选择性重写
     */
    public void init(){
        // NO-OP 钩子方法
    }

    private void initLayout(){
        // 尝试调用 setContentView(layoutId) 方法
        // 尝试根据类名(MainActivity) -- 资源文件名字(activity_main)
        String clazzName = this.getClass().getSimpleName(); // MainActivity
        if(clazzName.contains("Activity")){
            String activityName = clazzName.substring(0,clazzName.indexOf("Activity")).toLowerCase(Locale.US); // main
            String resName = "activity_" + activityName; // activity_main
            // 根据 resName 找到其对应的 resId (activity_main -- R.layout.activity_main)
            int resId = getResources().getIdentifier(resName,"layout",getPackageName());
            if(resId != 0){
                // 确实找到了资源ID
                setContentView(resId);
            }else{
                setMyContentView();
            }
            ButterKnife.bind(this);
            headerView = findViewById(R.id.headerview);
        }
    }

    public void setHeaderView(View headerView){
        this.headerView = headerView;
    }

    /**
     * 由子类重写
     * 提供子类所使用的布局文件的名称
     */
    public abstract void setMyContentView();

    /**
     * 设置 headerView 中的标题
     * @param title
     */
    public void setHeaderTitle(String title){
        setHeaderTitle(title, Constants.CENTER);
    }

    public void setHeaderTitle(String title,int position){
        TextView tv = (TextView) headerView.findViewById(R.id.tv_headerview_title);
        switch (position){
            case Constants.LEFT:
                tv.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
                break;
            case Constants.CENTER:
                tv.setGravity(Gravity.CENTER);
                break;
            default:
                tv.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
                break;
        }
        if(title == null){
            tv.setText("");
        }else{
            tv.setText(title);
        }
    }

    public void setHeaderTitle(String title,Constants.Position position){
        switch (position){
            case LEFT:
                setHeaderTitle(title, Constants.LEFT);
                break;
            case CENTER:
                setHeaderTitle(title, Constants.CENTER);
                break;
            case RIGHT:
                setHeaderTitle(title, Constants.RIGHT);
                break;
        }
    }

    /**
     * 设定 HeaderView 的 ImageView
     * @param pos           设定 左侧/右侧 的 ImageView
     * @param resId         ImageView 中显示图片的资源 ID
     * @param colorFilter   是否需要为 ImageView 中显示的图像添加白色的前景，不需要就传入false
     * @param listener      是否需要为 ImageView 添加单击事件监听器，不需要则传入 null
     */
    public void setHeaderImage(Constants.Position pos, int resId, boolean colorFilter, View.OnClickListener listener){
        ImageView imageView;
        if(pos == Constants.Position.LEFT){
            imageView = (ImageView) headerView.findViewById(R.id.iv_headerview_left);
        }else{
            imageView = (ImageView) headerView.findViewById(R.id.iv_headerview_right);
        }
        imageView.setImageResource(resId);
        if(colorFilter){
            imageView.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
        if(listener != null){
            imageView.setOnClickListener(listener);
        }
    }

    // 输出吐丝和打印log相关方法
    public void toast(String text){
        if(!TextUtils.isEmpty(text)){
            toast.setText(text);
            toast.show();
        }
    }

    public void log(String log){
        if(Constants.DEBUG){
            Log.d(Constants.TAG, getClass().getSimpleName() + "输出的日志："+log);
        }
    }

    public void toastAndLog(String text , String log){
        toast(text);
        log(log);
    }

    public void log(String log,int error,String msg){
        log(log+",错误代码："+error+": " + msg);
    }

    public void toastAndLog(String text,int error,String msg){
        toast(text);
        log(text+",错误代码："+error+": "+msg);
    }

    /**
     * 界面跳转的相关方法
     * @param clazz
     * @param isFinish
     */
    public void jumpTo(Class<?> clazz,boolean isFinish){
        Intent intent = new Intent(this,clazz);
        startActivity(intent);
        if(isFinish){
            this.finish();
        }
    }

    public void jumpTo(Intent intent,boolean isFinish){
        startActivity(intent);
        if(isFinish){
            this.finish();
        }
    }

    /**
     * 判断是否有未输入内容的 EditText
     * @param ets    用来检测的多个 EditText
     * @return  true    有未输入内容的 EditText
     *          false   所有的 EditText 都输入了内容
     */
    public boolean isEmpty(EditText... ets){
        for(EditText et : ets){
            String text = et.getText().toString();
            if(TextUtils.isEmpty(text)){
                SpannableString ss = new SpannableString("请输入完整!");
                ss.setSpan(new ForegroundColorSpan(Color.RED),0,3,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new BackgroundColorSpan(Color.BLACK),3,6,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);;
                ss.setSpan(new ImageSpan(this,R.drawable.ue058),5,6,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                et.setError(ss);
                return true;
            }
        }
        return false;
    }

    /**
     * 更新当前设备上登录用户的位置
     * @param updateListener
     */
    public void updateUserLocation(UpdateListener updateListener){
        MyUser user = userManager.getCurrentUser(MyUser.class);
        if(user != null){
            // 更新当前设备上登录用户的位置
            user.setLocation(MyApp.lastPoint);
            if(updateListener != null){
                user.update(this,updateListener);
            }else{
                user.update(this);
            }
        }
    }

    public void getMyFriends(FindListener<BmobChatUser> callback){
        // 1.清空本地数据库 friends 数据表中的内容
        bmobDB.deleteAllContact();
        // 2.从服务器上获取罪行的当前登录用户好友列表
        // 3.把好友列表写入本地数据库 friends 表
        userManager.queryCurrentContactList(callback);
        // 4.做后续的操作（eg：界面跳转）
        // 写到 callback 的相应回调方法中即可
    }
}
