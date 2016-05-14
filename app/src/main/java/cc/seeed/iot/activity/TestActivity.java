package cc.seeed.iot.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.udp.ConfigUdpSocket;
import cc.seeed.iot.util.CommonUrl;
import cc.seeed.iot.util.MLog;
import cc.seeed.iot.util.NetworkUtils;
import cc.seeed.iot.util.RegularUtils;


/**
 * Created by seeed on 2016/4/5.
 */
public class TestActivity extends BaseActivity {

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
    @InjectView(R.id.mTvDomain)
    TextView mTvDomain;
    @InjectView(R.id.mTvIP)
    TextView mTvIP;

    private ConfigUdpSocket udpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.inject(this);
    }

    @OnClick({R.id.mBtnSend, R.id.mBtnCheckOut, R.id.mBtngetIp})
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
        }
    }

    public void getDomain(){
        String url = mEtIpAdress.getText().toString().trim();
        if (!RegularUtils.isWebsite(url)){
            App.showToastShrot("格式不正确");
        }else {
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
