package cc.seeed.iot.ui_smartconfig;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.esptouch.udp.UDPSocketClient;
import cc.seeed.iot.ui_setnode.GroveListRecyclerAdapter;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.GroverDriver;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ConfigNodeListActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mNodeListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ConfigNodeListRecyclerAdapter mNodeListAdapter;

    private UDPSocketClient udpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_node_list);

        udpClient = new UDPSocketClient();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("WIFI Iot Node");

        mNodeListView = (RecyclerView) findViewById(R.id.config_nodes_list);
        if (mNodeListView != null) {
            mNodeListView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mNodeListView.setLayoutManager(layoutManager);

            setupAdapter();
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.config_nodes_swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setupAdapter();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 2500);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        String cmd_find_node = "Blank?";
        byte[] bytes = cmd_find_node.getBytes(Charset.forName("US-ASCII"));
        for (byte b : bytes) {
            Log.e("iot", "cmd:" + b);
        }
        byte[][] data = {bytes};
        String ip = "192.168.18.255";
        udpClient.sendData(data, ip, 1025, 100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public class LocalNode {
        public String ip;
        public String mac;
    }

    private void setupAdapter() {
        ArrayList<LocalNode> localNodes = new ArrayList<>();
        ConfigNodeListActivity.LocalNode l = new LocalNode();
        l.ip = "192.168.18.119";
        l.mac = "00:00:00:11:22";
        localNodes.add(l);

        mNodeListAdapter = new ConfigNodeListRecyclerAdapter(localNodes);
        mNodeListView.setAdapter(mNodeListAdapter);

    }
}
