package cc.seeed.iot.activity.user;

import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.adapter.LoginAndRegistAdapter;
import cc.seeed.iot.fragment.LoginFragment;
import cc.seeed.iot.fragment.RegistFragment;
import cc.seeed.iot.util.DialogUtils;
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

    private List<Fragment> mFList;
    private LoginAndRegistAdapter mAdapter;
    private int mSelectTab = 1;//默认选择登录界面
    private static final int RC_SIGN_IN = 9001;
    int[] menuIds = {R.id.mRlRegist, R.id.mRlLogin};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_and_regist);
        ButterKnife.inject(this);
        initData();
    }

    private void initData() {
        mMainPager.setOffscreenPageLimit(2);
        mFList = new ArrayList<Fragment>();
        mFList.add(new RegistFragment());
        mFList.add(new LoginFragment());

        mAdapter = new LoginAndRegistAdapter(getSupportFragmentManager(), mFList);
        mMainPager.setAdapter(mAdapter);
        mMainPager.setOnPageChangeListener(this);
        mMainPager.setCurrentItem(mSelectTab, false);
        seleted(menuIds[mSelectTab]);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick({R.id.mTvSelectServer,R.id.mRlLogin,R.id.mRlRegist,R.id.mRlGoogle, R.id.mRlFacebook})
    public void onClick(View view) {
        seleted(view.getId());
        mMainPager.setCurrentItem(mSelectTab, false);
        switch (view.getId()) {
            case R.id.mTvSelectServer:
                DialogUtils.showSelectServer(LoginAndRegistActivity.this,null);
                break;
            case R.id.mRlGoogle:
                App.showToastShrot("G+");
                break;
            case R.id.mRlFacebook:
                App.showToastShrot("F");
                break;
        }
    }

    private void loginWithGoogle(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleApiClient  mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this )
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
                break;
            case R.id.mRlLogin:
                mSelectTab = 1;
                mRegistrTag.setVisibility(View.GONE);
                mLoginTag.setVisibility(View.VISIBLE);
                mTvLogin.setTextColor(Color.parseColor("#ffffffff"));
                mTvRegist.setTextColor(Color.parseColor("#b2ffffff"));
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
            android.os.Process.killProcess(Process.myPid());
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
