package cc.seeed.iot.ui_login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.activity.TestActivity;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.util.ToolUtil;

public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    User user;
    ProgressDialog dialog;

    @InjectView(R.id.input_email)
    EditText _emailText;
    @InjectView(R.id.input_password)
    EditText _passwordText;
    @InjectView(R.id.btn_login)
    Button _loginButton;
    @InjectView(R.id.forgot_pwd)
    TextView _forgotPwd;
    @InjectView(R.id.link_server)
    TextView _serverLink;
    @InjectView(R.id.link_signup)
    TextView _signupLink;
    @InjectView(R.id.mIvLogo)
    ImageView mIvLogo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        _emailText.setText(App.getSp().getString("user_email", ""));

        user = UserLogic.getInstance().getUser();

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hideKeyboard(_emailText);
                login();
            }
        });

        _forgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ResetPwd01Activity.class);
                startActivity(intent);
            }
        });

        _serverLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SelServerActivity.class);
                startActivity(intent);
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
        mIvLogo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                if (ToolUtil.isApkDebug()){
                    Intent intent = new Intent(getApplicationContext(), TestActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh_layout();
    }

    private void refresh_layout() {
        String ota_server_url = ((App) getApplication()).getOtaServerUrl();
       // String server = CommonUrl.OTA_SERVER_URL.replace("https://", "");
        _serverLink.setText(getString(R.string.serverOn) + " " + ota_server_url + getString(R.string.change));
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            //    onLoginFailed();
            return;
        }

        //      _loginButton.setEnabled(false);

        dialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        dialog.setIndeterminate(true);
        dialog.setMessage("Authenticating...");
        dialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        App.getSp().edit().putString("user_email", email).commit();
        UserLogic.getInstance().login(email, password);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6) {
            _passwordText.setError("enter more than six characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    @Override
    public String[] monitorEvents() {
        return new String[]{Cmd_UserLogin};
    }

    @Override
    public void onEvent(String event, boolean ret, String errInfo, Object[] data) {
        if (Cmd_UserLogin.equals(event)) {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (ret) {
                Intent intent = new Intent(this, MainScreenActivity.class);
                startActivity(intent);
                finish();
            } else {
                App.showToastShrot(errInfo);
            }
        }

    }
}
