package me.hgko.networkinfo.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.hgko.networkinfo.R;
import me.hgko.networkinfo.domain.Constants;
import me.hgko.networkinfo.domain.LteSignalInfo;
import me.hgko.networkinfo.manager.DataManager;

public class ModeActivity extends AppCompatActivity {

    private final DataManager dataManager = DataManager.getInstance();

    private final long FINISH_INTERVAL_TIME = 2000;
    @BindView(R.id.radio1)
    RadioButton radio1;
    @BindView(R.id.radio2)
    RadioButton radio2;
    @BindView(R.id.radio3)
    RadioButton radio3;
    @BindView(R.id.radio4)
    RadioButton radio4;
    @BindView(R.id.radio5)
    RadioButton radio5;
    @BindView(R.id.moveBtn)
    Button moveBtn;
    private long backPressedTime = 0;

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode);
        ButterKnife.bind(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    @OnClick(R.id.moveBtn)
    public void onViewClicked() {
        int mode = 1;
        if (radio1.isChecked()) {
            mode = LteSignalInfo.HOUSE_MODE;
        } else if (radio2.isChecked()) {
            mode = LteSignalInfo.COMPANY_MODE;
        } else if (radio3.isChecked()) {
            mode = LteSignalInfo.DRIVING_MODE;
        } else if (radio4.isChecked()) {
            mode = LteSignalInfo.WALKING_MODE;
        } else if (radio5.isChecked()) {
            mode = LteSignalInfo.ETC_MODE;
        }

        dataManager.setMode(mode);

        startActivity(new Intent(ModeActivity.this, MainActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
}
