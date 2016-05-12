package cc.seeed.iot.ui_login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.logic.UserLogic;

public class ResetPwd02Activity extends BaseActivity {

    @InjectView(R.id.mTvReqAgain)
    TextView mTvReqAgain;
    @InjectView(R.id.mEtCode)
    EditText mEtCode;
    @InjectView(R.id.mEtNewPwd)
    EditText mEtNewPwd;
    @InjectView(R.id.mEtReNewPwd)
    EditText mEtReNewPwd;
    @InjectView(R.id.mBtnResetPwd)
    AppCompatButton mBtnResetPwd;
    @InjectView(R.id.mTvCancel)
    TextView mTvCancel;


    private static final String TAG = "ResetPwd01Activity";
    public static final String Intent_Email = "intent_email";
    ProgressDialog dialog;
    String email;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pwd_02);
        ButterKnife.inject(this);
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        email = intent.getStringExtra(Intent_Email);
        if (email.isEmpty()) {
            finish();
        }
    }

    public void resetEmail() {
        Log.d(TAG, "ResetEmail");

        if (!validate()) {
            App.showToastShrot("Reset failed");
            return;
        }

        mBtnResetPwd.setEnabled(false);

        dialog = new ProgressDialog(ResetPwd02Activity.this, R.style.AppTheme_Dark_Dialog);
        dialog.setIndeterminate(true);
        dialog.setMessage("Reset your password ...");
        dialog.show();

        String code = mEtCode.getText().toString();
        String newPwd = mEtNewPwd.getText().toString();

        UserLogic.getInstance().resetPwd(email, code, newPwd);
    }

    public boolean validate() {
        boolean valid = true;

        String code = mEtCode.getText().toString();
        String newPwd = mEtNewPwd.getText().toString();
        String reNewPwd = mEtReNewPwd.getText().toString();

        if (code == null || code.length() != 6) {
            mEtCode.setError("6-digit verification code");
            mEtCode.requestFocus();
            return false;
        }

        if (newPwd.isEmpty() || newPwd.length() < 6) {
            mEtNewPwd.setError("Password more than six char");
            mEtNewPwd.requestFocus();
            return false;
        }
        if (reNewPwd.isEmpty() || reNewPwd.length() < 6) {
            mEtReNewPwd.setError("Password more than six char");
            mEtReNewPwd.requestFocus();
            return false;
        }

        if (!newPwd.equals(reNewPwd)) {
            mEtReNewPwd.setError("Passwords don't match");
            mEtReNewPwd.requestFocus();
            return false;
        }

        return valid;
    }

    public void sendCodeAgain() {

        dialog = new ProgressDialog(ResetPwd02Activity.this, R.style.AppTheme_Dark_Dialog);
        dialog.setIndeterminate(true);
        dialog.setMessage("Reset your email...");
        dialog.show();

        UserLogic.getInstance().forgetPwd(email);
    }

    @Override
    public String[] monitorEvents() {
        return new String[]{Cmd_UserResetPwd, Cmd_UserForgetPwd};
    }

    @Override
    public void onEvent(String event, boolean ret, String errInfo, Object[] data) {
        if (Cmd_UserResetPwd.equals(event)) {
            if (dialog != null) {
                dialog.dismiss();
            }
            mBtnResetPwd.setEnabled(true);
            if (ret) {
                startActivity(new Intent(ResetPwd02Activity.this, LoginActivity.class));
            } else {
                App.showToastLong(errInfo);
            }
        } else if (Cmd_UserForgetPwd.equals(event)) {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (ret) {
                App.showToastShrot("Verification code has been sent");
            } else {
                App.showToastShrot(errInfo);
            }
        }
    }

    @OnClick({R.id.mTvReqAgain, R.id.mBtnResetPwd, R.id.mTvCancel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mTvReqAgain:
                sendCodeAgain();
                break;
            case R.id.mBtnResetPwd:
                hideKeyboard(view);
                resetEmail();
                break;
            case R.id.mTvCancel:
                finish();
                break;
        }
    }
}