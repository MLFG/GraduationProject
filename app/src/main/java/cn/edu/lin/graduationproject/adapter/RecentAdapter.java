package cn.edu.lin.graduationproject.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.im.bean.BmobRecent;
import cn.bmob.im.db.BmobDB;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.util.EmoUtil;
import cn.edu.lin.graduationproject.util.TimeUtil;
import cn.edu.lin.graduationproject.view.BadgeView;

/**
 * Created by liminglin on 17-3-1.
 */

public class RecentAdapter extends MyBaseAdapter<BmobRecent> {

    private static final String TAG = "RecentAdapter";

    public RecentAdapter(Context context, List<BmobRecent> dataSource) {
        super(context, dataSource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.item_recent_layout,parent,false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        BmobRecent recent = getItem(position);
        // 头像
        setAvatar(recent.getAvatar(),viewHolder.ivAvatar);
        // 用户名
        viewHolder.tvUsername.setText(recent.getUserName());
        // 时间
        viewHolder.tvTime.setText(TimeUtil.getTime(recent.getTime()*1000));
        // 正文（根据不同的聊天类型，来做不同的显示）
        int msgType = recent.getType();
        switch (msgType){
            case 1: // 文本类型的聊天消息
                viewHolder.tvContent.setText(EmoUtil.getSpannableString(recent.getMessage()));
                break;
            case 2: // 图形类型的聊天消息
                viewHolder.tvContent.setText("[图片]");
                break;
            case 3: // 位置类型的聊天消息
                viewHolder.tvContent.setText("[位置]");
                break;
            case 4: // 语音类型的聊天消息
                viewHolder.tvContent.setText("[语音]");
                break;
            default:
                throw new RuntimeException("不正确的消息格式.");
        }
        // 未读的数量
        int count = BmobDB.create(context).getUnreadCount(recent.getTargetid());
        // 如果 count 不为 0 ，则 BadgeView 以红底白字的形式来显示 count
        viewHolder.bvCount.setBadgeCount(count);

        return convertView;
    }

    class ViewHolder{
        @BindView(R.id.iv_item_recent_avatar)
        ImageView ivAvatar;
        @BindView(R.id.tv_item_recent_name)
        TextView tvUsername;
        @BindView(R.id.tv_item_recent_time)
        TextView tvTime;
        @BindView(R.id.tv_item_recent_content)
        TextView tvContent;
        @BindView(R.id.bv_item_recent_unread)
        BadgeView bvCount;

        public ViewHolder(View convertView){
            ButterKnife.bind(this,convertView);
        }
    }
}
