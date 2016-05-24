package cc.seeed.iot.activity.user;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.ui_login.LoginActivity;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.RegularUtils;
import cc.seeed.iot.util.ToolUtil;
import cc.seeed.iot.view.FontButton;
import cc.seeed.iot.view.FontEditView;

public class ResetPwd03Activity extends BaseActivity {
    public static final String Intent_Code = "intent_code";

    @InjectView(R.id.mToolbar)
    Toolbar mToolbar;
    @InjectView(R.id.mEtPwd)
    FontEditView mEtPwd;
    @InjectView(R.id.mEtRePwd)
    FontEditView mEtRePwd;
    @InjectView(R.id.mBtnSubmit)
    FontButton mBtnSubmit;

    String code;
    Dialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pwd_03);
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
        code = getIntent().getStringExtra(Intent_Code);
    }

    public void resetPwd() {
        String pwd = mEtPwd.getText().toString().trim();
        String rePwd = mEtRePwd.getText().toString().trim();
        if (TextUtils.isEmpty(pwd) || pwd.length() < 6) {
            mEtPwd.setError(getString(R.string.pwd_format_error));
            mEtPwd.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(rePwd) || rePwd.length() < 6) {
            mEtRePwd.setError(getString(R.string.pwd_format_error));
            mEtRePwd.requestFocus();
            return;
        } else {
            if (!rePwd.equals(pwd)) {
                mEtRePwd.setError(getString(R.string.pwd_dont_match_error));
                mEtRePwd.requestFocus();
                return;
            }
        }

        dialog = DialogUtils.showProgressDialog(this, getString(R.string.reset_pwd_pwd));
        String email = App.getSp().getString(Constant.SP_USER_EMAIL, "");
        UserLogic.getInstance().resetPwd(email, code, pwd);
    }


    @OnClick(R.id.mBtnSubmit)
    public void onClick(View v) {
        MobclickAgent.onEvent(this, "11004");
        hideKeyboard(v);
       resetPwd();
    }

    @Override
    public String[] monitorEvents() {
        return new String[]{Cmd_UserResetPwd};
    }

    @Override
    public void onEvent(String event, boolean ret, String errInfo, Object[] data) {
        if (Cmd_UserResetPwd.equals(event)) {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (ret) {
                startActivity(new Intent(ResetPwd03Activity.this, LoginAndRegistActivity.class));
                finish();
            } else {
                App.showToastLong(errInfo);
            }
        }
    }
}