package cc.seeed.iot.logic;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.entity.ServerBean;
import cc.seeed.iot.entity.UpdateApkBean;
import cc.seeed.iot.mgr.UiObserverManager;
import cc.seeed.iot.net.INetUiThreadCallBack;
import cc.seeed.iot.net.NetManager;
import cc.seeed.iot.net.Packet;
import cc.seeed.iot.net.Request;
import cc.seeed.iot.util.CommonUrl;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.util.MLog;
import cc.seeed.iot.util.SystemUtils;
import cc.seeed.iot.util.ToolUtil;

/**
 * Created by seeed on 2016/3/4.
 */
public class SystemLogic extends BaseLogic {
    private static SystemLogic logic;
    UpdateApkBean updateApkBean;
    ServerBean serverBean;

    public static SystemLogic getInstance() {
        if (logic == null) {
            logic = new SystemLogic();
        }
        return logic;
    }

    public UpdateApkBean getUpdateApkBean() {
        if (updateApkBean == null) {
            String jsonStr = App.getSp().getString(Constant.SP_APP_VERSION_JSON, "");
            Gson gson = new Gson();
            updateApkBean = gson.fromJson(jsonStr, UpdateApkBean.class);
        }
        return updateApkBean;
    }

    public void checkUpdateApk(final Context context, final boolean isNotice) {
        //   String url = "http://192.168.3.65/seeed/api/index.php?r=makermap/version/get-new-version-message";
        final Dialog dialog = DialogUtils.showProgressDialog(context, context.getString(R.string.msg_loading));
        RequestParams params = new RequestParams();
        params.put("type", 1);
        params.put("app_name", "wio");
        NetManager.getInstance().postRequest(CommonUrl.Hinge_Get_NewVersion, Cmd_App_Update, params, new INetUiThreadCallBack() {
            @Override
            public void onResp(Request req, Packet resp) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                Gson gson = new Gson();
                updateApkBean = gson.fromJson(resp.data, UpdateApkBean.class);
                App.getSp().edit().putString(Constant.SP_APP_VERSION_JSON, resp.data).commit();
                checkUpdateApk(context, updateApkBean, isNotice);
            }
        });
    }

    private void checkUpdateApk(final Context context, final UpdateApkBean bean, final boolean isNotice) {
        if (bean != null) {
            PackageInfo info = SystemUtils.getPackageInfo();
            if (TextUtils.isEmpty(bean.version_name) || info == null || TextUtils.isEmpty(info.versionName)) {
                return;
            }

            final String downUrl = bean.getUrl();

            if (!isNotice) {
                String notUpdateVersion = App.getSp().getString(bean.version_name, "");
                if (notUpdateVersion.equals(bean.version_name)) {
                    return;
                }
            }

            if (isUpdate(info.versionName, bean.version_name)) {
                if (!bean.is_force) {//普通更新
                   /* AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    if (!TextUtils.isEmpty(bean.getVersion_title())) {
                        builder.setTitle(bean.version_title);
                    }
                    builder.setMessage(bean.version_message);
                    builder.setNeutralButton("Update now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ToolUtil.downApk(context, downUrl);
                            //   MobclickAgent.onKillProcess(context);
                            //  android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    });
                    builder.setNegativeButton(isNotice ? "Cancel" : "Skip for now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!isNotice)
                                App.getSp().edit().putString(bean.version_name, bean.version_name).commit();
                        }
                    });
                    builder.show();*/
                    String content = TextUtils.isEmpty(bean.version_title) ? bean.version_message : bean.version_title + "\r\n" + bean.version_message;
                    DialogUtils.showWarningDialog(context, null,content, "Update now", isNotice ? "Cancel" : "Skip for now",false, new DialogUtils.OnErrorButtonClickListenter() {
                        @Override
                        public void okClick() {
                            ToolUtil.downApk(context, downUrl);
                        }

                        @Override
                        public void cancelClick() {
                            if (!isNotice)
                                App.getSp().edit().putString(bean.version_name, bean.version_name).commit();
                        }
                    });
                } else {//强制更新
                  /*  AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    if (!TextUtils.isEmpty(bean.getVersion_title())) {
                        builder.setTitle(bean.version_title);
                    }
                    builder.setMessage(bean.version_message);
                    builder.setNeutralButton("Update now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ToolUtil.downApk(context, downUrl);
                            MobclickAgent.onKillProcess(context);
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();*/
                    String content = TextUtils.isEmpty(bean.version_title) ? bean.version_message : bean.version_title + "\r\n" + bean.version_message;
                    Dialog dialog = DialogUtils.showWarningDialog(context,null, content, "Update now", null,false, new DialogUtils.OnErrorButtonClickListenter() {
                        @Override
                        public void okClick() {
                            ToolUtil.downApk(context, downUrl);
                            MobclickAgent.onKillProcess(context);
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(0);
                        }

                        @Override
                        public void cancelClick() {
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                }
            } else {
                if (isNotice) {
                    UiObserverManager.getInstance().dispatchEvent(Cmd_App_Update, false, "", null);
                    App.showToastLong("App is new version");
                }
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
                } else if (localVersionCode > serverVersionCode) {
                    isNeedUpdate = false;
                    break;
                }
            }
        }

        return isNeedUpdate;
    }

    public void getServerStopMsg() {
        //  String url = "http://192.168.3.65/seeed/api/index.php?r=wiolink/version/choose-server";
        NetManager.getInstance().postRequest(CommonUrl.Hinge_StopServer, Cmd_Stop_server, null, new INetUiThreadCallBack() {
                    @Override
                    public void onResp(Request req, Packet resp) {
                        if (resp.status) {
                            try {
                                Gson gson = new Gson();
                                ServerBean bean = gson.fromJson(resp.data, ServerBean.class);
                                if (bean != null) {
                                    bean.setReqTime(System.currentTimeMillis() / 1000);
                                    List<String> boldText = bean.getContent().get(0).getBoldText();
                                    ServerBean.ContentBean contentBean = bean.getContent().get(0);
                                    if (boldText != null && boldText.size() > 0) {
                                        for (int i = 0; i < boldText.size(); i++) {
                                            String bold = boldText.get(i);
                                            if (!TextUtils.isEmpty(bold)) {
                                                contentBean.setPopText(contentBean.getPopText().replaceAll(bold, "<b><font color=\"#484848\">" + bold + "</font></b>"));
                                            }
                                        }
                                        contentBean.setPopText(contentBean.getPopText().replaceAll("[\\r\\n]+", "<br><br>"));
                                        contentBean.setPopText("<html>" +
                                                "<head>" +
                                                "" +
                                                "</head>" +
                                                "<body>" +
                                                "<p>" + contentBean.getPopText() +
                                                "</p>" +
                                                "</body>" +
                                                "</html>");
                                        bean.getContent().set(0, contentBean);
                                        serverBean = bean;
                                        String s = gson.toJson(bean);
                                        App.getSp().edit().putString(Constant.APP_STOP_SERVER_MSG, s).commit();
                                    } else {
                                        serverBean = bean;
                                        App.getSp().edit().putString(Constant.APP_STOP_SERVER_MSG, resp.data).commit();
                                    }
                                }

                            } catch (Exception e) {
                                MLog.e(e.getMessage());
                            }
                        }
                        UiObserverManager.getInstance().dispatchEvent(req.cmd, resp.status, "", null);
                    }
                }
        );
    }

    public ServerBean getServerBean() {
        if (serverBean == null) {
            String msg = App.getSp().getString(Constant.APP_STOP_SERVER_MSG, "");
            Gson gson = new Gson();
            ServerBean bean = gson.fromJson(msg, ServerBean.class);
            if (bean != null) {
                serverBean = bean;
            } else {
                serverBean = new ServerBean();
                serverBean.setReqTime(0);
                serverBean.setMaxVerstamp(1472659199);
                ServerBean.ContentBean contentBean = new ServerBean.ContentBean();
//                contentBean.setPopText("<![CDATA[This option is for users who registered with our old Global Server.<br><br> Because we are making changes our server architecture, our old Global Server will be terminated on <b><font color=\"#484848\">September 1, 2016</font></b>. At that time, all your data will be transferred automatically to new Global Server.<br><br> We highly recommend that you use our new Global Server for your new projects.]]>");
                contentBean.setPopText("<html>" +
                        "<head>" +
                        "" +
                        "</head>" +
                        "<body>" +
                        "<p>" +
                        "This option is for users who registered with our old Global Server.<br><br> Because we are making changes our server architecture, our old Global Server will be terminated on <b><font color=\"#484848\">September 1, 2016</font></b>. At that time, all your data will be transferred automatically to new Global Server.<br><br> We highly recommend that you use our new Global Server for your new projects." +
                        "</p>" +
                        "" +
                        "</body>" +
                        "</html>");
                contentBean.setPopStartTime(1471622400);
                contentBean.setPopEndTime(1472659199);
                contentBean.setServerEndTime(1472659199);
                List<ServerBean.ContentBean> list = new ArrayList<>();
                list.add(contentBean);
                serverBean.setContent(list);
            }
        }
        return serverBean;
    }
}
