package cn.edu.lin.graduationproject.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.db.BmobDB;
import cn.bmob.v3.listener.UpdateListener;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.util.DialogUtil;


/**
 * Created by liminglin on 17-3-1.
 */

public class InvitationAdapter extends MyBaseAdapter<BmobInvitation> {

    private static final String TAG = "InvitationAdapter";

    public InvitationAdapter(Context context, List<BmobInvitation> dataSource) {
        super(context, dataSource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.item_newfriend_layout,parent,false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final BmobInvitation invitation = getItem(position);
        // 设置头像
        setAvatar(invitation.getAvatar(),viewHolder.ivAvatar);
        // 设置发送该添加好友申请的用户名
        viewHolder.tvUsername.setText(invitation.getFromname());
        viewHolder.ibAgree.setVisibility(View.VISIBLE);
        viewHolder.ibReject.setVisibility(View.VISIBLE);
        viewHolder.tvAdd.setVisibility(View.INVISIBLE);

        viewHolder.ibAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 同意添加好友
                // 1.根据 添加好友申请 发送者的 username 在服务器的 _user 表中查询，确认确实有该用户信息
                // 2.更新当前登录用户在 _user 表中的内容，更新 contacts 字段值，将 添加好友申请 发送者作为当前登录用户的好友
                // 3.更新当前登录用户所对应的本地数据库中 tab_new_contacts 数据表中 username 所发送的所有添加好友申请数据记录的 status 值，从初始的 2 更新为 1
                // 4.当前登录用户向 添加好友申请 发送者的设备推送一条信息，该消息的 tag 值为 agree
                // 5.向当前登录用户所对应的本地数据中 friends 表中插入一条数据记录
                // 6.回调监听器中的回调方法
                BmobUserManager.getInstance(context).agreeAddContact(invitation, new UpdateListener() {
                    @Override
                    public void onSuccess() {
                        // 添加好友完毕
                        viewHolder.ibAgree.setVisibility(View.INVISIBLE);
                        viewHolder.ibReject.setVisibility(View.INVISIBLE);
                        viewHolder.tvAdd.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        Toast.makeText(context,"添加好友失败，请稍候重试.",Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFailure: 添加好友失败,错误代码："+i+","+s);
                    }
                });
            }
        });
        viewHolder.ibReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 拒绝添加好友
                // 将 添加好友申请 从 tab_new_contacts 数据表中删除即可
                DialogUtil.showDialog(context, "删除通知", "您确定要删除" + invitation.getFromname() + "的添加好友申请吗？", true, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<BmobInvitation> list = BmobDB.create(context).queryBmobInviteList();

                        for (BmobInvitation bi : list){
                            if(bi.getFromname().equals(invitation.getFromname())){
                                BmobDB.create(context).deleteInviteMsg(bi.getFromid(),String.valueOf(bi.getTime()));
                            }
                        }

                        // 从数据源中删除对应的内容
                        remove(invitation);
                    }
                });

            }
        });
        return convertView;
    }

    class ViewHolder{
        @Bind(R.id.iv_item_newfriend_avatar)
        ImageView ivAvatar;
        @Bind(R.id.tv_item_newfriend_name)
        TextView tvUsername;
        @Bind(R.id.ib_item_newfriend_agree)
        ImageButton ibAgree;
        @Bind(R.id.ib_item_newfriend_reject)
        ImageButton ibReject;
        @Bind(R.id.tv_item_newfriend_add)
        TextView tvAdd;

        public ViewHolder(View convertView){
            ButterKnife.bind(this,convertView);
        }
    }
}
