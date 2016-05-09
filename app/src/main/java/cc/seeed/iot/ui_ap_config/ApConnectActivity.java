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

import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.util.MLog;
import cc.seeed.iot.udp.ConfigUdpSocket;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.NodeListResponse;
import cc.seeed.iot.webapi.model.SuccessResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ApConnectActivity extends AppCompatActivity implements OnClickListener {
    private static final String TAG = "ApConnectActivity";
    private static final String AP_IP = "192.168.4.1";
    private final static String PION_WIFI_PREFIX = "PionOne_";
    private final static String WIO_WIFI_PREFIX = "Wio";
    private TextView mSsidView;
    private EditText mPasswordView;
    private EditText mNodeNameView;
    private Button mConnectBtnView;

    private String ssid;
    private String node_name;
    private String board;
    private String node_sn;
    private String node_key;
    private ConfigUdpSocket udpClient;
    private ProgressDialog mProgressDialog;

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

        mProgressDialog = new ProgressDialog(ApConnectActivity.this);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        ssid = intent.getStringExtra("ssid");
        node_sn = intent.getStringExtra("node_sn");
        node_key = intent.getStringExtra("node_key");
        board = intent.getStringExtra("board");
        mSsidView.setText(ssid);
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
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
            final String password = mPasswordView.getText().toString();
            node_name = mNodeNameView.getText().toString();
            if (node_name.isEmpty()) {
                mNodeNameView.setError("Node name is empty");
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    udpClient = new ConfigUdpSocket();
                    udpClient.setSoTimeout(10000); //1s timeout
                    udpClient.sendData("VERSION", "192.168.4.1");
                    for (int i = 0; i < 3; i++) {
                        try {
                            byte[] bytes = udpClient.receiveData();
                            String resurt = new String(bytes);
                            if (resurt != null && resurt.length() >= 3) {
                                if (resurt.substring(0, 3).equals("1.1")) {
                                    MLog.d(this, "get version success: " + new String(bytes));
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String ota_server_ip = ((App) getApplication()).getOtaServerIP();
                                            String exchange_server_ip = ota_server_ip;

                                            String cmd_connect = "APCFG: " + ssid + "\t" + password + "\t" +
                                                    node_key + "\t" + node_sn + "\t" + exchange_server_ip + "\t"
                                                    + ota_server_ip + "\t";
                                            Log.i(TAG, "cmd_connect: " + cmd_connect);
                                            Log.i(TAG, "AP ip: " + AP_IP);
                                            new SetNodeSn().execute(cmd_connect, AP_IP);
                                        }
                                    });
                                    break;
                                } else if (resurt.substring(0, 3).equals("1.2")) {
                                    MLog.d(this, "get version success: " + new String(bytes));
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String ota_server_url = ((App) getApplication()).getOtaServerUrl();
                                            String cmd_connect = "APCFG: " + ssid + "\t" + password + "\t" +
                                                    node_key + "\t" + node_sn + "\t" + ota_server_url + "\t"
                                                    + ota_server_url + "\t";
                                            Log.i(TAG, "cmd_connect: " + cmd_connect);
                                            Log.i(TAG, "AP ip: " + AP_IP);
                                            new SetNodeSn().execute(cmd_connect, AP_IP);
                                        }
                                    });
                                }
                            }
                        } catch (SocketTimeoutException e) {
                            MLog.d(this, "get version fail");
                        } catch (IOException e) {
                            MLog.d(this, "get version fail");
                        }
                    }
                }
            }).start();
        }
    }

    private class SetNodeSn extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            showProgressDialog("Sending wifi password to Wio...");
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

                } catch (IOException e) {
                    Log.e(TAG, "Error[SetNodeSn]:" + e);
                    return false;
                }
            }
            return true;
        }


        @Override
        protected void onPostExecute(Boolean b) {
            //remove Wio wifi config
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration c : wifiConfigurations) {
                if (c.SSID.contains(PION_WIFI_PREFIX) || c.SSID.contains(WIO_WIFI_PREFIX)) {
                    wifiManager.removeNetwork(c.networkId);
                    wifiManager.saveConfiguration();
                }
            }
            new checkNodeIsOnline().execute();
        }
    }

    private class checkNodeIsOnline extends AsyncTask<Void, Integer, Boolean> {
        private Boolean state_online = false;

        @Override
        protected void onPreExecute() {
            showProgressDialog("Waiting Wio get ip address...");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            for (int i = 0; i < 30; i++) {
                IotApi api = new IotApi();
                User user = UserLogic.getInstance().getUser();
                api.setAccessToken(user.token);
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
                publishProgress(30 - i);
            }
            return state_online;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int i = values[0];
            showProgressDialog("Waiting Wio get ip address...[" + i + "]");
        }

        @Override
        protected void onPostExecute(Boolean state_online) {
            if (ApConnectActivity.this.isFinishing()) { // or call isFinishing() if min sdk version < 17
                return;
            }
            dismissProgressDialog();

            if (state_online) {
                attemptRename(node_name);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(ApConnectActivity.this);
                builder.setTitle("Error");
                builder.setMessage("Wio can not connect to your AP.\n" +
                        "Please check AP password or closer with AP.\n" +
                        "Please reset Wio to configure mode and try again.");
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(ApConnectActivity.this, GoReadyActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("board", board);
                        startActivity(intent);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }

        private void attemptRename(final String node_name) {
            final ProgressDialog mProgressBar = new ProgressDialog(ApConnectActivity.this);

            mProgressBar.setMessage("Setting Wio name...");
            mProgressBar.show();
            IotApi api = new IotApi();
            User user =  UserLogic.getInstance().getUser();
            api.setAccessToken(user.token);
            IotService iot = api.getService();
            iot.nodesRename(node_name, node_sn, new Callback<SuccessResponse>() {
                @Override
                public void success(SuccessResponse successResponse, Response response) {
                    mProgressBar.dismiss();
                    Intent intent = new Intent(ApConnectActivity.this, MainScreenActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                @Override
                public void failure(RetrofitError error) {
                    mProgressBar.dismiss();
                    Toast.makeText(ApConnectActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showProgressDialog(String message) {
        mProgressDialog.setMessage(message);
//            mProgressDialog.setIndeterminate(false);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
