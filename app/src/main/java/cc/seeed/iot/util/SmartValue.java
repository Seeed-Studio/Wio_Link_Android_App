package cc.seeed.iot.util;

import cc.seeed.iot.App;

/**
 * author: Jerry on 2016/5/4 17:31.
 * description:
 */
public class SmartValue<T> {
    private T debug;
    private T release;

    public SmartValue(T release, T debug) {
        this.debug = debug;
        this.release = release;
    }

    public T getVal() {
        int server = App.getApp().getSp().getInt(Constant.SERVER_SELECT, Constant.Server.In_Net.getValue());

        if (ToolUtil.isApkDebug()) {
            if (server == Constant.Server.In_Net.getValue()){
                return debug;
            }else {
                return release;
            }
        } else {
            return release;
        }
    }
}
