package cc.seeed.iot.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cc.seeed.iot.R;
import cc.seeed.iot.adapter.HelpAdapter;
import cc.seeed.iot.entity.FAQBean;

import br.tiagohm.markdownview.MarkdownView;
import br.tiagohm.markdownview.css.InternalStyleSheet;
import br.tiagohm.markdownview.css.styles.Github;

/**
 * author: Jerry on 2016/6/1 16:01.
 * description:
 */
public class PrivacyPolicyActivity extends BaseActivity {

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.mMdvPrivacy)
    MarkdownView mMdvPrivacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_privacy);
        ButterKnife.inject(this);

        initDate();
        initView();
    }

    private void initDate() {

    }

    private void initView() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.privacy_policy);

        mMdvPrivacy.addStyleSheet(new Github());
//        mMdvPrivacy.loadMarkdownFromAsset("privacy_policy.md");
//        mMdvPrivacy.loadMarkdownFromFile(new File());
        mMdvPrivacy.loadMarkdownFromUrl("http://iot.seeed.cc/privacy_policy.md");
    }

 /*   @Override
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
            //   DialogUtils.showShare(HelpActivity.this,"activity Share","Share",grove.GroveName);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
*/

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
