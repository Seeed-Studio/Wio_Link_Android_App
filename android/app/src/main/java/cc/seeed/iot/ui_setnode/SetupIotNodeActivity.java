package cc.seeed.iot.ui_setnode;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
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
import cc.seeed.iot.ui_setnode.model.GroveFliter;
import cc.seeed.iot.ui_setnode.model.NodeConfigModel;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.OtaStatusResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SetupIotNodeActivity extends AppCompatActivity
        implements GroveFilterRecyclerAdapter.MainViewHolder.MyItemClickListener, View.OnClickListener {
    public Toolbar mToolbar;
    public Toolbar mToolbarAction;
    ArrayList<Node> nodes;
    Node node;
    User user;

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

    View mSetNodeLayout;
    ImageButton pin1View, pin2View, pin3View, pin4View, pin5View, pin6View;

    UiStateControl uiStateControl;

    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_iot_node);
        View view = (View) findViewById(R.id.setup_iot_node);
        uiStateControl = new UiStateControl(view);
        mProgressDialog = new ProgressDialog(this);

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

        mSetNodeLayout = (View) findViewById(R.id.set_node);
        mSetNodeLayout.setOnClickListener(this);
        pin1View = (ImageButton) findViewById(R.id.grove_1);
        pin2View = (ImageButton) findViewById(R.id.grove_2);
        pin3View = (ImageButton) findViewById(R.id.grove_3);
        pin4View = (ImageButton) findViewById(R.id.grove_4);
        pin5View = (ImageButton) findViewById(R.id.grove_5);
        pin6View = (ImageButton) findViewById(R.id.grove_6);

        pin1View.setOnClickListener(this);
        pin2View.setOnClickListener(this);
        pin3View.setOnClickListener(this);
        pin4View.setOnClickListener(this);
        pin5View.setOnClickListener(this);
        pin6View.setOnClickListener(this);

        nodes = ((MyApplication) SetupIotNodeActivity.this.getApplication()).getNodes();
        int position = getIntent().getIntExtra("position", 1);
        node = new Node();
        node = nodes.get(position);
        Snackbar.make(mToolbar, "Here's a " + node.name, Snackbar.LENGTH_LONG).show();

        user = ((MyApplication) SetupIotNodeActivity.this.getApplication()).getUser();

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
        mGroveListAdapter.updateAll(groverDrivers);
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
        getMenuInflater().inflate(R.menu.ui_setup, menu);
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
            if (node.name == null)
                return false;
            Snackbar.make(mToolbar, "Here's a Snackbar" + node.name, Snackbar.LENGTH_LONG).show();

//            String yaml = "" +
//                    "Grove_Example1:\r\n" +
//                    "  name: Grove_Example\r\n" +
//                    "  construct_arg_list:\r\n" +
//                    "    pinsda: 4\r\n" +
//                    "    pinscl: 5\r\n";


            NodeConfigModel nodeConfigModel = new NodeConfigModel();
            nodeConfigModel.addPinNode(1, mGroveDrivers.get(0));
            nodeConfigModel.addPinNode(2, mGroveDrivers.get(0));
            nodeConfigModel.addPinNode(3, mGroveDrivers.get(2));
            String yaml = nodeConfigModel.getConfigYaml();

            Log.e("iot", "yaml" + yaml);

            String Base64Yaml = Base64.encodeToString(yaml.getBytes(), Base64.DEFAULT);
            updateNode(node.node_key, Base64Yaml);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateNode(final String node_key, String base64Yaml) {
        mProgressDialog.setMessage("Ready to ota...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setButton(ProgressDialog.BUTTON_POSITIVE,
                "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        mProgressDialog.show();
        mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);

        IotApi api = new IotApi();
        final IotService iot = api.getService();
        iot.userDownload(node_key, base64Yaml, new Callback<OtaStatusResponse>() {
            @Override
            public void success(OtaStatusResponse otaStatusResponse, Response response) {
                if (otaStatusResponse.status.equals("200")) {
                    mProgressDialog.setMessage(otaStatusResponse.ota_msg);
                    displayStatus(node_key);
                } else {
                    mProgressDialog.setMessage("Error:" + otaStatusResponse.ota_msg);
                    mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                mProgressDialog.dismiss();
                Log.e("iot", "error:" + error);
            }
        });
    }

    private void displayStatus(final String node_key) {
        IotApi api = new IotApi();
        final IotService iot = api.getService();
        iot.otaStatus(node_key, new Callback<OtaStatusResponse>() {
                    @Override
                    public void success(OtaStatusResponse otaStatusResponse, Response response) {
                        if (otaStatusResponse.status.equals("200")) {
                            if (otaStatusResponse.ota_status.equals("going")) {
                                displayStatus(node_key);
                                mProgressDialog.setMessage(otaStatusResponse.ota_msg);
                            } else if (otaStatusResponse.ota_status.equals("done")) {
                                mProgressDialog.setMessage(otaStatusResponse.ota_msg);
                                mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                                mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                            } else if (otaStatusResponse.ota_status.equals("error")) {
                                mProgressDialog.setMessage(otaStatusResponse.ota_status + ":" + otaStatusResponse.ota_msg);
                                mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                            }
                        } else {
                            mProgressDialog.setMessage(otaStatusResponse.status + ":" + otaStatusResponse.msg);
                            mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e("iot", "error:" + error);
                        mProgressDialog.dismiss();
                    }
                }

        );
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
//                Snackbar.make(v, "Todo:set node: " + mGroveListAdapter.getSelectedItem().GroveName, Snackbar.LENGTH_SHORT).show();
                mToolbarAction.setVisibility(View.GONE);
                mGroveListAdapter.clearSelectItem();
                uiStateControl.activatedClear();
                uiStateControl.selectedPin();
                break;

            case R.id.ib_cancel:
                mToolbarAction.setVisibility(View.GONE);
                uiStateControl.activatedClear();
                mGroveListAdapter.clearSelectItem();

                break;

            case R.id.grove_1:
                uiStateControl.activatedPin(1);
                mGroveListAdapter.updateAll(
                        new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.GPIO));

//                mGroveListView.setActivated(true);
//                mGroveListView.setSelected(true);
//                mGroveListView.smoothScrollToPosition(7+2);
//                mGroveListAdapter.selectItem(7);
                break;
            case R.id.grove_2:
                uiStateControl.activatedPin(2);
                mGroveListAdapter.updateAll(
                        new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.GPIO));
                break;
            case R.id.grove_3:
                uiStateControl.activatedPin(3);
                mGroveListAdapter.updateAll(
                        new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.GPIO));
                break;
            case R.id.grove_4:
                uiStateControl.activatedPin(4);
                mGroveListAdapter.updateAll(
                        new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.ANALOG));
                break;
            case R.id.grove_5:
                uiStateControl.activatedPin(5);
                mGroveListAdapter.updateAll(
                        new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.UART));
                break;
            case R.id.grove_6:
                uiStateControl.activatedPin(6);
                mGroveListAdapter.updateAll(
                        new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.I2C));
                break;
            case R.id.set_node:
                if (mToolbarAction.getVisibility() == View.GONE)
                    uiStateControl.activatedClear();
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
            if (uiStateControl.pinSetIsTrue()) {
                mGroveListAdapter.selectItem(mGroveListView.getChildAdapterPosition(v));
                mToolbarAction.setVisibility(View.VISIBLE);
            } else {
                Snackbar.make(v, "Todo:grove detail page", Snackbar.LENGTH_SHORT).show();
            }
//            uiStateControl.selectGrove(mGroveListAdapter);
        }

    }


    public class NodePinConfig {
        public Boolean filled;
        public GroverDriver groverDriver;
        public String GroveInstanceName;
    }


    public interface UiSet {
        public void activatedPin(int pin);

        public void activatedClear();

        public void selectedPin();

        public void selectGrove(GroveListRecyclerAdapter adapter);

        public Boolean pinSetIsTrue();

        public int getSetPin();
    }

    public static class UiStateControl implements UiSet {

        private Boolean pin_set_statu;
        SparseBooleanArray nodePinActivated;
        SparseBooleanArray nodePinSelected;


        View v;
        ImageButton pin1View, pin2View, pin3View, pin4View, pin5View, pin6View;
        RecyclerView mGroveListView;
        Toolbar mToolbarAction;

        public UiStateControl(View view) {
            pin_set_statu = false;
            nodePinActivated = new SparseBooleanArray();
            nodePinSelected = new SparseBooleanArray();

            this.v = view;
            pin1View = (ImageButton) view.findViewById(R.id.grove_1);
            pin2View = (ImageButton) view.findViewById(R.id.grove_2);
            pin3View = (ImageButton) view.findViewById(R.id.grove_3);
            pin4View = (ImageButton) view.findViewById(R.id.grove_4);
            pin5View = (ImageButton) view.findViewById(R.id.grove_5);
            pin6View = (ImageButton) view.findViewById(R.id.grove_6);

            mGroveListView = (RecyclerView) view.findViewById(R.id.grove_list);
            mToolbarAction = (Toolbar) view.findViewById(R.id.toolbar_bottom);
        }

        @Override
        public void activatedPin(int pin) {
            if (nodePinSelected.get(pin, false)) {
                Snackbar.make(v, "Todo:node fill", Snackbar.LENGTH_SHORT).show();
                pin_set_statu = true;
                nodePinActivated.clear();
                nodePinActivated.put(pin, true);
                pin1View.setActivated(nodePinActivated.get(1, false));
                pin2View.setActivated(nodePinActivated.get(2, false));
                pin3View.setActivated(nodePinActivated.get(3, false));
                pin4View.setActivated(nodePinActivated.get(4, false));
                pin5View.setActivated(nodePinActivated.get(5, false));
                pin6View.setActivated(nodePinActivated.get(6, false));

                //todo, sroll to grove

            } else {
                pin_set_statu = true;
                nodePinActivated.clear();
                nodePinActivated.put(pin, true);
                pin1View.setActivated(nodePinActivated.get(1, false));
                pin2View.setActivated(nodePinActivated.get(2, false));
                pin3View.setActivated(nodePinActivated.get(3, false));
                pin4View.setActivated(nodePinActivated.get(4, false));
                pin5View.setActivated(nodePinActivated.get(5, false));
                pin6View.setActivated(nodePinActivated.get(6, false));

                //todo, select recyclcerview status
            }
        }

        @Override
        public void activatedClear() {
            pin_set_statu = false;
            nodePinActivated.clear();
            pin1View.setActivated(nodePinActivated.get(1, false));
            pin2View.setActivated(nodePinActivated.get(2, false));
            pin3View.setActivated(nodePinActivated.get(3, false));
            pin4View.setActivated(nodePinActivated.get(4, false));
            pin5View.setActivated(nodePinActivated.get(5, false));
            pin6View.setActivated(nodePinActivated.get(6, false));

            //Todo, clear grove recycleview selected state
        }


        @Override
        public void selectedPin() {
            pin_set_statu = false;
            int pin = nodePinActivated.keyAt(0);
            nodePinSelected.put(pin, true);
            pin1View.setSelected(nodePinSelected.get(1, false));
            pin2View.setSelected(nodePinSelected.get(2, false));
            pin3View.setSelected(nodePinSelected.get(3, false));
            pin4View.setSelected(nodePinSelected.get(4, false));
            pin5View.setSelected(nodePinSelected.get(5, false));
            pin6View.setSelected(nodePinSelected.get(6, false));
        }

        @Override
        public void selectGrove(GroveListRecyclerAdapter adapter) {
            adapter.selectItem(mGroveListView.getChildAdapterPosition(v));
            mToolbarAction.setVisibility(View.VISIBLE);
        }

        @Override
        public Boolean pinSetIsTrue() {
            return pin_set_statu;
        }

        @Override
        public int getSetPin() {
            return 0;
        }


    }
}


