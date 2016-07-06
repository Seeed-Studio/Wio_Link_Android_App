package cc.seeed.iot.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.seeed.iot.App;
import cc.seeed.iot.webapi.model.Node;

/**
 * Created by seeed on 2016/2/25.
 */
public class ToolUtil {
    /**
     * 开启电话
     *
     * @param activity
     * @param phone
     */
    public static void startPhone(Activity activity, String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + phone);
        intent.setData(data);
        activity.startActivity(intent);
    }

    public static int getViewHeight(View mBottomBar2) {
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        mBottomBar2.measure(w, h);
        int height = mBottomBar2.getMeasuredHeight();
        int width = mBottomBar2.getMeasuredWidth();

        return height;
    }

    /**
     * 判断apk是不是debug版本
     *
     * @return
     */
    public static boolean isApkDebug() {
        try {
            ApplicationInfo info = App.getApp().getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
        }
        return false;
    }

    public static void downApk(Context context, String url) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse(Html.fromHtml(url).toString());
        intent.setData(data);
        intent.setPackage("com.google.android.browser");
        intent.addCategory("android.intent.category.BROWSABLE");
        intent.setComponent(new ComponentName("com.android.browser",
                "com.android.browser.BrowserActivity"));
        context.startActivity(intent);
    }

    /**
     * 保存文件
     *
     * @param bm
     * @param fileName
     * @throws IOException
     */
    public static boolean saveFile(Bitmap bm, String pathName, String fileName) {
        boolean isSaveSuccess = false;
        String path = getSDPath() + pathName;
        try {
            File dirFile = new File(path);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            File myCaptureFile = new File(path + fileName);
            if (!myCaptureFile.exists()) {
                myCaptureFile.createNewFile();
            } else {
                isSaveSuccess = true;
                return isSaveSuccess;
            }
            FileOutputStream fileOutputStream = new FileOutputStream(myCaptureFile);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            bos.flush();
            bos.close();
            isSaveSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSaveSuccess;
    }

    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();

    }

    /**
     * 获取文件名字,包括后缀
     *
     * @return
     */
    public static String getFileName(String pathName) {
        String fileName = null;
        if (!TextUtils.isEmpty(pathName)) {
            String[] strings = pathName.split("/");
            if (strings.length > 0) {
                fileName = strings[strings.length - 1];
            }
        }
        return fileName;
    }
    public static final int dp2px(float dp, Resources res) {
        return (int) (dp * res.getDisplayMetrics().density + 0.5f);
    }

    /**
     * 检查当前网络是否可用
     *
     * @return
     */
    public static boolean isNetworkAvailable() {
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) App.getApp().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        } else {
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++) {
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * 判断当前应用程序处于最上层
     */
    public static boolean isTopActivity(final Context context,String className) {
        boolean isTop = false;
        ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        if (cn.getClassName().contains(className)) {
            isTop = true;
        }
        return isTop;
    }

    /**
     * 判断是否安装目标应用
     *
     * @param packageName 目标应用安装后的包名
     * @return 是否已安装目标应用
     */
    public static boolean isInstallByread(String packageName) {
        try {
            ApplicationInfo info = App.getApp().getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String getSimpleName(String name){
        String groveName = "";
        if (name.startsWith("Grove-")){
            groveName = name.split("Grove-")[1];
        }else if (name.startsWith("Grove - ")){
            groveName = name.split("Grove - ")[1];
        }else if (name.startsWith("Grove_")){
            groveName = name.split("Grove_")[1];
        }else {
            groveName = name;
        }

        return groveName;
    }


    public static String getApiUrl(Node node) {
        String url;
        String ota_server_url = App.getApp().getOtaServerUrl();
        String server_endpoint = ota_server_url + "/v1/node/resources?";
        server_endpoint = server_endpoint.replace("https", "http");
        String node_key = node.node_key;
        String dataxserver = node.dataxserver;
        if (dataxserver == null)
            dataxserver = ota_server_url;
//        if (dataxserver.equals(CommonUrl.OTA_SERVER_URL) || dataxserver.equals(CommonUrl.OTA_INTERNATIONAL_URL))
//            url = server_endpoint + "access_token=" + node_key;
//        else
        url = server_endpoint + "access_token=" + node_key + "&data_server=" + dataxserver;
        Log.i("iot", "Url:" + url);

        return url;
    }

}
