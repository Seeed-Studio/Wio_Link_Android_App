package cc.seeed.iot.ui_splash;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.ui_setup.SetupActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                choice();
            }
        }, 1000);

    }

    private void choice() {
        Boolean loginState = ((MyApplication) getApplication()).getConfigState();
        if (loginState) {
            Intent intent = new Intent(this, MainScreenActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
        }
    }

}
