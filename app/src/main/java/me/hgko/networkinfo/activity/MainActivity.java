package me.hgko.networkinfo.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.hgko.networkinfo.R;
import me.hgko.networkinfo.adapter.ViewPagerAdapter;
import me.hgko.networkinfo.domain.Constants;
import me.hgko.networkinfo.fragment.FusedLocationFragment;
import me.hgko.networkinfo.fragment.GoogleMapFragment;
import me.hgko.networkinfo.fragment.LocationFragment;
import me.hgko.networkinfo.fragment.MobileFragment;
import me.hgko.networkinfo.fragment.WeatherFragment;
import me.hgko.networkinfo.fragment.WirelessFragment;

import static me.hgko.networkinfo.util.CommonUtils.controlBackgroundServices;

/**
 * 메인 화면
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;

    private ActionBarDrawerToggle toggle;

    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        setupViewPager();
        controlBackgroundServices(MainActivity.this, Constants.FLAG_START);
    }

    /**
     * View Pager 설정
     */
    private void setupViewPager() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new MobileFragment());
        viewPagerAdapter.addFragment(new WirelessFragment());
        viewPagerAdapter.addFragment(new LocationFragment());
        //viewPagerAdapter.addFragment(new GoogleMapFragment());
        //viewPagerAdapter.addFragment(new WeatherFragment());
        //viewPagerAdapter.addFragment(new FusedLocationFragment());
        viewPager.setOffscreenPageLimit(viewPagerAdapter.getCount());
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(this, "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("networkLog", "MainActivity onDestroy()");
        controlBackgroundServices(MainActivity.this, Constants.FLAG_STOP);
    }
}
