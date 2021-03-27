package cc.seeed.iot.ui_main;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
import cc.seeed.iot.activity.FeedbackActivity;
import cc.seeed.iot.activity.GrovesActivity;
import cc.seeed.iot.activity.HelpActivity;
import cc.seeed.iot.activity.NodeSettingActivity;
import cc.seeed.iot.activity.SetupDeviceActivity;
import cc.seeed.iot.activity.TestActivity;
import cc.seeed.iot.entity.ServerBean;
import cc.seeed.iot.entity.UpdateApkBean;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.logic.ConfigDeviceLogic;
import cc.seeed.iot.logic.SystemLogic;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.activity.add_step.Step01GoReadyActivity;
import cc.seeed.iot.ui_setnode.model.NodeConfigHelper;
import cc.seeed.iot.util.Common;
import cc.seeed.iot.util.CommonUrl;
import cc.seeed.iot.util.ComparatorUtils;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.FileUtil;
import cc.seeed.iot.util.ImgUtil;
import cc.seeed.iot.util.MLog;
import cc.seeed.iot.util.SystemUtils;
import cc.seeed.iot.util.TimeUtils;
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
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
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

    public static final int PHOTOZOOM = 0; // 相册
    public static final int PHOTOTAKE = 1; // 拍照
    ProgressDialog progressDialog;
    Dialog dialog;

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
    private TextView mTvFeedBack;
    private TextView mTvUpdateApp;
    private ImageView mIvUpdateApp;
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
    private ImageView mIvNewGrove;

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
                List<GroverDriver> drivers = DBHelper.getGrovesAll();
                if (drivers == null || drivers.size() == 0)
                    getGrovesData();
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
            ab.setTitle(R.string.main_screen_activity_title);
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
                mTvFeedBack = (TextView) headerLayout.findViewById(R.id.mTvFeedBack);
                mTvUpdateApp = (TextView) headerLayout.findViewById(R.id.mTvUpdateApp);
                mIvUpdateApp = (ImageView) headerLayout.findViewById(R.id.mIvUpdateApp);
                mIvNewGrove = (ImageView) headerLayout.findViewById(R.id.mIvNewGrove);
                mIvNewGrove.setVisibility(DBHelper.isHasNewGrove() ? View.VISIBLE : View.GONE);

                UpdateApkBean updateApkBean = SystemLogic.getInstance().getUpdateApkBean();
                if (updateApkBean == null || TextUtils.isEmpty(updateApkBean.version_name)) {
                    mIvUpdateApp.setVisibility(View.GONE);
                } else {
                    PackageInfo info = SystemUtils.getPackageInfo();
                    if (SystemLogic.getInstance().isUpdate(info.versionName, updateApkBean.version_name)) {
                        mIvUpdateApp.setVisibility(View.VISIBLE);
                    } else {
                        mIvUpdateApp.setVisibility(View.GONE);
                    }
                }

                if (TextUtils.isEmpty(user.email) || user.email.startsWith("testadmin")) {
                    mTvEmail.setText(user.getNickname());
                } else {
                    mTvEmail.setText(user.email);
                }
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
                mTvFeedBack.setOnClickListener(this);
                mTvUpdateApp.setOnClickListener(this);
            }
        }
    }

    private void initData() {
        checkServerTip();
        checkAppVersion();
        if (!isLogin()) {
            return;
        }
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
                        mIvNewGrove.setVisibility(DBHelper.isHasNewGrove() ? View.VISIBLE : View.GONE);
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
                                mTvDeviceNum.setText(R.string.zero_device);
                                mLLNoDevice.setVisibility(View.VISIBLE);
                                mSRL.setVisibility(View.GONE);
                            } else {
                                mLLNoDevice.setVisibility(View.GONE);
                                mSRL.setVisibility(View.VISIBLE);
                                mTvDeviceNum.setText(nodes.size() + " " + getString(R.string.devices));
                            }

                            for (Node n : nodes) {
                                getNodesConfig(n, nodes.indexOf(n));
                            }
                        } else if (msg.arg2 == 0) {
                            mTvDeviceNum.setText(R.string.zero_device);
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
        MobclickAgent.onResume(this);
        user = UserLogic.getInstance().getUser();
        if (TextUtils.isEmpty(user.email) || user.email.startsWith("testadmin")) {
            mTvEmail.setText(user.getNickname());
        } else {
            mTvEmail.setText(user.email);
        }
        ImgUtil.displayImg(mSDVAvatar, user.avater, R.mipmap.icon);
        getNodeList();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            DialogUtils.showQuitDialog(MainScreenActivity.this);
//        }
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
              /*  if (App.getApp().isDefaultServer()) {
                    setAvatarPopWindow(this);
                }*/
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
            case R.id.mTvFeedBack:
                MobclickAgent.onEvent(this, "12007");
                intent = new Intent(MainScreenActivity.this, FeedbackActivity.class);
                startActivity(intent);
                break;
            case R.id.mTvUpdateApp:
                MobclickAgent.onEvent(this, "12006");
                SystemLogic.getInstance().checkUpdateApk(this, true);
                break;
            case R.id.mBtnAddDevice:
                MobclickAgent.onEvent(this, "13006");
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
                MobclickAgent.onEvent(this, "13005");
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
                    if (n.name.equals("node000")||n.name.equals("YouShouldNeverSeeMeInYourApp")) {
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
                if (!isGotoLogin(error.getLocalizedMessage())) {
                    Toast.makeText(MainScreenActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    Message message = Message.obtain();
                    message.arg2 = 0;
                    message.what = MESSAGE_NODE_LIST_COMPLETE;
                    mHandler.sendMessage(message);
                }
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
                isGotoLogin(error.getLocalizedMessage());
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
                    groveDriver.setGroveName(ToolUtil.getSimpleName(groveDriver.GroveName));
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
        return new String[]{Cmd_Node_Remove, Cmd_Update_Avatar, Cmd_Change_User_Info};
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
                        mTvDeviceNum.setText(R.string.zero_device);
                        mLLNoDevice.setVisibility(View.VISIBLE);
                    } else {
                        mLLNoDevice.setVisibility(View.GONE);
                        mTvDeviceNum.setText(nodes.size() + " " + R.string.devices);
                    }
                }
            } else {
                App.showToastShrot(errInfo);
            }
        } else if (Cmd_Update_Avatar.equals(event)) {
            if (ret) {
                if (data != null) {
                    String path = (String) data[0];
                    if (!TextUtils.isEmpty(path)) {
                        UserLogic.getInstance().changeUserInfo(Common.ChangeAvatar, path);
                    }
                }
            } else {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                App.showToastShrot("Network Error,Picture upload fail.");
            }
        } else if (Cmd_Change_User_Info.equals(event)) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (ret) {
                user = UserLogic.getInstance().getUser();
                ImgUtil.displayImg(mSDVAvatar, user.avater, R.mipmap.icon);
            } else {
                App.showToastShrot(errInfo);
            }
        }
    }

    public void setAvatarPopWindow(Activity activity) {

        View view = LayoutInflater.from(activity).inflate(R.layout.popwindow_selete_avatar, null);
        final PopupWindow popWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popWindow.dismiss();
            }
        });
        //initPop(view);
        TextView mTVItem01 = (TextView) view.findViewById(R.id.mTVItem01);
        TextView mTVItem02 = (TextView) view.findViewById(R.id.mTVItem02);
        TextView mTVItem03 = (TextView) view.findViewById(R.id.mTVItem03);
        TextView mTVItem04 = (TextView) view.findViewById(R.id.mTVItem04);

        mTVItem01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        mTVItem02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainScreenActivity.this, MultiImageSelectorActivity.class);
                intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, false);
                intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 1);
                intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
                startActivityForResult(intent, PHOTOZOOM);

                popWindow.dismiss();
            }
        });
        mTVItem03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent();
                mIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(mIntent, PHOTOTAKE);
                popWindow.dismiss();
            }
        });
        mTVItem04.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popWindow.dismiss();
            }
        });


        popWindow.setFocusable(true);
        popWindow.setOutsideTouchable(true);
        popWindow.setBackgroundDrawable(new BitmapDrawable());
        popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popWindow.showAtLocation(new View(activity), Gravity.CENTER, 0, 0);
//        popWindow.showAsDropDown(targetView);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = null;
        switch (requestCode) {
            case PHOTOZOOM://相册
                if (data == null) {
                    return;
                }
                ArrayList<String> list1 = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                if (list1 != null && list1.size() > 0) {
                    String path = list1.get(0);
                    final ImgUtil.CompressInfo compressInfo = ImgUtil.compressBitmap(path, 200, 200);
                    if (compressInfo != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                FileUtil.uploadFile(Cmd_Update_Avatar, compressInfo.path, CommonUrl.Update_Img_Url.getVal());
                            }
                        }).start();
                        progressDialog = DialogUtils.showProgressDialog(MainScreenActivity.this, "");
                    }
                }
                break;

            case PHOTOTAKE:
                if (data != null) {
                    final String filePath = FileUtil.saveImage(data);
                    if (!TextUtils.isEmpty(filePath)) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                FileUtil.uploadFile(Cmd_Update_Avatar, filePath, CommonUrl.Update_Img_Url.getVal());
                            }
                        }).start();
                        progressDialog = DialogUtils.showProgressDialog(MainScreenActivity.this, "");
                    }
                }
                break;
        }
    }

    private void checkAppVersion() {
        SharedPreferences sp = App.getSp();
        long reqTime = sp.getLong(Constant.SP_APP_VERSION_REQ_TIME, 0);
        if (reqTime == 0 || reqTime < TimeUtils.getStartTime()) {
            if (dialog == null || !dialog.isShowing()) {
                SystemLogic.getInstance().checkUpdateApk(this, false);
            }
        }
        sp.edit().putLong(Constant.SP_APP_VERSION_REQ_TIME, System.currentTimeMillis() / 1000).commit();

    }

    private void checkServerTip() {
        String serverUrl = App.getApp().getOtaServerUrl();
        if (CommonUrl.OTA_INTERNATIONAL_OLD_URL.equals(serverUrl)) {
            final SharedPreferences sp = App.getSp();
            long reqTime = sp.getLong(Constant.SP_APP_SERVER_REQ_TIME, 0);
            boolean remindAgain = sp.getBoolean(Constant.SP_APP_SERVER_REMIND_AGAIN, true);
            if (reqTime == 0 || reqTime < TimeUtils.getStartTime()) {
                SystemLogic.getInstance().getServerStopMsg();
                ServerBean serverBean = SystemLogic.getInstance().getServerBean();
                if (serverBean == null && serverBean.getContent().get(0) != null) {
                    return;
                }
                ServerBean.ContentBean contentBean = serverBean.getContent().get(0);
                if (contentBean.getPopStartTime() < System.currentTimeMillis() / 1000) {
                    dialog = DialogUtils.showWarningDialog(this, null, contentBean.getPopText(), null, null, true, null);
                } else {
                    if (remindAgain)
                        dialog = DialogUtils.showWarningDialog(this, null, contentBean.getPopText(), null, "Dont't remind me again", true, new DialogUtils.OnErrorButtonClickListenter() {
                            @Override
                            public void okClick() {

                            }

                            @Override
                            public void cancelClick() {
                                sp.edit().putBoolean(Constant.SP_APP_SERVER_REMIND_AGAIN, false).commit();
                            }
                        });
                }
            }
            sp.edit().putLong(Constant.SP_APP_SERVER_REQ_TIME, System.currentTimeMillis() / 1000).commit();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}