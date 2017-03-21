package cn.edu.lin.graduationproject.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.v3.listener.PushListener;
import cn.edu.lin.graduationproject.R;

/**
 * Created by liminglin on 17-3-1.
 */

public class AddFriendAdapter extends MyBaseAdapter<BmobChatUser> {

    private static final String TAG = "AddFriendAdapter";

    public AddFriendAdapter(Context context, List<BmobChatUser> dataSource) {
        super(context, dataSource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.item_addfriend_layout,parent,false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final BmobChatUser user = getItem(position);
        // 头像
        String avatar = user.getAvatar();
        /*if(TextUtils.isEmpty(avatar)){
            viewHolder.ivAvatar.setImageResource(R.drawable.ic_launcher);
        }else{
            ImageLoader.getInstance().displayImage(avatar,viewHolder.ivAvatar);
        }*/
        setAvatar(avatar,viewHolder.ivAvatar);
        // 用户名
        viewHolder.tvUsername.setText(user.getUsername());
        viewHolder.btnAdd.setVisibility(View.VISIBLE);
        viewHolder.tvAdd.setVisibility(View.INVISIBLE);
        viewHolder.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 向第 position 个位置数据所代表的用户发送“添加好友申请”
                // 1) 去 _user 表中确认是否有 user 这个用户
                // 2) 创建了实体类 BmobMsg 对象，此时该对象两个重要属性值分别是：tag 属性，值为“add”；isReaded 属性，值为0
                // 3) 根据 2 创建的 BmobMsg 对象，构建对应的 JsonObject 对象
                // 4) 向 user 用户所使用的设备单独推送一条信息，消息的内容就是 3 所创建的 JsonObject
                // 5) 推送成功后，将 2 所创建的 BmobMsg 对象保存到了服务器的数据库 BmobMsg 数据表中，保存的数据记录两个字段值：tag 为 “add”，isRead 为 “0”
                // 6) 调用咱们自己写的监听器中的相应方法
                BmobChatManager.getInstance(context).sendTagMessage("add", user.getObjectId(), new PushListener() {
                    @Override
                    public void onSuccess() {
                        viewHolder.btnAdd.setVisibility(View.INVISIBLE);
                        viewHolder.tvAdd.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        Log.d(TAG, "onFailure: 发送添加好友申请失败了,"+i+","+s);
                    }
                });
            }
        });
        return convertView;
    }

    class ViewHolder{
        @BindView(R.id.iv_item_addfriend_avatar)
        ImageView ivAvatar;
        @BindView(R.id.tv_item_addfriend_name)
        TextView tvUsername;
        @BindView(R.id.btn_item_addfriend_add)
        Button btnAdd;
        @BindView(R.id.tv_item_addfriend_add)
        TextView tvAdd;

        public ViewHolder(View convertView){
            ButterKnife.bind(this,convertView);
        }
    }
}
