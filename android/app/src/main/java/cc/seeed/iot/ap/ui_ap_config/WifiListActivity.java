package cc.seeed.iot.ap.ui_ap_config;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import cc.seeed.iot.ap.R;


public class WifiListActivity extends AppCompatActivity
        implements WifiRecyclerViewHolder.IMyViewHolderClicks {
    private final static String TAG = "WifiListActivity";
    private final static String PION_WIFI_PREFIX = "PionOne";
    private Toolbar mToolbar;
    private RecyclerView mWifiListView;
    private WifiListRecyclerAdapter mWifiListAdapter;

    private String node_sn;
    private String node_key;
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
            mWifiListAdapter = new WifiListRecyclerAdapter(getWifiExceptPionList(), this);
            mWifiListView.setAdapter(mWifiListAdapter);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        node_sn = intent.getStringExtra("node_sn");
        node_key = intent.getStringExtra("node_key");
        Log.e(TAG, "node_sn:" + node_sn);
        Log.e(TAG, "node_key:" + node_key);
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
    protected void onDestroy() {
        super.onDestroy();
    }

    private ArrayList<ScanResult> getWifiExceptPionList() {
        ArrayList<ScanResult> scanPionResult = new ArrayList<>();
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        ArrayList<ScanResult> scanResult = (ArrayList) wifiManager.getScanResults();
        for (ScanResult wifi : scanResult) {
            if (wifi.SSID.contains(PION_WIFI_PREFIX)) {
                Log.d(TAG, "PionOne ssid:" + wifi.SSID);
                continue;
            }
            scanPionResult.add(wifi);
        }
        return scanPionResult;
    }


    @Override
    public void onItem(View caller) {
        int position = mWifiListView.getChildLayoutPosition(caller);
        ScanResult scanResult = mWifiListAdapter.getItem(position);
        Log.e(TAG, "item:" + scanResult.SSID + " " + scanResult.level);

        Intent intent = new Intent(this, ApConnectActivity.class);
        intent.putExtra("ssid", scanResult.SSID);
        intent.putExtra("node_key", node_key);
        intent.putExtra("node_sn", node_sn);
        startActivity(intent);
    }
}

