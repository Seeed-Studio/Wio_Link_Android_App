/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.seeed.iot.ui_main;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.datastruct.Constant;
import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.ui_ap_config.GoReadyActivity;
import cc.seeed.iot.ui_login.SetupActivity;
import cc.seeed.iot.ui_main.util.DividerItemDecoration;
import cc.seeed.iot.ui_setnode.SetupIotNodeActivity;
import cc.seeed.iot.ui_setnode.model.NodeConfigHelper;
import cc.seeed.iot.ui_setnode.model.PinConfigDBHelper;
import cc.seeed.iot.util.Common;
import cc.seeed.iot.util.DBHelper;
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

//import android.support.design.widget.FloatingActionButton;

/**
 * TODO
 */
public class MainScreenActivity extends AppCompatActivity
        implements NodeListRecyclerAdapter.OnClickListener, View.OnClickListener {
    private static final String TAG = "MainScreenActivity";
    private static final int MESSAGE_GROVE_LIST_START = 0x00;
    private static final int MESSAGE_GROVE_LIST_COMPLETE = 0x01;
    private static final int MESSAGE_NODE_LIST_START = 0x02;
    private static final int MESSAGE_NODE_LIST_COMPLETE = 0x03;
    private static final int MESSAGE_NODE_CONFIG_COMPLETE = 0x04;

    private DrawerLayout mDrawerLayout;
    //    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private NodeListRecyclerAdapter mAdapter;
    private TextView mEmail;
    private ImageView mAddTip;
    private List<Node> nodes;
    private User user;
    private boolean firstUseState;

    private Handler mHandler;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();

        if (firstUseState) {
            Message message = Message.obtain();
            message.what = MESSAGE_GROVE_LIST_START;
            mHandler.sendMessage(message);
            getGrovesData();
        }
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(R.string.app_name);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);

        }
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header);
        if (headerLayout != null) {
            mEmail = (TextView) headerLayout.findViewById(R.id.hd_email);
            if (((MyApplication) getApplication()).getOtaServerUrl().equals(Common.OTA_CHINA_URL)) {
                mEmail.setText(user.email + " (China)");
            } else if (((MyApplication) getApplication()).getOtaServerUrl().equals(Common.OTA_INTERNATIONAL_URL)) {
                mEmail.setText(user.email + " (International)");
            } else
                mEmail.setText(user.email + " (Customer)\n" +
                        ((MyApplication) getApplication()).getOtaServerIP());
        }


        mRecyclerView = (RecyclerView) findViewById(R.id.listview);
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layout = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(layout);
            mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.divider)));
            mAdapter = new NodeListRecyclerAdapter(nodes);
            mAdapter.setOnClickListener(this);
            mRecyclerView.setAdapter(mAdapter);
        }

        // Do not need refresh
        /*
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Message message = Message.obtain();
                        message.what = MESSAGE_NODE_LIST_START;
                        mHandler.sendMessage(message);
                        getNodeList();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 0);
            }
        });
        */

 /*       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MyApplication) getApplication()).setConfigState(true);
                Intent intent = new Intent(MainScreenActivity.this, GoReadyActivity.class);
                startActivity(intent);
            }
        });*/

        FloatingActionButton setupLinkAction = (FloatingActionButton) findViewById(R.id.setup_link);
        FloatingActionButton setupNodeAction = (FloatingActionButton) findViewById(R.id.setup_node);
        setupLinkAction.setOnClickListener(this);
        setupNodeAction.setOnClickListener(this);

        mProgressDialog = new ProgressDialog(this);

        mAddTip = (ImageView) findViewById(R.id.add_node_tip);
    }

    private void initData() {

        user = ((MyApplication) MainScreenActivity.this.getApplication()).getUser();
        nodes = DBHelper.getNodesAll();
        firstUseState = ((MyApplication) MainScreenActivity.this.getApplication()).getFirstUseState();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_GROVE_LIST_START:
                        mProgressDialog.setMessage("update grove list...");
                        mProgressDialog.setCanceledOnTouchOutside(false);
                        mProgressDialog.show();
                        break;
                    case MESSAGE_GROVE_LIST_COMPLETE:
                        mProgressDialog.dismiss();
                        ((MyApplication) MainScreenActivity.this.getApplication()).setFirstUseState(false);
                        break;
                    case MESSAGE_NODE_LIST_START:
                        mProgressDialog.setMessage("update wio link...");
                        mProgressDialog.setCanceledOnTouchOutside(false);
                        mProgressDialog.show();
                        break;

                    case MESSAGE_NODE_LIST_COMPLETE:
                        mProgressDialog.dismiss();
                        if (msg.arg2 == 1) {
                            mAdapter.updateAll(nodes);
                            if (nodes.isEmpty()) {
                                mAddTip.setVisibility(View.VISIBLE);
                            } else {
                                mAddTip.setVisibility(View.GONE);
                            }

                            for (Node n : nodes) {
                                getNodesConfig(n, nodes.indexOf(n));
                            }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ui_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.update:
                Message message = Message.obtain();
                message.what = MESSAGE_NODE_LIST_START;
                mHandler.sendMessage(message);
                getNodeList();
                return true;
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

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        mDrawerLayout.closeDrawers();
                        switch (menuItem.getItemId()) {
                            case R.id.nav_node_list:
                                menuItem.setChecked(true);
                                break;
                            case R.id.nav_grove_list: {
                                Intent intent = new Intent(MainScreenActivity.this,
                                        GrovesActivity.class);
                                startActivity(intent);
                            }
                            break;
//                            case R.id.nav_smartconfig: {
//                                ((MyApplication) getApplication()).setConfigState(false);
//                                Intent intent = new Intent(MainScreenActivity.this,
//                                        GoReadyActivity.class);
//                                startActivity(intent);
//                            }
//                            break;
                            case R.id.nav_setting: {
                                Intent intent = new Intent(MainScreenActivity.this,
                                        MainSettingActivity.class);
                                startActivity(intent);
                            }
                            break;
                            case R.id.nav_about: {
                                Intent intent = new Intent(MainScreenActivity.this,
                                        AboutActivity.class);
                                startActivity(intent);
                            }
                            break;
                            case R.id.nav_logout: {
                                ((MyApplication) getApplication()).setLoginState(false);
                                ((MyApplication) getApplication()).setFirstUseState(true);
                                DBHelper.delNodesAll();
                                DBHelper.delGrovesAll();
                                PinConfigDBHelper.delPinConfigAll();
                                Intent intent = new Intent(MainScreenActivity.this,
                                        SetupActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                            break;
                        }

                        return true;
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setup_link:
                setupActivity(Constant.WIO_LINK_V1_0);
                break;
            case R.id.setup_node:
                setupActivity(Constant.WIO_NODE_V1_0);
                break;
        }
    }

    private void setupActivity(String board) {
        ((MyApplication) getApplication()).setConfigState(true);
        Intent intent = new Intent(MainScreenActivity.this, GoReadyActivity.class);
        intent.putExtra("board", board);
        startActivity(intent);
    }

    @Override
    public void onClick(View v, final int position) {
        final Node node = mAdapter.getItem(position);
        int id = v.getId();
        switch (id) {
            case R.id.node_item:
                nodeSet(node);
                break;
//            case R.id.location:
//                break;
//            case R.id.favorite:
//                break;
            case R.id.setting:
                nodeSetting(node);
                break;
            case R.id.api:
                nodeApi(node);
                break;
            case R.id.remove:
                nodeRemove(node, position);
                break;
//            case R.id.dot:
//                PopupMenu popupMenu = new PopupMenu(this, v);
//                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//                        switch (item.getItemId()) {
//                            case R.id.remove:
//                                nodeRemove(node, position);
//                                return true;
//                            case R.id.detail:
//                                nodeApi(node);
//                                return true;
//                            case R.id.rename:
//                                nodeRename(node, position);
//                                return true;
//                        }
//                        return false;
//                    }
//                });
//                popupMenu.inflate(R.menu.ui_node_action);
//                popupMenu.show();
//                if (popupMenu.getDragToOpenListener() instanceof ListPopupWindow.ForwardingListener) {
//                    ListPopupWindow.ForwardingListener listener =
//                            (ListPopupWindow.ForwardingListener) popupMenu.getDragToOpenListener();
//                    listener.getPopup().setVerticalOffset(-v.getHeight());
//                    listener.getPopup().show();
//                }
//                break;
        }
    }

    private void nodeSetting(Node node) {
        Intent intent = new Intent(this, NodeSettingActivity.class);
        intent.putExtra("node_sn", node.node_sn);
        startActivity(intent);
    }

    public boolean nodeRemove(final Node node, final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Remove Wio Link");
        builder.setMessage("Confirm remove?");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final ProgressDialog progressDialog = new ProgressDialog(MainScreenActivity.this);
                progressDialog.setMessage("Wio link remove...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                IotApi api = new IotApi();
                User user = ((MyApplication) MainScreenActivity.this.getApplication()).getUser();
                api.setAccessToken(user.user_key);
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
        Intent intent = new Intent(this, NodeApiActivity.class);
        intent.putExtra("node_sn", node.node_sn);
        startActivity(intent);
        return true;
    }

    public boolean nodeSet(Node node) {
        Intent intent = new Intent(this, SetupIotNodeActivity.class);
        intent.putExtra("node_sn", node.node_sn);
        startActivity(intent);
        return true;
    }

    private void getNodeList() {
        IotApi api = new IotApi();
        api.setAccessToken(user.user_key);
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
                Toast.makeText(MainScreenActivity.this, error.getLocalizedMessage(),
                        Toast.LENGTH_LONG).show();
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
                    NodeConfigHelper.saveToDB(nodeJson, node);
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
        String token = user.user_key;
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


}