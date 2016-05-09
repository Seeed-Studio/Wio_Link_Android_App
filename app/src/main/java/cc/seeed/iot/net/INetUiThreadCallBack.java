package cc.seeed.iot.net;


/**
 * 回调,UI线程回调
 */
public abstract class INetUiThreadCallBack implements INetCallback{
    public abstract void onResp(Request req, Packet resp);
}
