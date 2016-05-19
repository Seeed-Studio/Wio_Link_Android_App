package cc.seeed.iot.activity.add_step;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.adapter.add_node.WifiListRecyclerAdapter;
import cc.seeed.iot.adapter.add_node.WifiRecyclerViewHolder;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.view.StepView;


public class Step03WifiListActivity extends BaseActivity
        implements WifiRecyclerViewHolder.IMyViewHolderClicks {
    private final static String TAG = "Step03WifiListActivity";
    private final static String PION_WIFI_PREFIX = "PionOne";
    private final static String WIO_WIFI_PREFIX = "Wio";

    private String board;
    private String node_sn;
    private String node_key;
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.mStepView)
    StepView mStepView;
    @InjectView(R.id.tip)
    TextView mTip;
    @InjectView(R.id.wifi_list)
    RecyclerView mWifiListView;

    private WifiListRecyclerAdapter mWifiListAdapter;
    private ScanResult scanResult;

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
        getSupportActionBar().setTitle(R.string.title_wifi_list_activity);
    }

    private void initData() {
        mStepView.setDoingStep(2);
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
        board = intent.getStringExtra("board");
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
        scanResult = mWifiListAdapter.getItem(position);
        DialogUtils.showEditWifiPwdDialog(Step03WifiListActivity.this, new DialogUtils.ButtonEditClickListenter() {
            @Override
            public void okClick(String pwd) {
                //  App.showToastShrot(pwd);
           //     DialogUtils.showProgressDialog(Step03WifiListActivity.this, "Sending wifi password to Wio...");
                Intent intent = new Intent(Step03WifiListActivity.this, Step04ApConnectActivity.class);
                intent.putExtra(Step04ApConnectActivity.Intent_Ssid, scanResult.SSID);
                intent.putExtra(Step04ApConnectActivity.Intent_Board, board);
                intent.putExtra(Step04ApConnectActivity.Intent_NodeKey, node_key);
                intent.putExtra(Step04ApConnectActivity.Intent_NodeSn, node_sn);
                intent.putExtra(Step04ApConnectActivity.Intent_WifiPwd, pwd);
                startActivity(intent);
            }
        });
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

