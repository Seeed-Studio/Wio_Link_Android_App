package cc.seeed.iot.activity;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
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

/**
 * author: Jerry on 2016/6/1 16:01.
 * description:
 */
public class HelpActivity extends BaseActivity {

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.mLvSupport)
    RecyclerView mLvSupport;

    List<FAQBean> beanList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help);
        ButterKnife.inject(this);

        initDate();
        initView();
    }

    private void initDate() {
        int url = R.mipmap.help01_breathing;
        String ques = "Setup mode";
        String answer = "When blue LED is breathing, your Wio device is under Setup mode and ready to be connected. \r\n" +
                "To set your WIO device to Setup mode, please press and hold Config button for about 4 second for WIO Link, Function button for WIO Node.";
        FAQBean bean1 = new FAQBean(url, ques, answer);

        url = R.mipmap.help02_blink_2;
        ques = "Requesting IP address from router";
        answer = "When blue LED blinks twice then quickly off for 1 second, your Wio device is requesting IP address from router. \r\n" +
                "It might take up to a minute.";
        FAQBean bean2 = new FAQBean(url, ques, answer);

        url = R.mipmap.help03_blink_1_quick;
        ques = "Connecting to server";
        answer = "When blue LED blinks once then quickly off for 1 second, your Wio device is connecting to server. It might take up to a minute.";
        FAQBean bean3 = new FAQBean(url, ques, answer);

        url = R.mipmap.help04_no_blink;
        ques = "Not getting IP / fail connecting to server";
        answer = "When blue LED is constantly on, indicates your Wio device is not getting IP / fail connecting to server. \r\n" +
                "Please check your internet connection and try again from the start.";
        FAQBean bean4 = new FAQBean(url, ques, answer);

        url = R.mipmap.help05_blink_01s;
        ques = "OTA";
        answer = "When blue LED is blinking constantly, your Wio device is updating firware (OTA).\r\n" +
                "It might take up to a minute.";
        FAQBean bean5 = new FAQBean(url, ques, answer);

        url = R.mipmap.help06_blink_1s;
        ques = "Device online";
        answer = "When blue LED is on for 1 second then off for another, your Wio device is online.";
        FAQBean bean6 = new FAQBean(url, ques, answer);

        beanList.add(bean1);
        beanList.add(bean2);
        beanList.add(bean3);
        beanList.add(bean4);
        beanList.add(bean5);
        beanList.add(bean6);

        HelpAdapter adapter = new HelpAdapter( beanList);
        View headerView = LayoutInflater.from(this).inflate(R.layout.help_header_layout, null);
        View footerView = LayoutInflater.from(this).inflate(R.layout.help_footer_layout, null);

        if (mLvSupport != null) {
            mLvSupport.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mLvSupport.setLayoutManager(layoutManager);
            mLvSupport.setAdapter(adapter);
        }
    }

    private void initView() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.help_toolbar);
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
