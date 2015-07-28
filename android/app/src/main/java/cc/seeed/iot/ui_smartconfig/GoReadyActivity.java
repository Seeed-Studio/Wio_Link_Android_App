package cc.seeed.iot.ui_smartconfig;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import cc.seeed.iot.R;

public class GoReadyActivity extends AppCompatActivity {
    public Toolbar mToolbar;
    public Button mGoReadyButtonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smartconfig_ready);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("WIFI Iot Node");


        mGoReadyButtonView = (Button) findViewById(R.id.smartconfig_ready_btn);
        mGoReadyButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo: do your work.
            }
        });
    }

}
