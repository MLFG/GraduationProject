package cn.edu.lin.graduationproject.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import cn.edu.lin.graduationproject.R;

/**
 * Created by liminglin on 17-2-28.
 */

public abstract class MyBaseAdapter<T> extends BaseAdapter{

    Context context;
    LayoutInflater inflater;
    List<T> dataSource;

    public MyBaseAdapter(Context context,List<T> dataSource){
        super();
        this.context = context;
        this.dataSource = dataSource;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    @Override
    public T getItem(int position) {
        return dataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addAll(List<T> list,boolean isClear){
        if(isClear){
            dataSource.clear();
        }
        dataSource.addAll(list);
        notifyDataSetChanged();
    }

    public void add(T t){
        dataSource.add(t);
        notifyDataSetChanged();
    }

    public void remove(T t){
        dataSource.remove(t);
        notifyDataSetChanged();
    }

    public void clear(){
        dataSource.clear();
        notifyDataSetChanged();
    }

    public void setAvatar(String avatar,ImageView iv){
        if(TextUtils.isEmpty(avatar)){
            iv.setImageResource(R.drawable.ic_launcher);
        }else{
            ImageLoader.getInstance().displayImage(avatar,iv);
        }
    }
}
