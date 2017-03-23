package cn.edu.lin.graduationproject.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.im.BmobUserManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.bean.Blog;
import cn.edu.lin.graduationproject.bean.Comment;
import cn.edu.lin.graduationproject.bean.MyUser;
import cn.edu.lin.graduationproject.bean.Zan;
import cn.edu.lin.graduationproject.listener.OnCommentBlogListener;
import cn.edu.lin.graduationproject.listener.OnDatasLoadFinishListener;
import cn.edu.lin.graduationproject.util.DBUtil;
import cn.edu.lin.graduationproject.util.TimeUtil;
import cn.sharesdk.onekeyshare.OnekeyShare;

/**
 * Created by liminglin on 17-3-1.
 */

public class BlogAdapter extends MyBaseAdapter<Blog>{
    private static final String TAG = "BlogAdapter";

    DBUtil dbUtil;
    OnCommentBlogListener listener;

    public BlogAdapter(Context context, List<Blog> dataSource,OnCommentBlogListener listener) {
        super(context, dataSource);
        dbUtil = new DBUtil(context);
        this.listener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        try{
            ViewHolder viewHolder;
            if(convertView == null){
                convertView = inflater.inflate(R.layout.item_blog_layout,parent,false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Blog blog = getItem(position);
            MyUser author = blog.getAuthor();
            setAvatar(author.getAvatar(),viewHolder.ivAvatar);
            viewHolder.tvUsername.setText(author.getUsername());
            viewHolder.tvContent.setText(blog.getContent());
            viewHolder.imageContainer.removeAllViews();
            String imgUrls = blog.getImgUrls();
            if(!TextUtils.isEmpty(imgUrls)){
                showBlogImages(imgUrls,viewHolder.imageContainer);
            }
            // 时间
            String time = blog.getCreatedAt();
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
            String descTime = TimeUtil.getTime(date.getTime());
            viewHolder.tvTime.setText(descTime);
            viewHolder.tvShare.setOnClickListener(v -> shareBlog(position));
            viewHolder.tvLove.setText(blog.getLove() + " 赞");
            viewHolder.tvLove.setOnClickListener(v -> loveBlog(position));
            viewHolder.tvComment.setOnClickListener(v -> commentBlog(position));
            viewHolder.commentContainer.removeAllViews();
            showBlogComments(position,viewHolder.commentContainer);
            return convertView;
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("不正确的时间格式.");
        }
    }

    /**
     * 显示对某一个 blog 的所有评论内容
     * @param position
     * @param commentContainer
     */
    private void showBlogComments(int position, final LinearLayout commentContainer){
        // 从服务器 Comment 数据表中，将针对 position 位置的 blog 的所有评论都查询出来
        BmobQuery<Comment> query = new BmobQuery<>();
        query.addWhereEqualTo("blogId",getItem(position).getObjectId());
        // query.order("-createdAt");// 越晚发布的评论呈现时越靠上
        query.findObjects(context, new FindListener<Comment>() {
            @Override
            public void onSuccess(List<Comment> list) {
                // 每一条评论放入一个 TextView，然后 TextView 放入到 commentContainer中
                for(Comment comment : list){
                    TextView tv = new TextView(context);
                    // eg -- abc 评论：xxxx
                    String username = comment.getUsername();
                    String content = comment.getContent();
                    // 评论内容是否需要添加时间，以及时间的格式
                    tv.setText(username + "评论：" + content);
                    tv.setTextColor(Color.BLUE);
                    int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,3,context.getResources().getDisplayMetrics());
                    tv.setPadding(padding,padding,padding,padding);
                    tv.setGravity(Gravity.CENTER_VERTICAL);
                    commentContainer.addView(tv);
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.d(TAG, "onError: 查询评论失败，错误代码："+ i + " : " + s);
            }
        });
    }

    /**
     * 评论一个 blog
     * @param position
     */
    protected void commentBlog(int position){
        Blog blog = getItem(position);
        listener.onComment(position,blog);
    }

    /**
     * 为 blog 点赞
     * @param position
     */
    protected void loveBlog(final int position){
        final String blogId = getItem(position).getObjectId();
        final String userId = BmobUserManager.getInstance(context).getCurrentUserObjectId();
        // 查询服务器 Zan 数据表
        BmobQuery<Zan> query = new BmobQuery<>();
        query.addWhereEqualTo("blogId",blogId);
        query.addWhereEqualTo("userId",userId);
        query.findObjects(context, new FindListener<Zan>() {
            @Override
            public void onSuccess(List<Zan> list) {
                if(list != null && list.size()>0){
                    // 说明当前登录用户已经为第 position 位置上的 blog 点过赞了
                    Toast.makeText(context,"已经点过赞了",Toast.LENGTH_SHORT).show();
                }else{
                    // 进行点赞操作
                    saveZan(position,blogId,userId);
                }
            }

            @Override
            public void onError(int i, String s) {
                if(i == 101){
                    // 此时服务器的数据库中根本还未创建 Zan 数据表
                    saveZan(position,blogId,userId);
                }else{
                    Log.d(TAG, "onError: 查询失败，错误代码："+i+","+s);
                }
            }
        });
    }

    /**
     * 进行点赞操作
     * @param position
     * @param blogId
     * @param userId
     */
    protected void saveZan(final int position, String blogId, String userId){
        Zan zan = new Zan();
        zan.setBlogId(blogId);
        zan.setUserId(userId);
        zan.save(context, new SaveListener() {
            @Override
            public void onSuccess() {
                Blog blog = getItem(position);
                blog.setLove(blog.getLove() + 1);
                blog.update(context, new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        // 刷新 ListView
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        Log.d(TAG, "onFailure: 更新 Blog 失败，错误代码：" + i + "," + s);
                    }
                });
            }

            @Override
            public void onFailure(int i, String s) {
                Log.d(TAG, "onFailure: 保存赞失败，错误代码：" + i + "," + s);
            }
        });
    }

    /**
     * 分享 blog 到第三方社交平台
     * @param position
     */
    protected void shareBlog(int position){
        Blog blog = getItem(position);
        OnekeyShare oks = new OnekeyShare();
        // 关闭 sso 授权
        oks.disableSSOWhenAuthorize();
        // title 标题，印象笔记、邮箱、信息、微信、人人网、QQ空间使用
        oks.setTitle("来自即时通信的分享");
        // titleUrl 是标题的网络链接，仅在 Linked-in，QQ 和 QQ 空间使用
        oks.setTitleUrl("http://sharesdk.cn");
        // text 是分享文本，所有平台都需要这个字段
        if(TextUtils.isEmpty(blog.getContent())){
            oks.setText("来自李铭淋毕业设计的分享");
        }else{
            oks.setText(blog.getContent());
        }
        // 分享网络图片，新浪微波分享网络图片需要通过审核后申请高级写入接口，否则注释掉测试新浪微波
        if(!TextUtils.isEmpty(blog.getImgUrls())){
            // 通过 ShareSDK 分享图片时，目前最多只能分享一幅图片
            String[] imgUrls = blog.getImgUrls().split("&");
            oks.setImageUrl(imgUrls[0]);
            // siteUrl 是分享次内荣的网站地址，仅在 QQ 空间使用
            oks.setSiteUrl(imgUrls[0]);
        }
        // url 仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://sharesdk.cn");
        // comment 是对这条分享的评论，尽在人人网和 QQ 空间使用
        oks.setComment("测试评论文本");
        // site 是分享次内容的网站名称，仅在 QQ 空间使用
        oks.setSite("ShareSDK");
        // 启动分享 GUI
        oks.show(context);
    }

    private void showBlogImages(String imgUrls,RelativeLayout imageContainer){
        String[] urls = imgUrls.split("&");
        // 每一个 ImageView 的尺寸
        // 整个屏幕的宽度 - 左边距 15dp - 右边距 15dp - 两个 ImageView 之间的距离 15dp
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int imgWidth = (int) ((screenWidth - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,45,context.getResources().getDisplayMetrics()))/2);
        int imgHeight = imgWidth;
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,5,context.getResources().getDisplayMetrics());
        for(int i = 0;i<urls.length;i++){
            final ImageView iv = new ImageView(context);
            iv.setId(i+1);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(imgWidth,imgHeight);
            if(i%2!=0){
                params.addRule(RelativeLayout.RIGHT_OF,i);
                params.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15,context.getResources().getDisplayMetrics());
            }
            if(i>=2){
                params.addRule(RelativeLayout.BELOW,i-1);
                params.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15,context.getResources().getDisplayMetrics());
            }
            iv.setLayoutParams(params);
            iv.setBackgroundResource(R.drawable.input_bg);
            String url = urls[i];
            if(dbUtil.isExist(url)){
                // 本地数据库中已经缓存了 url 所对应的图片
                dbUtil.get(url, new OnDatasLoadFinishListener<Bitmap>() {
                    @Override
                    public void onLoadFinish(List<Bitmap> datas) {
                        Bitmap bitmap = datas.get(0);
                        iv.setImageBitmap(bitmap);
                    }
                });
            }else{
                // 从网络中加载图片
                ImageLoader.getInstance().displayImage(url, iv, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        // 将这副图片放到本地数据库中缓存
                        iv.setImageBitmap(bitmap);
                        dbUtil.save(s,bitmap);
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {

                    }
                });
            }
            imageContainer.addView(iv);
        }
    }

    class ViewHolder{
        @BindView(R.id.iv_item_blog_avatar)
        ImageView ivAvatar;
        @BindView(R.id.tv_item_blog_username)
        TextView tvUsername;
        @BindView(R.id.tv_item_blog_content)
        TextView tvContent;
        @BindView(R.id.rl_item_blog_imagecontainer)
        RelativeLayout imageContainer;
        @BindView(R.id.tv_item_blog_time)
        TextView tvTime;
        @BindView(R.id.tv_item_blog_share)
        TextView tvShare;
        @BindView(R.id.tv_item_blog_love)
        TextView tvLove;
        @BindView(R.id.tv_item_blog_comment)
        TextView tvComment;
        @BindView(R.id.ll_item_blog_commentcontainer)
        LinearLayout commentContainer;

        public ViewHolder(View convertView){
            ButterKnife.bind(this,convertView);
        }
    }
}
