package cc.seeed.iot.activity.user;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.entity.UserPlatformInfo;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.RegularUtils;
import cc.seeed.iot.util.ToolUtil;
import cc.seeed.iot.view.FontEditView;

/**
 * author: Jerry on 2016/7/13 17:37.
 * description:
 */
public class BindEmailActivity extends BaseActivity {
    public static final String Intent_PlaUserInfo = "intent_plauserinfo";
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.mEtEmail)
    FontEditView mEtEmail;
    @InjectView(R.id.mEtPwd)
    FontEditView mEtPwd;
    @InjectView(R.id.mBtnBind)
    Button mBtnBind;

    Dialog dialog;
    UserPlatformInfo mUserPlatformInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_email);
        ButterKnife.inject(this);

        initView();
        initData();
    }

    private void initView() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.bind_email);
    }

    private void initData() {
        Intent intent = getIntent();
        mUserPlatformInfo = (UserPlatformInfo) intent.getSerializableExtra(Intent_PlaUserInfo);
        if (mUserPlatformInfo == null) {
            finish();
            return;
        }
        String email = App.getSp().getString(Constant.SP_USER_EMAIL, "");
        if (TextUtils.isEmpty(email)) {
            email = mUserPlatformInfo.getPlatformEmail();
        }
        mEtEmail.setText(email);
        mEtEmail.setSelection(email.length());
    }


    @OnClick(R.id.mBtnBind)
    public void onClick() {
        login();
    }

    private void login() {
        String email = mEtEmail.getText().toString().trim();
        String pwd = mEtPwd.getText().toString().trim();
        if (!RegularUtils.isEmail(email)) {
            mEtEmail.setError(getString(R.string.email_format_error));
            mEtEmail.requestFocus();
            return;
        } else if (TextUtils.isEmpty(pwd) || pwd.length() < 6) {
            mEtPwd.setError(getString(R.string.pwd_format_error));
            mEtPwd.requestFocus();
            return;
        } else {
            dialog = DialogUtils.showProgressDialog(this, "bind email....");
            App.getSp().edit().putString(Constant.SP_USER_EMAIL, email).commit();
            UserLogic.getInstance().login(email, pwd);
        }
    }

    private void bindPla() {
        User user = UserLogic.getInstance().getUser();
        // dialog = DialogUtils.showProgressDialog(this,"Binding ...");
        UserLogic.getInstance().bindOtherPlatform(mUserPlatformInfo.getPlatformID(), mUserPlatformInfo.getPlatformType(), mUserPlatformInfo.getPlatformNickname(), mUserPlatformInfo.getPlatformAvatar());
    }

    @Override
    public void onEvent(String event, boolean ret, String errInfo, Object[] data) {
        if (Cmd_UserLogin.equals(event)) {

            if (ToolUtil.isTopActivity(this, BindEmailActivity.class.getSimpleName())) {
                if (ret) {
                 /*   Intent intent = new Intent(this, MainScreenActivity.class);
                    startActivity(intent);
                    finish();*/
                    bindPla();
                } else {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    App.showToastLong(errInfo);
                }
            }
        }else if (Cmd_UserBindPlatform.equals(event)){
            if (dialog != null) {
                dialog.dismiss();
            }
            if (ret){
            //    App.showToastShrot("bind success");
                Intent intent = new Intent(this, MainScreenActivity.class);
                startActivity(intent);
                finish();
            }else {
                App.showToastShrot(errInfo);
            }

        }
    }

    @Override
    public String[] monitorEvents() {
        return new String[]{Cmd_UserLogin,Cmd_UserBindPlatform};
    }


}
