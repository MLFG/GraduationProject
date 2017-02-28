package cn.edu.lin.graduationproject.util;

import android.text.SpannableString;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.edu.lin.graduationproject.app.MyApp;

/**
 * Created by liminglin on 17-2-28.
 */

public class EmoUtil {

    public static List<String> emos = new ArrayList<String>();

    // 加载表情的编码信息
    static{
        emos.add("ue056");emos.add("ue057");emos.add("ue058");
        emos.add("ue059");emos.add("ue105");emos.add("ue106");
        emos.add("ue107");emos.add("ue108");emos.add("ue401");
        emos.add("ue402");emos.add("ue403");emos.add("ue404");
        emos.add("ue405");emos.add("ue406");emos.add("ue407");
        emos.add("ue408");emos.add("ue409");emos.add("ue40a");
        emos.add("ue40b");emos.add("ue40c");emos.add("ue40d");
        emos.add("ue40e");emos.add("ue40f");emos.add("ue410");
        emos.add("ue411");emos.add("ue412");emos.add("ue413");
        emos.add("ue414");emos.add("ue415");emos.add("ue416");
        emos.add("ue417");emos.add("ue418");emos.add("ue420");
        emos.add("ue421");emos.add("ue422");emos.add("ue423");
        emos.add("ue452");emos.add("ue453");emos.add("ue454");
        emos.add("ue455");emos.add("ue456");emos.add("ue457");
        emos.add("ue407");emos.add("ue408");emos.add("ue409");
        emos.add("ue40a");emos.add("ue40b");emos.add("ue40c");
        emos.add("ue40d");emos.add("ue40e");emos.add("ue40f");
        emos.add("ue410");emos.add("ue411");emos.add("ue412");
    }

    public static SpannableString getSpannableString(String string){
        SpannableString ss = new SpannableString(string);
        Pattern pattern = Pattern.compile("ue[0-9a-z]{3}");
        Matcher matcher = pattern.matcher(ss);
        while (matcher.find()){
            int startPos = matcher.start();
            int endPos = matcher.end();
            String resName = matcher.group();
            // resName -- resId

        }
        return ss;
    }
}
