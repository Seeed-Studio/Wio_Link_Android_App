package cc.seeed.iot.activity.user;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.CallbackManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.activity.SelectServerActivity;
import cc.seeed.iot.activity.TestActivity;
import cc.seeed.iot.adapter.LoginAndRegistAdapter;
import cc.seeed.iot.fragment.LoginFragment;
import cc.seeed.iot.fragment.RegistFragment;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.util.CommonUrl;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.OtherPlatformUtils;
import cc.seeed.iot.util.ToolUtil;
import cc.seeed.iot.view.FontTextView;

public class LoginAndRegistActivity extends BaseActivity implements ViewPager.OnPageChangeListener, GoogleApiClient.OnConnectionFailedListener {

    @InjectView(R.id.mRegistrTag)
    View mRegistrTag;
    @InjectView(R.id.mRlRegist)
    RelativeLayout mRlRegist;
    @InjectView(R.id.mLoginTag)
    View mLoginTag;
    @InjectView(R.id.mRlLogin)
    RelativeLayout mRlLogin;
    @InjectView(R.id.mMainPager)
    ViewPager mMainPager;
    @InjectView(R.id.mRlGoogle)
    RelativeLayout mRlGoogle;
    @InjectView(R.id.mRlFacebook)
    RelativeLayout mRlFacebook;
    @InjectView(R.id.mTvRegist)
    FontTextView mTvRegist;
    @InjectView(R.id.mTvLogin)
    FontTextView mTvLogin;
    @InjectView(R.id.mTvSelectServer)
    FontTextView mTvSelectServer;
    @InjectView(R.id.mLLOrtherLogin)
    LinearLayout mLLOrtherLogin;
    @InjectView(R.id.mRlSelectServer)
    RelativeLayout mRlSelectServer;

    private List<Fragment> mFList;
    private LoginAndRegistAdapter mAdapter;
    private Dialog dialog;
    private int mSelectTab = 1;//默认选择登录界面
    private CallbackManager callbackManager;
    private static final int RC_SIGN_IN = 9001;
    int[] menuIds = {R.id.mRlRegist, R.id.mRlLogin};

    GoogleApiClient mGoogleApiClient;
    int goToTest = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_and_regist);
        ButterKnife.inject(this);
        initData();
    }

    private void initData() {

        callbackManager = CallbackManager.Factory.create();
        mMainPager.setOffscreenPageLimit(2);
        mFList = new ArrayList<Fragment>();
        mFList.add(new RegistFragment());
        mFList.add(new LoginFragment());

        mAdapter = new LoginAndRegistAdapter(getSupportFragmentManager(), mFList);
        mMainPager.setAdapter(mAdapter);
        mMainPager.setOnPageChangeListener(this);
        mMainPager.setCurrentItem(mSelectTab, false);
        seleted(menuIds[mSelectTab]);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .requestProfile()
                .build();

      /*  String serverClientId = getString(R.string.server_client_id);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .requestServerAuthCode(serverClientId)
                .requestEmail()
                .build();*/

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


     /*   mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this *//* FragmentActivity *//*,
                        this *//* OnConnectionFailedListener *//*)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
        mGoogleApiClient.connect();*/
    }


    @Override
    public void onStart() {
        super.onStart();

     /*   OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        String serverUrl = App.getApp().getOtaServerUrl();
        if (CommonUrl.OTA_CHINA_URL.equals(serverUrl)) {
            mTvSelectServer.setText(getString(R.string.server_chinese));
        } else if (CommonUrl.OTA_INTERNATIONAL_URL.equals(serverUrl)) {
            mTvSelectServer.setText(getString(R.string.server_global));
        } else {
            mTvSelectServer.setText(getString(R.string.server_customize));
        }
    }

    @OnClick({R.id.mTvSelectServer, R.id.mRlLogin, R.id.mRlRegist, R.id.mRlGoogle, R.id.mRlFacebook, R.id.mRlSelectServer})
    public void onClick(View view) {
        seleted(view.getId());
        mMainPager.setCurrentItem(mSelectTab, false);
        switch (view.getId()) {
            case R.id.mTvSelectServer:
              /*  MobclickAgent.onEvent(this, "10005");
                DialogUtils.showSelectServer(LoginAndRegistActivity.this, App.getApp().getOtaServerUrl(), new DialogUtils.ButtonClickListenter() {
                    @Override
                    public void okClick(String url, String ip) {
                        App.getApp().saveUrlAndIp(url, ip);
                    }

                    @Override
                    public void cancelClick() {

                    }
                });*/
                startActivity(new Intent(LoginAndRegistActivity.this, SelectServerActivity.class));
                break;
            case R.id.mRlGoogle:
                //  App.showToastShrot("G+");
                MobclickAgent.onEvent(this, "10006");
                loginWithGoogle();
                break;
            case R.id.mRlFacebook:
                MobclickAgent.onEvent(this, "10007");
                if (!ToolUtil.isInstallByread("com.facebook.katana")) {
                    App.showToastShrot("You don't have to install Facebook");
                    return;
                }
                OtherPlatformUtils.getFacebookInfo(this, callbackManager, OtherPlatformUtils.LoginWithFacebook);
                //    dialog = DialogUtils.showProgressDialog(LoginAndRegistActivity.this, getString(R.string.loading_login));
                break;
            case R.id.mRlSelectServer:
                startActivity(new Intent(LoginAndRegistActivity.this, SelectServerActivity.class));
                break;
        }
    }

    private void loginWithGoogle() {
        try {
            /*OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
            if (opr.isDone()) {
              *//*  GoogleSignInResult result = opr.get();
                handleSignInResult(result);*//*
             //   revokeAccess();
                signOut();
            }else {*/
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
//            revokeAccess();
            //  signOut();
        } catch (Exception e) {
            e.printStackTrace();
            revokeAccess();
            signOut();
        }
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        App.showToastShrot("quxiao:" + status.toString());
                        // ...
                      /*  Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                        startActivityForResult(signInIntent, RC_SIGN_IN);*/
                        revokeAccess();
                    }
                });
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        App.showToastShrot("quxiao:" + status.toString());
                        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                        startActivityForResult(signInIntent, RC_SIGN_IN);
                        //   signOut();
                    }
                });
    }

    /**
     * 设置menu_tab的选中项.
     *
     * @param id, 传入对应的id
     */
    public void seleted(int id) {
        switch (id) {
            case R.id.mRlRegist:
                mSelectTab = 0;
                mRegistrTag.setVisibility(View.VISIBLE);
                mLoginTag.setVisibility(View.GONE);
                mTvRegist.setTextColor(Color.parseColor("#ffffffff"));
                mTvLogin.setTextColor(Color.parseColor("#b2ffffff"));
                MobclickAgent.onEvent(this, "10001");
                break;
            case R.id.mRlLogin:
                MobclickAgent.onEvent(this, "10002");
                mSelectTab = 1;
                mRegistrTag.setVisibility(View.GONE);
                mLoginTag.setVisibility(View.VISIBLE);
                mTvLogin.setTextColor(Color.parseColor("#ffffffff"));
                mTvRegist.setTextColor(Color.parseColor("#b2ffffff"));

                if (ToolUtil.isApkDebug()) {
                    goToTest++;
                    if (goToTest == 6) {
                        goToTest = 0;
                        startActivity(new Intent(this, TestActivity.class));
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mSelectTab = position;
        seleted(menuIds[position]);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
         //   Process.killProcess(Process.myPid());
//            finish();
            App.getApp().onTerminate();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result != null) {
                handleSignInResult(result);
            }
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        //   Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            App.showToastShrot("success");
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            //   mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            // updateUI(true);
            //    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            String personName = acct.getDisplayName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();
        } else {
            // Signed out, show unauthenticated UI.
            //  updateUI(false);
        }
    }

    @Override
    public String[] monitorEvents() {
        return new String[]{Cmd_UserOtherLogin, Cmd_AuthorizeCancel};
    }

    @Override
    public void onEvent(String event, boolean ret, String errInfo, Object[] data) {
        if (Cmd_UserOtherLogin.equals(event)) {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (ret) {
                Intent intent = new Intent(this, MainScreenActivity.class);
                startActivity(intent);
                finish();
            } else {
                App.showToastLong(errInfo);
            }
        } else if (Cmd_AuthorizeCancel.equals(event)) {
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        App.showToastShrot(connectionResult.toString());
    }


}
