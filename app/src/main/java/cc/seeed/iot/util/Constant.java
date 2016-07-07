package cc.seeed.iot.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tenwong on 15/7/13.
 */
public class Constant {

    public final static String[] groveTypes = {
            "All",
            "Input",
            "Output",
            "GPIO",
            "Analog",
            "UART",
            "I2C",
            "Event",
    };


    public enum GroveType {
        All("All"), INPUT("Input"), OUTPUT("Output"), GPIO("GPIO"),
        ANALOG("Analog"), UART("UART"), I2C("I2C"), EVENT("Event");
        private String value;

        GroveType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
        }

    public final static String WIO_LINK_V1_0 = "Wio Link v1.0";
    public final static String WIO_NODE_V1_0 = "Wio Node v1.0";

    //SharedPreferences
    public final static String SP_SERVER_SELECT = "sp_server_select";//保存服务器选择类型,内外 or 外网
    public final static String SP_USER_INFO = "sp_user_info";//保存用户信息
    public final static String SP_USER_TEST_INFO = "sp_user_test_info";//保存用户信息
    public final static String SP_SERVER_URL = "ota_server_url";
    public final static String SP_SERVER_IP = "ota_server_ip";
    public final static String SP_USER_EMAIL = "sp_user_email";
    public final static String SP_HISTORY_IP = "sp_history_ip";
    public final static String APP_FIRST_START = "app_first_start";//app first start
    public final static String APP_STOP_SERVER_MSG = "app_stop_server_msg";//app first start
    public final static String SP_APP_VERSION_REQ_TIME = "sp_app_version_req_time";
    public final static String SP_APP_VERSION_JSON = "sp_app_version_json";
    public final static String SP_APP_SERVER_REQ_TIME = "sp_app_server_req_time";
    public final static String SP_APP_SERVER_REMIND_AGAIN = "sp_app_server_remind_again";


    public enum Server {
        In_Net(1),
        Out_Net(2);
        private int value;

        Server(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum DialogButtonText {
        OK("OK"), CANCEL("CANCEL"), FAQ("FAQ"), TRY_AGAIN("TRY AGAIN");
        private String value;

        DialogButtonText(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static long SaveNewGroveTime = 60*60*24*30;//30天
}
