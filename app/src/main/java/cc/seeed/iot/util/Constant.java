package cc.seeed.iot.util;

/**
 * Created by tenwong on 15/7/13.
 */
public class Constant {

    public final static String[] groveTypes = {
            "All",
            "Input",
            "Output",
            "GPIO",
            "ANALOG",
            "UART",
            "I2C",
            "EVENT",
    };

    public final static String WIO_LINK_V1_0 = "Wio Link v1.0";
    public final static String WIO_NODE_V1_0 = "Wio Node v1.0";

    //SharedPreferences
    public final static String SERVER_SELECT = "server_select";
    public final static String USER_INFO = "user_info";


    public enum Server{
        In_Net(1),
        Out_Net(2);
        private int value;
        Server(int value) {
        }
        public int getValue(){
            return value;
        }
    }
}
