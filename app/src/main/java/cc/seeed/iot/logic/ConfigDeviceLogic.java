package cc.seeed.iot.logic;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.List;

import cc.seeed.iot.R;
import cc.seeed.iot.activity.SetupIotLinkActivity;
import cc.seeed.iot.entity.DialogBean;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.mgr.UiObserverManager;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.util.DialogUtils;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.GroveDriverListResponse;
import cc.seeed.iot.webapi.model.GroverDriver;
import cc.seeed.iot.webapi.model.Node;
import cc.seeed.iot.webapi.model.NodeJson;
import cc.seeed.iot.webapi.model.OtaStatusResponse;
import cc.seeed.iot.webapi.model.SuccessResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * author: Jerry on 2016/5/20 17:18.
 * description:
 */
public class ConfigDeviceLogic extends BaseLogic {
    public static  final int SUCCESS = 0;
    public static  final int FAIL = -1;
    public static  final int UPDATE_DONE = 1;
    public static  final int FIRWARE_DOWNLOAD_DONE = 2;
    private static ConfigDeviceLogic sIns;

    public static ConfigDeviceLogic getInstance() {
        if (sIns == null) {
            sIns = new ConfigDeviceLogic();
        }
        return sIns;
    }

    public void updateStute(final String node_key) {
        IotApi api = new IotApi();
        api.setAccessToken(node_key);
        final IotService iot = api.getService();
        iot.otaStatus(new Callback<OtaStatusResponse>() {
                          @Override
                          public void success(OtaStatusResponse otaStatusResponse, Response response) {
                              switch (otaStatusResponse.ota_status) {
                                  case "going":
                                      updateStute(node_key);
                                      break;
                                  case "done":
                                      UiObserverManager.getInstance().dispatchEvent(Cmd_UpdateFirwareStute, UPDATE_DONE, "error.getLocalizedMessage()", new Object[]{otaStatusResponse});
                                      break;
                                  case "error":
                                  DialogBean bean = new DialogBean("Connection Error", Constant.DialogButtonText.TRY_AGAIN.getValue(), Constant.DialogButtonText.CANCEL.getValue(), otaStatusResponse.ota_msg);
                                  UiObserverManager.getInstance().dispatchEvent(Cmd_UpdateFirwareStute, FAIL, "error.getLocalizedMessage()", new DialogBean[]{bean});
                              }
                          }

                          @Override
                          public void failure(RetrofitError error) {
                              DialogBean bean = new DialogBean("Connection Error", Constant.DialogButtonText.TRY_AGAIN.getValue(), Constant.DialogButtonText.CANCEL.getValue(), error.getLocalizedMessage());
                              UiObserverManager.getInstance().dispatchEvent(Cmd_UpdateFirwareStute, FAIL, "error.getLocalizedMessage()", new DialogBean[]{bean});
                          }
                      }
        );
    }

    public void updateFirware(final String node_key, NodeJson node_json) {
        IotApi api = new IotApi();
        api.setAccessToken(node_key);
        final IotService iot = api.getService();
        iot.userDownload(node_json, new Callback<OtaStatusResponse>() {
            @Override
            public void success(OtaStatusResponse otaStatusResponse, Response response) {
                updateStute(node_key);
            }

            @Override
            public void failure(RetrofitError error) {
                DialogBean bean = new DialogBean("Connection Error", Constant.DialogButtonText.TRY_AGAIN.getValue(), Constant.DialogButtonText.CANCEL.getValue(), error.getLocalizedMessage());
                UiObserverManager.getInstance().dispatchEvent(Cmd_UpdateFirware, FAIL, "error.getLocalizedMessage()", new DialogBean[]{bean});
            }
        });
    }

    public  void nodeReName(final Node node, final String newName) {
        IotApi api = new IotApi();
        User user =  UserLogic.getInstance().getUser();
        api.setAccessToken(user.token);
        final IotService iot = api.getService();
        iot.nodesRename(newName, node.node_sn, new Callback<SuccessResponse>() {
            @Override
            public void success(SuccessResponse successResponse, Response response) {
                UiObserverManager.getInstance().dispatchEvent(Cmd_Node_ReName, true, newName, null);
            }

            @Override
            public void failure(RetrofitError error) {
                UiObserverManager.getInstance().dispatchEvent(Cmd_Node_ReName, false, error.toString(), null);
            }
        });
    }

    public void nodeXserverIp(final Node node, String ip, final String url) {
        IotApi api = new IotApi();
        api.setAccessToken(node.node_key);
        final IotService iot = api.getService();
        iot.nodeSettingDataxserver(ip, url, new Callback<SuccessResponse>() {
            @Override
            public void success(SuccessResponse successResponse, Response response) {
                UiObserverManager.getInstance().dispatchEvent(Cmd_Node_SaveIp, true, url, null);
            }

            @Override
            public void failure(RetrofitError error) {
                UiObserverManager.getInstance().dispatchEvent(Cmd_Node_SaveIp, false, error.toString(), null);
            }
        });
    }

    public void removeNode(final Context context, final Node node, final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setMessage("Sure to delete this device?");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final Dialog progressDialog = DialogUtils.showProgressDialog(context,"Wio remove...");
                IotApi api = new IotApi();
                User user = UserLogic.getInstance().getUser();
                api.setAccessToken(user.token);
                final IotService iot = api.getService();
                iot.nodesDelete(node.node_sn, new Callback<SuccessResponse>() {
                    @Override
                    public void success(SuccessResponse successResponse, Response response) {
                        progressDialog.dismiss();
                        DBHelper.delNode(node.node_sn);
                        UiObserverManager.getInstance().dispatchEvent(Cmd_Node_Remove, true, ""+position, null);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        UiObserverManager.getInstance().dispatchEvent(Cmd_Node_Remove, false, error.toString(), null);
                    }
                });
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

   /* private void getGrovesData() {
        IotApi api = new IotApi();
        String token = user.token;
        api.setAccessToken(token);
        IotService iot = api.getService();
        iot.scanDrivers(new Callback<GroveDriverListResponse>() {
            @Override
            public void success(GroveDriverListResponse groveDriverListResponse, Response response) {
                for (GroverDriver groveDriver : groveDriverListResponse.drivers) {
                    groveDriver.save();
                }
                List<GroverDriver> g = DBHelper.getGrovesAll();
                updateGroveListAdapter(g);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, error.getLocalizedMessage());
            }
        });
    }*/
}
