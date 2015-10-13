package cc.seeed.iot;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.ui_setnode.model.PinConfig;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.NodeListResponse;
import cc.seeed.iot.yaml.IotYaml;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by tenwong on 15/7/9.
 */
public class MyApplication extends com.activeandroid.app.Application {
    private String grove_dir;

    private SharedPreferences sp;

    private List<Node> nodes = new ArrayList<Node>();

    private User user = new User();

    private String server_url;

    /**
     * into smartconfig state
     */
    private Boolean configState;

    /**
     * login state
     */
    private Boolean loginState;

    public Boolean getLoginState() {
        return loginState;
    }

    public void setLoginState(Boolean loginState) {
        this.loginState = loginState;
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("loginState", loginState);
        editor.apply();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("userName", user.email);
        editor.putString("userToken", user.user_key);
        editor.apply();
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public String getServer_url() {
        return server_url;
    }

    public void setServer_url(String server_url) {
        this.server_url = server_url;
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("server_url", server_url);
        editor.apply();
    }

    public Boolean getConfigState() {
        return configState;
    }

    public void setConfigState(Boolean configState) {
        this.configState = configState;
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("configState", configState);
        editor.apply();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        grove_dir = getFilesDir() + "/groves";

        sp = this.getSharedPreferences("IOT", Context.MODE_PRIVATE);
        sp.getString("serverAddress", "http://192.168.21.83:8080/v1");
        user.email = sp.getString("userName", "awong1900@163.com");
        user.user_key = sp.getString("userToken", "sBoKhjQNdtT8oTjukEeg98Ui3fuF3416zh-1Qm5Nkm0");

        server_url = sp.getString("server_url", "https://iot.seeed.cc/v1");

        configState = sp.getBoolean("configState", false);

        configState = sp.getBoolean("loginState", false);

        init();

        getGrovesData();

        getNodesData();

    }

    private void init() {
        IotApi.SetServerUrl(server_url);
    }

    public void getGrovesData() {
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
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(getClass().getName(), error.toString());
            }
        });
    }

    public void getNodesData() {
        IotApi api = new IotApi();
        String token = user.user_key;
        api.setAccessToken(token);
        final IotService iot = api.getService();
        iot.nodesList(new Callback<NodeListResponse>() {
            @Override
            public void success(NodeListResponse nodeListResponse, Response response) {
                if (nodeListResponse.status.equals("200")) {
                    DBHelper.delNodesAll();
                    nodes = nodeListResponse.nodes;
                    for (Node node : nodes) {
                        node.save();
                        //todo delete config with delete's node_sn
                        getNodesConfig(node);
                    }
                } else {
                    Log.e(getClass().getName(), nodeListResponse.msg);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(getClass().getName(), error.toString());
            }
        });
    }


    public void getNodesConfig(final Node node) {
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
                    Log.e(getClass().getName(), response.msg);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(getClass().getName(), error.toString());
            }

            private void saveToDB(String yaml) {
                List<PinConfig> pinConfigs = IotYaml.getNodeConfig(yaml);
                for (PinConfig pinConfig : pinConfigs) {
                    pinConfig.node_sn = node.node_sn;
//                    Log.e(getClass().getName(), pinConfig.toString());
                    pinConfig.save();
                }
            }
        });
    }
}