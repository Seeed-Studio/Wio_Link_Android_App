package cc.seeed.iot.activity.add_step;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.adapter.add_node.WifiRecyclerViewHolder;
import cc.seeed.iot.adapter.add_node.WifiWioListRecyclerAdapter;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.view.StepView;


public class Step02WifiWioListActivity extends BaseActivity
        implements WifiRecyclerViewHolder.IMyViewHolderClicks {
    private final static String TAG = "WifiPionListActivity";
    private final static int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 0x00;
    private final static String PION_WIFI_PREFIX = "PionOne_";
    private final static String WIO_WIFI_PREFIX = "Wio";
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.mStepView)
    StepView mStepView;
    @InjectView(R.id.tip)
    TextView mTip;
    @InjectView(R.id.wifi_list)
    RecyclerView mWifiListView;

    private WifiWioListRecyclerAdapter mWifiListAdapter;
    private ProgressDialog mWaitDialog;
    private List<ScanResult> scanPionResult = new ArrayList<>();

    private String board;
    private String node_sn;
    private String node_key;
    private String selected_ssid;
    private Boolean state_selected; //cause wifi broadcast up when wifi broadcast register

    private long startTime = 0;
    private long endTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_list);
        ButterKnife.inject(this);

        initToolBar();
        initData();
    }

    private void initToolBar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_wio_activity);
    }

    private void initData() {
        mStepView.setDoingStep(1);
        if (mWifiListView != null) {
            mWifiListView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mWifiListView.setLayoutManager(layoutManager);
            mWifiListAdapter = new WifiWioListRecyclerAdapter(getPionWifiList(), this);
            mWifiListView.setAdapter(mWifiListAdapter);
        }

        if (!isLocationEnabled(this)) {
            mTip.setVisibility(View.VISIBLE);
        } else {
            mTip.setVisibility(View.GONE);
        }
        mWaitDialog = new ProgressDialog(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        state_selected = false;
        selected_ssid = "";

        Intent intent = getIntent();
        board = intent.getStringExtra("board");
        node_sn = intent.getStringExtra("node_sn");
        node_key = intent.getStringExtra("node_key");
        IntentFilter actionFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        actionFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
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
        unregisterReceiver(wifiActionReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private List<ScanResult> getPionWifiList() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            Log.e(TAG, "checking...");
        } else {
            Log.e(TAG, "scaning...");
            getScanningResults();
            //do something, permission was previously granted; or legacy device
        }

        return scanPionResult;
    }

    private void getScanningResults() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> scanResult = wifiManager.getScanResults();
        scanPionResult.clear();
        for (ScanResult wifi : scanResult) {
            if (wifi.SSID.contains(PION_WIFI_PREFIX) || wifi.SSID.contains(WIO_WIFI_PREFIX)) {
                Log.i(TAG, "Wio ssid:" + wifi.SSID);
                scanPionResult.add(wifi);
            }
        }
    }


    @Override
    public void onItem(View caller) {
        MobclickAgent.onEvent(this, "17002");
        state_selected = true;
        int position = mWifiListView.getChildLayoutPosition(caller);
        ScanResult scanResult = mWifiListAdapter.getItem(position);
        selected_ssid = scanResult.SSID;

        if (selected_ssid.equals(getCurrentSsid()))
            goWifiListActivity();
        else {
            wifiConnect(selected_ssid);
        }


    }

    public void wifiConnect(String SSID) {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + SSID + "\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiManager.addNetwork(conf);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        if (list == null) {
            Log.e(TAG, "List<WifiConfiguration> is null!");
            return;
        }

        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals("\"" + SSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();

                mWaitDialog.setMessage("Connecting to " + SSID + "...");
                mWaitDialog.setCanceledOnTouchOutside(false);
                mWaitDialog.show();
                startTime = System.currentTimeMillis();
                break;
            }
        }
    }

    private BroadcastReceiver wifiActionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            endTime = System.currentTimeMillis();
            if (startTime != 0 && endTime - startTime > 60*1000){
                startTime = 0;
                if (mWaitDialog != null && mWaitDialog.isShowing()){
                    mWaitDialog.dismiss();
                }
                DialogUtils.showErrorDialog(Step02WifiWioListActivity.this,"","OK","","Connect fail..",null);
            }
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    WifiInfo wifiInfo = (WifiInfo) intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);

                    if (wifiInfo.getSSID().contains(selected_ssid) && state_selected) {
                        mWaitDialog.dismiss();
                        state_selected = false;
                        reEnableAllAps(context);
                        goWifiListActivity();
                    }
                }
            } else if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                Log.d(TAG, "refresh Pion list!");
                refreshPionList();
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


    private void refreshPionList() {
        mWifiListAdapter.updateAll(getPionWifiList());
    }

    private void goWifiListActivity() {
        Intent intentActivity = new Intent(this, Step03WifiListActivity.class);
        intentActivity.putExtra("board", board);
        intentActivity.putExtra("node_key", node_key);
        intentActivity.putExtra("node_sn", node_sn);
        startActivity(intentActivity);
    }

    private String getCurrentSsid() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID(); //getSSID return "ssid"
    }

    private static void reEnableAllAps(final Context ctx) {
        final WifiManager wifiMgr = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        final List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
        if (configurations != null) {
            for (final WifiConfiguration config : configurations) {
                wifiMgr.enableNetwork(config.networkId, false);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e(TAG, "onRequestPermissionsResult");
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Do something with granted permission
            getScanningResults();

        }
    }

    private boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }
}

