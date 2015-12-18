package cc.seeed.iot.ui_login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cc.seeed.iot.R;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.CommonResponse;
import retrofit.Callback;
import retrofit.RetrofitError;

public class ResetActivity extends AppCompatActivity {
    private static final String TAG = "ResetActivity";

    @InjectView(R.id.input_email)
    EditText _emailText;
    @InjectView(R.id.btn_reset)
    Button _resetButton;
    @InjectView(R.id.link_cancel)
    TextView _cancelLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        ButterKnife.inject(this);

        _resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                resetEmail();
            }
        });

        _cancelLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void resetEmail() {
        Log.d(TAG, "ResetEmail");

        if (!validate()) {
            onResetFailed();
            return;
        }

        _resetButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(ResetActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Reset your email...");
        progressDialog.show();

        String email = _emailText.getText().toString();

        //Implement your own resetEmail logic here.
        resetPassword(email, progressDialog);
    }


    public void onResetSuccess() {
        _resetButton.setEnabled(true);
    }

    public void onResetFailed() {
        Toast.makeText(getBaseContext(), "Reset failed", Toast.LENGTH_LONG).show();

        _resetButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        return valid;
    }

    private boolean resetPassword(String email, final ProgressDialog progressDialog) {
        IotApi api = new IotApi();
        IotService iot = api.getService();
        iot.userRetrievePassword(email, new Callback<CommonResponse>() {
            @Override
            public void success(CommonResponse response, retrofit.client.Response response1) {
                String status = response.status;
                progressDialog.dismiss();
                if (status.equals("200")) {
                    onResetSuccess();
                    AlertDialog.Builder builder = new AlertDialog.Builder(ResetActivity.this);
                    builder.setPositiveButton(R.string.ok, null).create();
                    builder.setTitle("Success");
                    builder.setMessage(response.msg);
                    builder.show();
                } else {
                    progressDialog.dismiss();
                    onResetFailed();
                    _emailText.setError(response.msg);
                    _emailText.requestFocus();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(ResetActivity.this, R.string.ConnectServerFail, Toast.LENGTH_LONG).show();
            }
        });

        return true;
    }
    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(_resetButton.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }
}