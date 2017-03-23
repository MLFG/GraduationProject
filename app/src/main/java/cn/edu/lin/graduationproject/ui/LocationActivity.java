package cn.edu.lin.graduationproject.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import butterknife.BindView;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.listener.UploadFileListener;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.app.MyApp;
import cn.edu.lin.graduationproject.constant.Constants;
import cn.edu.lin.graduationproject.util.PermissionUtils;

public class LocationActivity extends BaseActivity {

    String from; // "mylocation"代表需要定位 "showaddress" 代表需要显示一个指定地址
    @BindView(R.id.bmapView)
    MapView mapView;
    BaiduMap baiduMap;

    LocationClient client;
    BDLocationListener listener;

    ProgressDialog pd;

    PermissionUtils permissionUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setMyContentView() {
        setContentView(R.layout.activity_location);
    }

    @Override
    public void init() {
        super.init();
        permissionUtils = new PermissionUtils(this);
        from = getIntent().getStringExtra("from");
        baiduMap = mapView.getMap();
        initBaiduMap();
        if("mylocation".equals(from)){
            // 定位
            setHeaderTitle("我的位置");
            setHeaderImage(Constants.Position.LEFT, R.drawable.back_arrow_2, true, v -> finish());
            setHeaderImage(Constants.Position.RIGHT, R.drawable.ic_map_snap, true, v -> {
                pd = ProgressDialog.show(LocationActivity.this,"","截图中...");
                // 地图截图
                baiduMap.snapshot(bitmap -> permissionUtils.setPermissions(PermissionUtils.WRITE, grant -> {
                    try{
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),System.currentTimeMillis()+".jpg");
                        OutputStream stream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG,30,stream);
                        final String localfilePath = file.getAbsolutePath();
                        // 将地图截图上传到服务器
                        final BmobFile bf = new BmobFile(file);
                        bf.uploadblock(LocationActivity.this, new UploadFileListener() {
                            @Override
                            public void onSuccess() {
                                final String url = bf.getFileUrl(LocationActivity.this);
                                // 根据定位得到的经纬度，进行街道名称的查询
                                GeoCoder geoCoder = GeoCoder.newInstance();
                                geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                                    @Override
                                    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

                                    }

                                    @Override
                                    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                                        pd.dismiss();
                                        // 根据给定的经纬度找到了对应的街道名称
                                        String address;
                                        if(reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR){
                                            address = "位置道路";
                                        }else{
                                            address = reverseGeoCodeResult.getAddress();
                                        }
                                        Intent data = new Intent();
                                        data.putExtra("address",address);
                                        data.putExtra("localFilePath",localfilePath);
                                        data.putExtra("url",url);
                                        setResult(RESULT_OK,data);
                                        finish();
                                    }
                                });
                                ReverseGeoCodeOption option = new ReverseGeoCodeOption();
                                option.location(new LatLng(MyApp.lastPoint.getLatitude(),MyApp.lastPoint.getLongitude()));
                                geoCoder.reverseGeoCode(option);
                            }

                            @Override
                            public void onFailure(int i, String s) {
                                pd.dismiss();
                                toastAndLog("截图失败，稍后重试",i,s);
                            }
                        });
                    }catch (Exception e){
                        if(pd!=null){
                            pd.dismiss();
                        }
                        e.printStackTrace();
                    }
                }));
            });
            getMyLocation();
        }else{
            // 显示一个位置
            String address = getIntent().getStringExtra("address");
            setHeaderTitle(address);
            setHeaderImage(Constants.Position.LEFT, R.drawable.back_arrow_2, true, v -> finish());
            showAddress();
        }
    }

    private void showAddress(){
        double lat = getIntent().getDoubleExtra("lat",0.0);
        double lng = getIntent().getDoubleExtra("lng",0.0);

        LatLng location = new LatLng(lat,lng);

        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(location);
        baiduMap.animateMapStatus(msu);
        MarkerOptions option = new MarkerOptions();
        option.position(location);
        option.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
        baiduMap.addOverlay(option);
    }

    private void getMyLocation(){
        client = new LocationClient(this);
        listener = new MyLocationListener();
        client.registerLocationListener(listener);
        // 百度地图的官方配置
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
        // option.setIgnoreCacheException(false);
        option.setEnableSimulateGps(false);
        client.setLocOption(option);
        client.start();
    }

    private void initBaiduMap(){
        baiduMap.setMaxAndMinZoomLevel(20,15);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在 activity 执行 onDestroy 时执行 mMapView.onDestroy，实现地图生命周期管理
        mapView.onDestroy();
        if(client != null){
            client.stop();
            client = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    public class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            int code = bdLocation.getLocType();
            double lat = -1;
            double lng = -1;
            if(code == 61 || code == 66 || code == 161){
                lat = bdLocation.getLatitude();
                lng = bdLocation.getLongitude();
            }else{
                lat = MyApp.lastPoint.getLatitude();
                lng = MyApp.lastPoint.getLongitude();
            }

            LatLng mylocation = new LatLng(lat,lng);

            // 移动屏幕中心点
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(mylocation);
            baiduMap.animateMapStatus(msu);
            // 放一个标志
            MarkerOptions option = new MarkerOptions();
            option.position(mylocation);
            option.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
            baiduMap.addOverlay(option);

            runOnUiThread(() -> {
                // 放一个信息窗
                TextView textView = new TextView(LocationActivity.this);
                textView.setText("我在这");
                textView.setBackgroundColor(Color.BLUE);
                textView.setTextColor(Color.WHITE);
                int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,3,getResources().getDisplayMetrics());
                textView.setPadding(padding,padding,padding,padding);
                InfoWindow infoWindow = new InfoWindow(textView,mylocation,-50);
                baiduMap.showInfoWindow(infoWindow);
            });

            if(client.isStarted()){
                client.stop();
                client.registerLocationListener(listener);
            }
            if(mylocation.latitude != MyApp.lastPoint.getLatitude() || mylocation.longitude != MyApp.lastPoint.getLongitude()){
                MyApp.lastPoint = new BmobGeoPoint(lng,lat);
                updateUserLocation(null);
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }
}
