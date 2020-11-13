package cc.seeed.iot.web;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.umeng.analytics.MobclickAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;

/**
 * Created by zwc on 2015/7/6.
 */
public class WebActivity extends BaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    WebView wv;
    public static final String Param_Url = "param_url";
    public static final String Room_GenId = "room_gen_id";

    public static final String Param_Title = "param_title";
    public static final String ShowTitleBar = "show_titlebar";

    public static final String IsShowAnimation = "isShowAnimation";
    private static boolean isShowTitleBar = true;//默认显示titlebar

    String mTitle;
    String url;

    private SwipeRefreshLayout mSRLList;


    public static final String Param_CanShare = "param_canshare";
    boolean canShare = false;
    private String mRoomGenId;
    private Set<String> collectRoomIds;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getIntent().getBooleanExtra(IsShowAnimation, true)) {
            overridePendingTransition(0, 0);
        }
        setContentView(R.layout.activity_webview);
        wv = (WebView) findViewById(R.id.wv);
        mSRLList = (SwipeRefreshLayout) findViewById(R.id.mSRL);
        mSRLList.setOnRefreshListener(this);
        wv.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        WebSettings ws = wv.getSettings();
        ws.setDomStorageEnabled(true);
      //  ws.setUserAgentString("android.xl-"+ ChannelUtil.getChannel(this)+"-"+ ChannelUtil.getVersionName(this));
        ws.setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        wv.setWebChromeClient(new ChromeClient());
        wv.setWebViewClient(new WebClient());
        wv.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        url = getIntent().getStringExtra(Param_Url);
        if (TextUtils.isEmpty(url)) {
            url = "file:///android_asset/test.html";
        }
        mTitle = getIntent().getStringExtra(Param_Title);
        canShare = getIntent().getBooleanExtra(Param_CanShare, false);
        initShare(url, mTitle);
        setCookie(url);
        wv.loadUrl(url);

    }

    public void initShare(String url, String title) {

    }

    private void setCookie(String url) {
      /*  CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptFileSchemeCookies(true);
        HttpCookie cookie = new HttpCookie("token", UserLogic.getInstance().getToken());
        try {
            URL urlObj = new URL(url);
            URL newurl = new URL(urlObj.getProtocol(), urlObj.getHost(), urlObj.getPort(), "");
            cookieManager.setCookie(newurl.toString(), cookie.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onBackPressed() {
        if (wv.copyBackForwardList().getCurrentIndex() > 0) {
            wv.goBack();
        } else {
            super.onBackPressed(); // finishes activity
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
           /* case R.id.loading:
                inject = false;
                wv.reload();
                isloadSuccess = true;
                //wv.setVisibility(View.VISIBLE);
                break;*/
        }
    }

    @Override
    public void onRefresh() {
        inject = false;
        isloadSuccess = true;
        wv.reload();
    }

    public class ChromeClient extends InjectedChromeClient {

        public ChromeClient() {
            init(HostApp.class);
        }

        @Override
        protected void init(Class methodClass) {
            super.init(methodClass);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage(message);
            builder.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setCancelable(false);
            builder.create();
            builder.show();
            return false;
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            initShare(url, title);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress >= 30) {
                inject = false;
                if (!inject) {
                    injectJs(view);
                    inject = true;
                }
            }
        }
    }


    private static final String INJECTION_TOKEN = "inject_js/";
    boolean inject = false;
    private boolean isloadSuccess = true;

    public class WebClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

        }


        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (!inject) {
                injectJs(view);
                inject = true;
            }
            if (isloadSuccess) {
                mSRLList.setRefreshing(false);
                mSRLList.setVisibility(View.VISIBLE);
                wv.setVisibility(View.VISIBLE);
            } else {
                wv.setVisibility(View.GONE);
                mSRLList.setVisibility(View.GONE);
            }
        }


        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            isloadSuccess = false;
            wv.setVisibility(View.GONE);
            mSRLList.setVisibility(View.GONE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_DIAL,
                        Uri.parse(url));
                startActivity(intent);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            WebResourceResponse response = super.shouldInterceptRequest(view, url);
            if (url != null && url.contains(INJECTION_TOKEN)) {
                String assetPath = url.substring(url.indexOf(INJECTION_TOKEN) + INJECTION_TOKEN.length(), url.length());
                try {
                    response = new WebResourceResponse(
                            "application/javascript",
                            "UTF8",
                            view.getContext().getAssets().open(assetPath)
                    );
                } catch (IOException e) {
                    e.printStackTrace(); // Failed to load asset file
                }
            }
            return response;
        }

        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(WebActivity.this);
            builder.setMessage(R.string.notification_error_ssl_cert_invalid);
            builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.proceed();
                }
            });
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.cancel();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void loadJs(WebView view, String path) {
        try {
            InputStream ins = getAssets().open(path);
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(ins));
            String str;
            StringBuilder buf = new StringBuilder();
            buf.append("javascript:");
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            view.loadUrl(buf.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void injectJs(WebView view) {
        loadJs(view, "android.js");
        view.loadUrl("javascript:addJsFile('inject_js/bridge.js')");
        view.loadUrl("javascript:addJsFile('inject_js/HostApi.js')");
    }

    @Override
    public void onEvent(String event, boolean ret, String errInfom, Object[] data) {
        super.onEvent(event, ret, errInfom, data);
    }

    @Override
    public String[] monitorEvents() {
        return new String[]{};
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        wv.removeAllViews();
        wv.destroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
