package cc.seeed.iot.activity.user;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.RegularUtils;
import cc.seeed.iot.util.ToolUtil;
import cc.seeed.iot.view.FontButton;
import cc.seeed.iot.view.FontEditView;

public class ResetPwd01Activity extends BaseActivity {
    @InjectView(R.id.mToolbar)
    Toolbar mToolbar;
    @InjectView(R.id.mEtEmail)
    FontEditView mEtEmail;
    @InjectView(R.id.mBtnSubmit)
    FontButton mBtnSubmit;

    Dialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pwd_01);
        ButterKnife.inject(this);
        initToolBar();
        initData();
    }

    public void initToolBar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.reset_pwd_title_01));
    }

    private void initData() {
        String email = App.getSp().getString(Constant.SP_USER_EMAIL, "");
        mEtEmail.setText(email);
        mEtEmail.setSelection(email.length());
    }

    public void resetEmail() {
        String email = mEtEmail.getText().toString().trim();
        if (!RegularUtils.isEmail(email)) {
            mEtEmail.setError(getString(R.string.email_format_error));
            mEtEmail.requestFocus();
            return;
        }
        dialog = DialogUtils.showProgressDialog(this, getString(R.string.reset_pwd_email));
        App.getSp().edit().putString(Constant.SP_USER_EMAIL, email).commit();
        UserLogic.getInstance().forgetPwd(email);
    }


    @OnClick(R.id.mBtnSubmit)
    public void onClick() {
        hideKeyboard(mEtEmail);
        resetEmail();
    }

    @Override
    public String[] monitorEvents() {
        return new String[]{Cmd_UserForgetPwd};
    }

    @Override
    public void onEvent(String event, boolean ret, String errInfo, Object[] data) {
        if (Cmd_UserForgetPwd.equals(event)) {
            if (!ToolUtil.isTopActivity(ResetPwd01Activity.this, ResetPwd01Activity.this.getClass().getSimpleName())) {
                return;
            }
            if (dialog != null) {
                dialog.dismiss();
            }
            if (ret) {
                App.showToastShrot("Verification code has been sent");
                startActivity( new Intent(ResetPwd01Activity.this, ResetPwd02Activity.class));
            } else {
                mEtEmail.setError(errInfo);
                mEtEmail.requestFocus();
            }
        }
    }
}