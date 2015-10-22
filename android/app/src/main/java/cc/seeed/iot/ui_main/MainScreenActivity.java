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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.ui_ap_config.GoReadyActivity;
import cc.seeed.iot.ui_login.SetupActivity;
import cc.seeed.iot.ui_main.util.DividerItemDecoration;
import cc.seeed.iot.ui_setnode.SetupIotNodeActivity;
import cc.seeed.iot.ui_setnode.model.PinConfig;
import cc.seeed.iot.ui_setnode.model.PinConfigDBHelper;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.NodeListResponse;
import cc.seeed.iot.webapi.model.NodeResponse;
import cc.seeed.iot.yaml.IotYaml;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * TODO
 */
public class MainScreenActivity extends AppCompatActivity
        implements NodeListRecyclerAdapter.OnClickListener {
    private static final String TAG = "MainScreenActivity";
    private static final int MESSAGE_GROVE_LIST_START = 0x00;
    private static final int MESSAGE_GROVE_LIST_COMPLETE = 0x01;
    private static final int MESSAGE_NODE_LIST_START = 0x02;
    private static final int MESSAGE_NODE_LIST_COMPLETE = 0x03;
    private static final int MESSAGE_NODE_CONFIG_COMPLETE = 0x04;

    private DrawerLayout mDrawerLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private NodeListRecyclerAdapter mAdapter;
    private TextView mEmail;

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
            ab.setTitle("PION ONE");
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
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

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MyApplication) getApplication()).setConfigState(true);
                Intent intent = new Intent(MainScreenActivity.this, GoReadyActivity.class);
                startActivity(intent);
            }
        });

        mEmail = (TextView) findViewById(R.id.hd_email);
        mEmail.setText(user.email);

        mProgressDialog = new ProgressDialog(this);
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
                        mProgressDialog.setMessage("update pion one...");
                        mProgressDialog.setCanceledOnTouchOutside(false);
                        mProgressDialog.show();
                        break;

                    case MESSAGE_NODE_LIST_COMPLETE:
                        mProgressDialog.dismiss();
                        if (msg.arg2 == 1) {
                            mAdapter.updateAll(nodes);
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
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        switch (menuItem.getItemId()) {
                            case R.id.nav_nodes_list:
                                break;
                            case R.id.nav_smartconfig: {
                                ((MyApplication) getApplication()).setConfigState(false);
                                Intent intent = new Intent(MainScreenActivity.this,
                                        GoReadyActivity.class);
                                startActivity(intent);
                            }
                            break;
                            case R.id.nav_setting: {
                                Intent intent = new Intent(MainScreenActivity.this,
                                        SettingActivity.class);
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
                                DBHelper.delNodesAll();
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
    public void onClick(View v, final int position) {
        final Node node = mAdapter.getItem(position);
        int id = v.getId();
        switch (id) {
            case R.id.node_item:
                nodeSet(node);
                break;
            case R.id.location:
                break;
            case R.id.favorite:
                break;
            case R.id.rename:
                nodeRename(node, position);
                break;
            case R.id.detail:
                nodeDetail(node);
                break;
            case R.id.remove:
                nodeRemove(node, position);
                break;
            case R.id.dot:
                PopupMenu popupMenu = new PopupMenu(this, v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.remove:
                                nodeRemove(node, position);
                                return true;
                            case R.id.detail:
                                nodeDetail(node);
                                return true;
                            case R.id.rename:
                                nodeRename(node, position);
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.ui_node_action);
                popupMenu.show();
                if (popupMenu.getDragToOpenListener() instanceof ListPopupWindow.ForwardingListener) {
                    ListPopupWindow.ForwardingListener listener =
                            (ListPopupWindow.ForwardingListener) popupMenu.getDragToOpenListener();
                    listener.getPopup().setVerticalOffset(-v.getHeight());
                    listener.getPopup().show();
                }
                break;
        }
    }

    public boolean nodeRemove(final Node node, final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Remove Pion One");
        builder.setMessage("Confirm remove?");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final ProgressDialog progressDialog = new ProgressDialog(MainScreenActivity.this);
                progressDialog.setMessage("Node delete...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                IotApi api = new IotApi();
                User user = ((MyApplication) MainScreenActivity.this.getApplication()).getUser();
                api.setAccessToken(user.user_key);
                final IotService iot = api.getService();
                iot.nodesDelete(node.node_sn, new Callback<NodeResponse>() {
                    @Override
                    public void success(NodeResponse nodeResponse, Response response) {
                        progressDialog.dismiss();
                        nodes.remove(node);
                        DBHelper.delNode(node.node_sn);
                        mAdapter.removeItem(position);
                        Log.i(TAG, "Remove Node success!");
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        progressDialog.dismiss();
                        Log.e(TAG, "Remove Node fail!");
                    }
                });
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();

        return true;
    }

    public boolean nodeDetail(Node node) {
        Intent intent = new Intent(this, NodeDetailActivity.class);
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

    public boolean nodeRename(final Node node, final int position) {
        final LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_name_input, null);
        final EditText nameView = (EditText) view.findViewById(R.id.new_name);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Rename Pion One");
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String newName = nameView.getText().toString();
                final ProgressDialog progressDialog = new ProgressDialog(MainScreenActivity.this);
                progressDialog.setMessage("Node rename...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                IotApi api = new IotApi();
                User user = ((MyApplication) MainScreenActivity.this.getApplication()).getUser();
                api.setAccessToken(user.user_key);
                final IotService iot = api.getService();
                iot.nodesRename(newName, node.node_sn, new Callback<NodeResponse>() {
                    @Override
                    public void success(NodeResponse nodeResponse, Response response) {
                        progressDialog.dismiss();
                        nodes.get(position).name = newName;
                        node.save();
                        mAdapter.updateItem(position);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        progressDialog.dismiss();
                        Log.e("iot", "Delete Node fail!");
                    }
                });
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();

        return true;
    }

    private void getNodeList() {
        IotApi api = new IotApi();
        api.setAccessToken(user.user_key);
        final IotService iot = api.getService();
        iot.nodesList(new Callback<NodeListResponse>() {
            @Override
            public void success(NodeListResponse nodeListResponse, Response response) {
                if (nodeListResponse.status.equals("200")) {
                    List<Node> get_nodes = nodeListResponse.nodes;
                    ArrayList<Node> delNodes = new ArrayList<Node>();
                    for (Node n : get_nodes) {
                        if (n.name.equals("node000")) {
                            iot.nodesDelete(n.node_sn, new Callback<NodeResponse>() {
                                @Override
                                public void success(NodeResponse nodeResponse, Response response) {

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

                    DBHelper.delNodesAll();
                    for (Node node : nodes) {
                        node.save();
                    }

                    Message message = Message.obtain();
                    message.arg2 = 1;
                    message.what = MESSAGE_NODE_LIST_COMPLETE;
                    mHandler.sendMessage(message);

                } else {
                    Toast.makeText(MainScreenActivity.this, nodeListResponse.msg, Toast.LENGTH_LONG).show();
                    Message message = Message.obtain();
                    message.arg2 = 0;
                    message.what = MESSAGE_NODE_LIST_COMPLETE;
                    mHandler.sendMessage(message);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Message message = Message.obtain();
                message.arg2 = 0;
                message.what = MESSAGE_NODE_LIST_COMPLETE;
                mHandler.sendMessage(message);

                Toast.makeText(MainScreenActivity.this, "Connect server fail!", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void getNodesConfig(final Node node, final int position) {
        IotApi api = new IotApi();
        api.setAccessToken(node.node_key);
        final IotService iot = api.getService();
        iot.nodeConfig(new Callback<cc.seeed.iot.webapi.model.Response>() {
            @Override
            public void success(cc.seeed.iot.webapi.model.Response response, Response response2) {
                if (response.status.equals("200")) {
                    String yaml = response.msg;
                    saveToDB(yaml);
                } else {
                    Log.i(TAG, response.msg);
                }

                Message message = Message.obtain();
                message.arg1 = position;
                message.what = MESSAGE_NODE_CONFIG_COMPLETE;
                mHandler.sendMessage(message);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(getClass().getName(), error.toString());

            }

            private void saveToDB(String yaml) {
                List<PinConfig> pinConfigs = IotYaml.getNodeConfig(yaml);
                PinConfigDBHelper.delPinConfig(node.node_sn);
                for (PinConfig pinConfig : pinConfigs) {
                    pinConfig.node_sn = node.node_sn;
                    pinConfig.save();
                }
            }
        });
    }

    private void getGrovesData() {
        IotApi api = new IotApi();
        String token = user.user_key;
        api.setAccessToken(token);
        IotService iot = api.getService();
        iot.scanDrivers(new Callback<List<GroverDriver>>() {
            @Override
            public void success(List<GroverDriver> groverDrivers, retrofit.client.Response response) {
                for (GroverDriver groveDriver : groverDrivers) {
                    groveDriver.save();
                }

                Message message = Message.obtain();
                message.what = MESSAGE_GROVE_LIST_COMPLETE;
                mHandler.sendMessage(message);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, error.toString());
                Message message = Message.obtain();
                message.what = MESSAGE_GROVE_LIST_COMPLETE;
                mHandler.sendMessage(message);
            }
        });
    }

}