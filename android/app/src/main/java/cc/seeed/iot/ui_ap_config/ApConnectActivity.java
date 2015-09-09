package cc.seeed.iot.ui_ap_config;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.udp.ConfigUdpSocket;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.ui_smartconfig.GoReadyActivity;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.NodeListResponse;
import cc.seeed.iot.webapi.model.NodeResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ApConnectActivity extends AppCompatActivity implements OnClickListener {
    private static final String TAG = "ApConnectActivity";
    private static final String AP_IP = "192.168.4.1";
    private TextView mSsidView;
    private EditText mPasswordView;
    private EditText mNodeNameView;
    private Button mConnectBtnView;

    private String ssid;
    private String node_name;
    private String node_sn;
    private String node_key;
    private ConfigUdpSocket udpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ap_connect);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_ap_connect_activity);

        mSsidView = (TextView) findViewById(R.id.ssid);
        mPasswordView = (EditText) findViewById(R.id.wifi_password);
        mNodeNameView = (EditText) findViewById(R.id.node_name);
        mConnectBtnView = (Button) findViewById(R.id.first_time_how_to_api_key);
        mConnectBtnView.setOnClickListener(this);

        udpClient = new ConfigUdpSocket();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        ssid = intent.getStringExtra("ssid");
        node_sn = intent.getStringExtra("node_sn");
        node_key = intent.getStringExtra("node_key");
        mSsidView.setText(ssid);
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

    @Override
    public void onClick(View v) {
        if (v == mConnectBtnView) {
            String password = mPasswordView.getText().toString();
            node_name = mNodeNameView.getText().toString();
            if (node_name.isEmpty()) {
                mNodeNameView.setError("Node name is empty");
                return;
            }
            //APCFG: ssid\tpassword\tkey\tsn\t
            String cmd_connect = "APCFG: " + ssid + "\t" + password + "\t" +
                    node_key + "\t" + node_sn + "\t";

            Log.i(TAG, "cmd_connect: " + cmd_connect);
            Log.i(TAG, "ip: " + AP_IP);
            new SetNodeSn().execute(cmd_connect, AP_IP);
        }
    }

    private class SetNodeSn extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(ApConnectActivity.this);
            mProgressDialog.setMessage("Sending wifi password to Pion One...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String cmd = params[0];
            String ipAddr = params[1];
            udpClient.setSoTimeout(1000); //1s timeout
            udpClient.sendData(cmd, ipAddr);
            for (int i = 0; i < 3; i++) {
                try {
                    byte[] bytes = udpClient.receiveData();
                    if (new String(bytes).substring(0, 1 + 1).equals("ok")) {
                        Log.i(TAG, "set info to node success with udp.");
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    udpClient.setSoTimeout(3000);
                    udpClient.sendData(cmd, ipAddr);
                    continue;
                } catch (IOException e) {
                    Log.e(TAG, "Error[SetNodeSn]:" + e);
                    return false;
                }
            }
            return true;
        }


        @Override
        protected void onPostExecute(Boolean b) {
            mProgressDialog.dismiss();

            //remove pion one wifi config
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration c : wifiConfigurations) {
                if (c.SSID.contains("PionOne_")) {
                    wifiManager.removeNetwork(c.networkId);
                    wifiManager.saveConfiguration();
                }
            }

            new checkNodeIsOnline().execute();
        }
    }

    private class checkNodeIsOnline extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog mProgressDialog;
        private Boolean state_online = false;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(ApConnectActivity.this);
            mProgressDialog.setMessage("Waiting Pion One get ip address...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            for (int i = 0; i < 30; i++) {
                IotApi api = new IotApi();
                User user = ((MyApplication) getApplication()).getUser();
                api.setAccessToken(user.user_key);
                IotService iot = api.getService();
                iot.nodesList(new Callback<NodeListResponse>() {
                    @Override
                    public void success(NodeListResponse nodeListResponse, Response response) {
                        for (Node n : nodeListResponse.nodes) {
                            if (n.node_sn.equals(node_sn) && n.online) {
                                state_online = true;
                                break;
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                    }
                });

                if (state_online)
                    break;

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return state_online;
        }

        @Override
        protected void onPostExecute(Boolean state_online) {
            mProgressDialog.dismiss();

            if (state_online) {
                attemptRename(node_name);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(ApConnectActivity.this);
                builder.setTitle("Error");
                builder.setMessage("PION One can not connect to the router.\n" +
                        "Maybe AP password is wrong or AP connect timeout\n" +
                        "Please reset Pion One to config mode and try again.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(ApConnectActivity.this, GoReadyActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }

        private void attemptRename(final String node_name) {
            final ProgressDialog mProgressBar = new ProgressDialog(ApConnectActivity.this);

            mProgressBar.setMessage("Setting Pion One name...");
            mProgressBar.show();
            IotApi api = new IotApi();
            User user = ((MyApplication) getApplication()).getUser();
            api.setAccessToken(user.user_key);
            IotService iot = api.getService();
            iot.nodesRename(node_name, node_sn, new Callback<NodeResponse>() {
                @Override
                public void success(NodeResponse nodeResponse, Response response) {
                    String status = nodeResponse.status;
                    if (status.equals("200")) {
                        mProgressBar.dismiss();

                        Intent intent = new Intent(ApConnectActivity.this, MainScreenActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    } else {
                        mProgressBar.dismiss();
                        Toast.makeText(ApConnectActivity.this, "Rename Node fail!", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    mProgressBar.dismiss();
                    Toast.makeText(ApConnectActivity.this, "Connect server error!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
