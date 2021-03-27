package cc.seeed.iot.activity.add_step;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.activity.HelpActivity;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.NodeResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class Step01GoReadyActivity extends BaseActivity {
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.mIvCourse)
    SimpleDraweeView mIvCourse;
    @InjectView(R.id.mIvHelp)
    ImageView mIvHelp;
    @InjectView(R.id.mBtnGo)
    Button mBtnGo;

    private String board;
    private String node_sn;
    private String node_key;

    Node node;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.step01_go_ready_activity);
        ButterKnife.inject(this);

        initToolBar();
        initData();

    }

    private void initToolBar() {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.step01_go_ready_activity_title_wio_link);
        }
    }

    private void initData() {
        node_sn = getIntent().getStringExtra(Step04ApConnectActivity.Intent_NodeSn);
        if (TextUtils.isEmpty(node_sn)) {
            this.board = getIntent().getStringExtra("board");
        } else {
            node = DBHelper.getNodes(node_sn).get(0);
            board = node.board;
        }
        switch (board) {
            default:
            case Constant.WIO_LINK_V1_0:
                mIvCourse.setImageResource(R.mipmap.link_config);
                if (node != null) {
                    getSupportActionBar().setTitle(R.string.step01_go_ready_activity_title_change_wifi);
                } else {
                    getSupportActionBar().setTitle(R.string.step01_go_ready_activity_title_wio_link);
                }
                break;
            case Constant.WIO_NODE_V1_0:
                mIvCourse.setImageResource(R.mipmap.node_config);
                if (node != null) {
                    getSupportActionBar().setTitle(R.string.step01_go_ready_activity_title_change_wifi);
                } else {
                    getSupportActionBar().setTitle(R.string.step01_go_ready_activity_title_wio_node);
                }
                break;
        }

    }

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

    private void creatDevice(final String node_name, final String board) {
        final ProgressDialog mProgressBar = DialogUtils.showProgressDialog(this, "");

        mProgressBar.setMessage(getString(R.string.add_device_step01_progress_connect_server));
        mProgressBar.show();
        IotApi api = new IotApi();
        User user = UserLogic.getInstance().getUser();
        api.setAccessToken(user.token);
        IotService iot = api.getService();
        iot.nodesCreate(node_name, board, new Callback<NodeResponse>() {
                    @Override
                    public void success(NodeResponse nodeResponse, Response response) {
                        mProgressBar.dismiss();
                        node_key = nodeResponse.node_key;
                        node_sn = nodeResponse.node_sn;
                        Intent intent = new Intent(Step01GoReadyActivity.this, Step02WifiListActivity.class);
                        intent.putExtra(Step04ApConnectActivity.Intent_Board, board);
                        intent.putExtra(Step04ApConnectActivity.Intent_NodeKey, node_key);
                        intent.putExtra(Step04ApConnectActivity.Intent_NodeSn, node_sn);
                        startActivity(intent);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        mProgressBar.dismiss();
                        Toast.makeText(Step01GoReadyActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    @OnClick({R.id.mBtnGo, R.id.mIvHelp})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mBtnGo:
                MobclickAgent.onEvent(this, "17001");
                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.isWifiEnabled()) {
                    if (node != null) {
                        startWifiList();
                    } else {
                        creatDevice("node000", board);
                    }
                    wifiManager.startScan();
                } else {
                    DialogUtils.showErrorDialog(this, getString(R.string.add_device_step01_err_not_connect_wifi), getString(R.string.dialog_btn_OK), "", getString(R.string.link_to_wifi), null);
                }
                break;

            case R.id.mIvHelp:
                Intent intent = new Intent(Step01GoReadyActivity.this, HelpActivity.class);
                startActivity(intent);
                break;
        }

    }

    private void startWifiList() {
        Intent intent = new Intent(Step01GoReadyActivity.this, Step02WifiListActivity.class);
        intent.putExtra(Step04ApConnectActivity.Intent_ChangeWifi, true);
        intent.putExtra(Step04ApConnectActivity.Intent_Board, node.board);
        intent.putExtra(Step04ApConnectActivity.Intent_NodeKey, node.node_key);
        intent.putExtra(Step04ApConnectActivity.Intent_NodeSn, node.node_sn);
        startActivity(intent);
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

