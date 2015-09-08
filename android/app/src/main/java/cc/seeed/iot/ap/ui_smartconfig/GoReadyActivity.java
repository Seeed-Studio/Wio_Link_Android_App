package cc.seeed.iot.ap.ui_smartconfig;

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
import android.widget.Toast;

import cc.seeed.iot.ap.MyApplication;
import cc.seeed.iot.ap.datastruct.User;
import cc.seeed.iot.ap.webapi.IotApi;
import cc.seeed.iot.ap.webapi.IotService;
import cc.seeed.iot.ap.R;
import cc.seeed.iot.ap.ui_ap_config.WifiPionListActivity;
import cc.seeed.iot.ap.webapi.model.NodeResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GoReadyActivity extends AppCompatActivity {
    public Toolbar mToolbar;
    public Button mGoReadyButtonView;

    private String node_sn;
    private String node_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smartconfig_ready);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("WIFI Iot Node");


        mGoReadyButtonView = (Button) findViewById(R.id.smartconfig_ready_btn);
        mGoReadyButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                attemptLogin("node000");
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

    private void attemptLogin(final String node_name) {
        final ProgressDialog mProgressBar = new ProgressDialog(this);

        mProgressBar.setMessage("connect server...");
        mProgressBar.show();
        IotApi api = new IotApi();
        User user = ((MyApplication) getApplication()).getUser();
        api.setAccessToken(user.user_key);
        IotService iot = api.getService();
        iot.nodesCreate(node_name, new Callback<NodeResponse>() {
            @Override
            public void success(NodeResponse nodeResponse, Response response) {
                String status = nodeResponse.status;
                if (status.equals("200")) {
                    mProgressBar.dismiss();

                    node_key = nodeResponse.node_key;
                    node_sn = nodeResponse.node_sn;
                    Intent intent = new Intent(GoReadyActivity.this, WifiPionListActivity.class);
                    intent.putExtra("node_key", node_key);
                    intent.putExtra("node_sn", node_sn);
                    startActivity(intent);
                } else {
                    mProgressBar.dismiss();
                    Toast.makeText(GoReadyActivity.this, "Create Node fail!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                mProgressBar.dismiss();
                Toast.makeText(GoReadyActivity.this, "Connect server error!", Toast.LENGTH_LONG).show();
            }
        });
    }
}

