package cc.seeed.iot.net;

/**
 * 网络回调
 */
public interface INetCallback {
    void onResp(Request req, Packet resp);
}
