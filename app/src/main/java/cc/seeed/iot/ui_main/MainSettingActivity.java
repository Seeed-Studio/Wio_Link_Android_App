package cc.seeed.iot.ui_main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.activity.user.LoginAndRegistActivity;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.activity.user.ChangePwdActivity;
import cc.seeed.iot.util.CommonUrl;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.view.FontTextView;
import cc.seeed.iot.webapi.model.Node;

public class MainSettingActivity extends BaseActivity {
    public Node node;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.mLLPassword)
    LinearLayout mLLPassword;
    @InjectView(R.id.mTvConnectServer)
    FontTextView mTvConnectServer;
    @InjectView(R.id.mLLConnectServer)
    LinearLayout mLLConnectServer;
    @InjectView(R.id.mLLLogout)
    LinearLayout mLLLogout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.inject(this);
        initToolBar();
        initData();
    }

    public void initToolBar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Setting");
    }

    private void initData(){
        String url = App.getApp().getOtaServerUrl();
        if (CommonUrl.OTA_CHINA_URL.equals(url) || CommonUrl.OTA_INTERNATIONAL_URL.equals(url) ){
            mTvConnectServer.setText(url +"(default)");
        }else {
            mTvConnectServer.setText(url +"(custom)");
        }
    }


    @OnClick({R.id.mLLPassword, R.id.mLLConnectServer, R.id.mLLLogout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mLLPassword:
                MobclickAgent.onEvent(this, "14001");
                startActivity(new Intent(MainSettingActivity.this, ChangePwdActivity.class));
                break;
            case R.id.mLLConnectServer:
              /*  MobclickAgent.onEvent(this, "14002");
                DialogUtils.showSelectServer(this, App.getApp().getOtaServerUrl(),new DialogUtils.ButtonClickListenter() {

                    @Override
                    public void okClick(String url, String ip) {
                        if (CommonUrl.OTA_SERVER_URL.equals(url)){
                            mTvConnectServer.setText(url +"(default)");
                        }else {
                            mTvConnectServer.setText(url +"(custom)");
                        }
                        App.getApp().saveUrlAndIp(url, ip);
                        UserLogic.getInstance().logOut();
                        startActivity(new Intent(MainSettingActivity.this,LoginAndRegistActivity.class));
                    }

                    @Override
                    public void cancelClick() {

                    }
                });*/
                break;
            case R.id.mLLLogout:
                MobclickAgent.onEvent(this, "14003");
                UserLogic.getInstance().logOut();
                Intent intent = new Intent(this, LoginAndRegistActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
