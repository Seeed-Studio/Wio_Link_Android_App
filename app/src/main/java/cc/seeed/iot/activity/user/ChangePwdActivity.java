package cc.seeed.iot.activity.user;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.UserResponse;
import retrofit.Callback;
import retrofit.RetrofitError;

public class ChangePwdActivity extends BaseActivity {
    private static final String TAG = "ChangePwdActivity";
    public Toolbar mToolbar;
    ProgressDialog dialog;

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

        initView();
    }

    private void initView() {
        if (App.getApp().isDefaultServer()){
            _oldPwdText.setVisibility(View.VISIBLE);
        }else {
            _oldPwdText.setVisibility(View.GONE);
        }
        _savePwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePwd();
                hideKeyboard(v);
            }
        });
    }


    public void savePwd() {
        Log.d(TAG, "Save password");

        if (!validate()) {
            App.showToastShrot("Password changed failed");
            return;
        }

        _savePwdButton.setEnabled(false);

        dialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        dialog.setIndeterminate(true);
        dialog.setMessage("Change password...");
        dialog.show();

        String oldPwd = _oldPwdText.getText().toString().trim();
        String newPwd = _newPwdText.getText().toString().trim();
        UserLogic.getInstance().changePwd(oldPwd, newPwd);
        //checkOldPassword(progressDialog);
    }

    private boolean validate() {
        boolean valid = true;

        String oldPwd = _oldPwdText.getText().toString();
        String new_pwd = _newPwdText.getText().toString();
        String new_verify = _newPwdVerifyText.getText().toString();
        if (!App.getApp().isDefaultServer()){
            oldPwd = "123456";
        }
        if (oldPwd.isEmpty() || oldPwd.length() < 6) {
            _oldPwdText.setError("Password is too short (minimum is 6 characters)");
            valid = false;
        } else if (new_pwd.isEmpty() || new_pwd.length() < 6) {
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
    public String[] monitorEvents() {
        return new String[]{Cmd_UserChangePwd};
    }

    @Override
    public void onEvent(String event, boolean ret, String errInfo, Object[] data) {
        if (Cmd_UserChangePwd.equals(event)) {
            if (dialog != null) {
                dialog.dismiss();
            }
            _savePwdButton.setEnabled(true);
            if (ret) {
                App.showToastShrot("Password changed successfully.");
                finish();
            } else {
                App.showToastLong(errInfo);
            }
        }
    }
}
