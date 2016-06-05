package cc.seeed.iot.activity;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
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
import cc.seeed.iot.util.MLog;
import cc.seeed.iot.util.NetworkUtils;
import cc.seeed.iot.util.RegularUtils;
import cc.seeed.iot.util.ToolUtil;


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

    private ConfigUdpSocket udpClient;
    public int checkId = 0;

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


    @OnClick({R.id.mBtnSend, R.id.mBtnCheckOut, R.id.mBtngetIp, R.id.mBtnCreatUser, R.id.mBtnOpenWifi,R.id.mBtnEditName})
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
            case R.id.mBtnCreatUser:
                addUser();
                break;
            case R.id.mBtnOpenWifi:
                WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                wifiManager.setWifiEnabled(true);
                break;
            case R.id.mBtnEditName:
                DialogUtils.showEditNodeNameDialog(this,"",null);
                break;
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
                    udpClient = new ConfigUdpSocket();
                    udpClient.setSoTimeout(10000); //1s timeout
                    udpClient.sendData(order, "192.168.4.1");
                    for (int i = 0; i < 3; i++) {
                        try {
                            byte[] bytes = udpClient.receiveData();
                            if (new String(bytes).substring(0, 1 + 1).equals("ok")) {
                                MLog.d(this, "success");
                                break;
                            }
                        } catch (SocketTimeoutException e) {
                            udpClient.setSoTimeout(30000);
                            udpClient.sendData(order, "192.168.4.1");

                        } catch (IOException e) {
                            MLog.d(this, "fail");
                        }
                    }
                }
            }).start();

        }
    }

}
