package cc.seeed.iot.ui_smartconfig;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cc.seeed.iot.R;

public class SmartConnectActivity extends AppCompatActivity {
    public Toolbar mToolbar;
    public TextView mSssidView;
    public EditText mPasswordView;
    public Button mConnectBtnView;
    private EspWifiAdminSimple mWifiAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smartconfig_connect);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("WIFI Iot Node");

        mSssidView = (TextView) findViewById(R.id.ssid);
        mPasswordView = (EditText) findViewById(R.id.wifi_password);
        mConnectBtnView = (Button) findViewById(R.id.first_time_how_to_api_key);


        mWifiAdmin = new EspWifiAdminSimple(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // display the connected ap's ssid
        String apSsid = mWifiAdmin.getWifiConnectedSsid();
        if (apSsid != null) {
            mSssidView.setText(apSsid);
        } else {
            mSssidView.setText("");
        }
        // check whether the wifi is connected
        boolean isApSsidEmpty = TextUtils.isEmpty(apSsid);
        mConnectBtnView.setEnabled(!isApSsidEmpty);
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
}
