package cc.seeed.iot.logic;

import android.os.Message;
import android.util.Log;

import java.util.List;

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
import cc.seeed.iot.webapi.model.NodeJson;
import cc.seeed.iot.webapi.model.OtaStatusResponse;
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
