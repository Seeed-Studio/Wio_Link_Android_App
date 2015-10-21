package cc.seeed.iot;

import android.content.Context;
import android.content.SharedPreferences;

import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.webapi.ExchangeApi;
import cc.seeed.iot.webapi.IotApi;

/**
 * Created by tenwong on 15/7/9.
 */
public class MyApplication extends com.activeandroid.app.Application {
    private String grove_dir;
    private SharedPreferences sp;
    private User user = new User();
    private String ota_server_url;
    private String exchange_server_url;

    /**
     * into smartconfig state
     */
    private Boolean configState;

    /**
     * login state
     */
    private Boolean loginState;

    private Boolean firstUseState;

    public Boolean getLoginState() {
        return loginState;
    }

    public void setLoginState(Boolean loginState) {
        this.loginState = loginState;
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("loginState", loginState);
        editor.apply();
    }

    public Boolean getFirstUseState() {
        return firstUseState;
    }

    public void setFirstUseState(Boolean firstUseState) {
        this.firstUseState = firstUseState;
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("firstUseState", firstUseState);
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

    public String getServerUrl() {
        return ota_server_url;
    }

    public void setServerUrl(String server_url) {
        this.ota_server_url = server_url;
        IotApi.SetServerUrl(server_url);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("ota_server_url", server_url);
        editor.apply();
    }

    public String getExchangeServerUrl() {
        return exchange_server_url;
    }

    public void setExchangeServerUrl(String server_url) {
        this.exchange_server_url = server_url;
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("exchange_server_url", server_url);
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
        user.email = sp.getString("userName", "awong1900@163.com");
        user.user_key = sp.getString("userToken", "sBoKhjQNdtT8oTjukEeg98Ui3fuF3416zh-1Qm5Nkm0");
        ota_server_url = sp.getString("ota_server_url", "https://120.25.216.117/v1"); //https://iot.seeed.cc/v1
        exchange_server_url = sp.getString("exchange_server_url", "https://120.25.216.117/v1");
        configState = sp.getBoolean("configState", false);
        loginState = sp.getBoolean("loginState", false);
        firstUseState = sp.getBoolean("firstUseState", true);

        init();

    }

    private void init() {
        IotApi.SetServerUrl(ota_server_url);
        ExchangeApi.SetServerUrl(exchange_server_url);
    }

}