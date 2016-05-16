package cc.seeed.iot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.activity.user.LoginAndRegistActivity;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.ui_login.SetupActivity;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.webapi.model.Node;

public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        print_node();

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

        List<GroverDriver> groverDrivers= DBHelper.getGrovesAll();
        for (GroverDriver groverDriver:groverDrivers) {
            Log.e(getClass().getName(), groverDriver.ID + " " + groverDriver.GroveName + " " + groverDriver.ImageURL );
        }
    }

    private void choice() {
      //  Boolean loginState = ((App) getApplication()).getLoginState();

        if (UserLogic.getInstance().isLogin()) {
            Intent intent = new Intent(this, MainScreenActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, LoginAndRegistActivity.class);
            startActivity(intent);
        }
        finish();
    }

}
