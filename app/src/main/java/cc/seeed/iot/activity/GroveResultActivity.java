package cc.seeed.iot.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cc.seeed.iot.R;
import cc.seeed.iot.net.INetUiThreadCallBack;
import cc.seeed.iot.net.NetManager;
import cc.seeed.iot.net.Packet;
import cc.seeed.iot.net.Request;

/**
 * author: Jerry on 2016/6/16 10:06.
 * description:
 */
public class GroveResultActivity extends BaseActivity {

    @InjectView(R.id.mTvTemp)
    TextView mTvTemp;
    @InjectView(R.id.mTvHumidity)
    TextView mTvHumidity;
    @InjectView(R.id.mSRL)
    SwipeRefreshLayout mSRL;

    int okRuquest = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grove_result);
        ButterKnife.inject(this);

        mSRL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        mSRL.setRefreshing(true);
        loadData();
    }

    private void loadData() {
        String url = "https://192.168.4.110/v1/node/GroveTempHumD1/humidity?access_token=5934e6d664c1ec3672c7848844040ae1";
        NetManager.getInstance().get(url, "", new INetUiThreadCallBack() {
            @Override
            public void onResp(Request req, Packet resp) {
                setOkRuquest();
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(resp.data);
                    String humidity = jsonObject.getString("humidity");
                    mTvHumidity.setText(humidity + "%");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        url = "https://192.168.4.110/v1/node/GroveTempHumD1/temperature?access_token=5934e6d664c1ec3672c7848844040ae1";
        NetManager.getInstance().get(url, "", new INetUiThreadCallBack() {
            @Override
            public void onResp(Request req, Packet resp) {
                setOkRuquest();
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(resp.data);
                    String temp = jsonObject.getString("celsius_degree");
                    mTvTemp.setText(temp + "℃");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setOkRuquest(){
        okRuquest ++;
        if (okRuquest >= 2){
            okRuquest = 0;
            mSRL.setRefreshing(false);
        }
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
