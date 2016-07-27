package cc.seeed.iot;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.PlatformConfig;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.util.CommonUrl;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.NetworkUtils;
import cc.seeed.iot.webapi.IotApi;
import cn.sharesdk.framework.ShareSDK;

/**
 * Created by tenwong on 15/7/9.
 */
public class App extends com.activeandroid.app.Application {
    public static App sApp;
    private static SharedPreferences sp;
    private String ota_server_url;
    private String ota_server_ip;

    /**
     * login state
     */
    private Boolean loginState;

    private Boolean firstUseState;


    @Override
    public void onCreate() {
        Fresco.initialize(this);
        super.onCreate();
        sApp = this;
        sp = this.getSharedPreferences("IOT", Context.MODE_PRIVATE);
        loginState = sp.getBoolean("loginState", false);
        firstUseState = sp.getBoolean("firstUseState", true);
       // MobclickAgent.setDebugMode(true);
        FacebookSdk.sdkInitialize(getApplicationContext());
        PlatformConfig.setTwitter("hDpph8vR0zZgnXCH7XStcmu8Z", "E8qVkQUVjOmhrS0OPvKllX6HFaEqnMJeu9CpRpVQY12VpzAaE9");
        init();
        ShareSDK.initSDK(this);
        getIpAddress();
    }

    /**
     * 根据域名解析ip
     */
    public void getIpAddress() {
        if (ota_server_url.equals(CommonUrl.OTA_SERVER_URL)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    InetAddress address = null;
                    try {
                        address = InetAddress.getByName(NetworkUtils.getDomainName(CommonUrl.OTA_SERVER_URL));
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    if (address != null) {
                        ota_server_ip = address.getHostAddress();
                        getSp().edit().putString(Constant.SP_SERVER_IP, address.getHostAddress()).commit();
                    }
                }
            }).start();
        }
    }

    private void init() {
        ota_server_url = sp.getString(Constant.SP_SERVER_URL, CommonUrl.OTA_INTERNATIONAL_URL); //https://iot.seeed.cc/v1 //https://cn.iot.seeed.cc/v1
        ota_server_ip = sp.getString(Constant.SP_SERVER_IP, CommonUrl.OTA_INTERNATIONAL_IP);
        int firstStart = getSp().getInt(Constant.APP_FIRST_START, 0);
        if (firstStart == 0) {
            if (CommonUrl.OTA_CHINA_URL.equals(ota_server_url)||CommonUrl.OTA_CHINA_OLD_URL.equals(ota_server_url)
                    || CommonUrl.OTA_INTERNATIONAL_URL.equals(ota_server_url)||CommonUrl.OTA_INTERNATIONAL_OLD_URL.equals(ota_server_url)) {
                UserLogic.getInstance().logOut();
            }
            getSp().edit().putInt(Constant.APP_FIRST_START, 1).commit();
        }
        if (CommonUrl.OTA_CHINA_OLD_URL.equals(ota_server_url)){
            ota_server_url = CommonUrl.OTA_CHINA_URL;
        }
        IotApi.SetServerUrl(ota_server_url);
    }

    public static App getApp() {
        return sApp;
    }

    public static SharedPreferences getSp() {
        return sp;
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

    public static void showToastShrot(String str) {
        Toast toast = Toast.makeText(sApp, str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showToastLong(String str) {
        Toast toast = Toast.makeText(sApp, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
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

  /*  public User getUser() {
        return user;
    }*/

  /*  public void setUser(User user) {
        this.user = user;
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("userName", user.email);
        editor.putString("userToken", user.user_key);
        editor.apply();
    }*/

    public String getOtaServerUrl() {
        return ota_server_url;
    }

    public void setOtaServerUrl(String ota_server_url) {
        this.ota_server_url = ota_server_url;
        IotApi.SetServerUrl(ota_server_url);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constant.SP_SERVER_URL, ota_server_url);
        editor.apply();
    }
/*

    public String getExchangeServerUrl() {
        return exchange_server_url;
    }

    public void setExchangeServerUrl(String exchange_server_url) {
        this.exchange_server_url = exchange_server_url;
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("exchange_server_url", exchange_server_url);
        editor.apply();
    }
*/

    public String getOtaServerIP() {
        return ota_server_ip;
    }

    public void setOtaServerIP(String ota_server_ip) {
        this.ota_server_ip = ota_server_ip;
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constant.SP_SERVER_IP, ota_server_ip);
        editor.apply();
    }

    public void setConfigState(Boolean configState) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("configState", configState);
        editor.apply();
    }

    public void saveUrlAndIp(String url, String ip) {
        setOtaServerUrl(url);
        setOtaServerIP(ip);
    }

    public boolean isDefaultServer() {
        if (CommonUrl.OTA_CHINA_URL.equals(ota_server_url) || CommonUrl.OTA_INTERNATIONAL_URL.equals(ota_server_url) || CommonUrl.OTA_INTERNATIONAL_OLD_URL.equals(ota_server_url)) {
            return true;
        } else {
            return false;
        }
    }

    private List<Activity> activities = new ArrayList<Activity>();

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        for (Activity activity : activities) {
            activity.finish();
        }
        System.exit(0);
    }

}