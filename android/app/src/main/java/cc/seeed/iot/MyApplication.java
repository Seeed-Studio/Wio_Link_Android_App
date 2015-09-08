package cc.seeed.iot;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.model.Node;

/**
 * Created by tenwong on 15/7/9.
 */
public class MyApplication extends Application {
    private SharedPreferences sp;

    private ArrayList<Node> nodes = new ArrayList<Node>();

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

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<Node> nodes) {
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
        sp = this.getSharedPreferences("IOT", Context.MODE_PRIVATE);
        sp.getString("serverAddress", "http://192.168.21.83:8080/v1");
        user.email = sp.getString("userName", "awong1900@163.com");
        user.user_key = sp.getString("userToken", "sBoKhjQNdtT8oTjukEeg98Ui3fuF3416zh-1Qm5Nkm0");

        server_url = sp.getString("server_url", "https://iot.seeed.cc/v1");

        configState = sp.getBoolean("configState", false);

        configState = sp.getBoolean("loginState", false);

        init();
    }

    private void init() {
        IotApi.SetServerUrl(server_url);
    }
}
