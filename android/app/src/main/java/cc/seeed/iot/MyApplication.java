package cc.seeed.iot;

import android.content.Context;
import android.content.SharedPreferences;

import cc.seeed.iot.datastruct.User;
import cc.seeed.iot.util.Common;
import cc.seeed.iot.webapi.IotApi;

/**
 * Created by tenwong on 15/7/9.
 */
public class MyApplication extends com.activeandroid.app.Application {
    private SharedPreferences sp;
    private User user = new User();
    private String ota_server_url;
    private String exchange_server_url;
    private String ota_server_ip;
    private String exchange_server_ip;

    /**
     * into smartconfig state
     */
    private Boolean configState;

    /**
     * login state
     */
    private Boolean loginState;

    private Boolean firstUseState;


    @Override
    public void onCreate() {
        super.onCreate();
        sp = this.getSharedPreferences("IOT", Context.MODE_PRIVATE);
        user.email = sp.getString("userName", "awong1900@163.com");
        user.user_key = sp.getString("userToken", "sBoKhjQNdtT8oTjukEeg98Ui3fuF3416zh-1Qm5Nkm0");
        ota_server_url = sp.getString("ota_server_url", Common.OTA_INTERNATIONAL_URL); //https://iot.seeed.cc/v1 //https://cn.iot.seeed.cc/v1
        exchange_server_url = sp.getString("exchange_server_url", Common.EXCHANGE_INTERNATIONAL_URL); //https://cn.iot.seeed.cc/v1  //"https://iot.seeed.cc/v1";
        ota_server_ip = sp.getString("ota_server_ip", Common.OTA_INTERNATIONAL_IP);
        exchange_server_ip = sp.getString("exchange_server_ip", Common.EXCHANGE_INTERNATIONAL_IP);
        configState = sp.getBoolean("configState", false);
        loginState = sp.getBoolean("loginState", false);
        firstUseState = sp.getBoolean("firstUseState", true);

        init();

    }

    private void init() {
        IotApi.SetServerUrl(ota_server_url);
    }

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

    public String getOtaServerUrl() {
        return ota_server_url;
    }

    public void setOtaServerUrl(String ota_server_url) {
        this.ota_server_url = ota_server_url;
        IotApi.SetServerUrl(ota_server_url);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("ota_server_url", ota_server_url);
        editor.apply();
    }

    public String getExchangeServerUrl() {
        return exchange_server_url;
    }

    public void setExchangeServerUrl(String exchange_server_url) {
        this.exchange_server_url = exchange_server_url;
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("exchange_server_url", exchange_server_url);
        editor.apply();
    }

    public String getOtaServerIP() {
        return ota_server_ip;
    }

    public void setOtaServerIP(String ota_server_ip) {
        this.ota_server_ip = ota_server_ip;
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("ota_server_ip", ota_server_ip);
        editor.apply();
    }

    public String getExchangeServerIP() {
        return exchange_server_ip;
    }

    public void setExchangeServerIP(String exchange_server_ip) {
        this.exchange_server_ip = exchange_server_ip;
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("exchange_server_ip", exchange_server_ip);
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

}