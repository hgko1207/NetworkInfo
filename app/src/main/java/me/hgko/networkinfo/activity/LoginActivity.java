package me.hgko.networkinfo.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.hgko.networkinfo.R;
import me.hgko.networkinfo.domain.Constants;
import me.hgko.networkinfo.util.CommonUtils;

public class LoginActivity extends AppCompatActivity {

    private final static int REQUEST_CODE = 999;

    @BindView(R.id.loginBtn)
    Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        checkSession();
    }

    @OnClick(R.id.loginBtn)
    public void onViewClicked() {
        startLoginPage();
    }

    private void startLoginPage() {
        if (CommonUtils.checkInternetConnection(LoginActivity.this)) {
            Intent intent = new Intent(LoginActivity.this, AccountKitActivity.class);
            AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                    new AccountKitConfiguration.AccountKitConfigurationBuilder(
                            LoginType.PHONE,
                            AccountKitActivity.ResponseType.TOKEN);
            intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build());
            startActivityForResult(intent, REQUEST_CODE);
        } else {
            Toast.makeText(this, "인터넷 연결을 확인하고 다시 시도하십시오.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (result != null) {
                if (result.getAccessToken() != null) {
                    /**
                     * if phone number is verified and access token returned from Account kit
                     * get user information from Account Kit using token in getAccount() method
                     */
                    getAccount();
                } else if (result.getAuthorizationCode() != null) {
                    String userID = result.getAuthorizationCode().substring(0, 10);
                    saveLoginInfo(userID);
                }

                return;
            } else if (result.wasCancelled()) {
                Toast.makeText(this, "Request canceled", Toast.LENGTH_SHORT).show();
                return;
            } else {
                Toast.makeText(this, result.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Gets current account from Facebook Account Kit which include user's phone number.
     */
    private void getAccount() {
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(final Account account) {
                // Get phone number
                PhoneNumber phoneNumber = account.getPhoneNumber();
                saveLoginInfo(phoneNumber.toString());
            }

            @Override
            public void onError(final AccountKitError error) {
                Log.e("AccountKit", error.toString());
                Toast.makeText(getApplicationContext(), "Something went error", Toast.LENGTH_SHORT).show();
                System.exit(1);
            }
        });
    }

    /**
     * Save user phone number and server ip address and port number into shared preferences after phone number is verified by Account Kit service
     *
     * @param userNumber
     */
    private void saveLoginInfo(String userNumber) {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFERENCE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.USER_ID, userNumber);
        editor.apply();

        checkSession();
    }

    /**
     * Check user registered in this application
     */
    private void checkSession() {
        /**
         * Create shared preferences object
         */
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFERENCE_KEY, Context.MODE_PRIVATE);
        /**
         * Get user phone number from shared preferences
         */
        String userID = sharedPreferences.getString(Constants.USER_ID, null);

        /**
         * open Main activity if user registered
         */
        if (userID != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }
}
