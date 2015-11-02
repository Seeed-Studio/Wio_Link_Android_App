package cc.seeed.iot.util;

import android.content.Context;

/**
 * Created by tenwong on 15/10/20.
 */
public class Util {
    public static boolean checkIsChina(Context context) {
        return context.getResources().getConfiguration().locale.getCountry().equals("CN");
    }

}
