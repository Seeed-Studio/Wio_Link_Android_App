package cc.seeed.iot.web;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class JsCallJava {
    private final static String TAG = "JsCallJava";
    private HashMap<String, Method> mMethodsMap;
    private String mInjectedName;
    private Gson mGson;

    public JsCallJava (String injectedName, Class injectedCls) {
        mInjectedName = injectedName;
        mMethodsMap = new HashMap<String, Method>();
        try {
            if (TextUtils.isEmpty(injectedName)) {
                throw new Exception("injected name can not be null");
            }
            Method[] methods = injectedCls.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getModifiers() != (Modifier.PUBLIC | Modifier.STATIC)) {
                    continue;
                }
                mMethodsMap.put(method.getName(), method);
            }
        } catch(Exception e){
            Log.e(TAG, "init js error:" + e.getMessage());
        }
    }


    public void call(WebView webView, String jsonStr) {
        if (!TextUtils.isEmpty(jsonStr)) {
            try {
                JSONObject callJson = new JSONObject(jsonStr);
                String methodName = callJson.getString("method");
                JSONArray argsTypes = callJson.getJSONArray("types");
                JSONArray argsVals = callJson.getJSONArray("args");
                int len = argsTypes.length();
                Object[] values = new Object[len + 1];
                String currType;
                values[0] = webView;
                for (int k = 0; k < len; k++) {
                    currType = argsTypes.optString(k);
                    if ("function".equals(currType)) {
                        values[k + 1] = new JsCallback(webView, mInjectedName, argsVals.getInt(k));
                    } else {
                        values[k + 1] = argsVals.get(k);
                    }
                }
                Method currMethod = mMethodsMap.get(methodName);
                // 方法匹配失败
                if (currMethod != null) {
                    currMethod.invoke(null, values);
                } else {
                    Log.d("JsCallJava","no such method "+methodName);
                }
            } catch (Exception e) {
                //优先返回详细的错误信息
                e.printStackTrace();
            }
        }
    }
}