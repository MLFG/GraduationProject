package cn.edu.lin.graduationproject.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

import butterknife.ButterKnife;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.db.BmobDB;
import cn.edu.lin.graduationproject.R;
import cn.edu.lin.graduationproject.constant.Constants;
import cn.edu.lin.graduationproject.ui.BaseActivity;

/**
 * Created by liminglin on 17-2-28.
 */

public abstract class BaseFragment extends Fragment {

    BmobUserManager userManager;
    BmobChatManager chatManager;
    BmobDB bmobDB;

    View headerView;

    BaseActivity baseActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userManager = BmobUserManager.getInstance(getActivity());
        chatManager = BmobChatManager.getInstance(getActivity());
        bmobDB = BmobDB.create(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = null;
        // SettingFragment 标准的布局文件名称应该是 fragment_setting
        String clazzName = getClass().getSimpleName(); // SettingFragment
        if(clazzName.contains("Fragment")){
            // fragment_setting
            String layoutName = "fragment_" + clazzName.substring(0,clazzName.indexOf("Fragment")).toLowerCase(Locale.US);
            int layoutId = getResources().getIdentifier(layoutName,"layout",getActivity().getPackageName());
            if(layoutId != 0){
                view = inflater.inflate(layoutId,container,false);
            }else{
                view = createMyView(inflater,container,savedInstanceState);
            }
        }else{
            view = createMyView(inflater,container,savedInstanceState);
        }

        ButterKnife.bind(this,view);
        headerView = view.findViewById(R.id.headerview);
        baseActivity = (BaseActivity) getActivity();
        return view;
    }

    public abstract View createMyView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState);

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void init(){
        // NO-OP 钩子方法
    }

    public void setHeaderTitle(String title,Constants.Position position){
        baseActivity.setHeaderView(headerView);
        baseActivity.setHeaderTitle(title,position);
    }

    public void setHeaderImage(Constants.Position pos, int resId, boolean colorFilter, View.OnClickListener listener){
        baseActivity.setHeaderView(headerView);
        baseActivity.setHeaderImage(pos,resId,colorFilter,listener);
    }

    public void toast(String text){
        baseActivity.toast(text);
    }

    public void log(String log){
        baseActivity.log(log);
    }

    public void log(String log,int error,String msg){
        baseActivity.log(log,error,msg);
    }

    public void toastAndLog(String text,String log){
        baseActivity.toastAndLog(text,log);
    }

    public void toastAndLog(String text,int error,String msg){
        baseActivity.toastAndLog(text,error,msg);
    }

    public void jumpTo(Class<?> clazz,boolean isFinish){
        Intent intent = new Intent(getActivity(),clazz);
        startActivity(intent);
        if(isFinish){
            getActivity().finish();
        }
    }

    public void jumpTo(Intent intent,boolean isFinish){
        startActivity(intent);
        if(isFinish){
            getActivity().finish();
        }
    }
}
