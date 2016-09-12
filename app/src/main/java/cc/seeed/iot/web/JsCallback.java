/**
 * Summary: 异步回调页面JS函数管理对象
 * Version 1.0
 * Date: 13-11-26
 * Time: 下午7:55
 * Copyright: Copyright (c) 2013
 */

package cc.seeed.iot.web;

import android.util.Log;
import android.webkit.WebView;

import java.lang.ref.WeakReference;

public class JsCallback {
    private static final String CALLBACK_JS_FORMAT = "javascript:%s.callback(%d, '%s');";
    private int mIndex;
    private WeakReference<WebView> mWebViewRef;
    private String mInjectedName;

    public JsCallback (WebView view, String injectedName, int index) {
        mWebViewRef = new WeakReference<WebView>(view);
        mInjectedName = injectedName;
        mIndex = index;
    }

    public void apply (String result){
        try {
            if (mWebViewRef.get() == null) {
                return;
            }
            String execJs = String.format(CALLBACK_JS_FORMAT, "JSBridge", mIndex, result);
            Log.d("JsCallBack", execJs);
            mWebViewRef.get().loadUrl(execJs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
