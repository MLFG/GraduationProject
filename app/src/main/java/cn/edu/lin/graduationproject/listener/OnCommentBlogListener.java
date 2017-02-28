package cn.edu.lin.graduationproject.listener;

import cn.edu.lin.graduationproject.bean.Blog;

/**
 * Created by liminglin on 17-2-28.
 */

public interface OnCommentBlogListener {

    void onComment(int position,Blog blog);
}
