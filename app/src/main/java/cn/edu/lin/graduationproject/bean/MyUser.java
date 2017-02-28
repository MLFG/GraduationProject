package cn.edu.lin.graduationproject.bean;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.datatype.BmobRelation;

/**
 * 聊天用户实体类
 * Created by liminglin on 17-2-28.
 */

public class MyUser extends BmobUser{

    String nick;    // 昵称
    String avatar;  // 头像
    String installId;// 设备 id
    String deviceType; // 设备类型
    BmobRelation blacklist; // 黑名单
    BmobRelation contacts;  // 好友列表
    Boolean gender; // 性别 true 男生 false 女生
    BmobGeoPoint location; // 位置
    String pyname;  // 用户名的汉语拼音格式
    Character letter;// 用户拼音名字的首字母

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setInstallId(String installId) {
        this.installId = installId;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setBlacklist(BmobRelation blacklist) {
        this.blacklist = blacklist;
    }

    public void setContacts(BmobRelation contacts) {
        this.contacts = contacts;
    }

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

    public String getNick() {

        return nick;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getInstallId() {
        return installId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public BmobRelation getBlacklist() {
        return blacklist;
    }

    public BmobRelation getContacts() {
        return contacts;
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
