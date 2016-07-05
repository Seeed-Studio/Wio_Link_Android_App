package cc.seeed.iot.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.umeng.analytics.MobclickAgent;

import java.util.List;

import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.user.LoginAndRegistActivity;
import cc.seeed.iot.entity.ServerBean;
import cc.seeed.iot.logic.SystemLogic;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.util.CommonUrl;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.TimeUtils;
import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.webapi.model.Node;

public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setServer();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                choice();
            }
        }, 500);

    }

    private void print_node() {

        List<Node> nodes = DBHelper.getNodesAll();
        for (Node node : nodes) {
            Log.e(getClass().getName(), node.name + " " + node.node_key + " " + node.node_sn);
        }

        List<GroverDriver> groverDrivers = DBHelper.getGrovesAll();
        for (GroverDriver groverDriver : groverDrivers) {
            Log.e(getClass().getName(), groverDriver.ID + " " + groverDriver.GroveName + " " + groverDriver.ImageURL);
        }
    }

    private void choice() {
        if (UserLogic.getInstance().isLogin()) {
            Intent intent = new Intent(this, MainScreenActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, LoginAndRegistActivity.class);
            startActivity(intent);
        }
        finish();
    }

    private void setServer() {
        String serverUrl = App.getApp().getOtaServerUrl();
        if (CommonUrl.OTA_INTERNATIONAL_OLD_URL.equals(serverUrl)) {
            ServerBean serverBean = SystemLogic.getInstance().getServerBean();
            if (serverBean == null) {
                SystemLogic.getInstance().getServerStopMsg();
            } else {
                if (System.currentTimeMillis() / 1000 > serverBean.getContent().get(0).getServerEndTime()) {
                    App.getApp().saveUrlAndIp(CommonUrl.OTA_INTERNATIONAL_URL, CommonUrl.OTA_INTERNATIONAL_IP);
                    UserLogic.getInstance().logOut();
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

}
