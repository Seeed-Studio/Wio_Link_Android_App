package cc.seeed.iot.util;

import android.os.Environment;

import cc.seeed.iot.App;

/**
 * Created by tenwong on 15/10/20.
 */
public class Common {


        public static final String WioLink_Source = "4";
        public static final String WioLink_AppId = "wiolink";
        public static final String WioLink_AppKey = "MPP=tGjz</p5";
        public static final String WioLink_Common = "seeed_wiolink";//加密，解密使用字段

        public static String API_GET_TOKEN_KEY = "seeed_wiolink_return_token_2016#0509";
        public static String API_CHECK_TOKEN_KEY= "seeed_wiolink_check_token_2016#0509";

        public static int PlatformWithWeChat = 1;
        public static int PlatformWithFaceBook = 2;

        public static String AppRootPath = Environment.getExternalStorageDirectory().getPath() + "/seeed/"+ App.getApp().getPackageName()+"/";
        public static String ImgPath = Environment.getExternalStorageDirectory().getPath()+ "/seeed/wiolink/img/";

        public enum NodeOrder {
                VERSION("VERSION"), APCFG("APCFG"), SCAN("SCAN"), REBOOT("REBOOT");
                private String value;

                NodeOrder(String value) {
                        this.value = value;
                }

                public String getValue() {
                        return value;
                }
        }

}
