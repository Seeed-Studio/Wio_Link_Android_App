package cc.seeed.iot.ui_main;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.activity.GrovesActivity;
import cc.seeed.iot.activity.HelpActivity;
import cc.seeed.iot.activity.NodeSettingActivity;
import cc.seeed.iot.activity.SetupDeviceActivity;
import cc.seeed.iot.activity.TestActivity;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.logic.ConfigDeviceLogic;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.activity.add_step.Step01GoReadyActivity;
import cc.seeed.iot.ui_setnode.model.NodeConfigHelper;
import cc.seeed.iot.util.ComparatorUtils;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.ImgUtil;
import cc.seeed.iot.util.MLog;
import cc.seeed.iot.util.ToolUtil;
import cc.seeed.iot.view.FontTextView;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.GroveDriverListResponse;
import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.NodeConfigResponse;
import cc.seeed.iot.webapi.model.NodeJson;
import cc.seeed.iot.webapi.model.NodeListResponse;
import cc.seeed.iot.webapi.model.SuccessResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainScreenActivity extends BaseActivity
        implements NodeListRecyclerAdapter.OnClickListener, View.OnClickListener, Animation.AnimationListener, NodeListRecyclerAdapter.OnItemLongClickListener {
    private static final String TAG = "MainScreenActivity";
    private static final int MESSAGE_GROVE_LIST_START = 0x00;
    private static final int MESSAGE_GROVE_LIST_COMPLETE = 0x01;
    private static final int MESSAGE_NODE_LIST_START = 0x02;
    private static final int MESSAGE_NODE_LIST_COMPLETE = 0x03;
    private static final int MESSAGE_NODE_CONFIG_COMPLETE = 0x04;
    NavigationView navview;
    DrawerLayout drawerlayout;

    Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private SimpleDraweeView mSDVAvatar;
    private LinearLayout mLLUserInfo;
    private FontTextView mTvEmail;
    private FontTextView mTvDeviceNum;
    private TextView mTvSupportDevices;
    private TextView mTvFAQ;
    private TextView mTvGetDevices;
    private TextView mTvSetting;
    private TextView mTVAbout;
    private TextView mTvUpdateApp;
    private NavigationView navigationView;
    private LinearLayout mLLNoDevice;
    private Button mBtnAddDevice;
    private RelativeLayout mRlSelectAddDevies;
    private CoordinatorLayout mMainContent;
    private LinearLayout mLLWioLink;
    private LinearLayout mLLWioNode;
    private LinearLayout mLLAddDevice;
    private ImageView mIvAddDevice;
    private SwipeRefreshLayout mSRL;

    //    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private NodeListRecyclerAdapter mAdapter;
    private ImageView mAddTip;
    private List<Node> nodes;
    private User user;
    private boolean firstUseState;

    private Handler mHandler;
    private ProgressDialog mProgressDialog;
    private boolean isShowPopWindow = false;
    private boolean isAnimationRuning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();
        initMenu();

        if (firstUseState) {
            Message message = Message.obtain();
            message.what = MESSAGE_GROVE_LIST_START;
            mHandler.sendMessage(message);
            getGrovesData();
        }
    }

    private void initView() {
        mLLNoDevice = (LinearLayout) findViewById(R.id.mLLNoDevice);
        mBtnAddDevice = (Button) findViewById(R.id.mBtnAddDevice);
        mRlSelectAddDevies = (RelativeLayout) findViewById(R.id.mRlSelectAddDevies);
        mMainContent = (CoordinatorLayout) findViewById(R.id.main_content);
        mLLWioLink = (LinearLayout) findViewById(R.id.mLLWioLink);
        mLLWioNode = (LinearLayout) findViewById(R.id.mLLWioNode);
        mLLAddDevice = (LinearLayout) findViewById(R.id.mLLAddDevice);
        mIvAddDevice = (ImageView) findViewById(R.id.mIvAddDevice);
        mSRL = (SwipeRefreshLayout) findViewById(R.id.mSRL);
        mSRL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSRL.setRefreshing(true);
                getNodeList();
            }
        });

        mBtnAddDevice.setOnClickListener(this);
        mLLWioLink.setOnClickListener(this);
        mLLWioNode.setOnClickListener(this);
        mLLAddDevice.setOnClickListener(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.mipmap.menu);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("Devices");
        }
        toolbar.setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.listview);
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layout = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(layout);
            //     mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.divider)));
            mAdapter = new NodeListRecyclerAdapter(nodes);
            mAdapter.setOnClickListener(this);
            mAdapter.setOnItemLongClickListener(this);
            mRecyclerView.setAdapter(mAdapter);
        }

        FloatingActionButton setupLinkAction = (FloatingActionButton) findViewById(R.id.setup_link);
        FloatingActionButton setupNodeAction = (FloatingActionButton) findViewById(R.id.setup_node);
        setupLinkAction.setOnClickListener(this);
        setupNodeAction.setOnClickListener(this);

        mProgressDialog = new ProgressDialog(this);
    }


    private void initMenu() {

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            View headerLayout = navigationView.inflateHeaderView(R.layout.menu_layout);
            if (headerLayout != null) {
                mLLUserInfo = (LinearLayout) headerLayout.findViewById(R.id.mLLUserInfo);
                mTvEmail = (FontTextView) headerLayout.findViewById(R.id.mTvEmail);
                mSDVAvatar = (SimpleDraweeView) headerLayout.findViewById(R.id.mSDVAvatar);
                mTvDeviceNum = (FontTextView) headerLayout.findViewById(R.id.mTvDeviceNum);
                mTvSupportDevices = (TextView) headerLayout.findViewById(R.id.mTvSupportDevices);
                mTvFAQ = (TextView) headerLayout.findViewById(R.id.mTvFAQ);
                mTvGetDevices = (TextView) headerLayout.findViewById(R.id.mTvGetDevices);
                mTvSetting = (TextView) headerLayout.findViewById(R.id.mTvSetting);
                mTVAbout = (TextView) headerLayout.findViewById(R.id.mTVAbout);
                mTvUpdateApp = (TextView) headerLayout.findViewById(R.id.mTvUpdateApp);

                mTvEmail.setText(user.email);
                ImgUtil.displayImg(mSDVAvatar, user.avater, R.mipmap.icon);
                mLLUserInfo.setOnClickListener(this);
                mTvEmail.setOnClickListener(this);
                mSDVAvatar.setOnClickListener(this);
                mTvDeviceNum.setOnClickListener(this);
                mTvSupportDevices.setOnClickListener(this);
                mTvFAQ.setOnClickListener(this);
                mTvGetDevices.setOnClickListener(this);
                mTvSetting.setOnClickListener(this);
                mTVAbout.setOnClickListener(this);
                mTvUpdateApp.setOnClickListener(this);
            }
        }
    }

    private void initData() {

        user = UserLogic.getInstance().getUser();
        nodes = DBHelper.getNodesAll();
        firstUseState = ((App) MainScreenActivity.this.getApplication()).getFirstUseState();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_GROVE_LIST_START:
                        mProgressDialog.setMessage("");
                        mProgressDialog.setCanceledOnTouchOutside(false);
                        mProgressDialog.show();
                        break;
                    case MESSAGE_GROVE_LIST_COMPLETE:
                        mProgressDialog.dismiss();
                        ((App) MainScreenActivity.this.getApplication()).setFirstUseState(false);
                        break;
                    case MESSAGE_NODE_LIST_START:
                        mProgressDialog.setMessage("");
                        mProgressDialog.setCanceledOnTouchOutside(false);
                        mProgressDialog.show();
                        break;

                    case MESSAGE_NODE_LIST_COMPLETE:
                        mProgressDialog.dismiss();
                        mSRL.setRefreshing(false);
                        if (msg.arg2 == 1) {
                            Collections.sort(nodes, new ComparatorUtils.ComparatorNode());
                            mAdapter.updateAll(nodes);
                            if (nodes.isEmpty()) {
                                mTvDeviceNum.setText("0 DEVICES");
                                mLLNoDevice.setVisibility(View.VISIBLE);
                                mSRL.setVisibility(View.GONE);
                            } else {
                                mLLNoDevice.setVisibility(View.GONE);
                                mSRL.setVisibility(View.VISIBLE);
                                mTvDeviceNum.setText(nodes.size() + " DEVICES");
                            }

                            for (Node n : nodes) {
                                getNodesConfig(n, nodes.indexOf(n));
                            }
                        } else if (msg.arg2 == 0) {
                            mTvDeviceNum.setText("0 DEVICES");
                        }
                        break;
                    case MESSAGE_NODE_CONFIG_COMPLETE:
                        Node node = (Node) msg.obj;
                        int position = msg.arg1;
                        mAdapter.updateItem(position);
                        break;
                }
            }
        };
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float downX = ev.getX();
                float downY = ev.getY();
                if (isAnimationRuning) {
                    return true;
                } else {
                    if (isShowPopWindow) {
                        if (downX > mRlSelectAddDevies.getTop() && downX < mRlSelectAddDevies.getRight() &&
                                downY > mRlSelectAddDevies.getTop() && downY < mRlSelectAddDevies.getBottom()) {
                            hideSelectAddDevicePopWindow();
                        } else {
                            hideSelectAddDevicePopWindow();
                            return true;
                        }

                    }
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //  getMenuInflater().inflate(R.menu.ui_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
          /*  case R.id.update:
               *//* Message message = Message.obtain();
                message.what = MESSAGE_NODE_LIST_START;
                mHandler.sendMessage(message);
                getNodeList();*//*
                showSelectAddDevicePopWindow();
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getNodeList();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DialogUtils.showQuitDialog(MainScreenActivity.this);
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.setup_link:
                setupActivity(Constant.WIO_LINK_V1_0);
                break;
            case R.id.setup_node:
                setupActivity(Constant.WIO_NODE_V1_0);
                break;
            case R.id.mSDVAvatar:
                break;
            case R.id.mTvEmail:
                break;
            case R.id.mTvDeviceNum:
                break;
            case R.id.mTvSupportDevices:
                MobclickAgent.onEvent(this, "12001");
                intent = new Intent(MainScreenActivity.this, GrovesActivity.class);
                startActivity(intent);
                break;
            case R.id.mTvFAQ:
                intent = new Intent(MainScreenActivity.this, HelpActivity.class);
                startActivity(intent);
                MobclickAgent.onEvent(this, "12002");
                break;
            case R.id.mTvGetDevices:
                MobclickAgent.onEvent(this, "12003");
                break;
            case R.id.mTvSetting:
                MobclickAgent.onEvent(this, "12004");
                intent = new Intent(MainScreenActivity.this, MainSettingActivity.class);
                startActivity(intent);
                break;
            case R.id.mTVAbout:
                MobclickAgent.onEvent(this, "12005");
                intent = new Intent(MainScreenActivity.this, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.mTvUpdateApp:
                MobclickAgent.onEvent(this, "12006");
                break;
            case R.id.mBtnAddDevice:
                showSelectAddDevicePopWindow();
                break;
            case R.id.mLLWioNode:
                MobclickAgent.onEvent(this, "13002");
                setupActivity(Constant.WIO_NODE_V1_0);
                break;
            case R.id.mLLWioLink:
                MobclickAgent.onEvent(this, "13001");
                setupActivity(Constant.WIO_LINK_V1_0);
                break;
            case R.id.mLLAddDevice:
                showSelectAddDevicePopWindow();
                break;
            case R.id.toolbar:
                intent = new Intent(MainScreenActivity.this, TestActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void showSelectAddDevicePopWindow() {
        if (!isShowPopWindow && !isAnimationRuning) {
            isShowPopWindow = !isShowPopWindow;
            mRlSelectAddDevies.setVisibility(View.VISIBLE);
            Animation animationAdd = AnimationUtils.loadAnimation(this, R.anim.main_add_device_in);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.main_select_window_in);
            animation.setAnimationListener(this);
            animationAdd.setFillAfter(true);
            mIvAddDevice.startAnimation(animationAdd);
            mRlSelectAddDevies.setAnimation(animation);
        }
    }

    private void hideSelectAddDevicePopWindow() {
        if (isShowPopWindow && !isAnimationRuning) {
            isShowPopWindow = !isShowPopWindow;
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.main_select_window_out);
            Animation animationAdd = AnimationUtils.loadAnimation(this, R.anim.main_add_device_out);
            animation.setAnimationListener(this);
            animationAdd.setFillAfter(true);
            mRlSelectAddDevies.setAnimation(animation);
            mRlSelectAddDevies.setVisibility(View.GONE);
            mIvAddDevice.startAnimation(animationAdd);
        }
    }


    private void setupActivity(String board) {
        ((App) getApplication()).setConfigState(true);
        Intent intent = new Intent(MainScreenActivity.this, Step01GoReadyActivity.class);
        intent.putExtra("board", board);
        startActivity(intent);
    }

    @Override
    public void onClick(View v, final int position) {
        final Node node = mAdapter.getItem(position);
        int id = v.getId();
        nodeSet(node);
    }

    private void nodeSetting(Node node) {
        Intent intent = new Intent(this, NodeSettingActivity.class);
        intent.putExtra("node_sn", node.node_sn);
        startActivity(intent);
    }

    public boolean nodeRemove(final Node node, final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Remove Wio");
        builder.setMessage("Confirm remove?");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final ProgressDialog progressDialog = new ProgressDialog(MainScreenActivity.this);
                progressDialog.setMessage("Wio remove...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                IotApi api = new IotApi();
                User user = UserLogic.getInstance().getUser();
                api.setAccessToken(user.token);
                final IotService iot = api.getService();
                iot.nodesDelete(node.node_sn, new Callback<SuccessResponse>() {
                    @Override
                    public void success(SuccessResponse successResponse, Response response) {
                        progressDialog.dismiss();
                        nodes.remove(node);
                        DBHelper.delNode(node.node_sn);
                        mAdapter.removeItem(position);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        progressDialog.dismiss();
                    }
                });
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();

        return true;
    }

    public boolean nodeApi(Node node) {
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra("node_sn", node.node_sn);
        startActivity(intent);
        return true;
    }

    public boolean nodeSet(Node node) {
        // check database is correct?
        MobclickAgent.onEvent(this, "13004");
        try {
            DBHelper.getNodes(node.node_sn).get(0);
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "DBHelper.getNodes(node.node_sn).get(0) is null!");
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainScreenActivity.this);
            builder.setTitle("Error");
            builder.setMessage("The wio data is destroyed. Please CLEAR DATA on App Setting.");
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            android.app.AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }
        Intent intent = new Intent(this, SetupDeviceActivity.class);
       /* if (node.board.equals(Constant.WIO_LINK_V1_0)) {
            intent.setClass(this, SetupIotLinkActivity.class);
        } else if (node.board.equals(Constant.WIO_NODE_V1_0)) {
            intent.setClass(this, SetupIotNodeActivity.class);
        }*/
        intent.putExtra("node_sn", node.node_sn);
        startActivity(intent);
        return true;
    }

    private void getNodeList() {
        IotApi api = new IotApi();
        api.setAccessToken(user.token);
        final IotService iot = api.getService();
        iot.nodesList(new Callback<NodeListResponse>() {
            @Override
            public void success(NodeListResponse nodeListResponse, Response response) {
                List<Node> get_nodes = nodeListResponse.nodes;
                ArrayList<Node> delNodes = new ArrayList<Node>();
                for (Node n : get_nodes) {
                    if (n.name.equals("node000")) {
                        iot.nodesDelete(n.node_sn, new Callback<SuccessResponse>() {
                            @Override
                            public void success(SuccessResponse successResponse, Response response) {

                            }

                            @Override
                            public void failure(RetrofitError error) {

                            }
                        });
                        delNodes.add(n);
                    }
                }
                get_nodes.removeAll(delNodes);
                nodes = get_nodes;

                DBHelper.saveNodes(nodes);

                Message message = Message.obtain();
                message.arg2 = 1;
                message.what = MESSAGE_NODE_LIST_COMPLETE;
                mHandler.sendMessage(message);
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(MainScreenActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                Message message = Message.obtain();
                message.arg2 = 0;
                message.what = MESSAGE_NODE_LIST_COMPLETE;
                mHandler.sendMessage(message);
            }
        });

    }

    private void getNodesConfig(final Node node, final int position) {
        IotApi api = new IotApi();
        api.setAccessToken(node.node_key);
        final IotService iot = api.getService();
        iot.nodeConfig(new Callback<NodeConfigResponse>() {
            @Override
            public void success(NodeConfigResponse nodeConfigResponse, Response response) {
                if (nodeConfigResponse.type.equals("yaml")) {
                    Log.e(TAG, "do not support!");
                } else if (nodeConfigResponse.type.equals("json")) {
                    NodeJson nodeJson = nodeConfigResponse.config;
                    new NodeConfigHelper().saveToDB(nodeJson, node);
                }

                Message message = Message.obtain();
                message.arg1 = position;
                message.what = MESSAGE_NODE_CONFIG_COMPLETE;
                mHandler.sendMessage(message);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, error.getLocalizedMessage());
            }
        });
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

                Message message = Message.obtain();
                message.what = MESSAGE_GROVE_LIST_COMPLETE;
                mHandler.sendMessage(message);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, error.getLocalizedMessage());
                Message message = Message.obtain();
                message.what = MESSAGE_GROVE_LIST_COMPLETE;
                mHandler.sendMessage(message);
            }
        });
    }

    @Override
    public void onAnimationStart(Animation animation) {
        isAnimationRuning = true;
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        isAnimationRuning = false;
        if (isShowPopWindow) {
            MLog.e(this, "top: " + mRlSelectAddDevies.getTop());
            MLog.e(this, "left: " + mRlSelectAddDevies.getLeft());
            MLog.e(this, "buttom: " + mRlSelectAddDevies.getBottom());
            MLog.e(this, "right: " + mRlSelectAddDevies.getRight());
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    public void onItemLongClick(View v, int position) {
        MobclickAgent.onEvent(this, "13003");
        final Node node = mAdapter.getItem(position);
        ConfigDeviceLogic.getInstance().removeNode(MainScreenActivity.this, node, position);
        //   nodeRemove(node, position);
    }

    @Override
    public String[] monitorEvents() {
        return new String[]{Cmd_Node_Remove};
    }

    @Override
    public void onEvent(String event, boolean ret, String errInfo, Object[] data) {
        if (Cmd_Node_Remove.equals(event)) {
            if (ret) {
                if (ToolUtil.isTopActivity(MainScreenActivity.this, MainScreenActivity.this.getClass().getSimpleName())) {
                    int position;
                    try {
                        position = Integer.parseInt(errInfo);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    final Node node = mAdapter.getItem(position);
                    nodes.remove(node);
                    mAdapter.removeItem(position);
                    if (nodes.isEmpty()) {
                        mTvDeviceNum.setText("0 DEVICES");
                        mLLNoDevice.setVisibility(View.VISIBLE);
                    } else {
                        mLLNoDevice.setVisibility(View.GONE);
                        mTvDeviceNum.setText(nodes.size() + " DEVICES");
                    }
                }
            } else {
                App.showToastShrot(errInfo);
            }
        }
    }
}