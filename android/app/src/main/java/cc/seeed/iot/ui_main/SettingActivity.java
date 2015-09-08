package cc.seeed.iot.ui_main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.R;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar mToolbar;
    private TextView mUrlView;
    private Button mChangeView;

    private String server_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Setting");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mUrlView = (TextView) findViewById(R.id.url);
        mChangeView = (Button) findViewById(R.id.bt_change);
        mChangeView.setOnClickListener(this);

        initView();
    }

    private void initView() {
        server_url = ((MyApplication) getApplication()).getServer_url();
        mUrlView.setText(server_url);
        mUrlView.setEnabled(false);
        mChangeView.setText("Change");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.bt_change) {
            String action = mChangeView.getText().toString();
            if (action.equals("Change")) {
                mChangeView.setText("Confirm");
                mUrlView.setEnabled(true);
            } else if (action.equals("Confirm")) {
                mChangeView.setText("Change");
                mUrlView.setEnabled(false);

                saveServerUrl(mUrlView.getText().toString());
                //todo: if change error, then do not change to back?
            }
        }
    }

    private void saveServerUrl(String s) {
        ((MyApplication) getApplication()).setServer_url(s);
        IotApi.SetServerUrl(s);
    }

}
