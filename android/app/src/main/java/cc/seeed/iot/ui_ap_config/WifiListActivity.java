package cc.seeed.iot.ui_ap_config;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cc.seeed.iot.R;


public class WifiListActivity extends AppCompatActivity
        implements WifiRecyclerViewHolder.IMyViewHolderClicks {
    private final static String TAG = "WifiListActivity";
    private final static String PION_WIFI_PREFIX = "PionOne";
    private final static String WIO_WIFI_PREFIX = "Wio";
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
        getSupportActionBar().setTitle(R.string.title_wifi_list_activity);

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
        unregisterReceiver(wifiActionReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private List<ScanResult> getWifiExceptPionList() {
        List<ScanResult> scanNoPionResult = new ArrayList<>();
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

        return scanNoPionResult;
    }


    @Override
    public void onItem(View caller) {
        int position = mWifiListView.getChildLayoutPosition(caller);
        ScanResult scanResult = mWifiListAdapter.getItem(position);

        Intent intent = new Intent(this, ApConnectActivity.class);
        intent.putExtra("ssid", scanResult.SSID);
        intent.putExtra("node_key", node_key);
        intent.putExtra("node_sn", node_sn);
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
        mWifiListAdapter.updateAll(getWifiExceptPionList());
    }
}

