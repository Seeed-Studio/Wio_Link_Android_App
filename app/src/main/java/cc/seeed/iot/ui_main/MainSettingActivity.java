package cc.seeed.iot.ui_main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.ui_login.SetupActivity;
import cc.seeed.iot.ui_setnode.model.PinConfigDBHelper;
import cc.seeed.iot.util.DBHelper;
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
    }

    public void initToolBar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Setting");
    }


    @OnClick({R.id.mLLPassword, R.id.mLLConnectServer, R.id.mLLLogout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mLLPassword:
                break;
            case R.id.mLLConnectServer:
                DialogUtils.showSelectServer(this, new DialogUtils.ButtonClickListenter() {
                    @Override
                    public void okClick(String url) {
                        App.showToastShrot(url);
                    }

                    @Override
                    public void cancelClick() {

                    }
                });
                break;
            case R.id.mLLLogout:
                UserLogic.getInstance().logOut();
                ((App) getApplication()).setFirstUseState(true);
                DBHelper.delNodesAll();
                DBHelper.delGrovesAll();
                PinConfigDBHelper.delPinConfigAll();
                Intent intent = new Intent(this, SetupActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
    }
}
