package cc.seeed.iot.reserve_file;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.activity.NodeSettingActivity;
import cc.seeed.iot.adapter.set_node.GroveFilterRecyclerAdapter;
import cc.seeed.iot.adapter.set_node.GroveI2cListRecyclerAdapter;
import cc.seeed.iot.adapter.set_node.GroveListRecyclerAdapter;
import cc.seeed.iot.entity.DialogBean;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.logic.ConfigDeviceLogic;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.ui_main.WebActivity;
import cc.seeed.iot.ui_setnode.View.GrovePinsView;
import cc.seeed.iot.ui_setnode.model.InterfaceType;
import cc.seeed.iot.ui_setnode.model.NodeConfigHelper;
import cc.seeed.iot.ui_setnode.model.PinConfig;
import cc.seeed.iot.ui_setnode.model.PinConfigDBHelper;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.ImgUtil;
import cc.seeed.iot.view.CustomProgressDialog;
import cc.seeed.iot.view.FontButton;
import cc.seeed.iot.view.FontTextView;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.GroveDriverListResponse;
import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.NodeJson;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SetupIotNodeActivity extends BaseActivity
        implements GroveFilterRecyclerAdapter.MainViewHolder.MyItemClickListener,
        View.OnClickListener, View.OnDragListener, View.OnLongClickListener,
        GroveI2cListRecyclerAdapter.OnLongClickListener, GroveListRecyclerAdapter.OnLongClickListener {

    private static final String TAG = "SetupIotNodeActivity";
    private static final String GROVE_REMOVE = "grove/remove";
    private static final String GROVE_ADD = "grove/add";
    private static final int ADD_I2C_GROVE = 0x00;
    private static final int ADD_GROVE = 0x01;
    private static final int RMV_I2C_GROVE = 0x02;
    private static final int RMV_GROVE = 0x03;

    private static final int MESSAGE_UPDATE_DONE = 0x10;

    Node node;
    User user;
    List<PinConfig> pinConfigs = new ArrayList<>();

    GroveI2cListRecyclerAdapter mGroveI2cListAdapter;

    GroveListRecyclerAdapter mGroveListAdapter;

    GroveFilterRecyclerAdapter mGroveTypeListAdapter;
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.grove_selector)
    RecyclerView mGroveTypeListView;
    @InjectView(R.id.grove_list)
    RecyclerView mGroveListView;
    @InjectView(R.id.mIvRemove)
    ImageView mIvRemove;
    @InjectView(R.id.mRlRemove)
    RelativeLayout mRlRemove;
    @InjectView(R.id.mBtnUpdate)
    FontButton mBtnUpdate;
    @InjectView(R.id.node_view)
    ImageView nodeView;
    @InjectView(R.id.mNodeGrove_01)
    SimpleDraweeView grove0;
    @InjectView(R.id.mNodeGrove_02)
    SimpleDraweeView grove1;
    @InjectView(R.id.grove_i2c_list)
    RecyclerView groveI2cList;
    @InjectView(R.id.set_node)
    RelativeLayout mSetNodeLayout;
    @InjectView(R.id.setup_iot_node)
    LinearLayout setupIotNode;
    @InjectView(R.id.mTvUpdate)
    FontTextView mTvUpdate;
    @InjectView(R.id.mIvUpdate)
    ImageView mIvUpdate;
    @InjectView(R.id.mRlUpdate)
    RelativeLayout mRlUpdate;
    private List<GroverDriver> mGroveDrivers;

    GrovePinsView mGrovePinsView;
    private ImageView mDragRemoveView;
    private Animation animation;
    private boolean isUpdateIng = false;
    CustomProgressDialog progressDialog;
    int progress = 0;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_node);
        ButterKnife.inject(this);

        initView();
        initData();
        initHandler();
    }

    private void initView() {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mRlRemove.setOnDragListener(this);
        mSetNodeLayout.setOnClickListener(this);
        mBtnUpdate.setSelected(true);
    }

    private void initData() {
        mGroveDrivers = DBHelper.getGrovesAll();
        String node_sn = getIntent().getStringExtra("node_sn");
        node = DBHelper.getNodes(node_sn).get(0);

        mGrovePinsView = new GrovePinsView(this, setupIotNode, node);
        for (ImageView pinView : mGrovePinsView.pinViews) {
            pinView.setOnDragListener(this);
            pinView.setOnClickListener(this);
            pinView.setOnLongClickListener(this);
        }

        pinConfigs = PinConfigDBHelper.getPinConfigs(node.node_sn);
        getSupportActionBar().setTitle(node.name);
        if (node.online) {
            mToolbar.setLogo(R.mipmap.online_led);
        } else {
            mToolbar.setLogo(R.mipmap.offline_led);
        }
        user = UserLogic.getInstance().getUser();

        if (mGroveListView != null) {
            mGroveListView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mGroveListView.setLayoutManager(layoutManager);
            mGroveListAdapter = new GroveListRecyclerAdapter(mGroveDrivers);
            mGroveListAdapter.setOnLongClickListener(this);
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
        pinBadgeUpdateAll();
    }

    private void pinBadgeUpdateAll() {
        for (int i = 0; i < mGrovePinsView.pinViews.length; i++) {
            pinBadgeUpdate(i);
        }
    }

    private void pinBadgeUpdate(int position) {
        if (pinDeviceCount(position) > 1) {
            //  mGrovePinsView.badgeViews[position].setBadgeCount(pinDeviceCount(position));
            //   mGrovePinsView.badgeViews[position].setVisibility(View.VISIBLE);

        } else {
            // mGrovePinsView.badgeViews[position].setVisibility(View.GONE);
        }
    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ADD_I2C_GROVE: {
                        pinBadgeUpdateAll();
                    }
                    break;
                    case ADD_GROVE: {
                    }
                    case RMV_I2C_GROVE: {
                        PinConfig pinConfig = (PinConfig) msg.obj;
                        int position = pinConfig.position;
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
                        new AlertDialog.Builder(SetupIotNodeActivity.this)
                                .setTitle(R.string.update)
                                .setMessage(message)
                                .setPositiveButton(R.string.ok, null)
                                .show();
                    }
                    break;
                }
            }

           /* private boolean isI2cInterface(int position) {
                GrovePinsView.Tag tag = (GrovePinsView.Tag) mGrovePinsView.pinViews[position].getTag();
                return Arrays.asList(tag.interfaceTypes).contains(InterfaceType.I2C);
            }*/
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        getGrovesData();
    }

    @Override
    protected void onDestroy() {
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
        } else if (id == R.id.update) {
            List<String> menu = new ArrayList<>();
            menu.add("View API");
            menu.add("Device Setting");
            DialogUtils.showMenuPopWindow(this, mToolbar, menu, new DialogUtils.OnMenuItemChickListener() {
                @Override
                public void chickItem(View v, int position) {
                    Intent intent;
                    switch (position) {
                        case 0:
                            MobclickAgent.onEvent(SetupIotNodeActivity.this, "15003");
                            NodeJson node_josn = new NodeConfigHelper().getConfigJson(pinConfigs, node);
                            if (node_josn.connections.isEmpty()) {
                                DialogUtils.showErrorDialog(SetupIotNodeActivity.this, "Tip", "OK", "", "Forger add grove?", null);
                            } else {
                                intent = new Intent(SetupIotNodeActivity.this, WebActivity.class);
                                intent.putExtra("node_sn", node.node_sn);
                                startActivity(intent);
                            }
                            break;
                        case 1:
                            MobclickAgent.onEvent(SetupIotNodeActivity.this, "15004");
                            intent = new Intent(SetupIotNodeActivity.this, NodeSettingActivity.class);
                            intent.putExtra(NodeSettingActivity.Intent_NodeSn, node.node_sn);
                            startActivity(intent);
                            break;
                    }
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startUpdate() {
        if (node.name == null)
            return;

        NodeJson node_josn = new NodeConfigHelper().getConfigJson(pinConfigs, node);
        if (node_josn.connections.isEmpty()) {
            DialogUtils.showErrorDialog(this, "Tip", "OK", "", "Forger add grove?", null);
            return;
        }
        mBtnUpdate.setVisibility(View.GONE);
        mRlRemove.setVisibility(View.GONE);
        progressDialog = new CustomProgressDialog(this, R.style.AlertDialogBg);
        setProgressMsg("Preparing Server (10%)");
        isUpdateIng = true;
        ConfigDeviceLogic.getInstance().updateFirware(node.node_key, node_josn);
    }

    private void stopUpdate() {
        isUpdateIng = false;
        mBtnUpdate.setVisibility(View.VISIBLE);
        mRlRemove.setVisibility(View.GONE);
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
    @OnClick(R.id.mRlUpdate)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mNodeGrove_01:
            case R.id.mNodeGrove_02:
                GrovePinsView.Tag tag = (GrovePinsView.Tag) v.getTag();
                int position = tag.position;
                if (pinDeviceCount(position) > 1)
                    displayI2cListView(position);
                break;
            case R.id.mRlUpdate:
                MobclickAgent.onEvent(SetupIotNodeActivity.this, "15002");
                if (isUpdateIng) {
                    return;
                } else {
                    startUpdate();
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.mNodeGrove_01:
            case R.id.mNodeGrove_02:
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
        if (pinDeviceCount(position) <= 1) {
            return;
        }
        List<PinConfig> pinConfigs = new ArrayList<>();
        for (PinConfig p : this.pinConfigs) {
            if (p.position == position)
                pinConfigs.add(p);
        }
        DialogUtils.showRemoveGroveDialog(this, pinConfigs, new DialogUtils.OnItemRemoveClickListenter() {
            @Override
            public void onRemoveItem(Dialog dialog, PinConfig pinConfig, int position, int totalPin) {
                removeGrove(pinConfig);
                Message message = Message.obtain();
                message.what = RMV_I2C_GROVE;
                message.obj = pinConfig;
                mHandler.sendMessage(message);
                if (totalPin < 1) {
                    dialog.dismiss();
                }
            }
        });
    }

    private int pinDeviceCount(int position) {
        int count = 0;
        for (PinConfig pinConfig : pinConfigs) {
            if (pinConfig.position == position)
                count++;
        }
        return count;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onDrag(View v, DragEvent event) {
        int action = event.getAction();

        switch (v.getId()) {
            case R.id.mNodeGrove_01:
            case R.id.mNodeGrove_02:
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
                        ImgUtil.displayImg((SimpleDraweeView) v, groverDriver.ImageURL, R.mipmap.grove_default);
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
            case R.id.mRlRemove:
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED: {
                        return event.getClipDescription().hasMimeType(GROVE_REMOVE);
                    }
                    case DragEvent.ACTION_DRAG_ENTERED:
                        mIvRemove.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        mIvRemove.setColorFilter(Color.RED, PorterDuff.Mode.DST);
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
                        break;
                    }
                    case DragEvent.ACTION_DRAG_ENDED:
                        mIvRemove.setColorFilter(Color.RED, PorterDuff.Mode.DST);
                        mRlRemove.setVisibility(View.INVISIBLE);
                        mBtnUpdate.setVisibility(View.VISIBLE);
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
                updateGroveListAdapter(g);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, error.getLocalizedMessage());
            }
        });
    }

    @Override
    public String[] monitorEvents() {
        return new String[]{Cmd_UpdateFirware, Cmd_UpdateFirwareStute};
    }

    @Override
    public void onEvent(String event, int ret, String errInfo, Object[] data) {
        if (Cmd_UpdateFirwareStute.equals(event)) {
            if (ret == ConfigDeviceLogic.UPDATE_DONE) {
                stopUpdate();
                App.showToastShrot("Firmware Updated!");
            } else if (ret == ConfigDeviceLogic.UPDATEING) {
                if (progress <=80) {
                    progress += 12;
                    setProgressMsg("Preparing Server (" + progress + "%)");
                }
            }else if (ret == ConfigDeviceLogic.FAIL) {
                if (data != null && data.length > 0) {
                    DialogBean bean = (DialogBean) data[0];
                    showErrDialog(bean);
                }
            }
        } else if (Cmd_UpdateFirware.equals(event)) {
            if (ret == ConfigDeviceLogic.FAIL) {
                if (data != null && data.length > 0) {
                    DialogBean bean = (DialogBean) data[0];
                    showErrDialog(bean);
                }
            }else if (ret == ConfigDeviceLogic.SUCCESS){
                setProgressMsg("Preparing Server (40%)");
                progress = 40;
            }
        }
    }

    private void showErrDialog(final DialogBean bean) {
        DialogUtils.showErrorDialog(SetupIotNodeActivity.this, bean.title, bean.okName, bean.cancelName, bean.content, new DialogUtils.OnErrorButtonClickListenter() {
            @Override
            public void okClick() {
                if (bean.okName.equals(Constant.DialogButtonText.TRY_AGAIN.getValue())) {
                    startUpdate();
                } else {
                    stopUpdate();
                }
            }

            @Override
            public void cancelClick() {
                stopUpdate();
            }
        });
    }

    private void setProgressMsg(String msg){
        if (progressDialog != null && progressDialog.isShowing()){
            progressDialog.setMessage(msg);
        }
    }
}


