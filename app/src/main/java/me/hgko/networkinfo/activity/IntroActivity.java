package me.hgko.networkinfo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.hgko.networkinfo.R;
import me.hgko.networkinfo.domain.Constants;
import me.hgko.networkinfo.util.CommonUtils;

/**
 * 초기 Intro 화면
 */
public class IntroActivity extends Activity {

    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    @BindView(R.id.splashIcon)
    ImageView splashIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (checkPermisson(CommonUtils.getPermissions())) {
                    startLoginScreen();
                }
            }
        }, 2000);
    }

    /**
     * Check permissions
     * @param permissions
     * @return
     */
    private boolean checkPermisson(String[] permissions) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            List<String> permissionsNeededList = new ArrayList<>();

            for (String permisson : permissions) {
                int result = ContextCompat.checkSelfPermission(IntroActivity.this, permisson);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeededList.add(permisson);
                }
            }
            if (!permissionsNeededList.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsNeededList.toArray(new String[permissionsNeededList.size()]),
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                return false;
            }
            return true;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    int grantedPermissions = 0;
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            ++grantedPermissions;
                        }
                    }

                    if (grantedPermissions == grantResults.length) {
                        startLoginScreen();
                    } else {
                        checkMissedPermission();
                    }
                }
                return;
            }
        }
    }

    /**
     * Promt user for check Missed permission
     */
    private void checkMissedPermission() {
        final AlertDialog dialog = CommonUtils.showAlertDlg(IntroActivity.this, Constants.MISSED_PERMISSION, Constants.ASK_GRANT_MISSED_PERMISSIONS);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, Constants.NO, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    Thread.sleep(500);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, Constants.YES, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                checkPermisson(CommonUtils.getPermissions());
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void startLoginScreen() {
        startActivity(new Intent(IntroActivity.this, ModeActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
