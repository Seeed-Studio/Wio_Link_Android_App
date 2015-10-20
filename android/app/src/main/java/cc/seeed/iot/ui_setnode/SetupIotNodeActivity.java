package cc.seeed.iot.ui_setnode;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.datastruct.Constant;
import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.ui_setnode.View.GrovePinsView;
import cc.seeed.iot.ui_setnode.model.NodeConfigHelper;
import cc.seeed.iot.ui_setnode.model.PinConfig;
import cc.seeed.iot.ui_setnode.model.PinConfigDBHelper;
import cc.seeed.iot.util.DBHelper;
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
        View.OnClickListener, View.OnDragListener, View.OnLongClickListener {

    private static final String TAG = "SetupIotNodeActivity";
    public Toolbar mToolbar;
    public Toolbar mToolbarAction;
    Node node;
    User user;
    List<PinConfig> pinConfigs = new ArrayList<>();

    static View.OnClickListener mainOnClickListener; //Todo, no static
    static View.OnLongClickListener mainOnLongClickListener; //Todo, no static

    RecyclerView mGroveListView;
    GroveListRecyclerAdapter mGroveListAdapter;

    RecyclerView mGroveTypeListView;
    GroveFilterRecyclerAdapter mGroveTypeListAdapter;
    private List<GroverDriver> mGroveDrivers;

    ImageButton mCorrectView;
    ImageButton mCancelView;
    TextView mGroveNameView;

    SparseBooleanArray nodePinSelector;
    //    Map<Integer, NodePinConfig> nodePinConfigs;
    NodeConfigHelper nodeConfigModel;

    View mSetNodeLayout;
    GrovePinsView grovePinsView;

//    UiStateControl uiStateControl;

    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_iot_node);
        View view = (View) findViewById(R.id.setup_iot_node);
//        uiStateControl = new UiStateControl(view);
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

        mGroveDrivers = DBHelper.getGrovesAll();

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


        String node_sn = getIntent().getStringExtra("node_sn");
        node = DBHelper.getNodes(node_sn).get(0);

        nodeConfigModel = new NodeConfigHelper(node.node_sn);

        grovePinsView = new GrovePinsView(view, node);
        for (ImageView pinView : grovePinsView.pinViews) {
            pinView.setOnDragListener(this);
            pinView.setOnClickListener(this);
            pinView.setOnLongClickListener(this);
        }

//        pin1View.setOnLongClickListener(this);
//        pin2View.setOnLongClickListener(this);
//        pin3View.setOnLongClickListener(this);
//        pin4View.setOnLongClickListener(this);
//        pin5View.setOnLongClickListener(this);
//        pin6View.setOnLongClickListener(this);

        pinConfigs = PinConfigDBHelper.getPinConfigs(node.node_sn);
        Log.e(TAG, "ori_pinconfig" + pinConfigs.toString());

//        Snackbar.make(mToolbar, "Here's a " + node.name, Snackbar.LENGTH_LONG).show();
        getSupportActionBar().setTitle(node.name);

        user = ((MyApplication) SetupIotNodeActivity.this.getApplication()).getUser();

        mGroveListView = (RecyclerView) findViewById(R.id.grove_list);
        if (mGroveListView != null) {
            mGroveListView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mGroveListView.setLayoutManager(layoutManager);
            mGroveListAdapter = new GroveListRecyclerAdapter(mGroveDrivers);
            mGroveListView.setAdapter(mGroveListAdapter);
        }

        mGroveTypeListView = (RecyclerView) findViewById(R.id.grove_selector);
        if (mGroveTypeListView != null) {
            mGroveTypeListView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mGroveTypeListView.setLayoutManager(layoutManager);
            setupGroveSelectorAdapter();
        }
    }

//    private void nodePinConfigsInit() {
//        NodePinConfig n = new NodePinConfig();
    //Todo need init?
//    }

    private void setupGroveSelectorAdapter() {
        mGroveTypeListAdapter = new GroveFilterRecyclerAdapter(Constant.groveTypes);
        mGroveTypeListAdapter.setOnItemClickListener(this);
        mGroveTypeListView.setAdapter(mGroveTypeListAdapter);
    }

    private void updateGroveListAdapter(List<GroverDriver> groverDrivers) {
        mGroveListAdapter.updateAll(groverDrivers);
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
//                    "GroveMultiChannelGas:\r\n" +
//                    "  name: Grove-Multichannel Gas Sensor\r\n" +
//                    "  construct_arg_list:\r\n" +
//                    "    pinsda: 4\r\n" +
//                    "    pinscl: 5\r\n";

            String yaml = NodeConfigHelper.getConfigYaml(pinConfigs);
            Log.e("iot", "yaml:" + yaml);
            if (yaml.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Forger add grove?");
                builder.setTitle("Tip");
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
//                nodeConfigModel.removePinNode(1);
//                uiStateControl.removeSelectedPin(1);
                break;
            case 2:
//                nodeConfigModel.removePinNode(2);
//                uiStateControl.removeSelectedPin(2);
                break;
            case 3:
//                nodeConfigModel.removePinNode(3);
//                uiStateControl.removeSelectedPin(3);
                break;
            case 4:
//                nodeConfigModel.removePinNode(4);
//                uiStateControl.removeSelectedPin(4);
                break;
            case 5:
//                nodeConfigModel.removePinNode(5);
//                uiStateControl.removeSelectedPin(5);
                break;
            case 6:
//                nodeConfigModel.removePinNode(6);
//                uiStateControl.removeSelectedPin(6);
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
                                mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
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
    public void onItemClick(View view, int position) {
        String groveType = Constant.groveTypes[position];

        ArrayList<GroverDriver> inputGroves = new ArrayList<GroverDriver>();
        ArrayList<GroverDriver> outputGroves = new ArrayList<GroverDriver>();


        if (mGroveDrivers == null)
            return;

//        for (GroverDriver g : mGroveDrivers) {
//            if (!g.Inputs.isEmpty()) {
//                outputGroves.add(g);
//            }
//            if (!g.Outputs.isEmpty())
//                inputGroves.add(g);
//        }
//
//        mGroveTypeListAdapter.updateSelection(position);
//
//        if (groveType.equals("All")) {
//            updateGroveListAdapter(mGroveDrivers);
//        } else if (groveType.equals("Input")) {
//            updateGroveListAdapter(inputGroves);
//        } else if (groveType.equals("Output")) {
//            updateGroveListAdapter(outputGroves);
//        }

    }

//    @Override
//    public void onItemClick(View view, int position) {
//        String groveType = Constant.groveTypes[position];
//
//        ArrayList<GroverDriver> inputGroves = new ArrayList<GroverDriver>();
//        ArrayList<GroverDriver> outputGroves = new ArrayList<GroverDriver>();
//
//        if (mGroveDrivers == null)
//            return;
//
//        for (GroverDriver g : mGroveDrivers) {
//            if (!g.Inputs.isEmpty()) {
//                outputGroves.add(g);
//            }
//            if (!g.Outputs.isEmpty())
//                inputGroves.add(g);
//        }
//
//        mGroveTypeListAdapter.updateSelection(position);
//
//        if (uiStateControl.getSetPin() == 1 || uiStateControl.getSetPin() == 2 || uiStateControl.getSetPin() == 3) {
//            ArrayList<GroverDriver> inputGrovesGpio = new ArrayList<GroverDriver>();
//            ArrayList<GroverDriver> outputGrovesGpio = new ArrayList<GroverDriver>();
//
//            ArrayList<GroverDriver> groverGPIO =
//                    new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.GPIO);
//
//            for (GroverDriver g : groverGPIO) {
//                if (!g.Outputs.isEmpty()) { //grove's output is node's input
//                    inputGrovesGpio.add(g);
//                }
//                if (!g.Inputs.isEmpty())
//                    outputGrovesGpio.add(g);
//            }
//
//            if (groveType.equals("All")) {
//                updateGroveListAdapter(groverGPIO);
//            } else if (groveType.equals("Input")) {
//                updateGroveListAdapter(inputGrovesGpio);
//            } else if (groveType.equals("Output")) {
//                updateGroveListAdapter(outputGrovesGpio);
//            }
//        } else if (uiStateControl.getSetPin() == 4) {
//            ArrayList<GroverDriver> inputGrovesAnalog = new ArrayList<GroverDriver>();
//            ArrayList<GroverDriver> outputGrovesAnalog = new ArrayList<GroverDriver>();
//
//            ArrayList<GroverDriver> groverGPIO =
//                    new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.ANALOG);
//
//            for (GroverDriver g : groverGPIO) {
//                if (!g.Outputs.isEmpty()) {
//                    inputGrovesAnalog.add(g);
//                }
//                if (!g.Inputs.isEmpty())
//                    outputGrovesAnalog.add(g);
//            }
//
//            if (groveType.equals("All")) {
//                updateGroveListAdapter(groverGPIO);
//            } else if (groveType.equals("Input")) {
//                updateGroveListAdapter(inputGrovesAnalog);
//            } else if (groveType.equals("Output")) {
//                updateGroveListAdapter(outputGrovesAnalog);
//            }
//        } else if (uiStateControl.getSetPin() == 5) {
//            ArrayList<GroverDriver> inputGrovesUart = new ArrayList<GroverDriver>();
//            ArrayList<GroverDriver> outputGrovesUart = new ArrayList<GroverDriver>();
//
//            ArrayList<GroverDriver> groverGPIO =
//                    new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.UART);
//
//            for (GroverDriver g : groverGPIO) {
//                if (!g.Outputs.isEmpty()) {
//                    inputGrovesUart.add(g);
//                }
//                if (!g.Inputs.isEmpty())
//                    outputGrovesUart.add(g);
//            }
//
//            if (groveType.equals("All")) {
//                updateGroveListAdapter(groverGPIO);
//            } else if (groveType.equals("Input")) {
//                updateGroveListAdapter(inputGrovesUart);
//            } else if (groveType.equals("Output")) {
//                updateGroveListAdapter(outputGrovesUart);
//            }
//        } else if (uiStateControl.getSetPin() == 6) {
//            ArrayList<GroverDriver> inputGrovesI2c = new ArrayList<GroverDriver>();
//            ArrayList<GroverDriver> outputGrovesI2c = new ArrayList<GroverDriver>();
//
//            ArrayList<GroverDriver> groverGPIO =
//                    new GroveFliter(mGroveDrivers).getGroveFilterInterface(GroveFliter.I2C);
//
//            for (GroverDriver g : groverGPIO) {
//                if (!g.Outputs.isEmpty()) {
//                    inputGrovesI2c.add(g);
//                }
//                if (!g.Inputs.isEmpty())
//                    outputGrovesI2c.add(g);
//            }
//
//            if (groveType.equals("All")) {
//                updateGroveListAdapter(groverGPIO);
//            } else if (groveType.equals("Input")) {
//                updateGroveListAdapter(inputGrovesI2c);
//            } else if (groveType.equals("Output")) {
//                updateGroveListAdapter(outputGrovesI2c);
//            }
//
//        } else {
//            if (groveType.equals("All")) {
//                updateGroveListAdapter(mGroveDrivers);
//            } else if (groveType.equals("Input")) {
//                updateGroveListAdapter(inputGroves);
//            } else if (groveType.equals("Output")) {
//                updateGroveListAdapter(outputGroves);
//            }
//        }
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.grove_1:
                Snackbar.make(v, "Grove name:" + pinDeviceCount(1), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.grove_2:
                Snackbar.make(v, "Grove name:" + pinDeviceCount(2), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.grove_3:
                Snackbar.make(v, "Grove name:" + pinDeviceCount(3), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.grove_4:
                Snackbar.make(v, "Grove name:" + pinDeviceCount(4), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.grove_5:
                Snackbar.make(v, "Grove name:" + pinDeviceCount(5), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.grove_6:
                Snackbar.make(v, "Grove name:" + pinDeviceCount(6), Snackbar.LENGTH_LONG).show();
                if (pinDeviceCount(6) == 0) {
                    ;
                } else if (pinDeviceCount(6) == 1) {
                    ;
                } else if (pinDeviceCount(6) > 1) {
                    openI2cDeviceFolder();
                }
                break;
//            case R.id.set_node:
//                if (mToolbarAction.getVisibility() == View.GONE)
//                    uiStateControl.activatedClear();
//                break;
        }

    }

    private int pinDeviceCount(int position) {

        SparseIntArray sparseIntArray = new SparseIntArray();

        for (PinConfig pinConfig : pinConfigs) {
            int count = sparseIntArray.get(pinConfig.position, 0);
            count = count + 1;
            sparseIntArray.append(pinConfig.position, count);
        }
        return sparseIntArray.get(position, 0);
    }

    private void openI2cDeviceFolder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("I2C Device List");
//        builder.setView();
        builder.create().show();
    }

//    @Override
//    public boolean onLongClick(View v) {
//        switch (v.getId()) {
//            case R.id.grove_1:
//                if (uiStateControl.getSelectedPinStatus(1)) {
//                    pin1View.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//                        @Override
//                        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//                            menu.add(0, 1, 0, "Remove");
//                        }
//                    });
//                }
//                break;
//            case R.id.grove_2:
//                if (uiStateControl.getSelectedPinStatus(2)) {
//                    pin2View.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//                        @Override
//                        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//                            menu.add(0, 2, 0, "Remove");
//                        }
//                    });
//                }
//                break;
//            case R.id.grove_3:
//                if (uiStateControl.getSelectedPinStatus(3)) {
//                    pin3View.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//                        @Override
//                        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//                            menu.add(0, 3, 0, "Remove");
//                        }
//                    });
//                }
//                break;
//            case R.id.grove_4:
//                if (uiStateControl.getSelectedPinStatus(4)) {
//                    pin4View.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//                        @Override
//                        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//                            menu.add(0, 4, 0, "Remove");
//                        }
//                    });
//                }
//                break;
//            case R.id.grove_5:
//                if (uiStateControl.getSelectedPinStatus(5)) {
//                    pin5View.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//                        @Override
//                        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//                            menu.add(0, 5, 0, "Remove");
//                        }
//                    });
//                }
//                break;
//            case R.id.grove_6:
//                if (uiStateControl.getSelectedPinStatus(6)) {
//                    pin6View.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//                        @Override
//                        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//                            menu.add(0, 6, 0, "Remove");
//                        }
//                    });
//                }
//                break;
//        }
//        return false;
//    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        int action = event.getAction();
        Log.e("iot", "action" + action);
        GrovePinsView.Tag tag = (GrovePinsView.Tag) v.getTag();
        String interfaceType = tag.interfaceType;
        GroverDriver groverDriver = (GroverDriver) event.getLocalState();
        if (!interfaceType.equals(groverDriver.InterfaceType)) {
            Log.e("iot", groverDriver.InterfaceType);
            return false;
        }

        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:
                v.setActivated(true);
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                Log.e(TAG, "entered");
                v.setActivated(false);
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                v.setActivated(true);
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                v.setActivated(false);
                break;
            case DragEvent.ACTION_DROP:
                Log.e(TAG, "Drop " + groverDriver.GroveName);
                UrlImageViewHelper.setUrlDrawable((ImageView) v, groverDriver.ImageURL, R.drawable.grove_cold,
                        UrlImageViewHelper.CACHE_DURATION_INFINITE);
                PinConfig pinConfig = new PinConfig();
                pinConfig.position = ((GrovePinsView.Tag) v.getTag()).position;
                pinConfig.selected = true;
                pinConfig.grove_id = groverDriver.ID;
                pinConfig.node_sn = node.node_sn;

                if (pinConfig.position != 6) {
                    Log.e(TAG, "remove config");
                    Boolean status = false;
                    PinConfig dup_pinConfig = new PinConfig();
                    for (PinConfig p : pinConfigs)
                        if (p.position == pinConfig.position) {
                            Log.e(TAG, "remove config 2");
                            status = true;
                            dup_pinConfig = p;
                        }
                    if (status)
                        pinConfigs.remove(dup_pinConfig);
                }

                String groveInstanceName;
                List<String> groveInstanceNames = new ArrayList<>();
                for (PinConfig p : pinConfigs) {
                    groveInstanceNames.add(p.groveInstanceName);
                }
                groveInstanceName = groverDriver.ClassName;
                int i = 1;
                while (true) {
                    if (groveInstanceNames.contains(groveInstanceName)) {
                        groveInstanceName = groveInstanceName.split("_0")[0] + "_0" + Integer.toString(i);
                    } else {
                        groveInstanceNames.add(groveInstanceName);
                        break;
                    }
                    i++;
                }
                pinConfig.groveInstanceName = groveInstanceName;

                pinConfigs.add(pinConfig);
                Log.e(TAG, "drag pinConfigs " + pinConfigs);

                //todo change i2c number textview
                break;

        }

        return true;
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.grove_1:
                Snackbar.make(v, "Grove name:" + pinDeviceCount(1), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.grove_2:
                Snackbar.make(v, "Grove name:" + pinDeviceCount(2), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.grove_3:
                Snackbar.make(v, "Grove name:" + pinDeviceCount(3), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.grove_4:
                Snackbar.make(v, "Grove name:" + pinDeviceCount(4), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.grove_5:
                Snackbar.make(v, "Grove name:" + pinDeviceCount(5), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.grove_6:
                Snackbar.make(v, "Grove name:" + pinDeviceCount(6), Snackbar.LENGTH_LONG).show();
                if (pinDeviceCount(6) == 0) {
                    ;
                } else if (pinDeviceCount(6) == 1) {
                    ;
                } else if (pinDeviceCount(6) > 1) {
                    openI2cDeviceFolder();
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
//            if (uiStateControl.pinSetIsTrue()) {
//                mGroveListAdapter.selectItem(mGroveListView.getChildAdapterPosition(v));
//                mToolbarAction.setVisibility(View.VISIBLE);
//                mGroveNameView.setText(mGroveListAdapter.getSelectedItem().GroveName);
//            } else {
//                Snackbar.make(v, "Todo:grove detail page", Snackbar.LENGTH_SHORT).show();
//            }
//            uiStateControl.selectGrove(mGroveListAdapter);
        }

        @Override
        public boolean onLongClick(View v) {
//            Snackbar.make(v, "Todo:grove detail page", Snackbar.LENGTH_SHORT).show();

            String label = "grove_interface";
            String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
            ClipDescription clipDescription = new ClipDescription(label, mimeTypes);
            ClipData.Item item = new ClipData.Item("drag grove");
            ClipData clipData = new ClipData(clipDescription, item);
            View.DragShadowBuilder shadowBuiler = new View.DragShadowBuilder(v);

            mGroveListAdapter.selectItem(mGroveListView.getChildAdapterPosition(v));
            GroverDriver grove = mGroveListAdapter.getSelectedItem();

            v.startDrag(clipData, shadowBuiler, grove, 0);
            return true;
        }
    }

//    public interface UiSet {
//        public void activatedPin(int pin);
//
//        public void activatedClear();
//
//        public void selectedPin();
//
//        public void removeSelectedPin(int pin);
//
//        public void selectGrove(GroveListRecyclerAdapter adapter);
//
//        public Boolean pinSetIsTrue();
//
//        public int getSetPin();
//
//        public boolean getSelectedPinStatus(int pin);
//    }
//
//    public static class UiStateControl implements UiSet {
//        private Boolean pin_set_statu;
//        private SparseBooleanArray nodePinActivated;
//        private SparseBooleanArray nodePinSelected;
//
//        View v;
//        ImageButton pin1View, pin2View, pin3View, pin4View, pin5View, pin6View;
//        RecyclerView mGroveListView;
//        Toolbar mToolbarAction;
//
//
//        public UiStateControl(View view) {
//            pin_set_statu = false;
//            nodePinActivated = new SparseBooleanArray();
//            nodePinSelected = new SparseBooleanArray();
//
//            this.v = view;
//            pin1View = (ImageButton) view.findViewById(R.id.grove_1);
//            pin2View = (ImageButton) view.findViewById(R.id.grove_2);
//            pin3View = (ImageButton) view.findViewById(R.id.grove_3);
//            pin4View = (ImageButton) view.findViewById(R.id.grove_4);
//            pin5View = (ImageButton) view.findViewById(R.id.grove_5);
//            pin6View = (ImageButton) view.findViewById(R.id.grove_6);
//
//            mGroveListView = (RecyclerView) view.findViewById(R.id.grove_list);
//            mToolbarAction = (Toolbar) view.findViewById(R.id.toolbar_bottom);
//        }
//
//        @Override
//        public void activatedPin(int pin) {
//            if (nodePinSelected.get(pin, false)) {
//                pin_set_statu = true;
//                nodePinActivated.clear();
//                nodePinActivated.put(pin, true);
//                pin1View.setActivated(nodePinActivated.get(1, false));
//                pin2View.setActivated(nodePinActivated.get(2, false));
//                pin3View.setActivated(nodePinActivated.get(3, false));
//                pin4View.setActivated(nodePinActivated.get(4, false));
//                pin5View.setActivated(nodePinActivated.get(5, false));
//                pin6View.setActivated(nodePinActivated.get(6, false));
//
//            } else {
//                pin_set_statu = true;
//                nodePinActivated.clear();
//                nodePinActivated.put(pin, true);
//                pin1View.setActivated(nodePinActivated.get(1, false));
//                pin2View.setActivated(nodePinActivated.get(2, false));
//                pin3View.setActivated(nodePinActivated.get(3, false));
//                pin4View.setActivated(nodePinActivated.get(4, false));
//                pin5View.setActivated(nodePinActivated.get(5, false));
//                pin6View.setActivated(nodePinActivated.get(6, false));
//
//                //todo, select recyclcerview status
//            }
//        }
//
//        @Override
//        public void activatedClear() {
//            pin_set_statu = false;
//            nodePinActivated.clear();
//            Log.e("iot", "pin:" + nodePinActivated.keyAt(0));
//            pin1View.setActivated(nodePinActivated.get(1, false));
//            pin2View.setActivated(nodePinActivated.get(2, false));
//            pin3View.setActivated(nodePinActivated.get(3, false));
//            pin4View.setActivated(nodePinActivated.get(4, false));
//            pin5View.setActivated(nodePinActivated.get(5, false));
//            pin6View.setActivated(nodePinActivated.get(6, false));
//
//            //Todo, clear grove recycleview selected state
//        }
//
//
//        @Override
//        public void selectedPin() {
//            pin_set_statu = false;
//            int pin = nodePinActivated.keyAt(0);
//            nodePinSelected.put(pin, true);
//            pin1View.setSelected(nodePinSelected.get(1, false));
//            pin2View.setSelected(nodePinSelected.get(2, false));
//            pin3View.setSelected(nodePinSelected.get(3, false));
//            pin4View.setSelected(nodePinSelected.get(4, false));
//            pin5View.setSelected(nodePinSelected.get(5, false));
//            pin6View.setSelected(nodePinSelected.get(6, false));
//
//
//        }
//
//        @Override
//        public void removeSelectedPin(int pin) {
//            nodePinSelected.put(pin, false);
//            pin1View.setSelected(nodePinSelected.get(1, false));
//            pin2View.setSelected(nodePinSelected.get(2, false));
//            pin3View.setSelected(nodePinSelected.get(3, false));
//            pin4View.setSelected(nodePinSelected.get(4, false));
//            pin5View.setSelected(nodePinSelected.get(5, false));
//            pin6View.setSelected(nodePinSelected.get(6, false));
//        }
//
//        @Override
//        public void selectGrove(GroveListRecyclerAdapter adapter) {
//            adapter.selectItem(mGroveListView.getChildAdapterPosition(v));
//            mToolbarAction.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        public Boolean pinSetIsTrue() {
//            return pin_set_statu;
//        }
//
//        @Override
//        public int getSetPin() {
//            if (pin_set_statu) {
//                int pin = nodePinActivated.keyAt(0);
//                return pin;
//            } else {
//                return -1;
//            }
//        }
//
//        @Override
//        public boolean getSelectedPinStatus(int pin) {
//            boolean status = nodePinSelected.get(pin, false);
//            return status;
//        }
//    }
}


