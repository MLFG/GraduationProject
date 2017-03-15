package cn.edu.lin.graduationproject.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.im.bean.BmobRecent;
import cn.edu.lin.graduationproject.R;
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
        return null;
    }

    class ViewHolder{
        @Bind(R.id.iv_item_recent_avatar)
        ImageView ivAvatar;
        @Bind(R.id.tv_item_recent_name)
        TextView tvUsername;
        @Bind(R.id.tv_item_recent_time)
        TextView tvTime;
        @Bind(R.id.tv_item_recent_content)
        TextView tvContent;
        @Bind(R.id.bv_item_recent_unread)
        BadgeView bvCount;

        public ViewHolder(View convertView){
            ButterKnife.bind(this,convertView);
        }
    }
}
