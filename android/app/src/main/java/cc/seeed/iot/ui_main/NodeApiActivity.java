package cc.seeed.iot.ui_main;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import cc.seeed.iot.MyApplication;
import cc.seeed.iot.R;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.webapi.model.Node;

public class NodeApiActivity extends AppCompatActivity {
    final static String TAG = "NodeApiActivity";
    private Toolbar mToolbar;

    private ProgressBar mProgressBar;
    private WebView mWebView;
    private Node node;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("API");

        mWebView = (WebView) findViewById(R.id.api);
        mProgressBar = (ProgressBar) findViewById(R.id.pb);


        init();
        initView();

    }

    private void initView() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                mProgressBar.setProgress(progress);
                if (progress < 100) {
                    mProgressBar.setVisibility(View.VISIBLE);
                } else {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(NodeApiActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });

        String url = getApiUrl();
        mWebView.loadUrl(url);
    }


    private void init() {
        String node_sn = getIntent().getStringExtra("node_sn");
        node = DBHelper.getNodes(node_sn).get(0);
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
        } else if (id == R.id.copy) {
            copyTextUrl(getApiUrl());
            Toast.makeText(this, "API coped!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.share) {
            shareTextUrl(getApiUrl());
        }
        return super.onOptionsItemSelected(item);
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

    private String getApiUrl() {
        String server_url = ((MyApplication) NodeApiActivity.this.getApplication()).getExchangeServerUrl();
        String server_endpoint = server_url + "/node/resources?";
        String node_key = node.node_key;
        String url = server_endpoint + "access_token=" + node_key;
        Log.i("iot", "Url:" + url);

        return url;
    }
}
