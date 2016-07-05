package cc.seeed.iot.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.logic.ConfigDeviceLogic;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.NetworkUtils;
import cc.seeed.iot.util.RegularUtils;
import cc.seeed.iot.util.ToolUtil;
import cc.seeed.iot.view.FontTextView;
import cc.seeed.iot.webapi.model.Node;

public class NodeSettingActivity extends BaseActivity {
    public static final String Intent_NodeSn = "intent_node_sn";

    public Node node;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.mTvName)
    FontTextView mTvName;
    @InjectView(R.id.mLLName)
    LinearLayout mLLName;
    @InjectView(R.id.mTvConnectServer)
    FontTextView mTvConnectServer;
    @InjectView(R.id.mLLConnectServer)
    LinearLayout mLLConnectServer;
    @InjectView(R.id.mLlDeleteDevice)
    LinearLayout mLlDeleteDevice;

    Dialog progressDialog;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_setting);
        ButterKnife.inject(this);

        initView();
        initData();
    }

    private void initView() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.toolBar_title));
    }

    private void initData() {
        Intent intent = getIntent();
        String nodeSn = intent.getStringExtra(Intent_NodeSn);
        node = DBHelper.getNodes(nodeSn).get(0);
        if (node == null) {
            finish();
            return;
        }

        mTvName.setText(node.name);
        if (TextUtils.isEmpty(node.dataxserver)) {
            mTvConnectServer.setText("Not Set");
        } else {
            mTvConnectServer.setText(node.dataxserver);
        }

    }

    @OnClick({R.id.mLLName, R.id.mLLConnectServer, R.id.mLlDeleteDevice})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mLLName:
                MobclickAgent.onEvent(this, "16001");
                reName();
                break;
            case R.id.mLLConnectServer:
                MobclickAgent.onEvent(this, "16002");
                saveUrl();
                break;
            case R.id.mLlDeleteDevice:
                MobclickAgent.onEvent(this, "16003");
                ConfigDeviceLogic.getInstance().removeNode(NodeSettingActivity.this, node, 0);
                break;
        }
    }

    private void reName() {
        DialogUtils.showEditOneRowDialog(NodeSettingActivity.this, "Edit Device Name", "", new DialogUtils.ButtonEditClickListenter() {
            @Override
            public void okClick(Dialog dialog, String content) {
                if (TextUtils.isEmpty(content)) {
                    App.showToastShrot("The device name can not be empty");
                    return;
                } else {
                    dialog.dismiss();
                    progressDialog = DialogUtils.showProgressDialog(NodeSettingActivity.this, getString(R.string.loading));
                    ConfigDeviceLogic.getInstance().nodeReName(node.node_sn, content);
                }
            }
        });
    }

    private void saveUrl() {
        DialogUtils.showEditOneRowDialog(NodeSettingActivity.this, "Customized Server","", new DialogUtils.ButtonEditClickListenter() {
            @Override
            public void okClick(Dialog dialog, String content) {
                if (TextUtils.isEmpty(content)) {
                    content = App.getApp().getOtaServerUrl();
                }
                if (!RegularUtils.isWebsite(content)) {
                    App.showToastLong(getString(R.string.website_format_hint));
                    return;
                } else {
                    getIpAddress(NodeSettingActivity.this, dialog, content);
                }
            }
        });
    }

    public void getIpAddress(final Activity context, final Dialog dialog, final String url) {
        progressDialog = DialogUtils.showProgressDialog(context, getString(R.string.loading));
        NetworkUtils.getIpAddress(context, NetworkUtils.getDomainName(url), new NetworkUtils.OnIpCallback() {
            @Override
            public void okCallback(String ip) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                ConfigDeviceLogic.getInstance().nodeXserverIp(node, ip, url);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void failCallback(String error) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                App.showToastShrot(error);
            }
        });
    }


    @Override
    public String[] monitorEvents() {
        return new String[]{Cmd_Node_ReName, Cmd_Node_SaveIp, Cmd_Node_Remove};
    }

    @Override
    public void onEvent(String event, boolean ret, String errInfo, Object[] data) {
        if (Cmd_Node_ReName.equals(event)) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (ret) {
                mTvName.setText(errInfo);
            } else {
                App.showToastShrot(errInfo);
            }
        } else if (Cmd_Node_SaveIp.equals(event)) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (ret) {
                mTvConnectServer.setText(errInfo);
            } else {
                App.showToastShrot(errInfo);
            }
        } else if (Cmd_Node_Remove.equals(event)) {
            if (ret) {
                if (ToolUtil.isTopActivity(NodeSettingActivity.this, NodeSettingActivity.this.getClass().getSimpleName())) {
                    startActivity(new Intent(NodeSettingActivity.this, MainScreenActivity.class));
                    finish();
                }
            } else {
                App.showToastShrot(errInfo);
            }
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
