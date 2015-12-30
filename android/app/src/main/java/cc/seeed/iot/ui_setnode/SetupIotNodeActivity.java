package cc.seeed.iot.ui_setnode;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.datastruct.Constant;
import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.ui_main.NodeApiActivity;
import cc.seeed.iot.ui_setnode.View.GrovePinsView;
import cc.seeed.iot.ui_setnode.model.NodeConfigHelper;
import cc.seeed.iot.ui_setnode.model.PinConfig;
import cc.seeed.iot.ui_setnode.model.PinConfigDBHelper;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.GroveDriverListResponse;
import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.NodeJson;
import cc.seeed.iot.webapi.model.OtaStatusResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SetupIotNodeActivity extends AppCompatActivity
        implements GroveFilterRecyclerAdapter.MainViewHolder.MyItemClickListener,
        View.OnClickListener, View.OnDragListener, View.OnLongClickListener {

    private static final String TAG = "SetupIotNodeActivity";
    public static final String GROVE_REMOVE = "grove/remove";
    public static final String GROVE_REMOVE_PIN6 = "grove/remove/6";
    public static final String GROVE_ADD = "grove/add";
    private static final int ADD_I2C_GROVE = 0x00;
    private static final int RMV_I2C_GROVE = 0x01;

    private static final int MESSAGE_UPDATE_DONE = 0x10;

    public Toolbar mToolbar;
    Node node;
    User user;
    List<PinConfig> pinConfigs = new ArrayList<>();

    static View.OnClickListener mainOnClickListener; //Todo, no static
    static View.OnLongClickListener mainOnLongClickListener; //Todo, no static
    static View.OnLongClickListener pin6OnLongClickListener; //Todo, no static

    RecyclerView mGroveI2cListView;
    GroveI2cListRecyclerAdapter mGroveI2cListAdapter;

    RecyclerView mGroveListView;
    GroveListRecyclerAdapter mGroveListAdapter;

    RecyclerView mGroveTypeListView;
    GroveFilterRecyclerAdapter mGroveTypeListAdapter;
    private List<GroverDriver> mGroveDrivers;

    SparseBooleanArray nodePinSelector;
    NodeConfigHelper nodeConfigModel;

    View mSetNodeLayout;
    GrovePinsView mGrovePinsView;
    ProgressDialog mProgressDialog;
    private ImageView mDragRemoveView;
    private TextView i2cDeviceNumView;


    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_iot_node);
        View view = (View) findViewById(R.id.setup_iot_node);
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
        pin6OnLongClickListener = new Pin6OnClickListener(this);

        nodePinSelector = new SparseBooleanArray();

        mGroveDrivers = DBHelper.getGrovesAll();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mDragRemoveView = (ImageView) findViewById(R.id.grove_remove);
        mDragRemoveView.setOnDragListener(this);

        mSetNodeLayout = (View) findViewById(R.id.set_node);
        mSetNodeLayout.setOnClickListener(this);


        String node_sn = getIntent().getStringExtra("node_sn");
        node = DBHelper.getNodes(node_sn).get(0);

        nodeConfigModel = new NodeConfigHelper(node.node_sn);

        mGrovePinsView = new GrovePinsView(view, node);
        for (ImageView pinView : mGrovePinsView.pinViews) {
            pinView.setOnDragListener(this);
            pinView.setOnClickListener(this);
            pinView.setOnLongClickListener(this);
        }


        pinConfigs = PinConfigDBHelper.getPinConfigs(node.node_sn);
//        Log.e(TAG, "ori_pinconfig" + pinConfigs.toString());

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

        mGroveI2cListView = (RecyclerView) findViewById(R.id.grove_i2c_list);
        if (mGroveI2cListView != null) {
            mGroveI2cListView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mGroveI2cListView.setLayoutManager(layoutManager);
            mGroveI2cListAdapter = new GroveI2cListRecyclerAdapter(pinConfigs);
            mGroveI2cListView.setAdapter(mGroveI2cListAdapter);
        }

        mGroveTypeListView = (RecyclerView) findViewById(R.id.grove_selector);
        if (mGroveTypeListView != null) {
            mGroveTypeListView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mGroveTypeListView.setLayoutManager(layoutManager);
            setupGroveSelectorAdapter();
        }

        i2cDeviceNumView = (TextView) view.findViewById(R.id.i2c_device_num);
        i2cDeviceNumViewDisplay();

        initData();
    }

    private void i2cDeviceNumViewDisplay() {
        if (pinDeviceCount(6) > 1) {
            i2cDeviceNumView.setVisibility(View.VISIBLE);
            i2cDeviceNumView.setText("+" + String.valueOf(pinDeviceCount(6) - 1));
        } else {
            i2cDeviceNumView.setVisibility(View.GONE);
        }
    }

    private void initData() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ADD_I2C_GROVE:
                        //if i2c list visible, dynamic add, move to end position
                        updateI2cGroveList();
                        scrollI2cGroveListToEnd();
                        //refresh number display
                        i2cDeviceNumViewDisplay();
                        break;
                    case RMV_I2C_GROVE:
                        //if i2c list visible, dynamic remove

                        if (pinDeviceCount(6) < 2)
                            mGroveI2cListView.setVisibility(View.INVISIBLE);
                        else
                            updateI2cGroveList();

                        //refresh number display
                        i2cDeviceNumViewDisplay();

                        //refresh pin6 image
                        mGrovePinsView.updatePin6(pinConfigs);
                        break;

                    case MESSAGE_UPDATE_DONE: {
                        String message = (String) msg.obj;
                        new AlertDialog.Builder(SetupIotNodeActivity.this)
                                .setTitle(R.string.update)
                                .setMessage(message)
                                .setPositiveButton(R.string.ok, null)
                                .show();
                    }
                    break;
                }
            }
        };
    }

    private void scrollI2cGroveListToEnd() {
        mGroveI2cListView.smoothScrollToPosition(mGroveI2cListAdapter.getItemCount() - 1);
    }

    private void updateI2cGroveList() {
        List<PinConfig> pin6Configs = new ArrayList<>();
        for (PinConfig p : pinConfigs) {
            if (p.position == 6)
                pin6Configs.add(p);
        }
        mGroveI2cListAdapter.updateAll(pin6Configs);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getGrovesData();
    }

    private void setupGroveSelectorAdapter() {
        mGroveTypeListAdapter = new GroveFilterRecyclerAdapter(Constant.groveTypes);
        mGroveTypeListAdapter.setOnItemClickListener(this);
        mGroveTypeListView.setAdapter(mGroveTypeListAdapter);
        mGroveTypeListAdapter.updateSelection(0);
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
        } else if (id == R.id.api) {
            Intent intent = new Intent(this, NodeApiActivity.class);
            intent.putExtra("node_sn", node.node_sn);
            startActivity(intent);
        } else if (id == R.id.update) {
            if (node.name == null)
                return true;

            NodeJson node_josn = NodeConfigHelper.getConfigJson(pinConfigs);
            Log.i(TAG, "node_json:\n" + new Gson().toJson(node_josn));
            if (node_josn.connections.isEmpty()) {
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
            updateNode(node.node_key, node_josn);
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

    private void updateNode(final String node_key, NodeJson node_json) {
        mProgressDialog.show();
        mProgressDialog.setMessage("Ready to ota...");
        mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);
        IotApi api = new IotApi();
        api.setAccessToken(node_key);
        final IotService iot = api.getService();
        iot.userDownload(node_json, new Callback<OtaStatusResponse>() {
            @Override
            public void success(OtaStatusResponse otaStatusResponse, Response response) {
                mProgressDialog.setMessage(otaStatusResponse.ota_msg);
                displayStatus(node_key);
            }

            @Override
            public void failure(RetrofitError error) {
                mProgressDialog.setMessage("Error:" + error.getLocalizedMessage());
                mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
            }
        });
    }

    private void displayStatus(final String node_key) {
        IotApi api = new IotApi();
        api.setAccessToken(node_key);
        final IotService iot = api.getService();
        iot.otaStatus(new Callback<OtaStatusResponse>() {
                          @Override
                          public void success(OtaStatusResponse otaStatusResponse, Response response) {
                              if (otaStatusResponse.ota_status.equals("going")) {
                                  displayStatus(node_key);
                                  mProgressDialog.setMessage(otaStatusResponse.ota_msg);
                              } else if (otaStatusResponse.ota_status.equals("done")) {
                                  mProgressDialog.dismiss();

                                  Message message = Message.obtain();
                                  message.what = MESSAGE_UPDATE_DONE;
                                  message.obj = otaStatusResponse.ota_msg;
                                  mHandler.sendMessage(message);

                              } else if (otaStatusResponse.ota_status.equals("error")) {
                                  mProgressDialog.setMessage(otaStatusResponse.ota_status + ":" + otaStatusResponse.ota_msg);
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
                              mProgressDialog.setMessage(error.getLocalizedMessage());
                              mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                              mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                  @Override
                                  public void onClick(DialogInterface dialog, int which) {

                                  }
                              });
                          }
                      }

        );
    }

    @Override
    public void onItemClick(View view, int position) {
        String groveType = Constant.groveTypes[position];

        List<GroverDriver> inputGroves = new ArrayList<GroverDriver>();
        List<GroverDriver> outputGroves = new ArrayList<GroverDriver>();
        List<GroverDriver> gpioGroves = new ArrayList<GroverDriver>();
        List<GroverDriver> analogGroves = new ArrayList<GroverDriver>();
        List<GroverDriver> uartGroves = new ArrayList<GroverDriver>();
        List<GroverDriver> i2cGroves = new ArrayList<GroverDriver>();
        List<GroverDriver> eventGroves = new ArrayList<GroverDriver>();


        if (mGroveDrivers == null)
            return;

        for (GroverDriver g : mGroveDrivers) {
            if (!g.Writes.isEmpty()) {
                outputGroves.add(g);
            }
            if (!g.Reads.isEmpty()) {
                inputGroves.add(g);
            }
            if (g.HasEvent) {
                eventGroves.add(g);
            }
            switch (g.InterfaceType) {
                case "GPIO":
                    gpioGroves.add(g);
                    break;
                case "ANALOG":
                    analogGroves.add(g);
                    break;
                case "UART":
                    uartGroves.add(g);
                    break;
                case "I2C":
                    i2cGroves.add(g);
                    break;
            }
        }

        mGroveTypeListAdapter.updateSelection(position);

        if (groveType.equals("All")) {
            updateGroveListAdapter(mGroveDrivers);
        } else if (groveType.equals("Input")) {
            updateGroveListAdapter(inputGroves);
        } else if (groveType.equals("Output")) {
            updateGroveListAdapter(outputGroves);
        }else if (groveType.equals("GPIO")) {
            updateGroveListAdapter(gpioGroves);
        }else if (groveType.equals("ANALOG")) {
            updateGroveListAdapter(analogGroves);
        }else if (groveType.equals("UART")) {
            updateGroveListAdapter(uartGroves);
        }else if (groveType.equals("I2C")) {
            updateGroveListAdapter(i2cGroves);
        }else if (groveType.equals("EVENT")) {
            updateGroveListAdapter(eventGroves);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.grove_1:
//                Snackbar.make(v, "Grove name:" + pinDeviceCount(1), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.grove_2:
//                Snackbar.make(v, "Grove name:" + pinDeviceCount(2), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.grove_3:
//                Snackbar.make(v, "Grove name:" + pinDeviceCount(3), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.grove_4:
//                Snackbar.make(v, "Grove name:" + pinDeviceCount(4), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.grove_5:
//                Snackbar.make(v, "Grove name:" + pinDeviceCount(5), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.grove_6:
                if (pinDeviceCount(6) == 0) {
                    ;
                } else if (pinDeviceCount(6) == 1) {
//                    Snackbar.make(v, "Grove name:" + pinDeviceCount(5), Snackbar.LENGTH_LONG).show();
                } else if (pinDeviceCount(6) > 1) {
                    if (mGroveI2cListView.getVisibility() == View.VISIBLE)
                        mGroveI2cListView.setVisibility(View.INVISIBLE);
                    else {
                        mGroveI2cListView.setVisibility(View.VISIBLE);
                        updateI2cGroveList();
                    }
                }

                break;
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


    @Override
    public boolean onDrag(View v, DragEvent event) {
//        Log.e(TAG, v.toString());
//        Log.e(TAG, event.toString());
        int action = event.getAction();

        switch (v.getId()) {
            case R.id.grove_1:
            case R.id.grove_2:
            case R.id.grove_3:
            case R.id.grove_4:
            case R.id.grove_5:
            case R.id.grove_6:
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED: {
                        if (!event.getClipDescription().hasMimeType(GROVE_ADD))
                            return false;
                        GrovePinsView.Tag tag = (GrovePinsView.Tag) v.getTag();
                        String interfaceType = tag.interfaceType;
                        GroverDriver groverDriver = (GroverDriver) event.getLocalState();

                        if (!interfaceType.equals(groverDriver.InterfaceType)) {
//                            Log.e(TAG, groverDriver.InterfaceType);
                            return false;
                        }
                        v.setActivated(true);
                        ((ImageView) v).setImageAlpha(64);
                    }
                    break;
                    case DragEvent.ACTION_DRAG_ENTERED:
//                        Log.e(TAG, "entered");
                        v.setActivated(false);
                        ((ImageView) v).setImageAlpha(64);
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        v.setActivated(true);
                        ((ImageView) v).setImageAlpha(64);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        v.setActivated(false);
                        ((ImageView) v).setImageAlpha(255);
                        break;
                    case DragEvent.ACTION_DROP: {
                        GroverDriver groverDriver = (GroverDriver) event.getLocalState();

                        Log.i(TAG, "Drop " + groverDriver);
                        UrlImageViewHelper.setUrlDrawable((ImageView) v, groverDriver.ImageURL, R.drawable.grove_no,
                                UrlImageViewHelper.CACHE_DURATION_INFINITE);
                        PinConfig pinConfig = new PinConfig();
                        pinConfig.position = ((GrovePinsView.Tag) v.getTag()).position;
                        pinConfig.selected = true;
                        pinConfig.sku = groverDriver.SKU;
                        pinConfig.node_sn = node.node_sn;

                        if (pinConfig.position != 6) {
                            //One pin connect one grove
                            Boolean status = false;
                            PinConfig dup_pinConfig = new PinConfig();
                            for (PinConfig p : pinConfigs)
                                if (p.position == pinConfig.position) {
                                    status = true;
                                    dup_pinConfig = p;
                                }
                            if (status)
                                pinConfigs.remove(dup_pinConfig);
                        } else {
                            //duplicate i2c grove is not allowed
                            Boolean status = false;
                            PinConfig dup_pinConfig = new PinConfig();
                            for (PinConfig p : pinConfigs)
                                if (p.sku.equals(pinConfig.sku)) {
                                    status = true;
                                    dup_pinConfig = p;
                                }
                            if (status)
                                pinConfigs.remove(dup_pinConfig);
                        }

                        String groveInstanceName;
//                        List<String> groveInstanceNames = new ArrayList<>();
//                        for (PinConfig p : pinConfigs) {
//                            groveInstanceNames.add(p.groveInstanceName);
//                        }
//                        groveInstanceName = groverDriver.ClassName;
//                        int i = 1;
//                        while (true) {
//                            if (groveInstanceNames.contains(groveInstanceName)) {
//                                groveInstanceName = groveInstanceName.split("_0")[0] + "_0" + Integer.toString(i);
//                            } else {
//                                groveInstanceNames.add(groveInstanceName);
//                                break;
//                            }
//                            i++;
//                        }
                        if (pinConfig.position >= 1 && pinConfig.position <= 3)
                            groveInstanceName = groverDriver.ClassName + "_Digital" + (pinConfig.position - 1);
                        else if (pinConfig.position == 4)
                            groveInstanceName = groverDriver.ClassName + "_Analog";
                        else if (pinConfig.position == 5)
                            groveInstanceName = groverDriver.ClassName + "_UART";
                        else if (pinConfig.position == 6)
                            groveInstanceName = groverDriver.ClassName + "_I2C";
                        else
                            groveInstanceName = groverDriver.ClassName;

                        pinConfig.groveInstanceName = groveInstanceName;

                        pinConfigs.add(pinConfig);
                        Log.i(TAG, "drag pinConfigs " + pinConfigs);

                        if (v.getId() == R.id.grove_6) {
                            Message message = Message.obtain();
                            message.what = ADD_I2C_GROVE;
                            mHandler.sendMessage(message);
                        }
                    }
                    break;
                }
                break;
            case R.id.grove_remove:
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED: {
                        return event.getClipDescription().hasMimeType(GROVE_REMOVE) ||
                                event.getClipDescription().hasMimeType(GROVE_REMOVE_PIN6);
                    }
                    case DragEvent.ACTION_DRAG_ENTERED:
                        ((ImageView) v).setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        ((ImageView) v).setColorFilter(Color.RED, PorterDuff.Mode.DST);
                        break;
                    case DragEvent.ACTION_DROP: {
                        if (event.getClipDescription().hasMimeType(GROVE_REMOVE)) {
                            ImageView view = (ImageView) event.getLocalState();
                            view.setImageDrawable(null);
                            int position = ((GrovePinsView.Tag) view.getTag()).position;

                            removePinConfig(position);
                        } else if (event.getClipDescription().hasMimeType(GROVE_REMOVE_PIN6)) {
                            PinConfig pinConfig = (PinConfig) event.getLocalState();
//                            Log.e(TAG, pinConfig.sku);
                            removePinConfig(pinConfig.sku);

                            Message message = Message.obtain();
                            message.what = RMV_I2C_GROVE;
                            mHandler.sendMessage(message);
                        }
                        break;
                    }
                    case DragEvent.ACTION_DRAG_ENDED:
                        ((ImageView) v).setColorFilter(Color.RED, PorterDuff.Mode.DST);
                        mDragRemoveView.setVisibility(View.INVISIBLE);
                }
                break;
            default:
                Log.e(TAG, v.toString());
                break;
        }
        return true;
    }

    private void removePinConfig(int position) {
        if (position < 1 || position > 6)
            return;
        PinConfig rp = new PinConfig();
        for (PinConfig p : pinConfigs) {
            if (p.position == position)
                rp = p;
        }
        pinConfigs.remove(rp);
    }

    private void removePinConfig(String sku) {
        PinConfig rp = new PinConfig();
        for (PinConfig p : pinConfigs) {
            if (p.sku.equals(sku))
                rp = p;
        }
        pinConfigs.remove(rp);
    }


    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.grove_1:
                if (pinDeviceCount(1) > 0)
                    startDragRemove(v);
                break;
            case R.id.grove_2:
                if (pinDeviceCount(2) > 0)
                    startDragRemove(v);
                break;
            case R.id.grove_3:
                if (pinDeviceCount(3) > 0)
                    startDragRemove(v);
                break;
            case R.id.grove_4:
                if (pinDeviceCount(4) > 0)
                    startDragRemove(v);
            case R.id.grove_5:
                if (pinDeviceCount(5) > 0)
                    startDragRemove(v);
                break;
            case R.id.grove_6:
//                Snackbar.make(v, "Grove name:" + pinDeviceCount(6), Snackbar.LENGTH_LONG).show();
                if (pinDeviceCount(6) == 0) {
                    ;
                } else if (pinDeviceCount(6) == 1) {
                    startDragRemove(v);
                } else if (pinDeviceCount(6) > 1) {
//                    openI2cDeviceFolder();
                }
                break;
        }
        return true;
    }

    private void startDragRemove(View v) {
        mDragRemoveView.setVisibility(View.VISIBLE);

        String label = "grove_remove";
        String[] mimeTypes = {GROVE_REMOVE};
        ClipDescription clipDescription = new ClipDescription(label, mimeTypes);
        ClipData.Item item = new ClipData.Item("drag grove");
        ClipData clipData = new ClipData(clipDescription, item);
        View.DragShadowBuilder shadowBuiler = new View.DragShadowBuilder(v);

        v.startDrag(clipData, shadowBuiler, v, 0);
    }

    private class MainOnClickListener implements View.OnClickListener, View.OnLongClickListener {
        private final Context context;

        private MainOnClickListener(Context c) {
            this.context = c;
        }

        @Override
        public void onClick(View v) {
            ;
        }

        @Override
        public boolean onLongClick(View v) {
//            Snackbar.make(v, "Todo:grove detail page", Snackbar.LENGTH_SHORT).show();

            String label = "grove_add";
            String[] mimeTypes = {GROVE_ADD};
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

    private void getGrovesData() {
        IotApi api = new IotApi();
        String token = user.user_key;
        api.setAccessToken(token);
        IotService iot = api.getService();
        iot.scanDrivers(new Callback<GroveDriverListResponse>() {
            @Override
            public void success(GroveDriverListResponse groveDriverListResponse, Response response) {
                for (GroverDriver groveDriver : groveDriverListResponse.drivers) {
                    groveDriver.save();
                }
                List<GroverDriver> g = DBHelper.getGrovesAll();
                for (GroverDriver s : g) {
                    Log.e(TAG, s.Reads.toString());
                }
                updateGroveListAdapter(g);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, error.getLocalizedMessage());
            }
        });
    }

    private class Pin6OnClickListener implements View.OnLongClickListener {
        public Pin6OnClickListener(SetupIotNodeActivity setupIotNodeActivity) {
        }

        @Override
        public boolean onLongClick(View v) {
            mDragRemoveView.setVisibility(View.VISIBLE);

            String label = "grove_remove_6";
            String[] mimeTypes = {GROVE_REMOVE_PIN6};
            ClipDescription clipDescription = new ClipDescription(label, mimeTypes);
            ClipData.Item item = new ClipData.Item("drag grove");
            ClipData clipData = new ClipData(clipDescription, item);
            View.DragShadowBuilder shadowBuiler = new View.DragShadowBuilder(v);

//            mGroveListAdapter.selectItem(mGroveListView.getChildAdapterPosition(v));
//            GroverDriver grove = mGroveListAdapter.getSelectedItem();

            PinConfig pinConfig = mGroveI2cListAdapter.getItem(mGroveI2cListView.getChildAdapterPosition(v));

            v.startDrag(clipData, shadowBuiler, pinConfig, 0);
            return true;
        }
    }
}


