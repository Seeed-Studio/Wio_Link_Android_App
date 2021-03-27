package cc.seeed.iot.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.user.LoginAndRegistActivity;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.activity.user.ResetPwd01Activity;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.RegularUtils;
import cc.seeed.iot.util.ToolUtil;
import cc.seeed.iot.view.FontEditView;
import cc.seeed.iot.view.FontTextView;


/**
 * Created by Administrator on 2015/7/21.
 */
public class LoginFragment extends BaseFragment {

    @InjectView(R.id.mEtEmail)
    FontEditView mEtEmail;
    @InjectView(R.id.mEtPwd)
    FontEditView mEtPwd;
    @InjectView(R.id.mBtnLogin)
    Button mBtnLogin;
    @InjectView(R.id.mTvForgotPwd)
    FontTextView mTvForgotPwd;

    Dialog dialog;

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container) {

        //获取对应的布局
        View view = View.inflate(mActivity, R.layout.fragment_login, null);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void initData() {
        String email = App.getSp().getString(Constant.SP_USER_EMAIL, "");
        mEtEmail.setText(email);
        mEtEmail.setSelection(email.length());
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private void login() {
        String email = mEtEmail.getText().toString().trim();
        String pwd = mEtPwd.getText().toString().trim();
        if (!RegularUtils.isEmail(email)) {
            mEtEmail.setError(getString(R.string.msg_email_format_error));
            mEtEmail.requestFocus();
            return;
        } else if (TextUtils.isEmpty(pwd) || pwd.length() < 6) {
            mEtPwd.setError(getString(R.string.msg_password_too_short_error));
            mEtPwd.requestFocus();
            return;
        } else {
            dialog = DialogUtils.showProgressDialog(mActivity, "");
            App.getSp().edit().putString(Constant.SP_USER_EMAIL, email).commit();
            UserLogic.getInstance().login(email, pwd);
        }
    }

    @OnClick({R.id.mBtnLogin, R.id.mTvForgotPwd})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mBtnLogin:
                MobclickAgent.onEvent(mActivity, "10004");
                hideKeyboard(view);
                login();
                break;
            case R.id.mTvForgotPwd:
                MobclickAgent.onEvent(mActivity, "10008");
                Intent intent = new Intent(mActivity, ResetPwd01Activity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onEvent(String event, boolean ret, String errInfo, Object[] data) {
        if (Cmd_UserLogin.equals(event)) {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (ToolUtil.isTopActivity(mActivity, LoginAndRegistActivity.class.getSimpleName())) {
                if (ret) {
                    Intent intent = new Intent(mActivity, MainScreenActivity.class);
                    startActivity(intent);
                    mActivity.finish();
                } else {
                    App.showToastLong(errInfo);
                }
            }
        }
    }

    @Override
    public String[] monitorEvents() {
        return new String[]{Cmd_UserLogin};
    }
}
