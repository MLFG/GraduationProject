package cn.edu.lin.graduationproject.bean;

import cn.bmob.v3.BmobObject;

/**
 * 点赞实体类
 * Created by liminglin on 17-2-28.
 */

public class Zan extends BmobObject {

    String blogId; // 为那篇 blog 点的赞
    String userId; // 谁点的赞

    public void setBlogId(String blogId) {
        this.blogId = blogId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBlogId() {

        return blogId;
    }

    public String getUserId() {
        return userId;
    }
}
