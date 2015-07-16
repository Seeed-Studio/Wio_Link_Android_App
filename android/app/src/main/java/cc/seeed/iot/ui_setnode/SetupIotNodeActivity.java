package cc.seeed.iot.ui_setnode;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        implements GroveFilterRecyclerAdapter.MainViewHolder.MyItemClickListener, View.OnClickListener {
    public Toolbar mToolbar;
    public Toolbar mToolbarAction;
    ArrayList<Node> nodes;
    Node node;

    static View.OnClickListener mainOnClickListener; //Todo, no static

    RecyclerView mGroveListView;
    GroveListRecyclerAdapter mGroveListAdapter;

    RecyclerView mGroveTypeListView;
    GroveFilterRecyclerAdapter mGroveTypeListAdapter;
    private ArrayList<GroverDriver> mGroveDrivers;

    ImageButton mCorrectView;
    ImageButton mCancelView;

    SparseBooleanArray nodePinSelector;
    Map<Integer, NodePinConfig> nodePinConfigs;

    ImageButton pin1View;
    ImageButton pin2View, pin3View, pin4View, pin5View, pin6View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_iot_node);

        mainOnClickListener = new MainOnClickListener(this);

        nodePinSelector = new SparseBooleanArray();
        nodePinConfigsInit();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbarAction = (Toolbar) findViewById(R.id.toolbar_bottom);
        mToolbarAction.setVisibility(View.GONE);
        mCorrectView = (ImageButton) findViewById(R.id.ib_correct);
        mCancelView = (ImageButton) findViewById(R.id.ib_cancel);
        mCorrectView.setOnClickListener(this);
        mCancelView.setOnClickListener(this);

        final ImageButton pin1View = (ImageButton) findViewById(R.id.grove_1);
        final ImageButton pin2View = (ImageButton) findViewById(R.id.grove_2);
        final ImageButton pin3View = (ImageButton) findViewById(R.id.grove_3);
        final ImageButton pin4View = (ImageButton) findViewById(R.id.grove_4);
        final ImageButton pin5View = (ImageButton) findViewById(R.id.grove_5);
        final ImageButton pin6View = (ImageButton) findViewById(R.id.grove_6);
        //todo why use switch onclick have error?
        //todo bad code
        pin1View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nodePinSelector.clear();
                nodePinSelector.put(1, true);
                pin1View.setSelected(nodePinSelector.get(1, false));
                pin2View.setSelected(nodePinSelector.get(2, false));
                pin3View.setSelected(nodePinSelector.get(3, false));
                pin4View.setSelected(nodePinSelector.get(4, false));
                pin5View.setSelected(nodePinSelector.get(5, false));
                pin6View.setSelected(nodePinSelector.get(6, false));
            }
        });
        pin2View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nodePinSelector.clear();
                nodePinSelector.put(2, true);
                pin1View.setSelected(nodePinSelector.get(1, false));
                pin2View.setSelected(nodePinSelector.get(2, false));
                pin3View.setSelected(nodePinSelector.get(3, false));
                pin4View.setSelected(nodePinSelector.get(4, false));
                pin5View.setSelected(nodePinSelector.get(5, false));
                pin6View.setSelected(nodePinSelector.get(6, false));
            }
        });
        pin3View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nodePinSelector.clear();
                nodePinSelector.put(3, true);
                pin1View.setSelected(nodePinSelector.get(1, false));
                pin2View.setSelected(nodePinSelector.get(2, false));
                pin3View.setSelected(nodePinSelector.get(3, false));
                pin4View.setSelected(nodePinSelector.get(4, false));
                pin5View.setSelected(nodePinSelector.get(5, false));
                pin6View.setSelected(nodePinSelector.get(6, false));
            }
        });
        pin4View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nodePinSelector.clear();
                nodePinSelector.put(4, true);
                pin1View.setActivated(true);
                pin1View.setSelected(nodePinSelector.get(1, false));
                pin2View.setSelected(nodePinSelector.get(2, false));
                pin3View.setSelected(nodePinSelector.get(3, false));
                pin4View.setSelected(nodePinSelector.get(4, false));
                pin5View.setSelected(nodePinSelector.get(5, false));
                pin6View.setSelected(nodePinSelector.get(6, false));
            }
        });
        pin5View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nodePinSelector.clear();
                nodePinSelector.put(5, true);
                pin1View.setSelected(nodePinSelector.get(1, false));
                pin2View.setSelected(nodePinSelector.get(2, false));
                pin3View.setSelected(nodePinSelector.get(3, false));
                pin4View.setSelected(nodePinSelector.get(4, false));
                pin5View.setSelected(nodePinSelector.get(5, false));
                pin6View.setSelected(nodePinSelector.get(6, false));
            }
        });
        pin6View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nodePinSelector.clear();
                nodePinSelector.put(6, true);
                pin1View.setSelected(nodePinSelector.get(1, false));
                pin2View.setSelected(nodePinSelector.get(2, false));
                pin3View.setSelected(nodePinSelector.get(3, false));
                pin4View.setSelected(nodePinSelector.get(4, false));
                pin5View.setSelected(nodePinSelector.get(5, false));
                pin6View.setSelected(nodePinSelector.get(6, false));
            }
        });

        nodes = ((MyApplication) SetupIotNodeActivity.this.getApplication()).getNodes();
        int position = getIntent().getIntExtra("position", 1);
//        node = nodes.get(position);
//        Snackbar.make(toolbar, "Here's a " + node.name, Snackbar.LENGTH_LONG).show();


        mGroveListView = (RecyclerView) findViewById(R.id.grove_list);
        if (mGroveListView != null) {
            mGroveListView.setHasFixedSize(true);
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

    private void nodePinConfigsInit() {
        NodePinConfig n = new NodePinConfig();
        //Todo need init?
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
            Snackbar.make(mToolbar, "Here's a Snackbar" + node.name, Snackbar.LENGTH_LONG).show();
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

        mGroveTypeListAdapter.updateSelection(postion);

        if (groveType.equals("All")) {
            updateGroveListAdapter(mGroveDrivers);

        } else if (groveType.equals("Input")) {
            updateGroveListAdapter(inputGroves);

        } else if (groveType.equals("Output")) {
            updateGroveListAdapter(outputGroves);

        } else if (groveType.equals("Light")) {

        } else if (groveType.equals("Env")) {

        } else if (groveType.equals("Actuator")) {

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_correct:
                Snackbar.make(v, "Todo:set node: " + mGroveListAdapter.getSelectedItem().GroveName, Snackbar.LENGTH_SHORT).show();
                mToolbarAction.setVisibility(View.GONE);
                mGroveListAdapter.clearSelectItem();
                break;

            case R.id.ib_cancel:
                Snackbar.make(v, "Todo:set node", Snackbar.LENGTH_SHORT).show();
                mToolbarAction.setVisibility(View.GONE);
                mGroveListAdapter.clearSelectItem();

                break;

            case R.id.grove_1:
                selectPin(1);
                break;
            case R.id.grove_2:
//                selectPin(2);
                pin2View.setSelected(true);
                break;
            case R.id.grove_3:
                selectPin(3);
                break;
            case R.id.grove_4:
                selectPin(4);
                break;
            case R.id.grove_5:
                selectPin(5);
                break;
            case R.id.grove_6:
                selectPin(6);
                break;
        }

    }

    private class MainOnClickListener implements View.OnClickListener {
        private final Context context;

        private MainOnClickListener(Context c) {
            this.context = c;
        }

        @Override
        public void onClick(View v) {

            mGroveListAdapter.selectItem(mGroveListView.getChildAdapterPosition(v));
            mToolbarAction.setVisibility(View.VISIBLE);
        }

    }


    public class NodePinConfig {
        public Boolean filled;
        public GroverDriver groverDriver;
        public String GroveInstanceName;
    }

    public void selectPin(int pin) {
        nodePinSelector.clear();
        nodePinSelector.put(pin, true);
        pin1View.setPressed(true);
//        pin2View.setSelected(nodePinSelector.get(pin, false));
//        pin3View.setSelected(nodePinSelector.get(pin, false));
//        pin4View.setSelected(nodePinSelector.get(pin, false));
//        pin5View.setSelected(nodePinSelector.get(pin, false));
//        pin6View.setSelected(nodePinSelector.get(pin, false));
    }

}
