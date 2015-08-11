package cc.seeed.iot.ui_setnode;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
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
        implements GroveFilterRecyclerAdapter.MainViewHolder.MyItemClickListener,
        View.OnClickListener, View.OnLongClickListener {
    public Toolbar mToolbar;
    public Toolbar mToolbarAction;
    ArrayList<Node> nodes;
    Node node;
    User user;

    static View.OnClickListener mainOnClickListener; //Todo, no static
    static View.OnLongClickListener mainOnLongClickListener; //Todo, no static

    RecyclerView mGroveListView;
    GroveListRecyclerAdapter mGroveListAdapter;

    RecyclerView mGroveTypeListView;
    GroveFilterRecyclerAdapter mGroveTypeListAdapter;
    private ArrayList<GroverDriver> mGroveDrivers;

    ImageButton mCorrectView;
    ImageButton mCancelView;
    TextView mGroveNameView;

    SparseBooleanArray nodePinSelector;
    //    Map<Integer, NodePinConfig> nodePinConfigs;
    NodeConfigModel nodeConfigModel;

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
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setButton(ProgressDialog.BUTTON_POSITIVE,
                "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
//        mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);

        mainOnClickListener = new MainOnClickListener(this);
        mainOnLongClickListener = new MainOnClickListener(this);

        nodePinSelector = new SparseBooleanArray();
//        nodePinConfigsInit();
        nodeConfigModel = new NodeConfigModel();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mToolbarAction = (Toolbar) findViewById(R.id.toolbar_bottom);
        mToolbarAction.setVisibility(View.GONE);
        mCorrectView = (ImageButton) findViewById(R.id.ib_correct);
        mCancelView = (ImageButton) findViewById(R.id.ib_cancel);
        mGroveNameView = (TextView) findViewById(R.id.ib_name);
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

        pin1View.setOnLongClickListener(this);
        pin2View.setOnLongClickListener(this);
        pin3View.setOnLongClickListener(this);
        pin4View.setOnLongClickListener(this);
        pin5View.setOnLongClickListener(this);
        pin6View.setOnLongClickListener(this);

        nodes = ((MyApplication) SetupIotNodeActivity.this.getApplication()).getNodes();
        int position = getIntent().getIntExtra("position", 1);
        node = new Node();
        node = nodes.get(position);
//        Snackbar.make(mToolbar, "Here's a " + node.name, Snackbar.LENGTH_LONG).show();
        getSupportActionBar().setTitle(node.name);

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

//    private void nodePinConfigsInit() {
//        NodePinConfig n = new NodePinConfig();
    //Todo need init?
//    }

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
                return true;
//            String yaml = "" +
//                    "Grove_Example1:\r\n" +
//                    "  name: Grove_Example\r\n" +
//                    "  construct_arg_list:\r\n" +
//                    "    pinsda: 4\r\n" +
//                    "    pinscl: 5\r\n";


//            nodeConfigModel.addPinNode(1, mGroveDrivers.get(0));
//            nodeConfigModel.addPinNode(2, mGroveDrivers.get(0));
//            nodeConfigModel.addPinNode(3, mGroveDrivers.get(2));

            String yaml = nodeConfigModel.getConfigYaml();
            Log.e("iot", "yaml:" + yaml);
            if (yaml.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Forger add grove?");
                builder.setTitle("Tip");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create().show();
                return true;
            }
            String Base64Yaml = Base64.encodeToString(yaml.getBytes(), Base64.DEFAULT);
            updateNode(node.node_key, Base64Yaml);


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                nodeConfigModel.removePinNode(1);
                uiStateControl.removeSelectedPin(1);
                break;
            case 2:
                nodeConfigModel.removePinNode(2);
                uiStateControl.removeSelectedPin(2);
                break;
            case 3:
                nodeConfigModel.removePinNode(3);
                uiStateControl.removeSelectedPin(3);
                break;
            case 4:
                nodeConfigModel.removePinNode(4);
                uiStateControl.removeSelectedPin(4);
                break;
            case 5:
                nodeConfigModel.removePinNode(5);
                uiStateControl.removeSelectedPin(5);
                break;
            case 6:
                nodeConfigModel.removePinNode(6);
                uiStateControl.removeSelectedPin(6);
                break;
        }

        return super.onContextItemSelected(item);
    }

    private void updateNode(final String node_key, String base64Yaml) {
        mProgressDialog.show();
        mProgressDialog.setMessage("Ready to ota...");
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
                    mProgressDialog.setMessage("Error:" + otaStatusResponse.msg);
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

        if (uiStateControl.getSetPin() == 1 || uiStateControl.getSetPin() == 2 || uiStateControl.getSetPin() == 3) {
            ArrayList<GroverDriver> inputGrovesGpio = new ArrayList<GroverDriver>();
            ArrayList<GroverDriver> outputGrovesGpio = new ArrayList<GroverDriver>();

            ArrayList<GroverDriver> groverGPIO =
                    new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.GPIO);

            for (GroverDriver g : groverGPIO) {
                if (!g.Inputs.isEmpty()) {
                    inputGrovesGpio.add(g);
                }
                if (!g.Outputs.isEmpty())
                    outputGrovesGpio.add(g);
            }

            if (groveType.equals("All")) {
                updateGroveListAdapter(groverGPIO);
            } else if (groveType.equals("Input")) {
                updateGroveListAdapter(inputGrovesGpio);
            } else if (groveType.equals("Output")) {
                updateGroveListAdapter(outputGrovesGpio);
            }
        } else if (uiStateControl.getSetPin() == 4) {
            ArrayList<GroverDriver> inputGrovesAnalog = new ArrayList<GroverDriver>();
            ArrayList<GroverDriver> outputGrovesAnalog = new ArrayList<GroverDriver>();

            ArrayList<GroverDriver> groverGPIO =
                    new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.ANALOG);

            for (GroverDriver g : groverGPIO) {
                if (!g.Inputs.isEmpty()) {
                    inputGrovesAnalog.add(g);
                }
                if (!g.Outputs.isEmpty())
                    outputGrovesAnalog.add(g);
            }

            if (groveType.equals("All")) {
                updateGroveListAdapter(groverGPIO);
            } else if (groveType.equals("Input")) {
                updateGroveListAdapter(inputGrovesAnalog);
            } else if (groveType.equals("Output")) {
                updateGroveListAdapter(outputGrovesAnalog);
            }
        } else if (uiStateControl.getSetPin() == 5) {
            ArrayList<GroverDriver> inputGrovesUart = new ArrayList<GroverDriver>();
            ArrayList<GroverDriver> outputGrovesUart = new ArrayList<GroverDriver>();

            ArrayList<GroverDriver> groverGPIO =
                    new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.UART);

            for (GroverDriver g : groverGPIO) {
                if (!g.Inputs.isEmpty()) {
                    inputGrovesUart.add(g);
                }
                if (!g.Outputs.isEmpty())
                    outputGrovesUart.add(g);
            }

            if (groveType.equals("All")) {
                updateGroveListAdapter(groverGPIO);
            } else if (groveType.equals("Input")) {
                updateGroveListAdapter(inputGrovesUart);
            } else if (groveType.equals("Output")) {
                updateGroveListAdapter(outputGrovesUart);
            }
        } else if (uiStateControl.getSetPin() == 6) {
            ArrayList<GroverDriver> inputGrovesI2c = new ArrayList<GroverDriver>();
            ArrayList<GroverDriver> outputGrovesI2c = new ArrayList<GroverDriver>();

            ArrayList<GroverDriver> groverGPIO =
                    new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.I2C);

            for (GroverDriver g : groverGPIO) {
                if (!g.Inputs.isEmpty()) {
                    inputGrovesI2c.add(g);
                }
                if (!g.Outputs.isEmpty())
                    outputGrovesI2c.add(g);
            }

            if (groveType.equals("All")) {
                updateGroveListAdapter(groverGPIO);
            } else if (groveType.equals("Input")) {
                updateGroveListAdapter(inputGrovesI2c);
            } else if (groveType.equals("Output")) {
                updateGroveListAdapter(outputGrovesI2c);
            }

        } else {
            if (groveType.equals("All")) {
                updateGroveListAdapter(mGroveDrivers);
            } else if (groveType.equals("Input")) {
                updateGroveListAdapter(inputGroves);
            } else if (groveType.equals("Output")) {
                updateGroveListAdapter(outputGroves);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_correct:
                int position = uiStateControl.getSetPin();
                nodeConfigModel.addPinNode(position, mGroveListAdapter.getSelectedItem());
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
                if (nodeConfigModel.getPinNode(1).selected) {
                    Snackbar.make(v, "Grove name:" + nodeConfigModel.getPinNode(1).groverDriver.GroveName,
                            Snackbar.LENGTH_LONG).show();
                }
                mGroveListAdapter.updateAll(
                        new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.GPIO));
                break;
            case R.id.grove_2:
                uiStateControl.activatedPin(2);
                mGroveListAdapter.updateAll(
                        new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.GPIO));
                if (nodeConfigModel.getPinNode(2).selected) {
                    Snackbar.make(v, "Grove name:" + nodeConfigModel.getPinNode(2).groverDriver.GroveName,
                            Snackbar.LENGTH_LONG).show();
                }
                break;
            case R.id.grove_3:
                uiStateControl.activatedPin(3);
                mGroveListAdapter.updateAll(
                        new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.GPIO));
                if (nodeConfigModel.getPinNode(3).selected) {
                    Snackbar.make(v, "Grove name:" + nodeConfigModel.getPinNode(3).groverDriver.GroveName,
                            Snackbar.LENGTH_LONG).show();
                }
                break;
            case R.id.grove_4:
                uiStateControl.activatedPin(4);
                mGroveListAdapter.updateAll(
                        new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.ANALOG));
                if (nodeConfigModel.getPinNode(4).selected) {
                    Snackbar.make(v, "Grove name:" + nodeConfigModel.getPinNode(4).groverDriver.GroveName,
                            Snackbar.LENGTH_LONG).show();
                }
                break;
            case R.id.grove_5:
                uiStateControl.activatedPin(5);
                mGroveListAdapter.updateAll(
                        new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.UART));
                if (nodeConfigModel.getPinNode(5).selected) {
                    Snackbar.make(v, "Grove name:" + nodeConfigModel.getPinNode(5).groverDriver.GroveName,
                            Snackbar.LENGTH_LONG).show();
                }
                break;
            case R.id.grove_6:
                uiStateControl.activatedPin(6);
                mGroveListAdapter.updateAll(
                        new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.I2C));
                if (nodeConfigModel.getPinNode(6).selected) {
                    Snackbar.make(v, "Grove name:" + nodeConfigModel.getPinNode(6).groverDriver.GroveName,
                            Snackbar.LENGTH_LONG).show();
                }
                break;
            case R.id.set_node:
                if (mToolbarAction.getVisibility() == View.GONE)
                    uiStateControl.activatedClear();
                break;
        }

    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.grove_1:
                if (uiStateControl.getSelectedPinStatus(1)) {
                    pin1View.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                        @Override
                        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                            menu.add(0, 1, 0, "Remove");
                        }
                    });
                }
                break;
            case R.id.grove_2:
                if (uiStateControl.getSelectedPinStatus(2)) {
                    pin2View.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                        @Override
                        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                            menu.add(0, 2, 0, "Remove");
                        }
                    });
                }
                break;
            case R.id.grove_3:
                if (uiStateControl.getSelectedPinStatus(3)) {
                    pin3View.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                        @Override
                        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                            menu.add(0, 3, 0, "Remove");
                        }
                    });
                }
                break;
            case R.id.grove_4:
                if (uiStateControl.getSelectedPinStatus(4)) {
                    pin4View.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                        @Override
                        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                            menu.add(0, 4, 0, "Remove");
                        }
                    });
                }
                break;
            case R.id.grove_5:
                if (uiStateControl.getSelectedPinStatus(5)) {
                    pin5View.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                        @Override
                        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                            menu.add(0, 5, 0, "Remove");
                        }
                    });
                }
                break;
            case R.id.grove_6:
                if (uiStateControl.getSelectedPinStatus(6)) {
                    pin6View.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                        @Override
                        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                            menu.add(0, 6, 0, "Remove");
                        }
                    });
                }
                break;
        }
        return false;
    }

    private class MainOnClickListener implements View.OnClickListener, View.OnLongClickListener {
        private final Context context;

        private MainOnClickListener(Context c) {
            this.context = c;
        }

        @Override
        public void onClick(View v) {
            if (uiStateControl.pinSetIsTrue()) {
                mGroveListAdapter.selectItem(mGroveListView.getChildAdapterPosition(v));
                mToolbarAction.setVisibility(View.VISIBLE);
                mGroveNameView.setText(mGroveListAdapter.getSelectedItem().GroveName);
            } else {
                Snackbar.make(v, "Todo:grove detail page", Snackbar.LENGTH_SHORT).show();
            }
//            uiStateControl.selectGrove(mGroveListAdapter);
        }

        @Override
        public boolean onLongClick(View v) {
            Snackbar.make(v, "Todo:grove detail page", Snackbar.LENGTH_SHORT).show();

            return false;
        }
    }

    public interface UiSet {
        public void activatedPin(int pin);

        public void activatedClear();

        public void selectedPin();

        public void removeSelectedPin(int pin);

        public void selectGrove(GroveListRecyclerAdapter adapter);

        public Boolean pinSetIsTrue();

        public int getSetPin();

        public boolean getSelectedPinStatus(int pin);
    }

    public static class UiStateControl implements UiSet {
        private Boolean pin_set_statu;
        private SparseBooleanArray nodePinActivated;
        private SparseBooleanArray nodePinSelected;

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
                pin_set_statu = true;
                nodePinActivated.clear();
                nodePinActivated.put(pin, true);
                pin1View.setActivated(nodePinActivated.get(1, false));
                pin2View.setActivated(nodePinActivated.get(2, false));
                pin3View.setActivated(nodePinActivated.get(3, false));
                pin4View.setActivated(nodePinActivated.get(4, false));
                pin5View.setActivated(nodePinActivated.get(5, false));
                pin6View.setActivated(nodePinActivated.get(6, false));

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
            Log.e("iot", "pin:" + nodePinActivated.keyAt(0));
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
        public void removeSelectedPin(int pin) {
            nodePinSelected.put(pin, false);
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
            if (pin_set_statu) {
                int pin = nodePinActivated.keyAt(0);
                return pin;
            } else {
                return -1;
            }
        }

        @Override
        public boolean getSelectedPinStatus(int pin) {
            boolean status = nodePinSelected.get(pin, false);
            return status;
        }
    }
}


