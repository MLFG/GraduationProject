package cn.edu.lin.graduationproject.bean;

import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.datatype.BmobRelation;

/**
 * 聊天用户实体类
 * Created by liminglin on 17-2-28.
 */

public class MyUser extends BmobChatUser{

    Boolean gender; // 性别 true 男生 false 女生
    BmobGeoPoint location; // 位置
    String pyname;  // 用户名的汉语拼音格式
    Character letter;// 用户拼音名字的首字母

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public void setLocation(BmobGeoPoint location) {
        this.location = location;
    }

    public void setPyname(String pyname) {
        this.pyname = pyname;
    }

    public void setLetter(Character letter) {
        this.letter = letter;
    }

    public Boolean getGender() {
        return gender;
    }

    public BmobGeoPoint getLocation() {
        return location;
    }

    public String getPyname() {
        return pyname;
    }

    public Character getLetter() {
        return letter;
    }
}
