package cn.edu.lin.graduationproject.util;

import com.baidu.mapapi.model.LatLng;

import cn.bmob.v3.datatype.BmobGeoPoint;

/**
 * 计算距离工具类
 * Created by liminglin on 17-2-28.
 */

public class DistanceUtil {
    private static final double EARTH_RADIUS = 6378.137*1000; // 米

    private static double rad(double d){
        return d * Math.PI / 180.0;
    }

    /**
     * 获取两个经纬度的具体坐标独对应的点的直线距离
     * @param lat1 第一个点的纬度
     * @param lng1 第一个点的经度
     * @param lat2 第二个点的纬度
     * @param lng2 第二个点的经度
     * @return 返回两点间的距离
     */
    public static double getDistance(double lat1,double lng1,double lat2,double lng2){
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);

        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
                Math.cos(radLat1)*Math.cos(radLat2) *
                Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    /**
     * 方法重载，根据两个经纬度坐标点对象计算两点间距离
     * @param l1 第一个经纬度坐标对象
     * @param l2 第二个经纬度坐标对象
     * @return 返回两点间的距离
     */
    public static double getDistance(LatLng l1,LatLng l2){
        return getDistance(l1.latitude,l1.longitude,l2.latitude,l2.longitude);
    }

    /**
     * 方法重载，根据Bmob两个经纬度坐标点对象计算两点间距离
     * @param b1 第一个 Bmob 坐标点对象
     * @param b2 第二个 Bmob 坐标点对象
     * @return 返回两点间的距离
     */
    public static double getDistance(BmobGeoPoint b1,BmobGeoPoint b2){
        return getDistance(b1.getLatitude(),b1.getLongitude(),b2.getLatitude(),b2.getLongitude());
    }
}
