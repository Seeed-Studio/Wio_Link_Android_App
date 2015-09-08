package cc.seeed.iot.ui_smartconfig;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import cc.seeed.iot.R;
import cc.seeed.iot.udp.ConfigNodeData;
import cc.seeed.iot.udp.ConfigUdpSocket;


public class ConfigNodeListActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mNodeListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ConfigNodeListRecyclerAdapter mNodeListAdapter;
    private ConfigUdpSocket udpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_node_list);
        udpClient = new ConfigUdpSocket();

        Log.e("iot", "ip:" + getLocalBroadcastAddress());

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("WIFI Iot Node");

        mNodeListView = (RecyclerView) findViewById(R.id.config_nodes_list);
        if (mNodeListView != null) {
            mNodeListView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mNodeListView.setLayoutManager(layoutManager);


        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.config_nodes_swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new searchConfigNode().execute();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 0);
            }
        });

        new searchConfigNode().execute();

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

    private void setupAdapter(ArrayList<ConfigNodeData> localNodes) {
        mNodeListAdapter = new ConfigNodeListRecyclerAdapter(localNodes);
        mNodeListView.setAdapter(mNodeListAdapter);

    }


    private class searchConfigNode extends AsyncTask<String, Void, ArrayList<ConfigNodeData>> {
        //todo: real-time refresh
        private ProgressDialog mProgressDialog;


        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(ConfigNodeListActivity.this);
//            mProgressDialog.setMessage("search node...");
            mProgressDialog.setTitle("Search node...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }

        @Override
        protected ArrayList<ConfigNodeData> doInBackground(String... params) {
            ArrayList<ConfigNodeData> configNodeDatas = new ArrayList<>();
            udpClient.setSoTimeout(6000); //3s timeout //todo: long wait
            udpClient.sendData(ConfigUdpSocket.CMD_BLANK, getLocalBroadcastAddress());
            while (true) {
                try {
                    ConfigNodeData configNodeData = udpClient.receiveNodeData();
                    if (configNodeData != null && !configNodeDatas.contains(configNodeData)) {
                        configNodeDatas.add(configNodeData);
                    }
                } catch (SocketTimeoutException e) {
                    break;
                } catch (IOException e) {
                    Log.e("iot", "Error[AsyIO]:" + e);
                }
            }

            Log.i("iot", "configNodeDatas: " + configNodeDatas);
            return configNodeDatas;
        }

        @Override
        protected void onPostExecute(ArrayList<ConfigNodeData> configNodeDatas) {
            setupAdapter(configNodeDatas);
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        udpClient.closeSocket();
    }

    private String getLocalIpAddress() {
        WifiManager wifiMan = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        return ip;
    }

    private String getLocalBroadcastAddress() {
        String localIpAddress = getLocalIpAddress();
        if (localIpAddress.isEmpty())
            return "";
        String[] ip = localIpAddress.split("\\.");
        return ip[0] + "." + ip[1] + "." + ip[2] + ".255";
    }

}

