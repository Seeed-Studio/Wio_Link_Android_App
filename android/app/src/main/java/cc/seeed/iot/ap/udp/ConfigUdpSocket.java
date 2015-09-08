package cc.seeed.iot.ap.udp;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by tenwong on 15/7/30.
 */
public class ConfigUdpSocket {
    /**
     * Search all node with node config state, reponse node_sn and node's ip
     */
    public static String CMD_BLANK = "Blank?";

    /**
     * Search all node with node normal state, reponse node_sn and node's ip
     */
    public static String CMD_NODE = "Node?";

    public final int PORT = 1025;
    private final byte[] buffer;
    private DatagramSocket socket;
    private DatagramPacket mReceivePacket;

    public ConfigUdpSocket() {
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            Log.e("iot", "ConfigUdpSocket error:" + e);
        }
        buffer = new byte[1024];
        mReceivePacket = new DatagramPacket(buffer, buffer.length);
    }

    public void sendData(String data, String ipStr) {
        byte[] d = data.getBytes(Charset.forName("US-ASCII"));
        try {
            InetAddress address = InetAddress.getByName(ipStr);
            DatagramPacket packet = new DatagramPacket(d, d.length, address, PORT);
            socket.send(packet);
        } catch (Exception e) {
            Log.e("iot", "sendData error:" + e);
        }
    }


    public byte[] receiveData() throws IOException {
        byte[] data;
        socket.receive(mReceivePacket);
        data = Arrays.copyOf(mReceivePacket.getData(), mReceivePacket.getLength());
        return data;
    }

    public ConfigNodeData receiveNodeData() throws IOException {
        ConfigNodeData configNodeData = new ConfigNodeData();
        byte[] b = receiveData();
        String d = new String(b);

        if (!d.substring(0, 4).equals("Node")) {
            Log.e("iot", "invalid data!");
            return null;
        }

        d = d.replaceFirst("Node: ", "");
        d = d.replaceAll("\r\n", "");

        String data[] = d.split(",");
        configNodeData.node_sn = data[0];
        configNodeData.mac = data[1];
        configNodeData.ip = data[2];

        return configNodeData;

    }

    public void closeSocket() {
        socket.close();
    }

    public boolean setSoTimeout(int timeout) {
        try {
            socket.setSoTimeout(timeout);
            return true;
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return false;
    }
}
