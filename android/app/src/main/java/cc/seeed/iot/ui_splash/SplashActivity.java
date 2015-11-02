package cc.seeed.iot.ui_splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.ui_login.SetupActivity;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.webapi.model.Node;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        print_node();

        new Handler().postDelayed(new Runnable() {
            public void run() {
                choice();
            }
        }, 1000);

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
        Boolean loginState = ((MyApplication) getApplication()).getLoginState();
        if (loginState) {
            Intent intent = new Intent(this, MainScreenActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
        }
    }

}
