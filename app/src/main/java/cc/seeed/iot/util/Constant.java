package cc.seeed.iot.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tenwong on 15/7/13.
 */
public class Constant {

    public final static String[] groveTypes = {
            "All",
            "INPUT",
            "OUTPUT",
            "GPIO",
            "ANALOG",
            "UART",
            "I2C",
            "EVENT",
    };


    public enum GroveType {
        All("All"), INPUT("INPUT"), OUTPUT("OUTPUT"), GPIO("GPIO"),
        ANALOG("ANALOG"), UART("UART"), I2C("I2C"), EVENT("EVENT");
        private String value;

        GroveType(String value) {
        }

        public String getValue() {
            return value;
        }
        }

    public final static String WIO_LINK_V1_0 = "Wio Link v1.0";
    public final static String WIO_NODE_V1_0 = "Wio Node v1.0";

    //SharedPreferences
    public final static String SERVER_SELECT = "server_select";//保存服务器选择类型,内外 or 外网
    public final static String USER_INFO = "user_info";//保存用户信息


    public enum Server {
        In_Net(1),
        Out_Net(2);
        private int value;

        Server(int value) {
        }

        public int getValue() {
            return value;
        }
    }
}
