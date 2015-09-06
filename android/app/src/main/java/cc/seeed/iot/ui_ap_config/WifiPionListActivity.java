package cc.seeed.iot.ui_ap_config;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;

import cc.seeed.iot.R;


public class WifiPionListActivity extends AppCompatActivity
        implements WifiRecyclerViewHolder.IMyViewHolderClicks {
    private final static String Tag = "WifiListActivity";
    private final static String PION_WIFI_PREFIX = "PionOne";
    private Toolbar mToolbar;
    private RecyclerView mWifiListView;
    private WifiPionListRecyclerAdapter mWifiListAdapter;
    private ProgressDialog mWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_list);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Config Pion One");

        mWifiListView = (RecyclerView) findViewById(R.id.wifi_list);
        if (mWifiListView != null) {
            mWifiListView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mWifiListView.setLayoutManager(layoutManager);
            mWifiListAdapter = new WifiPionListRecyclerAdapter(getPionWifiList(), this);
            mWifiListView.setAdapter(mWifiListAdapter);
        }

        mWaitDialog = new ProgressDialog(this);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private ArrayList<ScanResult> getPionWifiList() {
        ArrayList<ScanResult> scanPionResult = new ArrayList<>();
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        ArrayList<ScanResult> scanResult = (ArrayList) wifiManager.getScanResults();
        for (ScanResult wifi : scanResult) {
            if (wifi.SSID.contains(PION_WIFI_PREFIX)) {
                Log.d(Tag, "PionOne ssid:" + wifi.SSID);
                scanPionResult.add(wifi);
            }
        }
        return scanPionResult;
    }

    @Override
    public void onItem(View caller) {
        Log.e("iot", "item:" + mWifiListView.getChildLayoutPosition(caller));
        int position = mWifiListView.getChildLayoutPosition(caller);
        ScanResult scanResult = mWifiListAdapter.getItem(position);
        Log.e("iot", "item:" + scanResult.SSID + " " + scanResult.level);
//        Intent intent = new Intent(this, WifiListActivity.class);
//        startActivity(intent);
        mWaitDialog.setMessage("Connecting to " + scanResult.SSID + "...");
        mWaitDialog.setCanceledOnTouchOutside(false);
        mWaitDialog.show();
        wifiConnect(scanResult.SSID);

    }

    public void wifiConnect(String SSID) {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + SSID + "\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        int id = wifiManager.addNetwork(conf);
        wifiManager.enableNetwork(id, true);
    }
}

