package cc.seeed.iot.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.udp.ConfigUdpSocket;
import cc.seeed.iot.ui_main.MainScreenActivity;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.LocationUtil;
import cc.seeed.iot.util.MLog;
import cc.seeed.iot.util.NetworkUtils;
import cc.seeed.iot.util.RegularUtils;
import cc.seeed.iot.util.SystemUtils;
import cc.seeed.iot.util.ToolUtil;
import cc.seeed.iot.web.WebActivity;


/**
 * Created by seeed on 2016/4/5.
 */
public class TestActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    @InjectView(R.id.mEtOrder)
    EditText mEtOrder;
    @InjectView(R.id.mBtnSend)
    Button mBtnSend;
    @InjectView(R.id.mEtRegular)
    EditText mEtRegular;
    @InjectView(R.id.mBtnCheckOut)
    Button mBtnCheckOut;
    @InjectView(R.id.mEtWebSite)
    EditText mEtWebSite;
    @InjectView(R.id.mBtnWebSite)
    Button mBtnWebSite;

    String order;
    @InjectView(R.id.mEtIpAdress)
    EditText mEtIpAdress;
    @InjectView(R.id.mBtngetIp)
    Button mBtngetIp;
    @InjectView(R.id.mRBInterNet)
    RadioButton mRBInterNet;
    @InjectView(R.id.mRBOutNet)
    RadioButton mRBOutNet;
    @InjectView(R.id.mRGServer)
    RadioGroup mRGServer;
    @InjectView(R.id.mBtnCreatUser)
    Button mBtnCreatUser;
    @InjectView(R.id.mTvDomain)
    TextView mTvDomain;
    @InjectView(R.id.mTvIP)
    TextView mTvIP;
    @InjectView(R.id.mBtnOpenWifi)
    Button mBtnOpenWifi;
    @InjectView(R.id.mBtnEditName)
    Button mBtnEditName;
    @InjectView(R.id.mBtnNodeResult)
    Button mBtnNodeResult;
    @InjectView(R.id.mBtnGithub)
    Button mBtnGithub;

    public int checkId = 0;
    LocationUtil locationUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.inject(this);
        if (!ToolUtil.isApkDebug()) {
            finish();
        }
        mRGServer.setOnCheckedChangeListener(this);
        initData();
    }

    public void initData() {
        int server = App.getApp().getSp().getInt(Constant.SP_SERVER_SELECT, Constant.Server.In_Net.getValue());

        if (server == Constant.Server.In_Net.getValue()) {
            checkId = mRBInterNet.getId();
            mRBInterNet.setChecked(true);
        } else {
            checkId = mRBOutNet.getId();
            mRBOutNet.setChecked(true);
        }

        locationUtil = new LocationUtil();
        //    locationUtil.init(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkId == checkedId) {
            return;
        } else {
            checkId = checkedId;

        }
        if (checkedId == R.id.mRBInterNet) {
            App.getApp().getSp().edit().putInt(Constant.SP_SERVER_SELECT, Constant.Server.In_Net.getValue()).commit();
            UserLogic.getInstance().logOut();
            startActivity(new Intent(TestActivity.this, WelcomeActivity.class));
        } else {
            App.getApp().getSp().edit().putInt(Constant.SP_SERVER_SELECT, Constant.Server.Out_Net.getValue()).commit();
            UserLogic.getInstance().logOut();
            startActivity(new Intent(TestActivity.this, WelcomeActivity.class));
        }
    }


    @OnClick({R.id.mBtnSend, R.id.mBtnCheckOut, R.id.mBtngetIp, R.id.mBtnCreatUser, R.id.mBtnOpenWifi, R.id.mBtnEditName, R.id.mBtnNodeResult, R.id.mBtnGithub,R.id.mBtnWebSite})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mBtnSend:
                sendOrder();
                break;
            case R.id.mBtnCheckOut:
                regular();
                break;
            case R.id.mBtngetIp:
                getDomain();
                break;
            case R.id.mBtnWebSite:
                gotoWebView();
                break;
            case R.id.mBtnCreatUser:
                addUser();
                break;
            case R.id.mBtnOpenWifi:
                WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                wifiManager.setWifiEnabled(true);
                break;
            case R.id.mBtnEditName:
//                DialogUtils.showEditNodeNameDialog(this, "", null);
              //  showDialog();
               // gotoConfig();
                gotoWifiSetting();
                //  DialogUtils.showWarningDialog(this,null);
                //  new LocationUtil().startLocation(this);
                //   locationUtil.startLocation();
                //     locationUtil.location(this);
                break;
            case R.id.mBtnNodeResult:
                startActivity(new Intent(this, GroveResultActivity.class));
                break;
            case R.id.mBtnGithub:
                String url = "https://github.com/login/oauth/authorize?client_id=1af08fd6cf012a0aeb49&redirect_uri=http://www.seeedstudio.com&scope=user:follow%20user:email";

                Intent intent = new Intent(this, WebActivity.class);
                intent.putExtra(WebActivity.Param_Url, url);
                startActivity(intent);
                break;
        }

    }

    private void gotoWebView(){
        String Url = mEtWebSite.getText().toString().trim();
        if (!RegularUtils.isWebsite(Url)){
            App.showToastShrot("网址不合法");
        }else {
            Intent intent = new Intent(this, WebActivity.class);
            intent.putExtra(WebActivity.Param_Url,Url);
            startActivity(intent);
        }
    }

    private void gotoWifiSetting(){
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); //直接进入手机中的wifi网络设置界面
      /*  Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");
        startActivity(wifiSettingsIntent);*/
       /* Intent intent = new Intent();
        intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
        intent.putExtra("extra_prefs_show_button_bar", true);
//intent.putExtra("extra_prefs_set_next_text", "完成");
//intent.putExtra("extra_prefs_set_back_text", "返回");
        intent.putExtra("wifi_enable_next_on_connect", true);
        startActivity(intent);*/
    }

    private void showDialog() {
        new checkNodeIsOnline().execute();
    }

    private class checkNodeIsOnline extends AsyncTask<Void, Integer, Boolean> {
        private Boolean state_online = false;

        @Override
        protected void onPreExecute() {
            //  showProgressDialog("Waiting Wio get ip address...");
            //   showProgressText("Get device status...");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            state_online = true;
            return state_online;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }

        @Override
        protected void onPostExecute(Boolean state_online) {
            if (isFinishing()) { // or call isFinishing() if min sdk version < 17
                return;
            }
            if (state_online) {

                DialogUtils.showErrorDialog(TestActivity.this, "Connection Error", "TRY AGAIN", "Cancel", "Please check your internet connection and try again.\r\n" +
                        "If still can’t slove the problem, please try FAQ section and contact us there. ", new DialogUtils.OnErrorButtonClickListenter() {
                    @Override
                    public void okClick() {
                        App.showToastShrot("ok");
                    }

                    @Override
                    public void cancelClick() {
                        App.showToastShrot("cancel");
                    }
                });
            }
        }
    }

    private void addUser() {
        String userStr = App.getSp().getString(Constant.SP_USER_TEST_INFO, "");
        String userStr1 = App.getSp().getString(Constant.SP_USER_INFO, "");
        if (!TextUtils.isEmpty(userStr1)) {
            return;
        }
        try {
            Gson gson = new Gson();
            User user = gson.fromJson(userStr, User.class);
            if (user == null) {
                user = new User();
                user.setEmail("947700923@qq.com");
                user.setNickname("Jerry_Test");
                user.setToken("VSFFsdgagagF");
                user.setUserid("161319");
                UserLogic.getInstance().saveUser(user);
                UserLogic.getInstance().setToken("");
            } else {
                UserLogic.getInstance().saveUser(user);
            }

            startActivity(new Intent(this, MainScreenActivity.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getDomain() {
        String url = mEtIpAdress.getText().toString().trim();
        if (!RegularUtils.isWebsite(url)) {
            App.showToastShrot("格式不正确");
        } else {
            mTvDomain.setText(NetworkUtils.getDomainName(url));
            getIpAddress(url);
        }
    }

    public void getIpAddress(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InetAddress address = null;
                try {
                    address = InetAddress.getByName(NetworkUtils.getDomainName(url));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                if (address != null) {
                    final InetAddress finalAddress = address;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mTvIP.setText(finalAddress.getHostAddress());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            }
        }).start();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    public void regular() {
        String url = mEtRegular.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            App.showToastShrot("input is emputy");
            return;
        } else {
            if (RegularUtils.isWebsite(url)) {
                if (RegularUtils.isIP(url)) {
                    App.showToastShrot("is IP");
                } else if (RegularUtils.isDomainName(url)) {
                    App.showToastShrot("is doma name");
                } else {
                    App.showToastShrot("is false");
                }
            } else {
                App.showToastShrot("website address format error");
            }
        }
    }

    public void sendOrder() {
        order = mEtOrder.getText().toString().trim();
        if (TextUtils.isEmpty(order)) {
            App.showToastShrot("input is emputy");
            return;
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ConfigUdpSocket  udpClient = new ConfigUdpSocket();
                    udpClient.setSoTimeout(10000); //1s timeout
                    udpClient.sendData(order, "192.168.4.1");
                    try {
                        byte[] bytes = udpClient.receiveData();
                        if (new String(bytes).substring(0, 1 + 1).equals("ok")) {
                            MLog.d(this, "success");
                        }
                    } catch (SocketTimeoutException e) {
                        udpClient.setSoTimeout(3000);
                        udpClient.sendData(order, "192.168.4.1");
                        MLog.d("time out");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showCheckVersionTip();
                            }
                        });
                    } catch (IOException e) {
                        MLog.d(this, "fail");
                    }
                }
            }).start();
        }
    }

    private void showCheckVersionTip() {
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(TestActivity.this)
                .setMessage("Failed to get the firmware version")
                .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendOrder();
                    }
                })
                .setNeutralButton("Feedback", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(TestActivity.this, FeedbackActivity.class));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override

                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public void gotoConfig() {
       /* PackageInfo info = SystemUtils.getPackageInfo();
        Intent i = new Intent("miui.intent.action.APP_PERM_EDITOR");
        i.setClassName("com.android.settings", "com.miui.securitycenter.permission.AppPermissionsEditor");
        i.putExtra("extra_package_uid", info.applicationInfo.uid);
        try {
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "只有MIUI才可以设置哦", Toast.LENGTH_SHORT).show();
        }*/

        /*Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings","com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }

        try {
            startActivity(localIntent);
        } catch (Exception e) {
            Toast.makeText(this, "只有MIUI才可以设置哦", Toast.LENGTH_SHORT).show();
        }*/
        SystemUtils.gotoAppConfigView(this);
    }

}
