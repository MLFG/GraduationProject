package cn.edu.lin.graduationproject.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.adapter.BlogAdapter;
import cn.edu.lin.graduationproject.bean.Blog;
import cn.edu.lin.graduationproject.bean.Comment;
import cn.edu.lin.graduationproject.constant.Constants;
import cn.edu.lin.graduationproject.listener.OnCommentBlogListener;
import cn.edu.lin.graduationproject.ui.PostBlogActivity;

/**
 * Created by liminglin on 17-3-1.
 */

public class FindFragment extends BaseFragment implements OnCommentBlogListener {

    @Bind(R.id.ptr_find_blogs)
    PullToRefreshListView ptrListView;
    ListView listView;
    List<Blog> blogs;
    BlogAdapter adapter;

    @Bind(R.id.ll_find_commentcontainer)
    LinearLayout commentContainer;
    @Bind(R.id.et_find_comment)
    EditText etComment;

    Blog blog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find,container,false);
        return view;
    }

    @Override
    public void init() {
        super.init();
        initHeaderView();
        initListView();
    }

    private void initHeaderView() {
        setHeaderTitle("圈子", Constants.Position.CENTER);
        setHeaderImage(Constants.Position.RIGHT, R.drawable.ic_new_blog, true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpTo(PostBlogActivity.class,false);
            }
        });
    }

    private void initListView() {
        listView = ptrListView.getRefreshableView();
        blogs = new ArrayList<>();
        adapter = new BlogAdapter(getActivity(),blogs,this);
        listView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        BmobQuery<Blog> query = new BmobQuery<>();
        query.include("author");
        query.order("-createdAt");
        query.findObjects(getActivity(), new FindListener<Blog>() {
            @Override
            public void onSuccess(List<Blog> list) {
                adapter.addAll(list,true);
            }

            @Override
            public void onError(int i, String s) {
                toastAndLog("加载博客失败，稍后重试",i,s);
            }
        });
    }

    @Override
    public void onComment(int position, Blog blog) {
        // 显示 / 隐藏供用户输入评论内容视图
        if(commentContainer.getVisibility() == View.VISIBLE){
            commentContainer.setVisibility(View.INVISIBLE);
            this.blog = null;
        }else{
            commentContainer.setVisibility(View.VISIBLE);
            this.blog = blog;
        }
    }

    @OnClick(R.id.btn_find_send)
    public void sendComment(View view){
        String content = etComment.getText().toString();
        if(TextUtils.isEmpty(content)){
            return;
        }
        Comment comment = new Comment();
        comment.setBlogId(blog.getObjectId());
        comment.setContent(content);
        comment.setUsername(userManager.getCurrentUserName());
        comment.save(getActivity(), new SaveListener() {
            @Override
            public void onSuccess() {
                etComment.setText("");
                commentContainer.setVisibility(View.INVISIBLE);
                blog = null;
                refresh();
            }

            @Override
            public void onFailure(int i, String s) {
                toastAndLog("发布评论失败，稍后重试",i,s);
            }
        });
    }
}
