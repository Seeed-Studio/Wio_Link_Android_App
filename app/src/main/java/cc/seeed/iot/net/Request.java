package cc.seeed.iot.net;

public class Request {
    public long timeOut = 8 * 1000;
    public INetCallback callback;//网络请求回调
    public String cmd;//请求关键字
}
