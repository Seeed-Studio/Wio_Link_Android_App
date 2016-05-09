package cc.seeed.iot.util;

import android.text.TextUtils;
import android.util.Log;

/**
 * Logcat management class
 */

public class MLog {
    private MLog() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static boolean isShowLog = true;// 是否需要打印bug，可以在application的onCreate函数里面初始化
    private static String TAG = "MLog";

    /**
     * Configure whether to display the log
     *
     * @param isShow true:show log,false:don't show
     */
    public static void setIsShowLog(boolean isShow) {
        isShowLog = isShow;
    }

    /**
     * Configuring the default display tag
     *
     * @param tag
     */
    public static void setDefaultTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            TAG = tag;
        }
    }

    //----------------------使用默认标签显示-------------------------------------
    public static void i(String msg) {
        if (isShowLog)
            Log.i(TAG, msg);
    }

    public static void d(String msg) {
        if (isShowLog)
            Log.d(TAG, msg);
    }

    public static void e(String msg) {
        if (isShowLog)
            Log.e(TAG, msg);
    }

    public static void v(String msg) {
        if (isShowLog)
            Log.v(TAG, msg);
    }

    //------------------------通过传入一个类,获取这个类的简单名字来作为标签----------------------------------------------------
    public static void i(Object tag, String msg) {
        if (isShowLog)
            Log.i(tag.getClass().getSimpleName(), msg);
    }

    public static void d(Object tag, String msg) {
        if (isShowLog)
            Log.i(tag.getClass().getSimpleName(), msg);
    }

    public static void e(Object tag, String msg) {
        if (isShowLog)
            Log.i(tag.getClass().getSimpleName(), msg);
    }

    public static void v(Object tag, String msg) {
        if (isShowLog)
            Log.i(tag.getClass().getSimpleName(), msg);
    }

    //------------------------自定义标签---------------------------------------------------
    public static void i(String tag, String msg) {
        if (isShowLog)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (isShowLog)
            Log.i(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (isShowLog)
            Log.i(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (isShowLog)
            Log.i(tag, msg);
    }
}