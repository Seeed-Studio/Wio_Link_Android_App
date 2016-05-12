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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

public class SetupIotLinkActivity extends AppCompatActivity
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

    RecyclerView mGroveListView;
    GroveListRecyclerAdapter mGroveListAdapter;

    RecyclerView mGroveTypeListView;
    GroveFilterRecyclerAdapter mGroveTypeListAdapter;
    private List<GroverDriver> mGroveDrivers;

    View mSetNodeLayout;
    GrovePinsView mGrovePinsView;
    ProgressDialog mProgressDialog;
    private ImageView mDragRemoveView;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_link);
        View view = findViewById(R.id.setup_iot_link);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCanceledOnTouchOutside(false);
//        mProgressDialog.setCancelable(false);
        mProgressDialog.setButton(ProgressDialog.BUTTON_POSITIVE,
                "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
//        mProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);

        mGroveDrivers = DBHelper.getGrovesAll();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mDragRemoveView = (ImageView) findViewById(R.id.grove_remove);
        mDragRemoveView.setOnDragListener(this);

        mSetNodeLayout = findViewById(R.id.set_link);
        mSetNodeLayout.setOnClickListener(this);

        user =  UserLogic.getInstance().getUser();
        String node_sn = getIntent().getStringExtra("node_sn");
        node = DBHelper.getNodes(node_sn).get(0);

        /**
         * fake node for test
         */
//        node = new Node();
//        node.board = Constant.WIO_NODE_V1_0;
//        node.node_sn = "112233";
//        node.name = "menu_item_bg";
//        node.online = true;
//        node.node_key = "key1213";

        mGrovePinsView = new GrovePinsView(this, view, node);
        for (ImageView pinView : mGrovePinsView.pinViews) {
            pinView.setOnDragListener(this);
            pinView.setOnClickListener(this);
            pinView.setOnLongClickListener(this);
        }

        pinConfigs = PinConfigDBHelper.getPinConfigs(node.node_sn);
        /**
         * make fake pinConfig data
         */
//        PinConfig fake_pinConfig = new PinConfig();
//        fake_pinConfig.interfaceType = InterfaceType.GPIO;
//        fake_pinConfig.node_sn = "112233";
//        fake_pinConfig.position = 0;
//        fake_pinConfig.sku = "104990089";
//        pinConfigs.add(fake_pinConfig);
//        Log.e(TAG, "pinConfig" + pinConfigs.toString());

        getSupportActionBar().setTitle(node.name);


        mGroveListView = (RecyclerView) findViewById(R.id.grove_list);
        if (mGroveListView != null) {
            mGroveListView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mGroveListView.setLayoutManager(layoutManager);
            mGroveListAdapter = new GroveListRecyclerAdapter(mGroveDrivers);
            mGroveListAdapter.setOnLongClickListener(this);
            mGroveListView.setAdapter(mGroveListAdapter);
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

        mGroveTypeListView = (RecyclerView) findViewById(R.id.grove_selector);
        if (mGroveTypeListView != null) {
            mGroveTypeListView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mGroveTypeListView.setLayoutManager(layoutManager);
            setupGroveSelectorAdapter();
        }

        pinBadgeUpdateAll();

        initData();
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

    private void initData() {
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
                        new AlertDialog.Builder(SetupIotLinkActivity.this)
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
    }

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
                builder.create().show();
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
                                      if (SetupIotLinkActivity.this.isFinishing()) {
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
        } else if (groveType.equals("Input")) {
            updateGroveListAdapter(inputGroves);
        } else if (groveType.equals("Output")) {
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.grove_0:
            case R.id.grove_1:
            case R.id.grove_2:
            case R.id.grove_3:
            case R.id.grove_4:
            case R.id.grove_5:
                GrovePinsView.Tag tag = (GrovePinsView.Tag) v.getTag();
                int position = tag.position;
                if (pinDeviceCount(position) > 1)
                    displayI2cListView(position);
                break;
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
                        mDragRemoveView.setVisibility(View.INVISIBLE);
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
        mDragRemoveView.setVisibility(View.VISIBLE);

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
                mDragRemoveView.setVisibility(View.VISIBLE);

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
}


