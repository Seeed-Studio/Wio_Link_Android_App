package cc.seeed.iot.activity.add_step;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.NodeResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class Step01GoReadyActivity extends BaseActivity {
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.mIvCourse)
    ImageView mIvCourse;
    @InjectView(R.id.mBtnGo)
    Button mBtnGo;

    private String board;
    private String node_sn;
    private String node_key;

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
            getSupportActionBar().setTitle("Get Your Wio Ready");
        }
    }

    private void initData() {
        this.board = getIntent().getStringExtra("board");

        switch (board) {
            default:
            case Constant.WIO_LINK_V1_0:
                mIvCourse.setImageResource(R.drawable.link_config);
                break;
            case Constant.WIO_NODE_V1_0:
                mIvCourse.setImageResource(R.drawable.node_config);
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

    private void attemptLogin(final String node_name, final String board) {
        final ProgressDialog mProgressBar = new ProgressDialog(this);

        mProgressBar.setMessage("connect server...");
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
                        Intent intent = new Intent(Step01GoReadyActivity.this, Step02WifiWioListActivity.class);
                        intent.putExtra("board", board);
                        intent.putExtra("node_key", node_key);
                        intent.putExtra("node_sn", node_sn);
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

    @OnClick(R.id.mBtnGo)
    public void onClick() {
        attemptLogin("node000", board);
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
    }
}

