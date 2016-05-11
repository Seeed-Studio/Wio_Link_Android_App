package cc.seeed.iot.activity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.ui_main.NodeApiActivity;
import cc.seeed.iot.ui_setnode.GroveFilterRecyclerAdapter;
import cc.seeed.iot.ui_setnode.GroveI2cListRecyclerAdapter;
import cc.seeed.iot.ui_setnode.GroveListRecyclerAdapter;
import cc.seeed.iot.ui_setnode.View.GrovePinsView;
import cc.seeed.iot.ui_setnode.model.InterfaceType;
import cc.seeed.iot.ui_setnode.model.NodeConfigHelper;
import cc.seeed.iot.ui_setnode.model.PinConfig;
import cc.seeed.iot.ui_setnode.model.PinConfigDBHelper;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.util.ToolUtil;
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

public class SetupIotLinkActivity1 extends BaseActivity
        implements GroveFilterRecyclerAdapter.MainViewHolder.MyItemClickListener,
        View.OnClickListener, View.OnDragListener, View.OnLongClickListener,
        GroveI2cListRecyclerAdapter.OnLongClickListener, GroveListRecyclerAdapter.OnLongClickListener {

    private static final String TAG = "SetupIotLinkActivity";
    private static final String GROVE_REMOVE = "grove/remove";
    private static final String GROVE_ADD = "grove/add";
    private static final int ADD_I2C_GROVE = 0x00;
    private static final int ADD_GROVE = 0x01;
    private static final int RMV_I2C_GROVE = 0x02;
    private static final int RMV_GROVE = 0x03;

    private static final int MESSAGE_UPDATE_DONE = 0x10;

    public Toolbar mToolbar;
    Node node;
    User user;
    List<PinConfig> pinConfigs = new ArrayList<>();

    RecyclerView mGroveI2cListView;
    GroveI2cListRecyclerAdapter mGroveI2cListAdapter;

    GroveListRecyclerAdapter mGroveListAdapter;

    GroveFilterRecyclerAdapter mGroveTypeListAdapter;
    @InjectView(R.id.grove_0)
    ImageButton mIBGrove0;
    @InjectView(R.id.grove_1)
    ImageButton mIBGrove1;
    @InjectView(R.id.grove_2)
    ImageButton mIBGrove2;
    @InjectView(R.id.grove_3)
    ImageButton mIBGrove3;
    @InjectView(R.id.grove_4)
    ImageButton mIBGrove4;
    @InjectView(R.id.grove_5)
    ImageButton mIBGrove5;
    @InjectView(R.id.grove_i2c_list)
    RecyclerView groveI2cList;
    @InjectView(R.id.set_link)
    RelativeLayout setLink;
    @InjectView(R.id.grove_selector)
    RecyclerView mGroveTypeListView;
    @InjectView(R.id.grove_list)
    RecyclerView mGroveListView;
    @InjectView(R.id.setup_iot_link)
    LinearLayout setupIotLink;


    private List<GroverDriver> mGroveDrivers;

    Map<String , List<GroverDriver>> GpioMap = new HashMap<>();
    Map<String , List<GroverDriver>> AnalogMap = new HashMap<>();
    Map<String , List<GroverDriver>> I2cMap = new HashMap<>();
    Map<String , List<GroverDriver>> UartMap = new HashMap<>();

    private List<Map<String,List<GroverDriver>>> mGroveGpioDrivers = new ArrayList<>();

    GrovePinsView mGrovePinsView;
    ProgressDialog mProgressDialog;

    int selectGrovePin = -1;
    int Grove0 = 0;
    int Grove1 = 1;
    int Grove2 = 2;
    int Grove3 = 3;
    int Grove4 = 4;
    int Grove5 = 5;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_link_01);
        ButterKnife.inject(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCanceledOnTouchOutside(false);
//        mProgressDialog.setCancelable(false);
        mProgressDialog.setButton(ProgressDialog.BUTTON_POSITIVE,
                "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        initView();
        initData();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (mGroveTypeListView != null) {
            mGroveTypeListView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mGroveTypeListView.setLayoutManager(layoutManager);
            setupGroveSelectorAdapter();
        }

    }

    private void initData() {
        View view = findViewById(R.id.setup_iot_link);
        mGroveDrivers = DBHelper.getGrovesAll();
        user = UserLogic.getInstance().getUser();
        String node_sn = getIntent().getStringExtra("node_sn");
        node = DBHelper.getNodes(node_sn).get(0);
        getSupportActionBar().setTitle(node.name);

        if (mGroveDrivers != null) {
            for (GroverDriver driver : mGroveDrivers) {
                if (driver.InterfaceType.equals(InterfaceType.GPIO)) {
                    if (GpioMap.get(Constant.GroveType.All.getValue()) == null){
                        GpioMap.put(Constant.GroveType.All.getValue(),new ArrayList<GroverDriver>());
                    }
                    GpioMap.get(Constant.GroveType.All.getValue()).add(driver);
                    if (!driver.Writes.isEmpty()) {
                        if (GpioMap.get(Constant.GroveType.OUTPUT.getValue()) == null){
                            GpioMap.put(Constant.GroveType.OUTPUT.getValue(),new ArrayList<GroverDriver>());
                        }
                        GpioMap.get(Constant.GroveType.OUTPUT.getValue()).add(driver);
                    }
                    if (!driver.Reads.isEmpty()) {
                        if (GpioMap.get(Constant.GroveType.INPUT.getValue()) == null){
                            GpioMap.put(Constant.GroveType.INPUT.getValue(),new ArrayList<GroverDriver>());
                        }
                        GpioMap.get(Constant.GroveType.INPUT.getValue()).add(driver);
                    }
                    if (driver.HasEvent) {
                        if (GpioMap.get(Constant.GroveType.EVENT.getValue()) == null){
                            GpioMap.put(Constant.GroveType.EVENT.getValue(),new ArrayList<GroverDriver>());
                        }
                        GpioMap.get(Constant.GroveType.EVENT.getValue()).add(driver);
                    }
                } else if (driver.InterfaceType.equals(InterfaceType.ANALOG)) {
                    if (AnalogMap.get(Constant.GroveType.All.getValue()) == null){
                        AnalogMap.put(Constant.GroveType.All.getValue(),new ArrayList<GroverDriver>());
                    }
                    AnalogMap.get(Constant.GroveType.All.getValue()).add(driver);
                    if (!driver.Writes.isEmpty()) {
                        if (AnalogMap.get(Constant.GroveType.OUTPUT.getValue()) == null){
                            AnalogMap.put(Constant.GroveType.OUTPUT.getValue(),new ArrayList<GroverDriver>());
                        }
                        AnalogMap.get(Constant.GroveType.OUTPUT.getValue()).add(driver);
                    }
                    if (!driver.Reads.isEmpty()) {
                        if (AnalogMap.get(Constant.GroveType.INPUT.getValue()) == null){
                            AnalogMap.put(Constant.GroveType.INPUT.getValue(),new ArrayList<GroverDriver>());
                        }
                        AnalogMap.get(Constant.GroveType.INPUT.getValue()).add(driver);
                    }
                    if (driver.HasEvent) {
                        if (AnalogMap.get(Constant.GroveType.EVENT.getValue()) == null){
                            AnalogMap.put(Constant.GroveType.EVENT.getValue(),new ArrayList<GroverDriver>());
                        }
                        AnalogMap.get(Constant.GroveType.EVENT.getValue()).add(driver);
                    }
                } else if (driver.InterfaceType.equals(InterfaceType.I2C)) {
                    if (I2cMap.get(Constant.GroveType.All.getValue()) == null){
                        I2cMap.put(Constant.GroveType.All.getValue(),new ArrayList<GroverDriver>());
                    }
                    I2cMap.get(Constant.GroveType.All.getValue()).add(driver);
                    if (!driver.Writes.isEmpty()) {
                        if (I2cMap.get(Constant.GroveType.OUTPUT.getValue()) == null){
                            I2cMap.put(Constant.GroveType.OUTPUT.getValue(),new ArrayList<GroverDriver>());
                        }
                        I2cMap.get(Constant.GroveType.OUTPUT.getValue()).add(driver);
                    }
                    if (!driver.Reads.isEmpty()) {
                        if (I2cMap.get(Constant.GroveType.INPUT.getValue()) == null){
                            I2cMap.put(Constant.GroveType.INPUT.getValue(),new ArrayList<GroverDriver>());
                        }
                        I2cMap.get(Constant.GroveType.INPUT.getValue()).add(driver);
                    }
                    if (driver.HasEvent) {
                        if (I2cMap.get(Constant.GroveType.EVENT.getValue()) == null){
                            I2cMap.put(Constant.GroveType.EVENT.getValue(),new ArrayList<GroverDriver>());
                        }
                        I2cMap.get(Constant.GroveType.EVENT.getValue()).add(driver);
                    }
                } else if (driver.InterfaceType.equals(InterfaceType.UART)) {
                    if (UartMap.get(Constant.GroveType.All.getValue()) == null){
                        UartMap.put(Constant.GroveType.All.getValue(),new ArrayList<GroverDriver>());
                    }
                    UartMap.get(Constant.GroveType.All.getValue()).add(driver);
                    if (!driver.Writes.isEmpty()) {
                        if (UartMap.get(Constant.GroveType.OUTPUT.getValue()) == null){
                            UartMap.put(Constant.GroveType.OUTPUT.getValue(),new ArrayList<GroverDriver>());
                        }
                        UartMap.get(Constant.GroveType.OUTPUT.getValue()).add(driver);
                    }
                    if (!driver.Reads.isEmpty()) {
                        if (UartMap.get(Constant.GroveType.INPUT.getValue()) == null){
                            UartMap.put(Constant.GroveType.INPUT.getValue(),new ArrayList<GroverDriver>());
                        }
                        UartMap.get(Constant.GroveType.INPUT.getValue()).add(driver);
                    }
                    if (driver.HasEvent) {
                        if (UartMap.get(Constant.GroveType.EVENT.getValue()) == null){
                            UartMap.put(Constant.GroveType.EVENT.getValue(),new ArrayList<GroverDriver>());
                        }
                        UartMap.get(Constant.GroveType.EVENT.getValue()).add(driver);
                    }
                }
            }
        }


        /**
         * fake node for test
         */
        mGrovePinsView = new GrovePinsView(this, view, node);
        for (ImageView pinView : mGrovePinsView.pinViews) {
            pinView.setOnDragListener(this);
            pinView.setOnClickListener(this);
            pinView.setOnLongClickListener(this);
        }

        pinConfigs = PinConfigDBHelper.getPinConfigs(node.node_sn);

        if (mGroveListView != null) {
            mGroveListView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mGroveListView.setLayoutManager(layoutManager);
            mGroveListAdapter = new GroveListRecyclerAdapter(mGroveDrivers);
            mGroveListAdapter.setOnLongClickListener(this);
            mGroveListView.setAdapter(mGroveListAdapter);
            mGroveListAdapter.setOnItemClickListener(new GroveListRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(GroverDriver grove, int position) {
                    App.showToastShrot("name: "+grove.GroveName+" id: "+grove.ID);
                }
            });
        }

        mGroveI2cListView = (RecyclerView) findViewById(R.id.grove_i2c_list);
        if (mGroveI2cListView != null) {
            mGroveI2cListView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mGroveI2cListView.setLayoutManager(layoutManager);
            mGroveI2cListAdapter = new GroveI2cListRecyclerAdapter(pinConfigs);
            mGroveI2cListAdapter.setOnLongClickListen(this);
            mGroveI2cListView.setAdapter(mGroveI2cListAdapter);
        }

        pinBadgeUpdateAll();
    }

    public void dataClassification(List<GroverDriver> driverList){
    }

    private void pinBadgeUpdateAll() {
        for (int i = 0; i < mGrovePinsView.pinViews.length; i++) {
            pinBadgeUpdate(i);
        }
    }

    private void pinBadgeUpdate(int position) {
        if (pinDeviceCount(position) > 1) {
            mGrovePinsView.badgeViews[position].setBadgeCount(pinDeviceCount(position));
            mGrovePinsView.badgeViews[position].setVisibility(View.VISIBLE);

        } else {
            mGrovePinsView.badgeViews[position].setVisibility(View.GONE);
        }
    }

  /*  private void initData() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ADD_I2C_GROVE: {
                        PinConfig pinConfig = (PinConfig) msg.obj;
                        int position = pinConfig.position;
                        updateI2cGroveList(position);
                        scrollI2cGroveListToEnd();
                        pinBadgeUpdateAll();
                    }
                    break;
                    case ADD_GROVE: {
                        PinConfig pinConfig = (PinConfig) msg.obj;
                        int position = pinConfig.position;
                        if (isI2cInterface(position)) {
                            mGroveI2cListView.setVisibility(View.INVISIBLE);
                        }
                    }
                    case RMV_I2C_GROVE: {
                        PinConfig pinConfig = (PinConfig) msg.obj;
                        int position = pinConfig.position;
                        if (pinDeviceCount(position) < 2)
                            mGroveI2cListView.setVisibility(View.INVISIBLE);
                        else
                            updateI2cGroveList(position);

                        pinBadgeUpdateAll();

                        if (pinDeviceCount(position) == 0)
                            mGrovePinsView.pinViews[pinConfig.position].setImageDrawable(null);
                        else
                            mGrovePinsView.updatePin(pinConfigs, position);
                    }
                    break;

                    case RMV_GROVE: {
                        PinConfig pinConfig = (PinConfig) msg.obj;
                        mGrovePinsView.pinViews[pinConfig.position].setImageDrawable(null);
                    }
                    break;

                    case MESSAGE_UPDATE_DONE: {
                        String message = (String) msg.obj;
                        new AlertDialog.Builder(SetupIotLinkActivity1.this)
                                .setTitle(R.string.update)
                                .setMessage(message)
                                .setCancelable(false)
                                .setPositiveButton(R.string.ok, null)
                                .show();
                    }
                    break;
                }
            }

            private boolean isI2cInterface(int position) {
                GrovePinsView.Tag tag = (GrovePinsView.Tag) mGrovePinsView.pinViews[position].getTag();
                return Arrays.asList(tag.interfaceTypes).contains(InterfaceType.I2C);
            }
        };
    }*/

    private void scrollI2cGroveListToEnd() {
        mGroveI2cListView.smoothScrollToPosition(mGroveI2cListAdapter.getItemCount() - 1);
    }

    private void updateI2cGroveList(int position) {
        List<PinConfig> pinConfigs = new ArrayList<>();
        for (PinConfig p : this.pinConfigs) {
            if (p.position == position)
                pinConfigs.add(p);
        }
        mGroveI2cListAdapter.updateAll(pinConfigs);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getGrovesData();
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    private void setupGroveSelectorAdapter() {
        List<String> groveList = new ArrayList<>();
        groveList.add("All");
        groveList.add("INPUT");
        groveList.add("OUTPUT");
        groveList.add("GPIO");

        mGroveTypeListAdapter = new GroveFilterRecyclerAdapter(groveList);
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

            NodeJson node_josn = new NodeConfigHelper().getConfigJson(pinConfigs, node);
//            Log.i(TAG, "node_json:\n" + new Gson().toJson(node_josn));
            if (node_josn.connections.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Forger add grove?");
                builder.setTitle("Tip");
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                params.width = ToolUtil.dp2px(200, getResources());
                params.height = ToolUtil.dp2px(300, getResources());
                dialog.getWindow().setAttributes(params);
                return true;
            }
            updateNode(node.node_key, node_josn);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                              switch (otaStatusResponse.ota_status) {
                                  case "going":
                                      displayStatus(node_key);
                                      mProgressDialog.setMessage(otaStatusResponse.ota_msg);
                                      break;
                                  case "done":
                                      if (SetupIotLinkActivity1.this.isFinishing()) {
                                          return;
                                      }
                                      dismissProgressDialog();

                                      Message message = Message.obtain();
                                      message.what = MESSAGE_UPDATE_DONE;
                                      message.obj = otaStatusResponse.ota_msg;
                                      mHandler.sendMessage(message);

                                      break;
                                  case "error":
                                      mProgressDialog.setMessage(otaStatusResponse.ota_status + ":" + otaStatusResponse.ota_msg);
                                      mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                                      mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                          @Override
                                          public void onClick(DialogInterface dialog, int which) {

                                          }
                                      });
                                      break;
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
        } else if (groveType.equals("INPUT")) {
            updateGroveListAdapter(inputGroves);
        } else if (groveType.equals("OUTPUT")) {
            updateGroveListAdapter(outputGroves);
        } else if (groveType.equals("GPIO")) {
            updateGroveListAdapter(gpioGroves);
        } else if (groveType.equals("ANALOG")) {
            updateGroveListAdapter(analogGroves);
        } else if (groveType.equals("UART")) {
            updateGroveListAdapter(uartGroves);
        } else if (groveType.equals("I2C")) {
            updateGroveListAdapter(i2cGroves);
        } else if (groveType.equals("EVENT")) {
            updateGroveListAdapter(eventGroves);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.grove_0:
            case R.id.grove_1:
            case R.id.grove_2:
            case R.id.grove_3:
            case R.id.grove_4:
            case R.id.grove_5:
                GrovePinsView.Tag tag = (GrovePinsView.Tag) v.getTag();
                int position = tag.position;
                if (pinDeviceCount(position) == 1) {
                    startDragRemove(v);
                } else if (pinDeviceCount(position) > 1) {
                    displayI2cListView(position);
                }
                break;
        }
        return true;
    }

    private void displayI2cListView(int position) {
        if (pinDeviceCount(position) > 0) {
            if (mGroveI2cListView.getVisibility() == View.VISIBLE)
                mGroveI2cListView.setVisibility(View.INVISIBLE);
            else {
                mGroveI2cListView.setVisibility(View.VISIBLE);
                updateI2cGroveList(position);
            }
        }
    }

    private int pinDeviceCount(int position) {
        int count = 0;
        for (PinConfig pinConfig : pinConfigs) {
            if (pinConfig.position == position)
                count++;
        }
        return count;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
//        Log.e(TAG, v.toString());
//        Log.e(TAG, event.toString());
        int action = event.getAction();

        switch (v.getId()) {
            case R.id.grove_0:
            case R.id.grove_1:
            case R.id.grove_2:
            case R.id.grove_3:
            case R.id.grove_4:
            case R.id.grove_5:
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED: {
                        if (!event.getClipDescription().hasMimeType(GROVE_ADD))
                            return false;
                        GrovePinsView.Tag tag = (GrovePinsView.Tag) v.getTag();
                        String[] interfaceTypes = tag.interfaceTypes;
                        GroverDriver groverDriver = (GroverDriver) event.getLocalState();

                        if (!Arrays.asList(interfaceTypes).contains(groverDriver.InterfaceType))
                            return false;

                        v.setActivated(true);
                        ((ImageView) v).setImageAlpha(64);
                    }
                    break;
                    case DragEvent.ACTION_DRAG_ENTERED:
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
                        UrlImageViewHelper.setUrlDrawable((ImageView) v, groverDriver.ImageURL,
                                R.drawable.grove_no, UrlImageViewHelper.CACHE_DURATION_INFINITE);
                        int pin_position = ((GrovePinsView.Tag) v.getTag()).position;
                        PinConfig pinConfig = new PinConfig();
                        pinConfig.position = pin_position;
                        pinConfig.interfaceType = groverDriver.InterfaceType;
                        pinConfig.sku = groverDriver.SKU;
                        pinConfig.node_sn = node.node_sn;

                        if (isI2cGrove(pinConfig) && isHasI2cGrove(pin_position)) {
                            if (isSameI2cGrove(pinConfig))
                                removeGrove(pinConfig);
                        } else {
                            removePinAllGrove(pin_position);
                        }
                        addGrove(pinConfig);

//                        Log.e(TAG, "pinConfigs " + pinConfigs);

                        if (isHasI2cGrove(pin_position)) {
                            Message message = Message.obtain();
                            message.what = ADD_I2C_GROVE;
                            message.obj = pinConfig;
                            mHandler.sendMessage(message);
                        } else {
                            Message message = Message.obtain();
                            message.what = ADD_GROVE;
                            message.obj = pinConfig;
                            mHandler.sendMessage(message);
                        }
                    }
                    break;
                }
                break;
            case R.id.grove_remove:
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED: {
                        return event.getClipDescription().hasMimeType(GROVE_REMOVE);
                    }
                    case DragEvent.ACTION_DRAG_ENTERED:
                        ((ImageView) v).setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        ((ImageView) v).setColorFilter(Color.RED, PorterDuff.Mode.DST);
                        break;
                    case DragEvent.ACTION_DROP: {
                        if (event.getClipDescription().hasMimeType(GROVE_REMOVE)) {
                            PinConfig pinConfig = (PinConfig) event.getLocalState();

                            removeGrove(pinConfig);

                            if (isI2cGrove(pinConfig)) {
                                Message message = Message.obtain();
                                message.what = RMV_I2C_GROVE;
                                message.obj = pinConfig;
                                mHandler.sendMessage(message);
                            } else {
                                Message message = Message.obtain();
                                message.what = RMV_GROVE;
                                message.obj = pinConfig;
                                mHandler.sendMessage(message);
                            }
                        }

//                        Log.e(TAG, "pinConfigs " + pinConfigs);
                        break;
                    }
                    case DragEvent.ACTION_DRAG_ENDED:
                        ((ImageView) v).setColorFilter(Color.RED, PorterDuff.Mode.DST);
                }
                break;
            default:
                Log.e(TAG, v.toString());
                break;
        }

        return true;
    }

    private boolean isI2cGrove(PinConfig pinConfig) {
        return pinConfig.interfaceType.equals(InterfaceType.I2C);
    }

    public boolean isHasI2cGrove(int position) {
        for (PinConfig p : pinConfigs)
            if (p.position == position) {
                String interfaceType = DBHelper.getGroves(p.sku).get(0).InterfaceType;
                if (interfaceType.equals(InterfaceType.I2C))
                    return true;
            }
        return false;
    }

    private boolean isSameI2cGrove(PinConfig pinConfig) {
        Boolean status = false;
        for (PinConfig p : pinConfigs) {
            if ((p.position == pinConfig.position) && p.sku.equals(pinConfig.sku)) {
                status = true;
                break;
            }
        }
        return status;
    }

    private void addGrove(PinConfig pinConfig) {
        pinConfigs.add(pinConfig);
    }

    /**
     * remove all grove on position, complete replace
     *
     * @param position
     */
    private void removePinAllGrove(int position) {
        ArrayList<PinConfig> rPinConfigs = new ArrayList<>();
        for (PinConfig p : pinConfigs) {
            if (p.position == position) {
                rPinConfigs.add(p);
            }
        }
//        Log.e(TAG, "rPinconfigs:" + rPinConfigs);
        pinConfigs.removeAll(rPinConfigs);
    }

    /**
     * same position and same sku for remove
     *
     * @param pinConfig
     */
    private void removeGrove(PinConfig pinConfig) {
        PinConfig rp = new PinConfig();
        for (PinConfig p : pinConfigs)
            if ((p.position == pinConfig.position) && p.sku.equals(pinConfig.sku)) {
                rp = p;
                break;
            }
        pinConfigs.remove(rp);
    }

    private void startDragRemove(View v) {

        String label = "grove_remove";
        String[] mimeTypes = {GROVE_REMOVE};
        ClipDescription clipDescription = new ClipDescription(label, mimeTypes);
        ClipData.Item item = new ClipData.Item("drag grove");
        ClipData clipData = new ClipData(clipDescription, item);
        View.DragShadowBuilder shadowBuiler = new View.DragShadowBuilder(v);

        int pin_position = ((GrovePinsView.Tag) v.getTag()).position;
        PinConfig pinConfig = getPinConfig(pin_position);

        v.startDrag(clipData, shadowBuiler, pinConfig, 0);
    }

    private PinConfig getPinConfig(int pin_position) {
        for (PinConfig p : pinConfigs) {
            if (p.position == pin_position) {
                return p;
            }
        }
        return null;
    }

    @Override
    public void onLongClick(View v, int position) {
        if (v.getTag() == null)
            return;

        switch ((String) v.getTag()) {
            case "GroveList": {
                String label = "grove_add";
                String[] mimeTypes = {GROVE_ADD};
                ClipDescription clipDescription = new ClipDescription(label, mimeTypes);
                ClipData.Item item = new ClipData.Item("drag grove");
                ClipData clipData = new ClipData(clipDescription, item);
                View.DragShadowBuilder shadowBuiler = new View.DragShadowBuilder(v);

                mGroveListAdapter.selectItem(mGroveListView.getChildAdapterPosition(v));
                GroverDriver grove = mGroveListAdapter.getSelectedItem();

                v.startDrag(clipData, shadowBuiler, grove, 0);
            }
            break;
            case "I2cList": {

                String label = "grove_remove";
                String[] mimeTypes = {GROVE_REMOVE};
                ClipDescription clipDescription = new ClipDescription(label, mimeTypes);
                ClipData.Item item = new ClipData.Item("drag grove");
                ClipData clipData = new ClipData(clipDescription, item);
                View.DragShadowBuilder shadowBuiler = new View.DragShadowBuilder(v);

                PinConfig pinConfig = mGroveI2cListAdapter.getItem(mGroveI2cListView.getChildAdapterPosition(v));

                v.startDrag(clipData, shadowBuiler, pinConfig, 0);
            }
            break;
        }

    }

    private void getGrovesData() {
        IotApi api = new IotApi();
        String token = user.token;
        api.setAccessToken(token);
        IotService iot = api.getService();
        iot.scanDrivers(new Callback<GroveDriverListResponse>() {
            @Override
            public void success(GroveDriverListResponse groveDriverListResponse, Response response) {
                for (GroverDriver groveDriver : groveDriverListResponse.drivers) {
                    groveDriver.save();
                }
                List<GroverDriver> g = DBHelper.getGrovesAll();
//                for (GroverDriver s : g) {
//                    Log.e(TAG, s.Reads.toString());
//                }
                updateGroveListAdapter(g);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, error.getLocalizedMessage());
            }
        });
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @OnClick({R.id.grove_0, R.id.grove_1, R.id.grove_2, R.id.grove_3, R.id.grove_4, R.id.grove_5})
    public void onClick(View view) {
        GrovePinsView.Tag tag = (GrovePinsView.Tag) view.getTag();
        int position = tag.position;
        if (pinDeviceCount(position) > 1)
            displayI2cListView(position);
        switch (view.getId()) {
            case R.id.grove_0:
                App.showToastShrot("grove_0");
                break;
            case R.id.grove_1:
                App.showToastShrot("grove_1");
                break;
            case R.id.grove_2:
                App.showToastShrot("grove_2");
                break;
            case R.id.grove_3:
                App.showToastShrot("grove_3");
                break;
            case R.id.grove_4:
                App.showToastShrot("grove_4");
                break;
            case R.id.grove_5:
                App.showToastShrot("grove_5");
                break;
        }
    }

    public void loadGrove(){
       // mGroveTypeListAdapter.setData();
    }
}


