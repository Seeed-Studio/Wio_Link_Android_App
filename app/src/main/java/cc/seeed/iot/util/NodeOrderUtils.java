package cc.seeed.iot.util;

import java.io.IOException;
import java.net.SocketTimeoutException;

import cc.seeed.iot.udp.ConfigUdpSocket;

/**
 * author: Jerry on 2016/5/18 11:30.
 * description:
 */
public class NodeOrderUtils {
    public void getNodeVersion(final OnSendOrderListenter listenter) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConfigUdpSocket udpClient = new ConfigUdpSocket();
                udpClient.setSoTimeout(10000); //1s timeout
                udpClient.sendData(Common.NodeOrder.VERSION.getValue(), "192.168.4.1");
                for (int i = 0; i < 3; i++) {
                    try {
                        byte[] bytes = udpClient.receiveData();
                        String result = new String(bytes).substring(0, 3);
                        if (result.matches("[0-9].[0-9]")) {
                            if (listenter != null){
                                listenter.onSuccess(result);
                            }
                            MLog.d(this, "success");
                            break;
                        }
                    } catch (SocketTimeoutException e) {
                        udpClient.setSoTimeout(30000);
                        udpClient.sendData(Common.NodeOrder.VERSION.getValue(), "192.168.4.1");

                    } catch (IOException e) {
                        MLog.d(this, "fail");
                        if (listenter != null){
                            listenter.onFail(e.toString());
                        }
                    }
                }
            }
        }).start();
    }

    public interface OnSendOrderListenter {
        void onSuccess(String result);
        void onFail(String error);
    }
}
