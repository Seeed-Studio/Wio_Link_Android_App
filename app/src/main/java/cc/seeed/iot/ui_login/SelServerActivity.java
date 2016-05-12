package cc.seeed.iot.ui_login;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.util.CommonUrl;
import cc.seeed.iot.webapi.IotApi;

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
                hideKeyboard();
                saveServer();
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

        String ota_server_url = ((App) getApplication()).getOtaServerUrl();
        if (ota_server_url.equals(CommonUrl.OTA_SERVER_URL)) {
            _serverSpinner.setSelection(0, true);
        }  else {
            _serverSpinner.setSelection(1, true);
            _inputLayout.setVisibility(View.VISIBLE);
            _serverIpText.setText(ota_server_url);
        }

        _serverSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        _inputLayout.setVisibility(View.GONE);
                        break;
                    case 1:
                        _inputLayout.setVisibility(View.VISIBLE);
                        _serverIpText.setSelection(_serverIpText.getText().length());
                        _serverIpText.setError("e.g. https://192.168.31.2 or https://iot.seeed.cc");
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
        Toast.makeText(SelServerActivity.this, "Save failed", Toast.LENGTH_LONG).show();

        _saveButton.setEnabled(true);
    }

    public void saveServer() {

        _saveButton.setEnabled(false);

        String ota_server_ip;
        String ota_server_url;
        switch (_serverSpinner.getSelectedItemPosition()) {
            default:
            case 0:
                ota_server_ip = CommonUrl.OTA_SERVER_IP;
                ota_server_url = CommonUrl.OTA_SERVER_URL;
                saveUrlAndIp(ota_server_url, ota_server_ip);
                onSaveSuccess();
                break;
            case 1:

                ota_server_url = _serverIpText.getText().toString();

                final ProgressDialog progressDialog = new ProgressDialog(SelServerActivity.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Get your server's host address...");
                progressDialog.show();

                getHostAddress(ota_server_url, progressDialog);
                break;
        }
    }

    private void getHostAddress(final String ota_server_url, final ProgressDialog progressDialog) {
        new Thread() {
            @Override
            public void run() {
                try {
                    InetAddress address = InetAddress.getByName(new URL(ota_server_url).getHost());
                    final String ota_server_ip = address.getHostAddress();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.setMessage("Attempt to connect your server...");
                        }
                    });

                    GetStausCode getStausCode = new GetStausCode();
                    int response = getStausCode.run(ota_server_url + "/v1/test");
                    if (response == 200) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                saveUrlAndIp(ota_server_url, ota_server_ip);
                                onSaveSuccess();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                _serverIpText.setError("Can't connect to your server.");
                                onSaveFailed();
                            }
                        });
                    }
                    progressDialog.dismiss();

                } catch (final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            _serverIpText.setError(e.getMessage());
                            onSaveFailed();
                        }
                    });
                    progressDialog.dismiss();
                }
            }
        }.start();


    }

    private void saveUrlAndIp(String ota_server_url, String ota_server_ip) {
        ((App) getApplication()).setOtaServerIP(ota_server_ip);
        ((App) getApplication()).setOtaServerUrl(ota_server_url);
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

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(_saveButton.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public class GetStausCode {
//        OkHttpClient client = new OkHttpClient();
        OkHttpClient client = IotApi.getUnsafeOkHttpClient();

        int run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            return response.code();
        }
    }

}