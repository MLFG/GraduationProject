package cn.edu.lin.graduationproject.ui;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.util.List;

import butterknife.BindView;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.app.MyApp;
import cn.edu.lin.graduationproject.util.PermissionUtils;

/**
 * 欢迎界面
 *  1.定位
 *  2.动画效果
 *  3.界面跳转
 */
public class SplashActivity extends BaseActivity {

    private static final String TAG = "SplashActivity";

    @BindView(R.id.tv_splash_be)
    TextView tvBe;
    @BindView(R.id.tv_splash_she)
    TextView tvShe;

    LocationClient client; // 百度地图定位客户端
    BDLocationListener listener; // 百度地图定位监听器

    PermissionUtils permissionUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setMyContentView() {
        setContentView(R.layout.activity_splash);
    }

    @Override
    public void init() {
        super.init();
        permissionUtils = new PermissionUtils(this);
        permissionUtils.setPermissions(PermissionUtils.LOCATION,grant -> getLocation());
    }

    /**
     * 发起定位
     */
    private void getLocation(){
        client = new LocationClient(getApplicationContext());
        listener = new MyLocationListener();
        client.registerLocationListener(listener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd0911");
        int span = 1000*60*5;
        option.setScanSpan(span);
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);
        option.setIgnoreKillProcess(false);
        option.setEnableSimulateGps(false);
        client.setLocOption(option);
        // 发起定位请求
        client.start();
    }

    public class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            int code = bdLocation.getLocType();
            double lat = -1;
            double lng = -1;
            if(code == 61 || code == 66 || code == 161){
                // 定位成功
                lat = bdLocation.getLatitude();
                lng = bdLocation.getLongitude();
            }else if(code == 63){
                toastAndLog("初始定位失败，载入默认地址",63,"百度地图 NetworkCommunicationException!");
                // 定位不成功(手动指定)
                lat = 22.5428750000;
                lng = 114.0279300000;
            }else{
                // 定位不成功(手动指定)
                lat = 22.5428750000;
                lng = 114.0279300000;
            }
            // 获得定位结果后，为 MyApp.lastPoint 属性赋值
            MyApp.lastPoint = new BmobGeoPoint(lng,lat);
            // 启动动画
            runOnUiThread(() -> initAnim());
            // 停止继续发起定位请求
            if(client.isStarted()){
                client.unRegisterLocationListener(listener);
                client.stop();
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    /**
     * 播放动画效果
     */
    public void initAnim(){

        Animation animation = AnimationUtils.loadAnimation(this,R.anim.splash_anim);
        tvBe.startAnimation(animation);
        tvShe.startAnimation(animation);


        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tvBe.setVisibility(View.VISIBLE);
                tvShe.setVisibility(View.VISIBLE);
                // 动画结束后，界面跳转
                // 根据当前设备是否有处于登录状态的用户
                BmobChatUser user = userManager.getCurrentUser();
                if(user != null){
                    // 有则向 MainActivity 跳转
                    // 更新位置
                    updateUserLocation(new UpdateListener() {
                        @Override
                        public void onSuccess() {
                            // 获取当前登录用户的好友列表
                            getMyFriends(new FindListener<BmobChatUser>() {
                                @Override
                                public void onSuccess(List<BmobChatUser> list) {
                                    jumpTo(MainActivity.class,true);
                                }

                                @Override
                                public void onError(int i, String s) {
                                    switch (i){
                                        case 1000:
                                            // 当前登录用户一个好友都没有
                                            jumpTo(MainActivity.class,true);
                                            break;
                                        default:
                                            toastAndLog("获取当前登录用户好友列表失败",i,s);
                                            break;
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            switch (i){
                                case 206:
                                    // 设备的登录用户已经在其他设备上登录了
                                    MyApp.logout();
                                    break;
                                default:
                                    toastAndLog("更新位置失败",i,s);
                                    break;
                            }
                        }
                    });
                }else{
                    // 没有则向 LoginActivity 跳转
                    jumpTo(LoginActivity.class,true);
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(client != null){
            client.stop();
            client = null;
        }
    }
}
