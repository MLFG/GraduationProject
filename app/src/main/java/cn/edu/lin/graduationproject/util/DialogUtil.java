package cn.edu.lin.graduationproject.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * 显示对话框工具类
 * Created by liminglin on 17-2-28.
 */
public class DialogUtil {

    public static void showDialog(Context context, String title, String message, boolean cancelButton, DialogInterface.OnClickListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(android.R.drawable.ic_menu_info_details);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("确定",listener);
        if(cancelButton){
            builder.setNegativeButton("取消",null);
        }else{
            builder.setCancelable(false);
        }
        builder.create().show();
    }
}
