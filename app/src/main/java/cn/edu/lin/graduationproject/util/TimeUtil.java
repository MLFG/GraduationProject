package cn.edu.lin.graduationproject.util;

import java.text.SimpleDateFormat;

/**
 * 转换时间工具类
 * Created by liminglin on 17-2-28.
 */

public class TimeUtil {

    /**
     * 将 long 型的时间戳转为具有一定描述性的时间格式
     *
     * 今天 14:35 -- 14:35
     * 昨天 14:35 -- 昨天14:35
     * 前天 14:35 -- 前天14:35
     * 更早       -- xxxx/xx/xx mm:hh
     *
     * @param timestamp
     * @return
     */
    public static String getTime(long timestamp){

        String result = "";
        // 获取此时此刻的时间
        long now = System.currentTimeMillis();
        // (now - timestamp)/1000/60/60/24 计算两个时间戳之间相差多少天
        int day = (int) (now/1000/60/60/24 - timestamp/1000/60/60/24);

        switch(day){
            case 0:
                result = new SimpleDateFormat("HH:mm").format(timestamp);
                break;
            case 1:
                result = "昨天 " + new SimpleDateFormat("HH:mm").format(timestamp);
                break;
            case 2:
                result = "前天 " + new SimpleDateFormat("HH:mm").format(timestamp);
                break;
            default:
                result = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(timestamp);
                break;
        }
        return result;
    }
}
