package cc.seeed.iot.activity.user;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

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
import cc.seeed.iot.util.ToolUtil;
import cc.seeed.iot.view.FontButton;
import cc.seeed.iot.view.FontEditView;
import cc.seeed.iot.view.FontTextView;

public class ResetPwd02Activity extends BaseActivity {

    @InjectView(R.id.mToolbar)
    Toolbar mToolbar;
    @InjectView(R.id.mEtCode)
    FontEditView mEtCode;
    @InjectView(R.id.mBtnSubmit)
    FontButton mBtnSubmit;
    @InjectView(R.id.mTvReqAgain)
    FontTextView mTvReqAgain;

    Dialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pwd_02);
        ButterKnife.inject(this);
        initToolBar();
    }

    public void initToolBar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.reset_pwd_title_01));
    }

    private void checkCode(){
        String code = mEtCode.getText().toString();
        if (code == null || code.length() != 6) {
            mEtCode.setError("6-digit verification code");
            mEtCode.requestFocus();
            return ;
        }
        Intent intent = new Intent(this,ResetPwd03Activity.class);
        intent.putExtra(ResetPwd03Activity.Intent_Code, code);
        startActivity(intent);
    }

    public void sendCodeAgain() {
        dialog = DialogUtils.showProgressDialog(this, getString(R.string.reset_pwd_email));
        String email = App.getSp().getString(Constant.SP_USER_EMAIL, "");
        UserLogic.getInstance().sendCheckCodeToEmail(email);
    }

    @Override
    public String[] monitorEvents() {
        return new String[]{Cmd_UserForgetPwd};
    }

    @Override
    public void onEvent(String event, boolean ret, String errInfo, Object[] data) {
      if (Cmd_UserForgetPwd.equals(event)) {
          if (!ToolUtil.isTopActivity(ResetPwd02Activity.this, ResetPwd02Activity.this.getClass().getSimpleName())) {
              return;
          }
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


    @OnClick({R.id.mBtnSubmit, R.id.mTvReqAgain})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mBtnSubmit:
                hideKeyboard(view);
                checkCode();
                break;
            case R.id.mTvReqAgain:
                hideKeyboard(view);
                sendCodeAgain();
                break;
        }
    }
}