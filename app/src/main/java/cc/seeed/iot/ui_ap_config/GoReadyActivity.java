package cc.seeed.iot.ui_ap_config;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.User;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.NodeResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GoReadyActivity extends AppCompatActivity {
    public Toolbar mToolbar;
    public Button mGoReadyButtonView;

    private String board;
    private String node_sn;
    private String node_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ap_ready);

        this.board = getIntent().getStringExtra("board");

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Wio " + board.split(" ")[1]);
        }

        ImageView imageView = (ImageView) findViewById(R.id.bg);

        switch (board) {
            default:
            case Constant.WIO_LINK_V1_0:
                imageView.setImageResource(R.drawable.link_config);
                break;
            case Constant.WIO_NODE_V1_0:
                imageView.setImageResource(R.drawable.node_config);
                break;
        }

        mGoReadyButtonView = (Button) findViewById(R.id.ap_ready_btn);
        mGoReadyButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                attemptLogin("node000", board);
                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                wifiManager.startScan();
            }
        });
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
        User user = ((MyApplication) getApplication()).getUser();
        api.setAccessToken(user.user_key);
        IotService iot = api.getService();
        iot.nodesCreate(node_name, board, new Callback<NodeResponse>() {
                    @Override
                    public void success(NodeResponse nodeResponse, Response response) {
                        mProgressBar.dismiss();
                        node_key = nodeResponse.node_key;
                        node_sn = nodeResponse.node_sn;
                        Intent intent = new Intent(GoReadyActivity.this, WifiWioListActivity.class);
                        intent.putExtra("board", board);
                        intent.putExtra("node_key", node_key);
                        intent.putExtra("node_sn", node_sn);
                        startActivity(intent);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        mProgressBar.dismiss();
                        Toast.makeText(GoReadyActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }
}

