package cn.edu.lin.graduationproject.app;

import android.app.Activity;
import android.app.Application;
import android.media.MediaPlayer;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;

import java.util.List;

import cn.bmob.push.BmobPush;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobConfig;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.edu.lin.graduationproject.constant.Constants;

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

        // 设置 BmobConfig 初始化 Bmob
        BmobConfig config = new BmobConfig.Builder(this)
                .setApplicationId(Constants.BMOB_KEY)
                .setConnectTimeout(30)
                .setUploadBlockSize(1024*1024)
                .setFileExpiration(2500)
                .build();
        Bmob.initialize(config);
        // 使用推送服务时的初始化操作
        BmobInstallation.getCurrentInstallation().save();
        // 启动推送服务
        BmobPush.startWork(this);
        Log.d(TAG, "onCreate: base end");
    }
}
