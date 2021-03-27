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
import cc.seeed.iot.activity.PrivacyPolicyActivity;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.RegularUtils;
import cc.seeed.iot.view.FontEditView;
import cc.seeed.iot.view.FontTextView;


/**
 * Created by Administrator on 2015/7/21.
 */
public class RegistFragment extends BaseFragment {

    @InjectView(R.id.mEtEmail)
    FontEditView mEtEmail;
    @InjectView(R.id.mEtPwd)
    FontEditView mEtPwd;
    @InjectView(R.id.mEtRePwd)
    FontEditView mEtRePwd;
    @InjectView(R.id.mBtnRegist)
    Button mBtnRegist;
    @InjectView(R.id.mTvPrivacy)
    FontTextView mTvPrivacy;

    Dialog dialog;

    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container) {
        //获取对应的布局
        View view = View.inflate(mActivity, R.layout.fragment_regist, null);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void initData() {
        String email = App.getSp().getString(Constant.SP_USER_EMAIL, "");
        mEtEmail.setText(email);
        mEtEmail.setSelection(email.length());
    }

    public void regiest() {
        String email = mEtEmail.getText().toString().trim();
        String pwd = mEtPwd.getText().toString().trim();
        String rePwd = mEtRePwd.getText().toString().trim();
        if (!RegularUtils.isEmail(email)) {
            mEtEmail.setError(getString(R.string.msg_email_format_error));
            mEtEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(pwd) || pwd.length() < 6) {
            mEtPwd.setError(getString(R.string.msg_password_too_short_error));
            mEtPwd.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(rePwd) || rePwd.length() < 6) {
            mEtRePwd.setError(getString(R.string.msg_password_too_short_error));
            mEtRePwd.requestFocus();
            return;
        } else {
            if (!rePwd.equals(pwd)) {
                mEtRePwd.setError(getString(R.string.msg_password_dont_match_error));
                mEtRePwd.requestFocus();
                return;
            }
        }

        dialog = DialogUtils.showProgressDialog(mActivity, getString(R.string.loading_regist));
        App.getSp().edit().putString(Constant.SP_USER_EMAIL, email).commit();
        UserLogic.getInstance().regist(email, pwd);
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

    @OnClick({R.id.mBtnRegist, R.id.mTvPrivacy})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mBtnRegist:
                MobclickAgent.onEvent(mActivity, "10003");
                regiest();
                break;
            case R.id.mTvPrivacy:
                MobclickAgent.onEvent(mActivity, "10009");
                Intent intent2 = new Intent(mActivity, PrivacyPolicyActivity.class);
                startActivity(intent2);
                break;
        }

    }

    @Override
    public void onEvent(String event, boolean ret, String errInfo, Object[] data) {
        if (Cmd_UserRegiest.equals(event)) {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (ret) {
                Intent intent = new Intent(mActivity, MainScreenActivity.class);
                startActivity(intent);
                mActivity.finish();
            } else {
                App.showToastLong("Create account failed: " + errInfo);
            }
        }
    }

    @Override
    public void onEvent(String event, int ret, String errInfo, Object[] data) {

    }

    @Override
    public String[] monitorEvents() {
        return new String[]{Cmd_UserRegiest};
    }
}
