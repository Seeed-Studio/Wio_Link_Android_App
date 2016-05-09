package cc.seeed.iot.util;

/**
 * Created by tenwong on 15/10/20.
 */
public class CommonUrl {
 /*   public static String OTA_SERVER_URL = "https://cn.iot.seeed.cc";
    public static String OTA_INTERNATIONAL_URL = "https://iot.seeed.cc";

    public static String OTA_SERVER_IP = "120.25.216.117";
    public static String OTA_INTERNATIONAL_IP = "45.79.4.239";*/

    //  public static String OTA_SERVER_URL = "https://wio.seeed.io";
    //public static String OTA_INTERNATIONAL_URL = "https://wio.seeed.io";
//  public static String OTA_SERVER_IP = "192.168.21.48";
//   public static String OTA_INTERNATIONAL_IP = "192.168.21.48";
   // public static String ota_server_url = "https://wio.seeed.io";
  //  public static String ota_server_ip = "192.168.21.48";

    public static String EXCHANGE_CHINA_URL = "https://cn.iot.seeed.cc";
    //public static String EXCHANGE_INTERNATIONAL_URL = "https://iot.seeed.cc";

    public static String EXCHANGE_CHINA_IP = "120.25.216.117";
    //   public static String EXCHANGE_INTERNATIONAL_IP = "45.79.4.239";


    public static String Server_In_Prefix = "http://192.168.1.40/seeedcc-develop/api/index.php?";
    public static String Server_Out_Prefix = "http://bazaar.seeed.cc/api/index.php?";
    public static String Server_In_Ota_Url_Prefix = "https://192.168.4.99";
    public static String Server_In_Ota_Ip_Prefix = "192.168.4.99";
    public static String Server_Out_Ota_Url_Prefix = "https://wio.seeed.io";
    public static String Server_Out_Ota_Ip_Prefix = "192.168.21.48";

    public static String Hinge_User_Login = "r=common/user/login";
    public static String Hinge_User_Register = "r=common/user/register";
    public static String Hinge_Set_Token = "/v1/ext_users";


    public static SmartValue<String> Server_Prefix = new SmartValue<>(Server_Out_Prefix, Server_In_Prefix);
    public static String OTA_SERVER_URL = new SmartValue<String>(Server_Out_Ota_Url_Prefix, Server_In_Ota_Url_Prefix).getVal();
    public static String OTA_SERVER_IP = new SmartValue<String>(Server_Out_Ota_Ip_Prefix, Server_In_Ota_Ip_Prefix).getVal();


}
