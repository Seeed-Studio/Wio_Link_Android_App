package cc.seeed.iot.ui_smartconfig;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.esptouch.EsptouchTask;
import cc.seeed.iot.esptouch.IEsptouchResult;
import cc.seeed.iot.esptouch.IEsptouchTask;
import cc.seeed.iot.esptouch.task.__IEsptouchTask;
import cc.seeed.iot.ui_main.MainScreenActivity;

public class SmartConnectActivity extends AppCompatActivity implements OnClickListener {
    private static final String TAG = "IOT";
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
        mConnectBtnView.setOnClickListener(this);

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

    @Override
    public void onClick(View v) {

        if (v == mConnectBtnView) {
            String apSsid = mSssidView.getText().toString();
            String apPassword = mPasswordView.getText().toString();
            String apBssid = mWifiAdmin.getWifiConnectedBssid();
//            Boolean isSsidHidden = mSwitchIsSsidHidden.isChecked(); //todo: add hidden view
            Boolean isSsidHidden = false;
            String isSsidHiddenStr = "NO";
//            String taskResultCountStr = Integer.toString(mSpinnerTaskCount
//                    .getSelectedItemPosition()); //todo: what means taskCount?
            String taskResultCountStr = "1";
            if (isSsidHidden) {
                isSsidHiddenStr = "YES";
            }
            if (__IEsptouchTask.DEBUG) {
                Log.d(TAG, "mBtnConfirm is clicked, mEdtApSsid = " + apSsid
                        + ", " + " mEdtApPassword = " + apPassword);
            }
            new EsptouchAsyncTask3().execute(apSsid, apBssid, apPassword,
                    isSsidHiddenStr, taskResultCountStr);
        }
    }

    private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {

        private ProgressDialog mProgressDialog;

        private IEsptouchTask mEsptouchTask;
        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(SmartConnectActivity.this);
            mProgressDialog
                    .setMessage("Esptouch is configuring, please wait for a moment...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    synchronized (mLock) {
                        if (__IEsptouchTask.DEBUG) {
                            Log.i(TAG, "progress dialog is canceled");
                        }
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                    }
                }
            });
            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    "Waiting...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            mProgressDialog.show();
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(false);
        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            int taskResultCount = -1;
            synchronized (mLock) {
                String apSsid = params[0];
                String apBssid = params[1];
                String apPassword = params[2];
                String isSsidHiddenStr = params[3];
                String taskResultCountStr = params[4];
                boolean isSsidHidden = false;
                if (isSsidHiddenStr.equals("YES")) {
                    isSsidHidden = true;
                }
                taskResultCount = Integer.parseInt(taskResultCountStr);
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword,
                        isSsidHidden, SmartConnectActivity.this);
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(true);
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).
                    setText("Confirm");
            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                int count = 0;
                // max results to be displayed, if it is more than maxDisplayCount,
                // just show the count of redundant ones
                final int maxDisplayCount = 5;
                // the task received some results including cancelled while
                // executing before receiving enough results
                if (firstResult.isSuc()) {
                    StringBuilder sb = new StringBuilder();
                    for (IEsptouchResult resultInList : result) {
                        sb.append("Esptouch success, bssid = "
                                + resultInList.getBssid()
                                + ",InetAddress = "
                                + resultInList.getInetAddress()
                                .getHostAddress() + "\n");
                        count++;
                        if (count >= maxDisplayCount) {
                            break;
                        }
                    }
                    if (count < result.size()) {
                        sb.append("\nthere's " + (result.size() - count)
                                + " more result(s) without showing\n");
                    }
                    mProgressDialog.setMessage(sb.toString());
                    mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).
                            setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (((MyApplication) getApplication()).getConfigState()) {
                                        Intent intent = new Intent(SmartConnectActivity.this,
                                                ConfigNodeListActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(SmartConnectActivity.this,
                                                MainScreenActivity.class);
                                        startActivity(intent);
                                    }

                                    mProgressDialog.dismiss();
                                }
                            });

                } else {
                    mProgressDialog.setMessage("Esptouch fail");
                    mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).
                            setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Snackbar.make(v, "todo: reconnect node!", Snackbar.LENGTH_SHORT).show();
                                    mProgressDialog.dismiss();
                                }
                            });
                }
            }
        }
    }

}
