package cc.seeed.iot.ui_login;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cc.seeed.iot.R;

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
//                resetEmail();
            }
        });

        _cancelLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.server, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _serverSpinner.setAdapter(adapter);

        _serverSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
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

//    public void resetEmail() {
//        Log.d(TAG, "Signup");
//
//        if (!validate()) {
//            onResetFailed();
//            return;
//        }
//
//        _resetButton.setEnabled(false);
//
//        final ProgressDialog progressDialog = new ProgressDialog(ResetActivity.this,
//                R.style.AppTheme_Dark_Dialog);
//        progressDialog.setIndeterminate(true);
//        progressDialog.setMessage("Creating Account...");
//        progressDialog.show();
//
//        String email = _emailText.getText().toString();
//        String password = _passwordText.getText().toString();
//
//        // TODO: Implement your own resetEmail logic here.
//
//        new android.os.Handler().postDelayed(
//                new Runnable() {
//                    public void run() {
//                        // On complete call either onResetSuccess or onResetFailed
//                        // depending on success
//                        onResetSuccess();
//                        // onResetFailed();
//                        progressDialog.dismiss();
//                    }
//                }, 3000);
//    }
//
//
//    public void onResetSuccess() {
//        _signupButton.setEnabled(true);
//        setResult(RESULT_OK, null);
//        finish();
//    }
//
//    public void onResetFailed() {
//        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
//
//        _signupButton.setEnabled(true);
//    }
//
//    public boolean validate() {
//        boolean valid = true;
//
//        String email = _emailText.getText().toString();
//        String password = _passwordText.getText().toString();
//        String pwd_verify = _pwdVerifyText.getText().toString();
//
//        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            _emailText.setError("enter a valid email address");
//            valid = false;
//        } else {
//            _emailText.setError(null);
//        }
//
//        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
//            _passwordText.setError("between 4 and 10 alphanumeric characters");
//            valid = false;
//        } else {
//            _passwordText.setError(null);
//        }
//
//        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
//            _pwdVerifyText.setError("between 4 and 10 alphanumeric characters");
//            valid = false;
//        } else {
//            _pwdVerifyText.setError(null);
//        }
//
//        return valid;
//    }
}