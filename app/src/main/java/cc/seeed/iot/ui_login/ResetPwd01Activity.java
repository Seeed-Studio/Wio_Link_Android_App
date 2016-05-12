package cc.seeed.iot.ui_login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.util.ToolUtil;

public class ResetPwd01Activity extends BaseActivity {
    private static final String TAG = "ResetPwd01Activity";

    @InjectView(R.id.input_email)
    EditText _emailText;
    @InjectView(R.id.btn_reset)
    Button _resetButton;
    @InjectView(R.id.link_cancel)
    TextView _cancelLink;

    ProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pwd_01);
        ButterKnife.inject(this);

        _emailText.setText(App.getSp().getString("user_email", ""));

        _resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(_emailText);
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
           App.showToastShrot("Reset failed");
            return;
        }

        _resetButton.setEnabled(false);

        dialog = new ProgressDialog(ResetPwd01Activity.this, R.style.AppTheme_Dark_Dialog);
        dialog.setIndeterminate(true);
        dialog.setMessage("Reset your email...");
        dialog.show();

        String email = _emailText.getText().toString();
        App.getSp().edit().putString("user_email", email).commit();
        UserLogic.getInstance().forgetPwd(email);
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

    @Override
    public String[] monitorEvents() {
        return new String[]{Cmd_UserForgetPwd};
    }

    @Override
    public void onEvent(String event, boolean ret, String errInfo, Object[] data) {
       if (Cmd_UserForgetPwd.equals(event)){
           if (!ToolUtil.isTopActivity(ResetPwd01Activity.this,ResetPwd01Activity.this.getClass().getSimpleName())){
               return;
           }
           if (dialog != null){
               dialog.dismiss();
           }
           _resetButton.setEnabled(true);
           if (ret){
               App.showToastShrot("Verification code has been sent");
               Intent intent = new Intent(ResetPwd01Activity.this, ResetPwd02Activity.class);
               intent.putExtra(ResetPwd02Activity.Intent_Email,_emailText.getText().toString());
               startActivity(intent);
           }else {
               _emailText.setError(errInfo);
               _emailText.requestFocus();
               App.showToastShrot("Reset failed");
           }
       }
    }
}