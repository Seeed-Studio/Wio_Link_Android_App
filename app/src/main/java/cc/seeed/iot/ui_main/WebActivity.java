package cc.seeed.iot.ui_main;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.CallbackManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cc.seeed.iot.R;
import cc.seeed.iot.activity.BaseActivity;
import cc.seeed.iot.util.MLog;
import cc.seeed.iot.util.OtherPlatformUtils;
import cc.seeed.iot.util.ShareUtils;
import cc.seeed.iot.webapi.model.Node;

public class WebActivity extends BaseActivity {
    private final static String TAG = "WebActivity";
    private final static String RESOURCE = "/v1/node/resources?";
    public final static String Intent_Url = "Intent_Url";
    private Toolbar mToolbar;
    private SwipeRefreshLayout mSRL;

    private ProgressBar mProgressBar;
    private WebView mWebView;
    private Node node;
    private String url;

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api);

        callbackManager = CallbackManager.Factory.create();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mSRL = (SwipeRefreshLayout) findViewById(R.id.mSRL);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("API");

        mWebView = (WebView) findViewById(R.id.api);
        mProgressBar = (ProgressBar) findViewById(R.id.pb);


        init();
        initView();
        mSRL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.loadUrl(url);
            }
        });
    }

    boolean isReqing = false;
    private void initView() {
        mWebView.loadUrl(url);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                mProgressBar.setProgress(progress);
                if (progress < 100) {
                    mProgressBar.setVisibility(View.VISIBLE);
                } else {
                    mProgressBar.setVisibility(View.GONE);
                    mSRL.setRefreshing(false);
                    injectJs(view);
                }
                if (mWebView != null && !TextUtils.isEmpty(mWebView.getUrl())){
                    String[] split = mWebView.getUrl().split("code=");
                    if (split.length > 1 && !isReqing) {
                        isReqing = true;
                        MLog.e("code= " + split[1]);
                        OtherPlatformUtils.getGithubToken(split[1]);
                    }
                }
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(WebActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                String[] split = url.split("code=");
                if (split.length > 1){
                 //   App.showToastShrot("code= "+split[1]);
                }
            }

        });

        mWebView.setWebViewClient(new WebViewClient() {
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

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectJs(view);
            }
        });

     //   String url = getApiUrl();
    }


    private void init() {
     //   String node_sn = getIntent().getStringExtra("node_sn");
    //    node = DBHelper.getNodes(node_sn).get(0);
        url = getIntent().getStringExtra(Intent_Url);
        if (TextUtils.isEmpty(url)){
            finish();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.api_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.share) {
           // shareTextUrl(getApiUrl());
            ShareUtils.show(this,"API",url,null);
            ShareUtils.setIsWiki(false);
        }
        return super.onOptionsItemSelected(item);
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

    private void copyTextUrl(String apiUrl) {
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("api url", apiUrl);
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void shareTextUrl(String url) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, url);
        i.setType("text/plain");
        Intent sendIntent = Intent.createChooser(i, getString(R.string.share_api_to));
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(sendIntent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
