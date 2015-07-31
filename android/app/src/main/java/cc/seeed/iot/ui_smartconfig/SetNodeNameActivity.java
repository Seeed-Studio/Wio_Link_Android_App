package cc.seeed.iot.ui_smartconfig;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.udp.ConfigNodeData;
import cc.seeed.iot.udp.ConfigUdpSocket;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.NodeResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SetNodeNameActivity extends AppCompatActivity {
    public Toolbar mToolbar;
    public EditText mNodeNameView;
    public Button mGoPlayButtonView;
    private ConfigUdpSocket udpClient;
    public Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smartconfig_connected);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        intent = getIntent();

        udpClient = new ConfigUdpSocket();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("WIFI Iot Node");

        mNodeNameView = (EditText) findViewById(R.id.add_node_name);

        mGoPlayButtonView = (Button) findViewById(R.id.first_time_how_to_api_key);
        mGoPlayButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNode();
            }
        });

    }

    private void addNode() {
        String ip = intent.getStringExtra("ip");
        String node_name = mNodeNameView.getText().toString();

        Node node = attemptLogin(node_name);

        if (node == null)
            return;

        String cmd_set_sn = "KeySN: " + node.node_key + "," + node.node_sn;
        Log.e("iot", "cmd_sn: " + cmd_set_sn);

        new SetNodeSn().execute(cmd_set_sn, ip);

    }


    @Override
    public Intent getIntent() {
        intent = super.getIntent();

        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private Node attemptLogin(final String node_name) {
        final Node node = new Node();
        boolean cancel = false;
        View focusView = null;

        final ProgressDialog mProgressBar = new ProgressDialog(this);

        if (cancel) {
            focusView.requestFocus();
        } else {
//            showProgress(true);
            mProgressBar.setTitle("connect server...");
            mProgressBar.show();
            IotApi api = new IotApi();
            User user = ((MyApplication) getApplication()).getUser();
            api.setAccessToken(user.user_key);
            IotService iot = api.getService();
            iot.nodesCreate(node_name, new Callback<NodeResponse>() {
                @Override
                public void success(NodeResponse nodeResponse, Response response) {
                    String status = nodeResponse.status;
                    if (status.equals("200")) {
                        mProgressBar.dismiss();

                        node.name = node_name;
                        node.node_key = nodeResponse.node_key;
                        node.node_sn = nodeResponse.node_sn;

//                        mListener.onAddNode(node);
                    } else {
//                        showProgress(false);
                        mProgressBar.dismiss();
                        mNodeNameView.setError(nodeResponse.msg);
                        mNodeNameView.requestFocus();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
//                    showProgress(false);
                    mProgressBar.dismiss();
                    Toast.makeText(SetNodeNameActivity.this, "Connect server error!", Toast.LENGTH_LONG).show();
                }
            });
        }
        return node;
    }

    private class SetNodeSn extends AsyncTask<String, Void, Boolean> {
        //todo: real-time refresh
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(SetNodeNameActivity.this);
//            mProgressDialog.setMessage("search node...");
            mProgressDialog.setTitle("Set node_sn...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String cmd = params[0];
            String ipAddr = params[1];
            ArrayList<ConfigNodeData> configNodeDatas = new ArrayList<>();
            udpClient.setSoTimeout(1000); //1s timeout
            udpClient.sendData(cmd, ipAddr);
            for (int i = 0; i < 3; i++) {
                try {
                    byte[] bytes = udpClient.receiveData();
                    if (bytes.toString().equals("ok")) {
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    continue;
                } catch (IOException e) {
                    Log.e("iot", "Error[AsyIO]:" + e);
                }
            }

            Log.i("iot", "configNodeDatas: " + configNodeDatas);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean b) {
//            setupAdapter(configNodeDatas)
            //todo
            mProgressDialog.dismiss();
            Intent intent = new Intent(SetNodeNameActivity.this, MainScreenActivity.class);
            startActivity(intent);
        }
    }
}
