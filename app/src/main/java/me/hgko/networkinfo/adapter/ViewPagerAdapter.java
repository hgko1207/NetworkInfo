package me.hgko.networkinfo.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by inspace on 2018-07-23.
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> fragmentList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment) {
        fragmentList.add(fragment);
    }

    @Override
    public Fragment getItem(int position) {
        Log.d("MainActivity", "position : " + position);
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return (fragmentList != null) ? fragmentList.size() : 0;
    }

    @Override
    public void notifyDataSetChanged() {
        List<Fragment> update = new ArrayList<>();
        for (int i = 0, n = fragmentList.size(); i < n; i++) {
            Fragment f = fragmentList.get(i);
            if (f == null) continue;
            int pos = getItemPosition(f);
            while (update.size() <= pos) {
                update.add(null);
            }
            update.set(pos, f);
        }
        fragmentList = update;
        super.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        switch (position) {
            case 0:
                title = "모바일";
                break;
            case 1:
                title = "무선";
                break;
            case 2:
                title = "위치";
                break;
            case 3:
                title = "지도";
                break;
            case 4:
                title = "날씨";
                break;
            case 5:
                title = "위치";
                break;
        }
        return title;
    }
}
