package cc.seeed.iot.activity.add_step;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tbruyelle.rxpermissions.RxPermissions;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.adapter.add_node.WifiListRecyclerAdapter;
import cc.seeed.iot.adapter.add_node.WifiRecyclerViewHolder;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.udp.ConfigUdpSocket;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.MLog;
import cc.seeed.iot.util.NetworkUtils;
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


public class Step02WifiListActivity extends BaseActivity
        implements WifiRecyclerViewHolder.IMyViewHolderClicks {
    private static final String AP_IP = "192.168.4.1";
    private final static String PION_WIFI_PREFIX = "PionOne_";
    private final static String WIO_WIFI_PREFIX = "Wio";
    private final static String TAG = "Step02WifiListActivity";
    private final static int REQUEST_PERMISSIONS = 111;

    private String ssid;
    private String node_name;
    private String board;
    private String node_sn;
    private String node_key;
    private String wifiPwd;
    private ConfigUdpSocket udpClient;
    private Animation animation;
    private ProgressDialog dialog;
    private boolean isChangeWifi = false;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.mStepView)
    StepView mStepView;
    @InjectView(R.id.tip)
    TextView mTip;
    @InjectView(R.id.mTvTip)
    FontTextView mTvTip;
    @InjectView(R.id.wifi_list)
    RecyclerView mWifiListView;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;

    private WifiListRecyclerAdapter mWifiListAdapter;
    private ScanResult scanResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_list);
        ButterKnife.inject(this);
        mProgressBar.setVisibility(View.GONE);
//        mProgressBar.setMax(30);

        initToolBar();
        initData();

    }

    private void initToolBar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_wifi_list_activity);
    }

    private void initData() {
        mTvTip.setText("Please check if Wio app get Wi-Fi permission from your phone and make sure there's available Wi-Fi around.");
        mStepView.setDoingStep(1);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //   App.showToastShrot("没有权限");
            mTvTip.setVisibility(View.VISIBLE);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSIONS);
            //申请WRITE_EXTERNAL_STORAGE权限
            //  ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
        } else {
            if (mWifiListView != null) {
                mWifiListView.setHasFixedSize(true);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                mWifiListView.setLayoutManager(layoutManager);
                mWifiListAdapter = new WifiListRecyclerAdapter(getWifiExceptPionList(), this);
                mWifiListView.setAdapter(mWifiListAdapter);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        Intent intent = getIntent();
        board = intent.getStringExtra(Step04ApConnectActivity.Intent_Board);
        node_sn = intent.getStringExtra(Step04ApConnectActivity.Intent_NodeSn);
        node_key = intent.getStringExtra(Step04ApConnectActivity.Intent_NodeKey);
        isChangeWifi = intent.getBooleanExtra(Step04ApConnectActivity.Intent_ChangeWifi, false);

        IntentFilter actionFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiActionReceiver, actionFilter);

        new ScanWifi().start();
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
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        unregisterReceiver(wifiActionReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private List<ScanResult> getWifiExceptPionList() {
        List<ScanResult> scanNoPionResult = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> scanResult = wifiManager.getScanResults();

            Iterator<ScanResult> iterator = scanResult.iterator();
            //delete duplicate wifi
            List<String> ssidList = new ArrayList<>();
            while (iterator.hasNext()) {
                ScanResult s = iterator.next();
                if (ssidList.contains(s.SSID)) {
                    iterator.remove();
                } else {
                    ssidList.add(s.SSID);
                }
            }

            //delete pion wifi
            for (ScanResult s : scanResult) {
                if (s.SSID.contains(PION_WIFI_PREFIX) || s.SSID.contains(WIO_WIFI_PREFIX)) {
                    continue;
                }
                scanNoPionResult.add(s);
            }

            if (scanNoPionResult == null || scanNoPionResult.size() == 0) {
                mTvTip.setVisibility(View.VISIBLE);
            } else {
                mTvTip.setVisibility(View.GONE);
            }

        }
        return scanNoPionResult;
    }


    @Override
    public void onItem(View caller) {
        int position = mWifiListView.getChildLayoutPosition(caller);
        scanResult = mWifiListAdapter.getItem(position);
        String pwd = App.getSp().getString(scanResult.SSID, "");
        DialogUtils.showEditOneRowDialog(Step02WifiListActivity.this, "Enter Wifi Password", pwd, new DialogUtils.ButtonEditClickListenter() {
            @Override
            public void okClick(Dialog dialog, String pwd) {
                MobclickAgent.onEvent(Step02WifiListActivity.this, "17003");
                dialog.dismiss();
                wifiPwd = pwd;
                connectWifi(scanResult.SSID, pwd);
            }
        });
    }

    int flag = 0;

    private void connectWifi(String ssid, String pwd) {
        flag = 0;
        final Timer timer = new Timer();
        dialog = DialogUtils.showProgressDialog(Step02WifiListActivity.this, "Connecting to " + scanResult.SSID + " WiFI ");
        final WifiUtils wifiUtils = new WifiUtils(Step02WifiListActivity.this);
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
                            DialogUtils.showErrorDialog(Step02WifiListActivity.this, "Fail connect to Wifi", getString(R.string.dialog_btn_tryAgain),
                                    getString(R.string.dialog_btn_Cancel), getString(R.string.cont_connection_wifi), new DialogUtils.OnErrorButtonClickListenter() {
                                        @Override
                                        public void okClick() {
                                            connectWifi(scanResult.SSID, wifiPwd);
                                        }

                                        @Override
                                        public void cancelClick() {
                                        }
                                    });
                        }
                    });

                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                if (wifiUtils.isWifiConnected(Step02WifiListActivity.this)) {
                    timer.cancel();
                    MLog.e(this, "连接成功");
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    gotoStep03();
                } else {
                    //  MLog.e(this, "连接失败");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.setMessage("Connecting to " + scanResult.SSID + " WiFI (" + (30 - flag) + ")");
                    }
                });

            }
        }, 1500, 1000);
    }

    private void gotoStep03() {
        App.getSp().edit().putString(scanResult.SSID, wifiPwd).commit();
        Intent intent = new Intent(Step02WifiListActivity.this, Step03WifiWioListActivity.class);
        intent.putExtra(Step04ApConnectActivity.Intent_ChangeWifi, isChangeWifi);
        intent.putExtra(Step04ApConnectActivity.Intent_Ssid, scanResult.SSID);
        intent.putExtra(Step04ApConnectActivity.Intent_Board, board);
        intent.putExtra(Step04ApConnectActivity.Intent_NodeKey, node_key);
        intent.putExtra(Step04ApConnectActivity.Intent_NodeSn, node_sn);
        intent.putExtra(Step04ApConnectActivity.Intent_WifiPwd, wifiPwd);
        startActivity(intent);
    }

    private BroadcastReceiver wifiActionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                refreshWifiList();
                new ScanWifi().start();
            }
        }
    };

    private class ScanWifi extends Thread {
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifiManager.startScan();
        }
    }

    private void refreshWifiList() {
        if (mWifiListAdapter == null) {
            if (mWifiListView != null) {
                mWifiListView.setHasFixedSize(true);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                mWifiListView.setLayoutManager(layoutManager);
                mWifiListAdapter = new WifiListRecyclerAdapter(getWifiExceptPionList(), this);
                mWifiListView.setAdapter(mWifiListAdapter);
            }
        } else {
            mWifiListAdapter.updateAll(getWifiExceptPionList());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
            } else {
                // Permission Denied
            }
        }
    }
}

