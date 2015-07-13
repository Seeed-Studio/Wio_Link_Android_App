package cc.seeed.iot.ui_setnode;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.datastruct.Constant;
import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.webapi.model.Node;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SetupIotNodeActivity extends AppCompatActivity
        implements GroveFilterRecyclerAdapter.MainViewHolder.MyItemClickListener {
    public Toolbar toolbar;
    ArrayList<Node> nodes;
    Node node;

    RecyclerView mGroveListView;
    GroveListRecyclerAdapter mGroveListAdapter;

    RecyclerView mGroveTypeListView;
    GroveFilterRecyclerAdapter mGroveTypeListAdapter;
    private ArrayList<GroverDriver> mGroveDrivers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_iot_node);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nodes = ((MyApplication) SetupIotNodeActivity.this.getApplication()).getNodes();
        int position = getIntent().getIntExtra("position", 1);
//        node = nodes.get(position);
//        Snackbar.make(toolbar, "Here's a " + node.name, Snackbar.LENGTH_LONG).show();


        mGroveListView = (RecyclerView) findViewById(R.id.grove_list);
        if (mGroveListView != null) {
            mGroveListView.setHasFixedSize(true);
//            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

            mGroveListView.setLayoutManager(layoutManager);

            setupAdapter();
        }


        mGroveTypeListView = (RecyclerView) findViewById(R.id.grove_selector);
        if (mGroveTypeListView != null) {
            mGroveTypeListView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

            mGroveTypeListView.setLayoutManager(layoutManager);

            setupGroveSetectorAdapter();
        }
    }

    private void setupGroveSetectorAdapter() {
        mGroveTypeListAdapter = new GroveFilterRecyclerAdapter(Constant.groveTypes);
        mGroveTypeListAdapter.setOnItemClickListener(this);
        mGroveTypeListView.setAdapter(mGroveTypeListAdapter);
    }

    private void updateGroveListAdapter(ArrayList<GroverDriver> groverDrivers) {
        mGroveListAdapter = new GroveListRecyclerAdapter(groverDrivers);
        mGroveListView.setAdapter(mGroveListAdapter);
    }

    private void setupAdapter() {
        IotApi api = new IotApi();
        User user = ((MyApplication) SetupIotNodeActivity.this.getApplication()).getUser();
        api.setAccessToken(user.user_key);
        final IotService iot = api.getService();
        iot.scanDrivers(new Callback<List<GroverDriver>>() {
            @Override
            public void success(List<GroverDriver> groverDrivers, Response response) {
//                if (groverDriver.status.equals("200")) {
//                    nodes = (ArrayList) nodeListResponse.nodes;
//                    ((MyApplication) MainScreenActivity.this.getApplication()).setNodes(nodes);
//                    mGroveListAdapter = new NodeListRecyclerAdapter(nodes);

                mGroveDrivers = (ArrayList) groverDrivers;
                mGroveListAdapter = new GroveListRecyclerAdapter((ArrayList) groverDrivers);
                mGroveListView.setAdapter(mGroveListAdapter);
//                } else {
//                    Toast.makeText(MainScreenActivity.this, nodeListResponse.msg, Toast.LENGTH_LONG).show();
//                }
//                mGroveListAdapter.
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("iot", "fail");
                Toast.makeText(SetupIotNodeActivity.this, "连接服务器失败", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setup_iot_node, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.update) {
            //TODO update firmware
            Snackbar.make(toolbar, "Here's a Snackbar" + node.name, Snackbar.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemClick(View view, int postion) {
        String groveType = Constant.groveTypes[postion];

        ArrayList<GroverDriver> inputGroves = new ArrayList<GroverDriver>();
        ArrayList<GroverDriver> outputGroves = new ArrayList<GroverDriver>();

        if (mGroveDrivers == null)
            return;

        for (GroverDriver g : mGroveDrivers) {
            if (!g.Inputs.isEmpty()) {
                outputGroves.add(g);
            }
            if (!g.Outputs.isEmpty())
                inputGroves.add(g);
        }

        if (groveType.equals("All")) {
            updateGroveListAdapter(mGroveDrivers);

        } else if (groveType.equals("Input")) {
            updateGroveListAdapter(inputGroves);

        } else if (groveType.equals("Output")) {
            TextView v = (TextView) view.findViewById(R.id.grove_type);
            v.setTextColor(view.getResources().getColor(R.color.window_background_dark));

            updateGroveListAdapter(outputGroves);

        } else if (groveType.equals("Light")) {

        } else if (groveType.equals("Env")) {

        } else if (groveType.equals("Actuator")) {

        }
    }
}
