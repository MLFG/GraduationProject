package cn.edu.lin.graduationproject.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import java.util.Locale;

/**
 * 获取中文拼音的工具类
 * Created by liminglin on 17-2-28.
 */
public class PinYinUtil {

    /**
     * 将参数中的文字转为拼音格式
     * 老王  --  LAOWANG
     * 老王a --  LAOWANGA
     * 老王8 --  LAOWANG8
     * 8老王 --  #LAOWANG
     * @param string 用户的名字
     * @return
     */
    public static String getPinYin(String string){
        try{
            String result = "";
            // 1、设定拼音格式
            HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
            format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
            format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            // 2、按照设定的格式，逐字进行转换
            StringBuilder sb = new StringBuilder();
            for(int i = 0 ; i < string.length() ; i++){
                // 判断第i个位置上的字是不是中文
                String s = string.substring(i,i+1);
                if(s.matches("[\u4e00-\u9fff]")){ // 是中文
                    char c = string.charAt(i);
                    String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(c,format);
                    sb.append(pinyin[0]);
                }else if(s.matches("[a-zA-Z]")){ // 是英文
                    sb.append(s.toUpperCase(Locale.US));
                }else{ // 既不是中文也不是英文
                    if(i == 0){
                        sb.append("#");
                    }else{
                        sb.append(s);
                    }
                }
            }
            result = sb.toString();
            return result;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("拼音格式异常.");
        }
    }

    /**
     * 获取指定字串拼音的首字母
     * @param string
     * @return
     */
    public static char getFirstLetter(String string){
        return getPinYin(string).charAt(0);
    }
}
