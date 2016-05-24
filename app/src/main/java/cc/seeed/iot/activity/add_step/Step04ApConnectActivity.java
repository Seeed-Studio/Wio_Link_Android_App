package cc.seeed.iot.activity.add_step;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.udp.ConfigUdpSocket;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.MLog;
import cc.seeed.iot.util.NetworkUtils;
import cc.seeed.iot.view.FontTextView;
import cc.seeed.iot.view.StepView;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.NodeListResponse;
import cc.seeed.iot.webapi.model.SuccessResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class Step04ApConnectActivity extends BaseActivity {
    private static final String TAG = "Step04ApConnectActivity";
    private static final String AP_IP = "192.168.4.1";
    private final static String PION_WIFI_PREFIX = "PionOne_";
    private final static String WIO_WIFI_PREFIX = "Wio";

    public final static String Intent_WifiPwd = "Intent_WifiPwd";
    public final static String Intent_Ssid = "Intent_Ssid";
    public final static String Intent_Board = "Intent_board";
    public final static String Intent_NodeKey = "Intent_NodeKey";
    public final static String Intent_NodeSn = "Intent_NodeSn";
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.mIvLoading)
    ImageView mIvLoading;
    @InjectView(R.id.mTvHint)
    FontTextView mTvHint;
    @InjectView(R.id.mStepView)
    StepView mStepView;

    private String ssid;
    private String node_name;
    private String board;
    private String node_sn;
    private String node_key;
    private String wifiPwd;
    private ConfigUdpSocket udpClient;
    private Animation animation;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ap_connect);
        ButterKnife.inject(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        initToolBar();

        initData();
    }

    private void initToolBar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_ap_connect_activity);
    }

    private void initData() {
        Intent intent = getIntent();
        ssid = intent.getStringExtra(Intent_Ssid);
        node_sn = intent.getStringExtra(Intent_NodeSn);
        node_key = intent.getStringExtra(Intent_NodeKey);
        board = intent.getStringExtra(Intent_Board);
        wifiPwd = intent.getStringExtra(Intent_WifiPwd);

        udpClient = new ConfigUdpSocket();
        startLoading();
        sendOrder();
    }


    @Override
    protected void onDestroy() {
        //  dismissProgressDialog();
        stopLoading();
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

    private void sendOrder() {
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

                                        String cmd_connect = "APCFG: " + ssid + "\t" + wifiPwd + "\t" +
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
                                        ota_server_url = NetworkUtils.getDomainName(ota_server_url);
                                        //  String ota_server_ip = ((App) getApplication()).getOtaServerIP();
                                        String cmd_connect = "APCFG: " + ssid + "\t" + wifiPwd + "\t" +
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

    private class SetNodeSn extends AsyncTask<String, Void, Boolean> implements DialogInterface.OnKeyListener {
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            //  showProgressDialog("Sending wifi password to Wio...");
            mTvHint.setText("Sending wifi password to Wio...");
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
            Random random = new Random();
            String defaultName = "";
            switch (board) {
                default:
                case Constant.WIO_LINK_V1_0:
                    defaultName = "Wio Link " + random.nextInt(50);
                    break;
                case Constant.WIO_NODE_V1_0:
                    defaultName = "Wio Node " + random.nextInt(50);
                    break;
            }
            dialog = DialogUtils.showEditNodeNameDialog(Step04ApConnectActivity.this, defaultName, new DialogUtils.ButtonEditClickListenter() {
                @Override
                public void okClick(Dialog dialog,String content) {
                    dialog.dismiss();
                    node_name = content;
                    new checkNodeIsOnline().execute();
                }
            });
            dialog.setOnKeyListener(this);
            //   new checkNodeIsOnline().execute();
        }

        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK ) {
                return true;
            } else {
                return false;
            }
        }
    }

    private class checkNodeIsOnline extends AsyncTask<Void, Integer, Boolean>  {
        private Boolean state_online = false;

        @Override
        protected void onPreExecute() {
            //  showProgressDialog("Waiting Wio get ip address...");
            mTvHint.setText("Waiting Wio get ip address...");
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
            //  showProgressDialog("Waiting Wio get ip address...[" + i + "]");
            mTvHint.setText("Waiting Wio get ip address...[" + i + "]");
        }

        @Override
        protected void onPostExecute(Boolean state_online) {
            if (Step04ApConnectActivity.this.isFinishing()) { // or call isFinishing() if min sdk version < 17
                return;
            }

            if (state_online) {
                attemptRename(node_name);
            } else {
                DialogUtils.showErrorDialog(Step04ApConnectActivity.this, "Connection Error", "TRY AGAIN", "CANCEL", "Please check your internet connection and try again.\r\n" +
                        "If still can’t slove the problem, please try FAQ section and contact us there. ", new DialogUtils.OnErrorButtonClickListenter() {
                    @Override
                    public void okClick() {
                        new checkNodeIsOnline().execute();
                    }

                    @Override
                    public void cancelClick() {
                        stopLoading();
                        finish();
                    }
                });
            }
        }

        private void attemptRename(final String node_name) {
          /*  final ProgressDialog mProgressBar = new ProgressDialog(Step04ApConnectActivity.this);
            mProgressBar.setMessage("Setting Wio name...");
            mProgressBar.show();*/
            mTvHint.setText("Setting Wio name...");

            IotApi api = new IotApi();
            User user = UserLogic.getInstance().getUser();
            api.setAccessToken(user.token);
            IotService iot = api.getService();
            iot.nodesRename(node_name, node_sn, new Callback<SuccessResponse>() {
                @Override
                public void success(SuccessResponse successResponse, Response response) {
                    Intent intent = new Intent(Step04ApConnectActivity.this, MainScreenActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    stopLoading();
                }

                @Override
                public void failure(RetrofitError error) {
                    stopLoading();
                    Toast.makeText(Step04ApConnectActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    private void startLoading() {
        animation = AnimationUtils.loadAnimation(this, R.anim.loading);
        LinearInterpolator lir = new LinearInterpolator();
        animation.setInterpolator(lir);
        mIvLoading.setAnimation(animation);
        animation.start();
    }

    private void stopLoading() {
        if (animation != null) {
            animation.cancel();
        }
    }
}
