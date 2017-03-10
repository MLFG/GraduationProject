package cn.edu.lin.graduationproject.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.bean.MyUser;

/**
 * Created by liminglin on 17-3-1.
 */

public class FriendAdapter extends MyBaseAdapter<MyUser> implements SectionIndexer{
    private static final String TAG = "FriendAdapter";
    public FriendAdapter(Context context, List<MyUser> dataSource) {
        super(context, dataSource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.item_friend_layout,parent,false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        MyUser user = getItem(position);
        String avatar = user.getAvatar();
        if(TextUtils.isEmpty(avatar)){
            viewHolder.ivAvatar.setImageResource(R.drawable.ic_launcher);
        }else{
            ImageLoader.getInstance().displayImage(avatar,viewHolder.ivAvatar);
        }

        viewHolder.tvLetter.setText(user.getLetter() + "");
        viewHolder.tvUsername.setText(user.getUsername());
        if(getPositionForSection(getSectionForPosition(position))!=position){
            viewHolder.tvLetter.setVisibility(View.GONE);
        }else{
            viewHolder.tvLetter.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        for(int i = 0 ; i < getCount() ; i++){
            if(getItem(i).getLetter() == sectionIndex){
                return i;
            }
        }

        return -2;
    }

    @Override
    public int getSectionForPosition(int position) {
        return getItem(position).getLetter();
    }


    class ViewHolder{
        @BindView(R.id.iv_item_friend_avatar)
        ImageView ivAvatar;
        @BindView(R.id.tv_item_friend_name)
        TextView tvUsername;
        @BindView(R.id.tv_item_friend_letter)
        TextView tvLetter;

        public ViewHolder(View convertView){
            ButterKnife.bind(this,convertView);
        }
    }


}
