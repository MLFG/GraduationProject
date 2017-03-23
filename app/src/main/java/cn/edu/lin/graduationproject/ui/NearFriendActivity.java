package cn.edu.lin.graduationproject.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.listener.FindListener;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.app.MyApp;
import cn.edu.lin.graduationproject.bean.MyUser;
import cn.edu.lin.graduationproject.constant.Constants;
import cn.edu.lin.graduationproject.util.DistanceUtil;

public class NearFriendActivity extends BaseActivity {

    private static final String TAG = "NearFriendActivity";

    @BindView(R.id.mv_nearfriend_mapview)
    MapView mapView;
    BaiduMap baiduMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setMyContentView() {
        setContentView(R.layout.activity_near_friend);
    }

    @Override
    public void init() {
        super.init();
        initHeaderView();
        initBaiduMap();
    }

    private void initBaiduMap(){
        baiduMap = mapView.getMap();
        baiduMap.setMaxAndMinZoomLevel(20,15);
        baiduMap.setOnMarkerClickListener(marker -> {
            LatLng location = marker.getPosition();
            View view = getLayoutInflater().inflate(R.layout.infowindow_layout,mapView,false);
            ImageView ivAvatar = (ImageView) view.findViewById(R.id.iv_infowindow_avatar);
            TextView tvUsername = (TextView) view.findViewById(R.id.tv_infowindow_name);
            TextView tvDistance = (TextView) view.findViewById(R.id.tv_infowindow_distance);
            TextView tvTime = (TextView) view.findViewById(R.id.tv_infowindow_time);
            Button btnAdd = (Button) view.findViewById(R.id.btn_infowindow_add);

            Bundle bundle = marker.getExtraInfo();
            String avatar = bundle.getString("avatar");
            if(TextUtils.isEmpty(avatar)){
                ivAvatar.setImageResource(R.drawable.ic_launcher);
            }else{
                ImageLoader.getInstance().displayImage(avatar,ivAvatar);
            }
            tvUsername.setText(bundle.getString("username",""));
            tvTime.setText(bundle.getString("time"));
            tvDistance.setText(DistanceUtil.getDistance(MyApp.lastPoint,new BmobGeoPoint(location.longitude,location.latitude))+"米");

            InfoWindow infoWindow = new InfoWindow(view,location,-50);
            baiduMap.showInfoWindow(infoWindow);
            return true;
        });
    }

    private void initHeaderView(){
        setHeaderTitle("附近好友");
        setHeaderImage(Constants.Position.LEFT, R.drawable.back_arrow_2, false, v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh(){
        // 搜索当前登录用户附近的陌生人
        userManager.queryKiloMetersListByPage(
                false,
                0,  // 页码，一页最多返回 10 个数据
                "location", // 用来表示位置内容的字段名称
                MyApp.lastPoint.getLongitude(), // 当前登录用户的经度
                MyApp.lastPoint.getLatitude(),  // 当前登录用户的纬度
                false,  // 是否将当前登录用户的好友放入结果中，（true包含好友数据，false不包含好友数据）
                5.0,    // 搜索半径（公里）
                null,   // 搜索时除了距离条件外，是否还需要增加其他条件（若增加，则写字段名称 eg：gender）
                null,   // 如果制定了额外的搜索条件，则这里给条件的值得是什么（eg：gender 给false）
                new FindListener<MyUser>() {

                    @Override
                    public void onSuccess(List<MyUser> list) {
                        if(list == null || list.size() == 0){
                            toastAndLog("附近并没有好友","附近并没有好友");
                            finish();
                            return;
                        }
                        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(new LatLng(MyApp.lastPoint.getLatitude(),MyApp.lastPoint.getLongitude()));
                        for(MyUser mu : list){
                            Log.d(TAG, "用户名："+mu.getUsername()+"经纬度："+mu.getLocation().getLatitude()+" / "+mu.getLocation().getLongitude());
                            MarkerOptions option = new MarkerOptions();
                            option.position(new LatLng(mu.getLocation().getLatitude(),mu.getLocation().getLongitude()));
                            option.icon(BitmapDescriptorFactory.fromResource(mu.getGender()?R.drawable.boy:R.drawable.girl));
                            Marker marker = (Marker) baiduMap.addOverlay(option);
                            Bundle bundle = new Bundle();
                            bundle.putString("avatar",mu.getAvatar());
                            bundle.putString("username",mu.getUsername());
                            bundle.putString("time",mu.getUpdatedAt());
                            bundle.putString("objectId",mu.getObjectId());
                            marker.setExtraInfo(bundle);
                        }
                    }

                    @Override
                    public void onError(int i, String s) {
                        toastAndLog("查询附近的用户时出现错误",i,s);
                    }
                }
        );
    }
}