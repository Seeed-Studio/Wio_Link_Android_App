package cc.seeed.iot.ui_login;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.util.Common;

public class SelServerActivity extends AppCompatActivity {
    private static final String TAG = "SelServerActivity";

    @InjectView(R.id.input_ip)
    EditText _serverIpText;
    @InjectView(R.id.btn_save)
    Button _saveButton;
    @InjectView(R.id.link_cancel)
    TextView _cancelLink;
    @InjectView(R.id.spinner_server)
    Spinner _serverSpinner;
    @InjectView(R.id.input_layout)
    TextInputLayout _inputLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sel_server);
        ButterKnife.inject(this);

        _saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveServerIP();

            }
        });

        _cancelLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.server, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _serverSpinner.setAdapter(adapter);

        String ota_server_ip = ((MyApplication) getApplication()).getOtaServerIP();
        if (ota_server_ip.equals(Common.OTA_INTERNATIONAL_IP)) {
            _serverSpinner.setSelection(0, true);
        } else if (ota_server_ip.equals(Common.OTA_CHINA_IP)) {
            _serverSpinner.setSelection(1, true);
        } else {
            _serverSpinner.setSelection(2, true);
            _inputLayout.setVisibility(View.VISIBLE);
            _serverIpText.setText(ota_server_ip);
        }

        _serverSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        _inputLayout.setVisibility(View.GONE);
                        break;
                    case 1:
                        _inputLayout.setVisibility(View.GONE);
                        break;
                    case 2:
                        _inputLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void onSaveSuccess() {
        finish();
    }

    public void onSaveFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _serverIpText.setEnabled(true);
    }

    public void saveServerIP() {
        Log.d(TAG, "SaveServeIP");

        if (!validate()) {
            onSaveFailed();
            return;
        }

        _saveButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SelServerActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Saving server ip...");
        progressDialog.show();

        // TODO: Implement your own saveServerIP logic here.
        String ota_server_ip;
        String ota_server_url;
        if (_serverSpinner.getSelectedItemPosition() == 0) {
            ota_server_ip = Common.OTA_INTERNATIONAL_IP;
            ota_server_url = Common.OTA_INTERNATIONAL_URL;
        } else if (_serverSpinner.getSelectedItemPosition() == 1) {
            ota_server_ip = Common.OTA_CHINA_IP;
            ota_server_url = Common.OTA_CHINA_URL;
        } else {
            ota_server_ip = _serverIpText.getText().toString();
            ota_server_url = "http://" + ota_server_ip + ":8080";
        }

//        Log.e(TAG, ota_server_ip);
//        Log.e(TAG, ota_server_url);
        ((MyApplication) getApplication()).setOtaServerIP(ota_server_ip);
        ((MyApplication) getApplication()).setOtaServerUrl(ota_server_url);
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        onSaveSuccess();
                        progressDialog.dismiss();
                    }
                }, 1000);
    }

    public boolean validate() {
        boolean valid = true;

        String ip = _serverIpText.getText().toString();

        if (ip.isEmpty() || !Patterns.IP_ADDRESS.matcher(ip).matches()) {
            _serverIpText.setError("enter a valid ip address");
            valid = false;
        } else {
            _serverIpText.setError(null);
        }


        return valid;
    }
}