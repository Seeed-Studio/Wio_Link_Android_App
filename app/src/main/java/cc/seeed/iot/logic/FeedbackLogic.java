package cc.seeed.iot.logic;

import android.content.pm.PackageInfo;
import android.os.Build;

import com.loopj.android.http.RequestParams;

import cc.seeed.iot.mgr.UiObserverManager;
import cc.seeed.iot.net.INetUiThreadCallBack;
import cc.seeed.iot.net.NetManager;
import cc.seeed.iot.net.Packet;
import cc.seeed.iot.net.Request;
import cc.seeed.iot.util.CommonUrl;
import cc.seeed.iot.util.SystemUtils;

/**
 * Created by seeed on 2016/3/4.
 */
public class FeedbackLogic extends BaseLogic {
    private static FeedbackLogic logic;

    public static FeedbackLogic getInstance() {
        if (logic == null) {
            logic = new FeedbackLogic();
        }
        return logic;
    }

    public void submitFeedback(String email,String desc,int type){
        PackageInfo packageInfo = SystemUtils.getPackageInfo();
        String versionName = packageInfo.versionName;
        RequestParams params = new RequestParams();
        params.put("device_id", Build.MODEL);
        params.put("version_name",versionName);
        params.put("phone_version",Build.VERSION.RELEASE);
        params.put("phone_name","Android");
        params.put("feedback_text",desc);
        params.put("user_id","");
        params.put("feedback_email",email);
        params.put("feedback_type",type);

        NetManager.getInstance().postRequest(CommonUrl.Submmit_Feedback_Url.getVal(), Cmd_Feedback, params, new INetUiThreadCallBack() {
            @Override
            public void onResp(Request req, Packet resp) {
                UiObserverManager.getInstance().dispatchEvent(req.cmd, "true".equals(resp.data), resp.errorMsg, null);
            }
        });
    }

}
