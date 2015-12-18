package cc.seeed.iot.ui_main;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.UserResponse;
import retrofit.Callback;
import retrofit.RetrofitError;

public class ChangePwdActivity extends AppCompatActivity {
    private static final String TAG = "ChangePwdActivity";
    public Toolbar mToolbar;
    private User user;

    @InjectView(R.id.input_old_password)
    EditText _oldPwdText;
    @InjectView(R.id.input_new_password)
    EditText _newPwdText;
    @InjectView(R.id.input_new_password_verify)
    EditText _newPwdVerifyText;
    @InjectView(R.id.btn_save)
    Button _savePwdButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pwd);
        ButterKnife.inject(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Change password");

        user = ((MyApplication) getApplication()).getUser();

//        _savePwdButton = (Button) findViewById(R.id.btn_save);

        initView();
    }

    private void initView() {
        _savePwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePwd();
                hideKeyboard();
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(_savePwdButton.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void savePwd() {
        Log.d(TAG, "Save password");

        if (!validate()) {
            onChanePwdFailed();
            return;
        }

        _savePwdButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Change password...");
        progressDialog.show();

        // Implement your own resetEmail logic here.

        checkOldPassword(progressDialog);
    }

    private void checkOldPassword(final ProgressDialog progressDialog) {
        _oldPwdText.setError(null);
        _newPwdText.setError(null);
        _newPwdVerifyText.setError(null);

        final String email = user.email;
        String password = _oldPwdText.getText().toString();

        IotApi api = new IotApi();
        IotService iot = api.getService();
        iot.userLogin(email, password, new Callback<UserResponse>() {
            @Override
            public void success(UserResponse userResponse, retrofit.client.Response response) {
                String status = userResponse.status;
                if (status.equals("200")) {
                    changePwd(progressDialog);
                } else {
                    _oldPwdText.setError("Old password isn't valid");
                    _oldPwdText.requestFocus();
                    onChanePwdFailed();
                }
                progressDialog.dismiss();
            }

            @Override
            public void failure(RetrofitError error) {
                progressDialog.dismiss();
                onChanePwdFailed();

            }
        });
    }

    private void changePwd(final ProgressDialog progressDialog) {

        String password = _newPwdText.getText().toString();
        String access_token = user.user_key;
        IotApi api = new IotApi();
        IotService iot = api.getService();
        iot.userChangePassword(password, access_token, new Callback<UserResponse>() {
            @Override
            public void success(UserResponse userResponse, retrofit.client.Response response) {
                String status = userResponse.status;
                if (status.equals("200")) {
                    onSavePwdSuccess(userResponse);
                } else {
                    _newPwdText.setError(userResponse.msg);
                    _newPwdText.requestFocus();
                    onChanePwdFailed();
                }
                progressDialog.dismiss();
            }

            @Override
            public void failure(RetrofitError error) {
                progressDialog.dismiss();
                onChanePwdFailed();
            }
        });
    }

    public void onSavePwdSuccess(UserResponse userResponse) {
        _savePwdButton.setEnabled(true);
        user.user_key = userResponse.token;
        user.user_id = userResponse.user_id;
        ((MyApplication) getApplication()).setUser(user);
        Toast.makeText(getBaseContext(), "Password changed successfully.", Toast.LENGTH_LONG).show();
        _oldPwdText.setText("");
        _newPwdText.setText("");
        _newPwdVerifyText.setText("");

    }

    public void onChanePwdFailed() {
        Toast.makeText(getBaseContext(), "Password changed failed", Toast.LENGTH_LONG).show();
        _savePwdButton.setEnabled(true);
    }

    private boolean validate() {
        boolean valid = true;

        String new_pwd = _newPwdText.getText().toString();
        String new_verify = _newPwdVerifyText.getText().toString();

        if (new_pwd.isEmpty() || new_pwd.length() < 6) {
            _newPwdText.setError("Password is too short (minimum is 6 characters)");
            valid = false;
        } else if (!new_pwd.equals(new_verify)) {
            _newPwdText.setError("Password doesn't match the confirmation");
            valid = false;
        } else {
            _newPwdText.setError(null);
        }

        return valid;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
