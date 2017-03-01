package cn.edu.lin.graduationproject.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import cn.edu.lin.graduationproject.constant.Constants;

/**
 * 偏好设置工具类
 * Created by liminglin on 17-2-28.
 */

public class SPUtil {
    SharedPreferences sp;
    private static Editor editor;

    public SPUtil(Context context, String name){
        sp = context.getSharedPreferences(name,Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public SPUtil(Context context){
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sp.edit();
    }

    /**
     * 判断通知按钮是否打开
     * @return
     */
    public boolean isAllowNotification(){
        return sp.getBoolean(Constants.NOTIFICATION,true);
    }

    /**
     * 判断声音按钮是否打开
     * @return
     */
    public boolean isAllowSound(){
        return sp.getBoolean(Constants.SOUND,true);
    }

    /**
     * 判断震动按钮是否打开
     * @return
     */
    public boolean isAllowVibrate(){
        return sp.getBoolean(Constants.VIBRATE,true);
    }

    /**
     * 保存通知按钮的状态
     * @param flag
     */
    public void setNotification(boolean flag){
        editor.putBoolean(Constants.NOTIFICATION,flag);
        editor.commit();
    }

    /**
     * 保存声音按钮的状态
     * @param flag
     */
    public void setSound(boolean flag){
        editor.putBoolean(Constants.SOUND,flag);
        editor.commit();
    }

    /**
     * 保存震动按钮的状态
     * @param flag
     */
    public void setVibrate(boolean flag){
        editor.putBoolean(Constants.VIBRATE,flag);
        editor.commit();
    }
}
