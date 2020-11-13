package cc.seeed.iot.activity;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.logic.FeedbackLogic;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.RegularUtils;
import cc.seeed.iot.view.FontEditView;

/**
 * Created by seeed on 2016/3/15.
 */
public class FeedbackActivity extends BaseActivity  {

    int descWordsMax = 200;
    Dialog dialog;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.mETEmailAddress)
    FontEditView mETEmailAddress;
    @InjectView(R.id.mETDesc)
    EditText mETDesc;
    @InjectView(R.id.mTVFontNum)
    TextView mTVFontNum;
//    ImageView mIVAddImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ButterKnife.inject(this);
        initView();
    }

    private void initView() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.feedback_title);

        User user = UserLogic.getInstance().getUser();
        if (user != null && !TextUtils.isEmpty(user.email) && !user.email.startsWith("testadmin")){
            mETEmailAddress.setText(user.email);
            mETEmailAddress.setSelection(user.email.length());
            mETDesc.requestFocus();
        }else {
            mETEmailAddress.requestFocus();
        }

        mETDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTVFontNum.setText(s.length() + "/" + descWordsMax);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > descWordsMax) {
                    mETDesc.setText(s.subSequence(0, descWordsMax));
                    mETDesc.setSelection(descWordsMax);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feedback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
         //   quitHint();
               finish();
            return true;
        } else if (id == R.id.submit) {
            submitData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void submitData() {
        String email = mETEmailAddress.getText().toString().trim();
        String desc = mETDesc.getText().toString().trim();
        if (!RegularUtils.isEmail(email)) {
            App.showToastShrot(getResources().getString(R.string.feedback_email_format_error));
            return;
        }

        if (desc.length() < 15) {
            App.showToastShrot("Description can not less than 15 words");
            return;
        }
        if (dialog == null)
            dialog = DialogUtils.showProgressDialog(this,null);

        FeedbackLogic.getInstance().submitFeedback(email,"Wio_App: "+ desc, 1);
    }

    @Override
    public void onEvent(String event, boolean ret, String errInfo, Object[] data) {
        if (event.equals(Cmd_Feedback)) {
            if (dialog != null) {
                dialog.dismiss();
            }
            if (ret) {
                mETDesc.setText("");
                submitSuccess();
            } else {
                App.showToastShrot(getResources().getString(R.string.feedback_submit_fail));
            }
        }
    }

    private void submitSuccess() {
        if (isFinishing()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Success!");
        builder.setMessage("Thank you for your feedback. We will get in touch with you soon.");
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.show();
    }



    @Override
    public String[] monitorEvents() {
        return new String[]{Cmd_Feedback};
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public static boolean isActivityRunning(Context mContex, String activityClassName) {
        ActivityManager activityManager = (ActivityManager) mContex.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(Integer.MAX_VALUE);
        if (info != null && info.size() > 0) {
            ComponentName component = info.get(0).topActivity;
            String className = component.getClassName();
            if (className.equals(activityClassName)) {
                return true;
            }
        }
        return false;
    }
}
