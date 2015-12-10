package cc.seeed.iot.ui_login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.util.Common;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.UserResponse;
import retrofit.Callback;
import retrofit.RetrofitError;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private User user;

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

        user = ((MyApplication) getApplication()).getUser();

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        String ota_server_ip = ((MyApplication) getApplication()).getOtaServerIP();
        if (ota_server_ip.equals(Common.OTA_INTERNATIONAL_IP)) {
            _serverLink.setText(getString(R.string.serverOn) + " International" + getString(R.string.change));
        } else if (ota_server_ip.equals(Common.OTA_CHINA_IP)) {
            _serverLink.setText(getString(R.string.serverOn) + " China" + getString(R.string.change));
        } else {
            _serverLink.setText(getString(R.string.serverOn) + " " + ota_server_ip + getString(R.string.change));
        }
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        // Implement your own resetEmail logic here.

        attemptRegister(progressDialog);
    }

    public void onSignupSuccess(String email, UserResponse userResponse) {
        _signupButton.setEnabled(true);
        user.email = email;
        user.user_key = userResponse.token;
        user.user_id = userResponse.user_id;
        ((MyApplication) getApplication()).setUser(user);
        ((MyApplication) getApplication()).setLoginState(true);
        Intent intent = new Intent(this, MainScreenActivity.class);
        startActivity(intent);
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
            _passwordText.setError("enter more than six characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

//        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
//            _pwdVerifyText.setError("between 4 and 10 alphanumeric characters");
//            valid = false;
//        } else {
//            _pwdVerifyText.setError(null);
//        }

        return valid;
    }


    private void attemptRegister(final ProgressDialog progressDialog) {
        _emailText.setError(null);
        _passwordText.setError(null);

        final String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
//        String passwordVerify = mPasswordVerifyView.getText().toString();

        IotApi api = new IotApi();
        IotService iot = api.getService();
        iot.userCreate(email, password, new Callback<UserResponse>() {
            @Override
            public void success(UserResponse userResponse, retrofit.client.Response response) {
                String status = userResponse.status;
                if (status.equals("200")) {
                    onSignupSuccess(email, userResponse);
                } else {
                    _emailText.setError(userResponse.msg);
                    _emailText.requestFocus();
                    onSignupFailed();
                }
                progressDialog.dismiss();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(SignupActivity.this, R.string.ConnectServerFail, Toast.LENGTH_LONG).show();
                progressDialog.dismiss();

            }
        });
    }
}

