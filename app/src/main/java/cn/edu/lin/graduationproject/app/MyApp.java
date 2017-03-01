package cn.edu.lin.graduationproject.app;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.im.BmobChat;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatInstallation;
import cn.bmob.push.BmobPush;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.constant.Constants;
import cn.edu.lin.graduationproject.ui.LoginActivity;
import cn.sharesdk.framework.ShareSDK;

/**
 * Created by liminglin on 17-2-28.
 */

public class MyApp extends Application {

    private static final String TAG = "MyApp";
    /** MyApp 的 context 在以下两种情况不能使用
     * 1、界面跳转时，构建 Intent 最好不要使用 context（一定要用的话，应提供 Intent.FLAG_ACTIVITY_NEW_TASK）
     * 2、AlertDialog 弹窗时，不用 context 充当上下文
     * */
    public static MyApp context;
    public static MediaPlayer mediaPlayer;
    public static BmobGeoPoint lastPoint;

    public static List<Activity> activities;

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化百度地图
        SDKInitializer.initialize(getApplicationContext());

        // 初始化 Bmob
        Bmob.initialize(this, Constants.BMOB_KEY);
        // 使用推送服务时的初始化操作
        BmobInstallation.getCurrentInstallation(this).save();
        // 启动推送服务
        BmobPush.startWork(this);
        BmobChat.getInstance(this).init(Constants.BMOB_KEY);
        // 初始化分享 SDK
        ShareSDK.initSDK(this);
        context = this;
        mediaPlayer = MediaPlayer.create(this, R.raw.notify);
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        activities = new ArrayList<>();
        Log.d(TAG, "onCreate: base end");
    }

    public static void logout(){
        BmobUserManager userManager = BmobUserManager.getInstance(context);
        // 登出（解决服务器端Session中设备与用户的绑定）
        userManager.logout();
        // 还要继续解决 _installation 数据表中用户与设备的绑定
        BmobQuery<BmobChatInstallation> query = new BmobQuery<BmobChatInstallation>();
        query.addWhereEqualTo("installationId",BmobInstallation.getInstallationId(context));
        query.findObjects(context, new FindListener<BmobChatInstallation>() {
            @Override
            public void onSuccess(List<BmobChatInstallation> list) {
                BmobChatInstallation bci = list.get(0);
                bci.setUid("");
                bci.update(context, new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        for(Activity activity:activities){
                            activity.finish();
                        }
                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        Log.d(TAG, "onFailure: update fail");
                    }
                });
            }

            @Override
            public void onError(int i, String s) {
                Log.d(TAG, "onError: find no object");
            }
        });
    }
}
