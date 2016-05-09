package cc.seeed.iot.logic;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;

import cc.seeed.iot.App;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.mgr.UiObserverManager;
import cc.seeed.iot.net.INetUiThreadCallBack;
import cc.seeed.iot.net.NetManager;
import cc.seeed.iot.net.Packet;
import cc.seeed.iot.net.Request;
import cc.seeed.iot.util.Common;
import cc.seeed.iot.util.CommonUrl;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.EncryptUtil;
import cc.seeed.iot.util.MLog;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.LoginResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * author: Jerry on 2016/5/4 18:04.
 * description:管理用户信息的逻辑
 */
public class UserLogic extends BaseLogic {
    private static UserLogic sIns;
    User user;

    public static UserLogic getInstance() {
        if (sIns == null) {
            synchronized (UserLogic.class) {
                if (sIns == null) {
                    sIns = new UserLogic();
                }
            }
        }
        return sIns;
    }

    public void oldLogin(final String email, String pwd) {
        IotApi api = new IotApi();
        IotService iot = api.getService();
        iot.userLogin(email, pwd, new Callback<LoginResponse>() {
            @Override
            public void success(LoginResponse loginResponse, Response response) {
                if (loginResponse != null) {
                    if (user == null) {
                        user = new User();
                    }

                    user.setEmail(email);
                    user.setToken(loginResponse.token);
                    user.setUserid(loginResponse.user_id);

                    setUser(user);

                    UiObserverManager.getInstance().dispatchEvent(Cmd_UserLogin, true, "", null);
                } else {
                    UiObserverManager.getInstance().dispatchEvent(Cmd_UserLogin, false, "", null);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                UiObserverManager.getInstance().dispatchEvent(Cmd_UserLogin, false, error.toString(), null);
            }
        });
    }

    public void login(String email, String pwd) {
        RequestParams params = new RequestParams();

        params.put("email", email);
        params.put("password", pwd);
        NetManager.getInstance().postRequest(CommonUrl.Hinge_User_Login, Cmd_UserLogin, params, new INetUiThreadCallBack() {
            @Override
            public void onResp(Request req, Packet resp) {
                if (resp.status) {
                    try {
                        Gson gson = new Gson();
                        user = gson.fromJson(resp.data, User.class);
                        if (user != null) {
                            setToken();
                          //  UiObserverManager.getInstance().dispatchEvent(Cmd_UserLogin, resp.status, resp.errorMsg, null);
                            setUser(user);
                        } else {
                          //  UiObserverManager.getInstance().dispatchEvent(req.cmd, false, "", null);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                  //  UiObserverManager.getInstance().dispatchEvent(Cmd_UserLogin, resp.status, resp.errorMsg, null);
                }
            }
        });
    }

    public void setToken(){
        RequestParams params = new RequestParams();
     //   User user = UserLogic.getInstance().getUser();
        if (user == null){
            return;
        }

        params.put("bind_id", user.userid);
        params.put("bind_region", "seeed");
        params.put("token", user.token);
        params.put("secret", "!@#$%^&*RG)))))))JM<==TTTT==>((((((&^HVFT767JJH");
        NetManager.getInstance().post(CommonUrl.OTA_SERVER_URL+CommonUrl.Hinge_Set_Token, Cmd_SetToken, params, new INetUiThreadCallBack() {
            @Override
            public void onResp(Request req, Packet resp) {
                if (resp.status) {
                    App.showToastShrot("ok");
                    UiObserverManager.getInstance().dispatchEvent(Cmd_UserLogin, resp.status, resp.errorMsg, null);
                } else {
                    UiObserverManager.getInstance().dispatchEvent(Cmd_UserLogin, resp.status, resp.errorMsg, null);
                }
            }
        });
    }

    public void logOut() {
        this.user = null;
        App.getSp().edit().putString(Constant.USER_INFO, "").commit();
    }

    private void setUser(User user) {
        this.user = user;
        try {
            Gson gson = new Gson();
            String userJson = gson.toJson(this.user);
            App.getSp().edit().putString(Constant.USER_INFO, userJson).commit();
        } catch (Exception e) {
            MLog.e(UserLogic.this, e.toString());
        }
    }

    public User getUser() {
        if (user == null) {
            String userJson = App.getSp().getString(Constant.USER_INFO, "");
            try {
                Gson gson = new Gson();
                user = gson.fromJson(userJson, User.class);
            } catch (Exception e) {
                MLog.e(UserLogic.this, e.toString());
            }
        }
        return user;
    }

    public boolean isLogin() {
        getUser();
        if (user == null) {
            return false;
        } else {
            return true;
        }
    }

    public static String getToken() {
        User userBean = UserLogic.getInstance().getUser();
        if (userBean != null) {
            String decrypt = EncryptUtil.decrypt(userBean.getToken(), Common.API_GET_TOKEN_KEY);
            String encrypt = EncryptUtil.encrypt(decrypt, Common.API_CHECK_TOKEN_KEY);
            return encrypt;
        }
        return null;
    }
}
