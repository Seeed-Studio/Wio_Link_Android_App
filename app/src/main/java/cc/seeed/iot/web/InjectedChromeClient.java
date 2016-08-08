/**
 * Summary: 应用中使用的WebChromeClient基类
 * Version 1.0
 * Date: 13-11-8
 * Time: 下午2:31
 * Copyright: Copyright (c) 2013
 */

package cc.seeed.iot.web;

import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;


public class InjectedChromeClient extends WebChromeClient {
    private final String TAG = "InjectedChromeClient";
    private JsCallJava mJsCallJava;

//    public InjectedChromeClient () {
//        mJsCallJava = new JsCallJava("HostApp", HostApp.class);
//    }

    public InjectedChromeClient () {
    }

    protected void init(Class cl)
    {
        mJsCallJava = new JsCallJava(cl.getSimpleName(),cl);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        if (defaultValue.startsWith("call:")) {
            mJsCallJava.call(view, message);
            result.confirm("");
            return true;
        }
        return super.onJsPrompt(view,url,message,defaultValue,result);
    }
}
