package cn.edu.lin.graduationproject.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.edu.lin.graduationproject.R;

/**
 * Created by liminglin on 17-3-1.
 */

public class EmoGridViewAdapter extends MyBaseAdapter<String> {

    private static final String TAG = "EmoGridViewAdapter";

    public EmoGridViewAdapter(Context context, List<String> dataSource) {
        super(context, dataSource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.item_emo_layout,parent,false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String resName = getItem(position);
        // resName -> resId
        int resId = context.getResources().getIdentifier(resName,"drawable",context.getPackageName());
        viewHolder.ivEmo.setImageResource(resId);
        return convertView;
    }

    class ViewHolder{

        @Bind(R.id.iv_item_emo)
        ImageView ivEmo;

        public ViewHolder(View convertView){
            ButterKnife.bind(this,convertView);
        }
    }
}
