package cc.seeed.iot.activity.add_step;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.activity.FeedbackActivity;
import cc.seeed.iot.activity.HelpActivity;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.logic.ConfigDeviceLogic;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.udp.ConfigUdpSocket;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.MLog;
import cc.seeed.iot.util.NetworkUtils;
import cc.seeed.iot.util.RegularUtils;
import cc.seeed.iot.util.WifiUtils;
import cc.seeed.iot.view.FontTextView;
import cc.seeed.iot.view.StepView;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.NodeListResponse;
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
    public final static String Intent_ChangeWifi = "Intent_ChangeWifi";
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.mIvLoading)
    ImageView mIvLoading;
    @InjectView(R.id.mTvHint)
    FontTextView mTvHint;
    @InjectView(R.id.mStepView)
    StepView mStepView;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;

    private String ssid;
    private String node_name;
    private String board;
    private String node_sn;
    private String node_key;
    private String wifiPwd;
    private Animation animation;
    private Dialog dialog;
    private String defaultName = "";
    private boolean isSetDefName = true;
    private boolean isChangeWifi = false;

    private int sendOrderCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ap_connect);
        ButterKnife.inject(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        initToolBar();
        mStepView.setDoingStep(3);
        mProgressBar.setVisibility(View.VISIBLE);

        initData();
    }

    private void initToolBar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Syncing Data");
    }

    private void initData() {
        Intent intent = getIntent();
        isChangeWifi = intent.getBooleanExtra(Intent_ChangeWifi, false);
        ssid = intent.getStringExtra(Intent_Ssid);
        node_sn = intent.getStringExtra(Intent_NodeSn);
        node_key = intent.getStringExtra(Intent_NodeKey);
        board = intent.getStringExtra(Intent_Board);
        wifiPwd = intent.getStringExtra(Intent_WifiPwd);

        mProgressBar.setProgress(10);
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
        mTvHint.setText("Check the firmware version...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConfigUdpSocket udpClient = new ConfigUdpSocket();
                udpClient.setSoTimeout(5000); //1s timeout
                udpClient.sendData("VERSION", "192.168.4.1");
                for (int i = 0; i < 3; i++) {
                    try {
                        byte[] bytes = udpClient.receiveData();
                        String resurt = new String(bytes);
                        if (resurt != null && RegularUtils.isNodeVersionCode(resurt)) {
                            double versionCode = 1.1;
                            try {
                                versionCode = Double.parseDouble(resurt);
                            } catch (Exception e) {
                                return;
                            }
                            if (versionCode <= 1.1) {
                                MLog.d(this, "get version success: " + new String(bytes));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String ota_server_ip = App.getApp().getOtaServerIP();
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
                            } else if (versionCode >= 1.2) {
                                MLog.d(this, "get version success: " + new String(bytes));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String ota_server_url = App.getApp().getOtaServerUrl();
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
                                break;
                            }
                        }
                    } catch (SocketTimeoutException e) {
                        showCheckVersionTip();
                    } catch (Exception e) {
                        MLog.d(this, "get version fail");
                        showCheckVersionTip();
                    }
                }
            }
        }).start();
    }

    private void showCheckVersionTip() {
        sendOrderCount++;
        if (sendOrderCount == 3) {
            sendOrderCount = 0;
           /* runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog dialog = new AlertDialog.Builder(Step04ApConnectActivity.this)
                            .setMessage("Failed to get the firmware version")
                            .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sendOrder();
                                }
                            })
                            .setNeutralButton("Feedback", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(Step04ApConnectActivity.this, FeedbackActivity.class));
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override

                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            }).create();
                    dialog.setCancelable(true);
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                }
            });*/
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String ota_server_ip = App.getApp().getOtaServerIP();
                    String exchange_server_ip = ota_server_ip;

                    String cmd_connect = "APCFG: " + ssid + "\t" + wifiPwd + "\t" +
                            node_key + "\t" + node_sn + "\t" + exchange_server_ip + "\t"
                            + ota_server_ip + "\t";
                    Log.i(TAG, "cmd_connect: " + cmd_connect);
                    Log.i(TAG, "AP ip: " + AP_IP);
                    new SetNodeSn().execute(cmd_connect, AP_IP);
                }
            });
        }
    }

    private class SetNodeSn extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            //  showProgressDialog("Sending wifi password to Wio...");
            mTvHint.setText("Sending wifi password to Wio...");
        }

        @Override
        protected Boolean doInBackground(String... params) {
            ConfigUdpSocket udpClient = new ConfigUdpSocket();
            String cmd = params[0];
            String ipAddr = params[1];
            udpClient.setSoTimeout(5000); //1s timeout
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
            connectWifi(ssid, wifiPwd);
            mProgressBar.setProgress(30);
        }
    }

    int flag = 0;

    private void connectWifi(final String ssid, String pwd) {
        flag = 0;
        getSupportActionBar().setTitle(R.string.title_ap_connect_activity);
        mTvHint.setText("Waiting Wio get ip address...");
        final Timer timer = new Timer();
        final WifiUtils wifiUtils = new WifiUtils(this);
        wifiUtils.openWifi();
        wifiUtils.addNetwork(wifiUtils.CreateWifiInfo(ssid, pwd, 3));
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                flag++;
                if (flag >= 30) {
                    timer.cancel();
                    MLog.e(this, "超时");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogUtils.showErrorDialog(Step04ApConnectActivity.this, "Fail connect to Wifi", "TRY AGAIN", "FAQ", "Please check your internet connection and try again.\r\n" +
                                    "If still can’t slove the problem, please try FAQ section and contact us there. ", new DialogUtils.OnErrorButtonClickListenter() {
                                @Override
                                public void okClick() {
                                    connectWifi(ssid, wifiPwd);
                                }

                                @Override
                                public void cancelClick() {
                                    gotoHelp();
                                }
                            });
                        }
                    });
                }
                if (wifiUtils.isWifiConnected(Step04ApConnectActivity.this)) {
                    timer.cancel();
                    MLog.e(this, "连接成功");
                    //checkIsOnline();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new checkNodeIsOnline().execute();
                        }
                    });

                } else {
                    MLog.e(this, "连接失败");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setProgress(30 + flag);
                        mTvHint.setText("Waiting Wio get ip address...[" + (30 - flag) + "]");
                    }
                });

            }
        }, 1500, 1000);
    }

    private class checkNodeIsOnline extends AsyncTask<Void, Integer, Boolean> {
        private Boolean state_online = false;

        @Override
        protected void onPreExecute() {
            //  showProgressDialog("Waiting Wio get ip address...");
            showProgressText("Get device status...");
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
                            if (state_online)
                                break;
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("TAG", error.toString());
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
            showProgressText("Get device status...[" + i + "]");
            mProgressBar.setProgress(60 + (30 - i));
        }

        @Override
        protected void onPostExecute(Boolean state_online) {
            if (Step04ApConnectActivity.this.isFinishing()) { // or call isFinishing() if min sdk version < 17
                return;
            }

            if (state_online) {
                // attemptRename();
                if (isChangeWifi) {
                    gotoMain();
                } else {
                    setDefaultName();
                }
            } else {
                DialogUtils.showErrorDialog(Step04ApConnectActivity.this, "Connection Error", "TRY AGAIN", "Cancel", "Please check your internet connection and try again.\r\n" +
                        "If still can’t slove the problem, please try FAQ section and contact us there. ", new DialogUtils.OnErrorButtonClickListenter() {
                    @Override
                    public void okClick() {
                        MobclickAgent.onEvent(Step04ApConnectActivity.this, "17005");
                        new checkNodeIsOnline().execute();
                    }

                    @Override
                    public void cancelClick() {
                        MobclickAgent.onEvent(Step04ApConnectActivity.this, "17006");
                        stopLoading();
                        finish();
                    }
                });
            }
        }
    }

    private void setDefaultName() {
        isSetDefName = true;
        Random random = new Random();
        switch (board) {
            default:
            case Constant.WIO_LINK_V1_0:
                defaultName = "Wio Link " + random.nextInt(50);
                break;
            case Constant.WIO_NODE_V1_0:
                defaultName = "Wio Node " + random.nextInt(50);
                break;
        }
        ConfigDeviceLogic.getInstance().nodeReName(node_sn, defaultName);
        showProgressText("Setting Wio name...");
    }

    private void customName() {
        isSetDefName = false;
        dialog = DialogUtils.showEditNodeNameDialog(Step04ApConnectActivity.this, defaultName, new DialogUtils.ButtonEditClickListenter() {
            @Override
            public void okClick(Dialog dialog, String content) {
                if (defaultName.equals(content)) {
                    gotoMain();
                } else {
                    MobclickAgent.onEvent(Step04ApConnectActivity.this, "17004");
                    dialog.dismiss();
                    node_name = content;
                    ConfigDeviceLogic.getInstance().nodeReName(node_sn, content);
                    showProgressText("Setting Wio name...");
                }
            }
        });
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

    public void showProgressText(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvHint.setText(str);
            }
        });
    }

    @Override
    public String[] monitorEvents() {
        return new String[]{Cmd_Node_ReName};
    }

    @Override
    public void onEvent(String event, boolean ret, String errInfo, Object[] data) {
        if (Cmd_Node_ReName.equals(event)) {
            if (ret) {
                if (isSetDefName) {
                    customName();
                } else {
                    mProgressBar.setProgress(100);
                    gotoMain();
                }
            } else {
                stopLoading();
            }
        }
    }

    private void gotoMain() {
        Intent intent = new Intent(Step04ApConnectActivity.this, MainScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        stopLoading();
        finish();
    }

    private void gotoHelp() {
        startActivity(new Intent(this, HelpActivity.class));
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
