package cc.seeed.iot.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.umeng.socialize.UMShareAPI;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.R;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.util.ImgUtil;
import cc.seeed.iot.util.ShareUtils;
import cc.seeed.iot.view.FontTextView;
import cc.seeed.iot.webapi.model.GroverDriver;

/**
 * author: Jerry on 2016/6/1 16:01.
 * description:
 */
public class GroveDetailActivity extends BaseActivity {
    public static String Intent_GroveSku = "Intent_GroveSku";

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.mGroveImg)
    SimpleDraweeView mGroveImg;
    @InjectView(R.id.mLlLinkWiki)
    LinearLayout mLlLinkWiki;
    @InjectView(R.id.mTvGroveName)
    FontTextView mTvGroveName;
    @InjectView(R.id.mTvGroveDesc)
    FontTextView mTvGroveDesc;

    private GroverDriver grove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_grove_detail);
        ButterKnife.inject(this);

        initDate();
        initView();
    }

    private void initDate() {
        Intent intent = getIntent();
        String groveSku = intent.getStringExtra(Intent_GroveSku);
        List<GroverDriver> groves = DBHelper.getGroves(groveSku);
        if (groves == null && groves.size() == 0) {
            finish();
            return;
        }
        grove = groves.get(0);

    }

    private void initView() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.grove_detail);

        ImgUtil.displayImg(mGroveImg, grove.ImageURL, R.mipmap.grove_default);
        mTvGroveName.setText(grove.GroveName.length() > 23 ? grove.GroveName.substring(0,23)+"...":grove.GroveName);
        mTvGroveDesc.setText(grove.Description);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.grove_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.share) {
        //    DialogUtils.showShare(GroveDetailActivity.this,"activity Share","Share",grove.GroveName);
            ShareUtils.show(GroveDetailActivity.this,"Wiki",grove.WikiURL,null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @OnClick(R.id.mLlLinkWiki)
    public void onClick() {
     /*   Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra(WebActivity.Intent_Url,"http://www.seeedstudio.com/wiki/Grove_-_Magnetic_Switch");
        startActivity(intent);*/
        Uri uri = Uri.parse(grove.WikiURL);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get( this ).onActivityResult( requestCode, resultCode, data);
    }
}
