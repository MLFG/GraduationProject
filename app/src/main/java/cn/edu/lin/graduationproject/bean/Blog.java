package cn.edu.lin.graduationproject.bean;


import cn.bmob.v3.BmobObject;

/**
 * 朋友圈的实体类
 * Created by liminglin on 17-2-28.
 */

public class Blog extends BmobObject {
    MyUser author; // blog 的作者
    String content;// blog 的文本内容
    String imgUrls;// blog 配图的地址，多幅配图地址之间用&隔开
    Integer love;  // 被点咱的数量

    public void setAuthor(MyUser author) {
        this.author = author;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setImgUrls(String imgUrls) {
        this.imgUrls = imgUrls;
    }

    public void setLove(Integer love) {
        this.love = love;
    }

    public MyUser getAuthor() {

        return author;
    }

    public String getContent() {
        return content;
    }

    public String getImgUrls() {
        return imgUrls;
    }

    public Integer getLove() {
        return love;
    }
}
