package cc.seeed.iot.activity;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.add_step.Step01GoReadyActivity;
import cc.seeed.iot.activity.add_step.Step04ApConnectActivity;
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
import cc.seeed.iot.util.ComparatorUtils;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.ToolUtil;
import cc.seeed.iot.view.CustomProgressDialog;
import cc.seeed.iot.view.FontButton;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.GroveDriverListResponse;
import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.NodeJson;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SetupDeviceActivity extends BaseActivity
        implements GroveFilterRecyclerAdapter.MainViewHolder.MyItemClickListener,
        View.OnClickListener, View.OnDragListener, View.OnLongClickListener,
        GroveI2cListRecyclerAdapter.OnLongClickListener, GroveListRecyclerAdapter.OnLongClickListener, GroveListRecyclerAdapter.OnItemClickListener {

    private static final String TAG = "SetupIotLinkActivity";
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
    @InjectView(R.id.mBtnUpdate)
    FontButton mBtnUpdate;
    @InjectView(R.id.node_view)
    ImageView nodeView;
    @InjectView(R.id.mLinkGrove_01)
    ImageButton grove0;
    @InjectView(R.id.mLinkGrove_02)
    ImageButton grove1;
    @InjectView(R.id.mLinkGrove_03)
    ImageButton grove2;
    @InjectView(R.id.mLinkGrove_04)
    ImageButton grove3;
    @InjectView(R.id.mLinkGrove_05)
    ImageButton grove4;
    @InjectView(R.id.mLinkGrove_06)
    ImageButton grove5;
    @InjectView(R.id.mNodeGrove_01)
    ImageButton grove6;
    @InjectView(R.id.mNodeGrove_02)
    ImageButton grove7;
    @InjectView(R.id.set_link)
    RelativeLayout mSetNodeLayout;
    @InjectView(R.id.setup_device)
    RelativeLayout mSetupDevice;
    @InjectView(R.id.mRlRemove)
    RelativeLayout mRlRemove;
    @InjectView(R.id.mWioLinkLayout)
    RelativeLayout mWioLinkLayout;
    @InjectView(R.id.mWioNodeLayout)
    RelativeLayout mWioNodeLayout;

    private List<GroverDriver> mGroveDrivers;
    int progress = 0;
    CustomProgressDialog progressDialog;
    GrovePinsView mGrovePinsView;
    private boolean isUpdateIng = false;
    private NodeJson old_node_josn;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_device);
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
        Collections.sort(mGroveDrivers, new ComparatorUtils.ComparatorName());
        while (true) {
            if (mGroveDrivers == null || mGroveDrivers.size() == 0) {
                break;
            }
            int num = (int) ToolUtil.getSimpleName(mGroveDrivers.get(0).GroveName).charAt(0);
            if ((num >= 'a' && num <= 'z') || (num >= 'A' && num <= 'Z')) {
                break;
            } else {
                mGroveDrivers.add(mGroveDrivers.size(), mGroveDrivers.get(0));
                mGroveDrivers.remove(0);
            }
        }

        user = UserLogic.getInstance().getUser();
        getGrovesData();
        String node_sn = getIntent().getStringExtra("node_sn");
        node = DBHelper.getNodes(node_sn).get(0);

        if (node.board.equals(Constant.WIO_LINK_V1_0)) {
            mWioLinkLayout.setVisibility(View.VISIBLE);
            mWioNodeLayout.setVisibility(View.GONE);
        } else {
            mWioLinkLayout.setVisibility(View.GONE);
            mWioNodeLayout.setVisibility(View.VISIBLE);
        }

        List<PinConfig> list = PinConfigDBHelper.getPinConfigs(node.node_sn);
        old_node_josn = new NodeConfigHelper().getConfigJson(list, node);

        mGrovePinsView = new GrovePinsView(this, mSetupDevice, node);
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

        if (mGroveListView != null) {
            mGroveListView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mGroveListView.setLayoutManager(layoutManager);
            mGroveListAdapter = new GroveListRecyclerAdapter(mGroveDrivers);
            mGroveListAdapter.setOnLongClickListener(this);
            mGroveListAdapter.setOnItemClickListener(this);
            mGroveListView.setAdapter(mGroveListAdapter);
        }

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
            mGrovePinsView.badgeViews[position].setText("+" + pinDeviceCount(position));
            mGrovePinsView.badgeViews[position].setVisibility(View.VISIBLE);
        } else {
            mGrovePinsView.badgeViews[position].setVisibility(View.GONE);
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
                    break;
                    case RMV_I2C_GROVE: {
                        PinConfig pinConfig = (PinConfig) msg.obj;
                        int position = pinConfig.position;
                        pinBadgeUpdateAll();
                        if (pinDeviceCount(position) == 0) {
                            mGrovePinsView.pinViews[pinConfig.position].setImageDrawable(null);
                            mGrovePinsView.pinViews[pinConfig.position].setActivated(false);
                            mGrovePinsView.pinViews[pinConfig.position].setPressed(false);
                        }else
                            mGrovePinsView.updatePin(pinConfigs, position);
                    }
                    break;

                    case RMV_GROVE: {
                        PinConfig pinConfig = (PinConfig) msg.obj;
                        mGrovePinsView.pinViews[pinConfig.position].setImageDrawable(null);
                        mGrovePinsView.pinViews[pinConfig.position].setActivated(false);
                        mGrovePinsView.pinViews[pinConfig.position].setPressed(false);
                    }
                    break;
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            quitHint();
            //   finish();
            return true;
        } else if (id == R.id.update) {
            List<String> menu = new ArrayList<>();
            menu.add("View API");
            menu.add("Device Setting");
            menu.add("Change Wi-Fi Network");
            menu.add("Help");
            DialogUtils.showMenuPopWindow(this, mToolbar, menu, new DialogUtils.OnMenuItemChickListener() {
                @Override
                public void chickItem(View v, int position) {
                    Intent intent;
                    switch (position) {
                        case 0:
                            MobclickAgent.onEvent(SetupDeviceActivity.this, "15003");
                            NodeJson node_josn = new NodeConfigHelper().getConfigJson(pinConfigs, node);
                            if (node_josn.connections.isEmpty()) {
                                DialogUtils.showErrorDialog(SetupDeviceActivity.this, "Tip", "OK", "", "Sure leave without updating hardware?", null);
                            } else {
                                intent = new Intent(SetupDeviceActivity.this, WebActivity.class);
                                intent.putExtra(WebActivity.Intent_Url, ToolUtil.getApiUrl(node));
                                startActivity(intent);
                            }
                            break;
                        case 1:
                            MobclickAgent.onEvent(SetupDeviceActivity.this, "15004");
                            intent = new Intent(SetupDeviceActivity.this, NodeSettingActivity.class);
                            intent.putExtra(NodeSettingActivity.Intent_NodeSn, node.node_sn);
                            startActivity(intent);
                            break;
                        case 2:
                            intent = new Intent(SetupDeviceActivity.this, Step01GoReadyActivity.class);
                            intent.putExtra(Step04ApConnectActivity.Intent_NodeSn, node.node_sn);
                            startActivity(intent);
                            break;
                        case 3:
                            intent = new Intent(SetupDeviceActivity.this, HelpActivity.class);
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
            DialogUtils.showErrorDialog(this, "Tip", "OK", "", "No Grove was found in API. Please update hardware and try again.", null);
            return;
        }

        if (!node.online) {
            DialogUtils.showErrorDialog(this, "Tip", "OK", "", "Node is offline", null);
            return;
        }
        mBtnUpdate.setEnabled(false);
        mBtnUpdate.setSelected(false);
        progressDialog = new CustomProgressDialog(this, R.style.AlertDialogBg);
        setProgressMsg("Preparing Server (10%)");
        mRlRemove.setVisibility(View.GONE);
        isUpdateIng = true;
        ConfigDeviceLogic.getInstance().updateFirware(node.node_key, node_josn);
    }

    private void stopUpdate() {
        isUpdateIng = false;
        progressDialog.dismiss();
        mRlRemove.setVisibility(View.GONE);
        mBtnUpdate.setEnabled(true);
        mBtnUpdate.setSelected(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            quitHint();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void quitHint() {
        NodeJson node_josn = new NodeConfigHelper().getConfigJson(pinConfigs, node);
        if (old_node_josn.connections.toString().equals(node_josn.connections.toString())) {
            finish();
        } else {
            DialogUtils.showErrorDialog(this, "", "OK", "Cancel", "Sure leave without updating hardware?", new DialogUtils.OnErrorButtonClickListenter() {
                @Override
                public void okClick() {
                    finish();
                }

                @Override
                public void cancelClick() {

                }
            });
        }
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
    @OnClick(R.id.mBtnUpdate)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mNodeGrove_01:
            case R.id.mNodeGrove_02:
            case R.id.mLinkGrove_06:
                GrovePinsView.Tag tag = (GrovePinsView.Tag) v.getTag();
                int position = tag.position;
                displayI2cListView(position);
                break;
            case R.id.mBtnUpdate:
                MobclickAgent.onEvent(this, "15002");
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
        GrovePinsView.Tag tag = (GrovePinsView.Tag) v.getTag();
        int position = tag.position;
        if (node.board.equals(Constant.WIO_LINK_V1_0)) {
            switch (v.getId()) {
                case R.id.mLinkGrove_01:
                case R.id.mLinkGrove_02:
                case R.id.mLinkGrove_03:
                case R.id.mLinkGrove_04:
                case R.id.mLinkGrove_05:
                    if (pinDeviceCount(position) == 1) {
                        startDragRemove(v);
                    }
                    break;
                case R.id.mLinkGrove_06:
                    displayI2cListView(position);
                    break;
            }
        } else {
            switch (v.getId()) {
                case R.id.mNodeGrove_01:
                case R.id.mNodeGrove_02:
                    if (pinDeviceCount(position) == 1) {
                        startDragRemove(v);
                    } else if (pinDeviceCount(position) > 1) {
                        displayI2cListView(position);
                    }
                    break;
            }
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
            case R.id.mLinkGrove_01:
            case R.id.mLinkGrove_02:
            case R.id.mLinkGrove_03:
            case R.id.mLinkGrove_04:
            case R.id.mLinkGrove_05:
            case R.id.mLinkGrove_06:
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

                        v.setPressed(true);
                        ((ImageView) v).setImageAlpha(64);
                    }
                    break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        v.setPressed(false);
                        ((ImageView) v).setImageAlpha(64);
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        v.setPressed(true);
                        ((ImageView) v).setImageAlpha(64);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        v.setPressed(false);
                        ((ImageView) v).setImageAlpha(255);
                        break;
                    case DragEvent.ACTION_DROP: {
                        GroverDriver groverDriver = (GroverDriver) event.getLocalState();
                        UrlImageViewHelper.setUrlDrawable((ImageView) v, groverDriver.ImageURL,
                                R.mipmap.grove_default, UrlImageViewHelper.CACHE_DURATION_INFINITE);
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
                        v.setActivated(true);

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
        mRlRemove.setVisibility(View.VISIBLE);
        mBtnUpdate.setVisibility(View.GONE);

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
                mRlRemove.setVisibility(View.VISIBLE);

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
                Collections.sort(g, new ComparatorUtils.ComparatorName());
                while (true) {
                    if (g == null || g.size() == 0) {
                        break;
                    }
                    int num = (int) ToolUtil.getSimpleName(g.get(0).GroveName).charAt(0);
                    if ((num >= 'a' && num <= 'z') || (num >= 'A' && num <= 'Z')) {
                        break;
                    } else {
                        g.add(g.size(), g.get(0));
                        g.remove(0);
                    }
                }
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
                old_node_josn = new NodeConfigHelper().getConfigJson(pinConfigs, node);
                //  DialogUtils.showErrorDialog(SetupIotLinkActivity.this, "", "OK", "", "Firware Updated!", null);
                App.showToastShrot("Firmware Updated!");
            } else if (ret == ConfigDeviceLogic.UPDATEING) {
                if (progress <= 80) {
                    progress += 12;
                    setProgressMsg("Preparing Server (" + progress + "%)");
                }
            } else if (ret == ConfigDeviceLogic.FAIL) {
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
            } else if (ret == ConfigDeviceLogic.SUCCESS) {
                setProgressMsg("Preparing Server (40%)");
                progress = 40;
            }
        }
    }

    private void showErrDialog(final DialogBean bean) {
        DialogUtils.showErrorDialog(SetupDeviceActivity.this, bean.title, bean.okName, bean.cancelName, bean.content, new DialogUtils.OnErrorButtonClickListenter() {
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

    private void setProgressMsg(String msg) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setMessage(msg);
        }
    }

    @Override
    public void onItemClick(GroverDriver grove, int position) {
        Intent intent = new Intent(SetupDeviceActivity.this, GroveDetailActivity.class);
        intent.putExtra(GroveDetailActivity.Intent_GroveSku, grove.SKU);
        startActivity(intent);
    }
}


