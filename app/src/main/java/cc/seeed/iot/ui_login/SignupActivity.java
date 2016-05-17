package cc.seeed.iot.ui_login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.util.CommonUrl;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.UserResponse;
import retrofit.Callback;
import retrofit.RetrofitError;

public class SignupActivity extends BaseActivity {
    private static final String TAG = "SignupActivity";
    private User user;
    ProgressDialog dialog;

    @InjectView(R.id.input_email)
    EditText _emailText;
    @InjectView(R.id.input_password)
    EditText _passwordText;
    //    @InjectView(R.id.input_pwd_verify) EditText _pwdVerifyText;
    @InjectView(R.id.btn_signup)
    Button _signupButton;
    @InjectView(R.id.link_login)
    TextView _loginLink;
    @InjectView(R.id.link_server)
    TextView _serverLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.inject(this);

        user = UserLogic.getInstance().getUser();
        _emailText.setText(App.getSp().getString("user_email", ""));

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
        _serverLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SelServerActivity.class);
                startActivity(intent);
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
        _serverLink.setText(getString(R.string.serverOn) + " " + ota_server_url + getString(R.string.change));
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        dialog = new ProgressDialog(SignupActivity.this, R.style.AppTheme_Dark_Dialog);
        dialog.setIndeterminate(true);
        dialog.setMessage("Creating Account...");
        dialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        App.getSp().edit().putString("user_email", email).commit();
        UserLogic.getInstance().regist(email, password);
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Create account failed", Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
//        String pwd_verify = _pwdVerifyText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6) {
            _passwordText.setError("Password is too short (minimum is 6 characters)");
            valid = false;
        } else {
            _passwordText.setError(null);
        }
        return valid;
    }


    @Override
    public String[] monitorEvents() {
        return new String[]{Cmd_UserRegiest};
    }

    @Override
    public void onEvent(String event, boolean ret, String errInfo, Object[] data) {
        if (Cmd_UserRegiest.equals(event)) {
            if (dialog != null){
                dialog.dismiss();
            }
            _signupButton.setEnabled(true);
            if (ret) {
                Intent intent = new Intent(this, MainScreenActivity.class);
                startActivity(intent);
            }else {
                App.showToastShrot("Create account failed: "+errInfo);
            }
        }
    }
}

