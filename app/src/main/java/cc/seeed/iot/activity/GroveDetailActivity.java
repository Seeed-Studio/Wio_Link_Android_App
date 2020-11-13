package cc.seeed.iot.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.CallbackManager;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cc.seeed.iot.R;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.util.ImgUtil;
import cc.seeed.iot.util.ShareUtils;
import cc.seeed.iot.util.TimeUtil;
import cc.seeed.iot.util.ToolUtil;
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
    @InjectView(R.id.rlHack)
    RelativeLayout rlHack;
    @InjectView(R.id.mLlHack)
    LinearLayout mLlHack;
    @InjectView(R.id.mTvGroveName)
    FontTextView mTvGroveName;
    @InjectView(R.id.mTvGroveDesc)
    FontTextView mTvGroveDesc;
    @InjectView(R.id.mIvNewGrove)
    ImageView mIvNewGrove;
    @InjectView(R.id.mTvAddDate)
    FontTextView mTvAddDate;
    @InjectView(R.id.mTvContributer)
    FontTextView mTvContributer;
    @InjectView(R.id.mTvGuide)
    FontTextView mTvGuide;
    @InjectView(R.id.mLlContributer)
    LinearLayout mLlContributer;

    private GroverDriver grove;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_grove_detail);
        ButterKnife.inject(this);
        callbackManager = CallbackManager.Factory.create();

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
        mTvGroveName.setText(grove.GroveName.length() > 23 ? grove.GroveName.substring(0, 23) + "..." : grove.GroveName);
        mTvGroveDesc.setText(grove.Description);
        if (TextUtils.isEmpty(grove.WikiURL)) {
            mLlLinkWiki.setVisibility(View.GONE);
        } else {
            mLlLinkWiki.setVisibility(View.VISIBLE);
        }

        if (grove.NeedHack) {
            rlHack.setVisibility(View.VISIBLE);
        } else {
            rlHack.setVisibility(View.GONE);
        }

        mIvNewGrove.setVisibility(ToolUtil.isNewGrove(grove.AddedAt)?View.VISIBLE:View.GONE);
        if (TextUtils.isEmpty(grove.Author)){
            mLlContributer.setVisibility(View.GONE);
        }else {
            mLlContributer.setVisibility(View.VISIBLE);
            mTvContributer.setText(grove.Author);
        }

        if (TextUtils.isEmpty(grove.AddedAt)){
            mTvAddDate.setVisibility(View.GONE);
        }else {
            mTvAddDate.setVisibility(View.VISIBLE);
            try {
                long addTime = Long.parseLong(grove.AddedAt);
                mTvAddDate.setText(String.format("%s%s", getString(R.string.add_on), TimeUtil.long2String(addTime, "dd/MM/yyyy")));
            }catch (Exception e){
                mTvAddDate.setVisibility(View.GONE);
            }
        }

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
            ShareUtils.show(GroveDetailActivity.this, "Wiki", grove.WikiURL, null);
            ShareUtils.setIsWiki(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @OnClick(R.id.mLlLinkWiki)
    public void onClick() {
      /*  Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra(WebActivity.Param_Url,grove.WikiURL + "?utm_source=Wio&utm_medium=Android&utm_campaign=WIKI");
        startActivity(intent);*/
        Uri uri = Uri.parse(grove.WikiURL + "?utm_source=Wio&utm_medium=Android&utm_campaign=WIKI");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @OnClick(R.id.mLlHack)
    public void hackLink() {
        Uri uri = Uri.parse(grove.HackGuideURL + "?utm_source=Wio&utm_medium=Android&utm_campaign=GUIDE");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
       /* if (UMShareAPI.get(this) != null)
            UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);*/
    }
}
