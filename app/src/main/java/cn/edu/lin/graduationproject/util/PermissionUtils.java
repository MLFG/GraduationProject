package cn.edu.lin.graduationproject.util;

import android.Manifest;
import android.content.Context;
import android.text.TextUtils;

import com.tbruyelle.rxpermissions.RxPermissions;

import static android.Manifest.permission_group.CALENDAR;

/**
 * Created by liminglin on 17-3-21.
 */

public class PermissionUtils {

    public static final String CAMERA = Manifest.permission.CAMERA;
    public static final String CALL = Manifest.permission.CALL_PHONE;
    public static final String WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String READ = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String CONTACTS = Manifest.permission.READ_CONTACTS;
    public static final String LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String AUDIO = Manifest.permission.RECORD_AUDIO;



    private static RxPermissions rxPermissions;

    public PermissionUtils(Context context){
        if(context == null){
            throw new NullPointerException("Context not null");
        }
        if(rxPermissions == null){
            rxPermissions = RxPermissions.getInstance(context);
        }
    }

    public static PermissionUtils getInstance(Context context){
        return new PermissionUtils(context);
    }

    public void setPermissions(String permission,Listener listener){
        if(TextUtils.isEmpty(permission)){
            throw new NullPointerException("permission not null");
        }
        if(listener == null){
            throw new NullPointerException("PermissionListener not null");
        }
        if(TextUtils.equals(permission,CAMERA)){
            rxPermissions.request(CAMERA).subscribe(listener::isPermission);
        }else if(TextUtils.equals(permission,CALL)){
            rxPermissions.request(CALL).subscribe(listener::isPermission);
        }else if(TextUtils.equals(permission,WRITE)){
            rxPermissions.request(WRITE).subscribe(listener::isPermission);
        }else if(TextUtils.equals(permission,READ)){
            rxPermissions.request(READ).subscribe(listener::isPermission);
        }else if(TextUtils.equals(permission,CONTACTS)){
            rxPermissions.request(CONTACTS).subscribe(listener::isPermission);
        }else if(TextUtils.equals(permission,LOCATION)){
            rxPermissions.request(LOCATION).subscribe(listener::isPermission);
        }else if(TextUtils.equals(permission,AUDIO)){
            rxPermissions.request(AUDIO).subscribe(listener::isPermission);
        }else{
            throw new IllegalArgumentException("Please enter the correct permissions");
        }
    }

    public interface Listener{
        void isPermission(boolean grant);
    }
}
