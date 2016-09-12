package cc.seeed.iot.net;

/**
 * 返回时的包
 */
public class Packet {
    public String data;//返回的内容
    public int code;//状态码
    public String errorMsg;//错误信息
    public boolean status ;//判断网络请求是不是成功
}
