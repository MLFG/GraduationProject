package cn.edu.lin.graduationproject.bean;

import cn.bmob.v3.BmobObject;

/**
 * 评论的实体类
 * Created by liminglin on 17-2-28.
 */

public class Comment extends BmobObject {

    String blogId;      // 评论是针对哪一个 blog
    String content;     // 评论的文本内容
    String username;    // 发布评论者的用户名

    public void setBlogId(String blogId) {
        this.blogId = blogId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBlogId() {

        return blogId;
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }
}
