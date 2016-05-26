package cc.seeed.iot.logic;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.umeng.analytics.MobclickAgent;

import cc.seeed.iot.R;
import cc.seeed.iot.entity.UpdateApkBean;
import cc.seeed.iot.mgr.UiObserverManager;
import cc.seeed.iot.net.INetUiThreadCallBack;
import cc.seeed.iot.net.NetManager;
import cc.seeed.iot.net.Packet;
import cc.seeed.iot.net.Request;
import cc.seeed.iot.util.CommonUrl;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.SystemUtils;
import cc.seeed.iot.util.ToolUtil;

/**
 * Created by seeed on 2016/3/4.
 */
public class SystemLogic extends BaseLogic {
    private static SystemLogic logic;
    UpdateApkBean updateApkBean;

    public static SystemLogic getInstance() {
        if (logic == null) {
            logic = new SystemLogic();
        }
        return logic;
    }

    public void checkUpdateApk(final Context context) {
        final Dialog dialog = DialogUtils.showProgressDialog(context,context.getString(R.string.loading));
        RequestParams params = new RequestParams();
        params.put("type", 1);
        NetManager.getInstance().postRequest(CommonUrl.Hinge_Get_NewVersion, Cmd_App_Update, params, new INetUiThreadCallBack() {
            @Override
            public void onResp(Request req, Packet resp) {
                if (dialog != null){
                    dialog.dismiss();
                }
                Gson gson = new Gson();
                updateApkBean = gson.fromJson(resp.data, UpdateApkBean.class);
                checkUpdateApk(context, updateApkBean);
            }
        });
    }

    private void checkUpdateApk(final Context context, UpdateApkBean bean) {
        if (bean != null) {
            PackageInfo info = SystemUtils.getPackageInfo();
            if (TextUtils.isEmpty(bean.version_name) || info == null || TextUtils.isEmpty(info.versionName)) {
                return;
            }

            final String downUrl = bean.getUrl();


            if (isUpdate(info.versionName, bean.version_name)) {
                if (!bean.is_force) {//普通更新
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage( bean.version_message);
                    builder.setNeutralButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ToolUtil.downApk(context, downUrl);
                            MobclickAgent.onKillProcess(context);
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                } else {//强制更新
                   /* DialogUtils.showMsgInfoDialogOutsideCheck(context, bean.version_message, app.getResources().getString(R.string.sure), new DialogUtil.DismissClickListener() {
                        @Override
                        public void okClick() {
                            ToolUtil.downApk(context, downUrl);
                            MobclickAgent.onKillProcess(context);
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }

                        @Override
                        public void cancelClick() {

                        }
                    });*/
                }
            }else {
                UiObserverManager.getInstance().dispatchEvent(Cmd_App_Update, true, "", null);
            }
        }
    }

    public boolean isUpdate(String localVersionName, String serverVersionName) {
        boolean isNeedUpdate = false;
        String[] local = localVersionName.split("\\.");
        String[] server = serverVersionName.split("\\.");

        if (local.length == 3 && server.length == 3) {
            for (int i = 0; i < local.length; i++) {
                int localVersionCode = Integer.parseInt(local[i]);
                int serverVersionCode = Integer.parseInt(server[i]);
                if (localVersionCode < serverVersionCode) {
                    isNeedUpdate = true;
                    break;
                }
            }
        }

        return isNeedUpdate;
    }

}
