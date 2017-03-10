package cn.edu.lin.graduationproject.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import cn.edu.lin.graduationproject.fragment.FindFragment;
import cn.edu.lin.graduationproject.fragment.FriendFragment;
import cn.edu.lin.graduationproject.fragment.MessageFragment;
import cn.edu.lin.graduationproject.fragment.SettingFragment;

/**
 * Created by liminglin on 17-3-1.
 */

public class MyPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "MyPagerAdapter";

    List<Fragment> fragments;

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new ArrayList<>();
        fragments.add(new MessageFragment());
        fragments.add(new FriendFragment());
        fragments.add(new FindFragment());
        fragments.add(new SettingFragment());
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
