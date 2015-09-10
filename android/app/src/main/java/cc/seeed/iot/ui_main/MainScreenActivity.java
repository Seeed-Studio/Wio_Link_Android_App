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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
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

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.ui_login.SetupActivity;
import cc.seeed.iot.ui_setnode.SetupIotNodeActivity;
import cc.seeed.iot.ui_smartconfig.GoReadyActivity;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.NodeListResponse;
import cc.seeed.iot.webapi.model.NodeResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * TODO
 */
public class MainScreenActivity extends AppCompatActivity
        implements NodeListRecyclerAdapter.NodeAction {
    private final static String TAG = "MainScreenActivity";
    private DrawerLayout mDrawerLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private NodeListRecyclerAdapter mAdapter;
    private TextView mEmail;

    private ArrayList<Node> nodes;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = ((MyApplication) MainScreenActivity.this.getApplication()).getUser();
        nodes = ((MyApplication) MainScreenActivity.this.getApplication()).getNodes();

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

//            setupAdapter();
            ((MyApplication) MainScreenActivity.this.getApplication()).setNodes(nodes);
            mAdapter = new NodeListRecyclerAdapter(nodes);
            mRecyclerView.setAdapter(mAdapter);
            setupAdapter();
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setupAdapter();
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
                setupAdapter();
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

    private void setupAdapter() {
        final ProgressDialog mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("search node list...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        IotApi api = new IotApi();
        User user = ((MyApplication) MainScreenActivity.this.getApplication()).getUser();
        api.setAccessToken(user.user_key);
        final IotService iot = api.getService();
        iot.nodesList(new Callback<NodeListResponse>() {
            @Override
            public void success(NodeListResponse nodeListResponse, Response response) {
                mProgressDialog.dismiss();
                if (nodeListResponse.status.equals("200")) {
                    nodes = (ArrayList) nodeListResponse.nodes;
                    ArrayList<Node> delNodes = new ArrayList<Node>();
                    for (Node n : nodes) {
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
                    nodes.removeAll(delNodes);

//                    ((MyApplication) MainScreenActivity.this.getApplication()).setNodes(nodes);
//                    mAdapter = new NodeListRecyclerAdapter(nodes);
//                    mRecyclerView.setAdapter(mAdapter);
                    mAdapter.updateAll(nodes);

                } else {
                    Toast.makeText(MainScreenActivity.this, nodeListResponse.msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                mProgressDialog.dismiss();
                Toast.makeText(MainScreenActivity.this, "Connect server fail...", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void addItem(Node node) {
        nodes.add(node);
        ((MyApplication) MainScreenActivity.this.getApplication()).setNodes(nodes);
        mAdapter.notifyItemInserted(nodes.size());
    }

    private void removeItem(Node node) {
        nodes.remove(node);
        mAdapter.notifyItemRemoved(nodes.size());
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
    public boolean nodeRemove(final int position) {//todo: rubbish code

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
                final int p = position;
                Node node = nodes.get(position);
                IotApi api = new IotApi();
                User user = ((MyApplication) MainScreenActivity.this.getApplication()).getUser();
                api.setAccessToken(user.user_key);
                final IotService iot = api.getService();
                iot.nodesDelete(node.node_sn, new Callback<NodeResponse>() {
                    @Override
                    public void success(NodeResponse nodeResponse, Response response) {
                        progressDialog.dismiss();
                        nodes.remove(nodeResponse);
                        ((MyApplication) MainScreenActivity.this.getApplication()).setNodes(nodes);
                        mAdapter.removeItem(p);
                        Log.i("iot", "Delete Node success!");
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

    @Override
    public boolean nodeDetail(int position) {
        Intent intent = new Intent(this, NodeDetailActivity.class);
        intent.putExtra("position", position);
        startActivity(intent);
        return true;
    }

    @Override
    public boolean nodeSet(int position) {
        Intent intent = new Intent(this, SetupIotNodeActivity.class);
        intent.putExtra("position", position);
        startActivity(intent);
        return true;
    }

    @Override
    public boolean nodeRename(final int position) {
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
                final int p = position;
                Node node = nodes.get(position);
                IotApi api = new IotApi();
                User user = ((MyApplication) MainScreenActivity.this.getApplication()).getUser();
                api.setAccessToken(user.user_key);
                final IotService iot = api.getService();
                iot.nodesRename(newName, node.node_sn, new Callback<NodeResponse>() {
                    @Override
                    public void success(NodeResponse nodeResponse, Response response) {
//                        progressDialog.dismiss();
//                        nodes.remove(nodeResponse);
//                        for ()
//                        ((MyApplication) MainScreenActivity.this.getApplication()).setNodes(nodes);
//                        mAdapter.removeItem(p);
//                        Log.i("iot", "Delete Node success!");
                        progressDialog.dismiss();
                        setupAdapter();
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

}