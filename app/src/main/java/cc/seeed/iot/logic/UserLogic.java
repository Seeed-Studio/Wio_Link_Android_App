package cc.seeed.iot.logic;

import android.text.TextUtils;

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
                            setUser(user);
                            setToken(Cmd_UserLogin);
                            //  UiObserverManager.getInstance().dispatchEvent(Cmd_UserLogin, resp.status, resp.errorMsg, null);
                        } else {
                            UiObserverManager.getInstance().dispatchEvent(req.cmd, false, "", null);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    UiObserverManager.getInstance().dispatchEvent(Cmd_UserLogin, resp.status, resp.errorMsg, null);
                }
            }
        });
    }

    public void regiest(String email, String pwd){
        RequestParams params = new RequestParams();

        params.put("email", email);
        params.put("password", pwd);
        NetManager.getInstance().postRequest(CommonUrl.Hinge_User_Register, Cmd_UserRegiest, params, new INetUiThreadCallBack() {
            @Override
            public void onResp(Request req, Packet resp) {
                if (resp.status) {
                    try {
                        Gson gson = new Gson();
                        user = gson.fromJson(resp.data, User.class);
                        if (user != null) {
                            setUser(user);
                            setToken(Cmd_UserRegiest);
                            //  UiObserverManager.getInstance().dispatchEvent(Cmd_UserLogin, resp.status, resp.errorMsg, null);
                        } else {
                              UiObserverManager.getInstance().dispatchEvent(req.cmd, false, "", null);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                      UiObserverManager.getInstance().dispatchEvent(Cmd_UserRegiest, resp.status, resp.errorMsg, null);
                }
            }
        });
    }

    /**
     * 重置密码并且发送验证码到邮件
     * @param email
     */
    public void forgetPwd(String email) {
        RequestParams params = new RequestParams();
        params.put("email", email);
        NetManager.getInstance().postRequest(CommonUrl.Hinge_User_ForgetPwd, Cmd_UserForgetPwd, params, new INetUiThreadCallBack() {
            @Override
            public void onResp(Request req, Packet resp) {
                if (resp.status) {

                }
                UiObserverManager.getInstance().dispatchEvent(req.cmd, resp.status, resp.errorMsg, null);
            }
        });
    }


    public void resetPwd(String email, String code, String pwd) {
        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("password", pwd);
        params.put("code", code);
        NetManager.getInstance().postRequest(CommonUrl.Hinge_User_ResetPwd, Cmd_UserResetPwd, params, new INetUiThreadCallBack() {
            @Override
            public void onResp(Request req, Packet resp) {
                if (resp.status) {

                }
                UiObserverManager.getInstance().dispatchEvent(req.cmd, resp.status, resp.errorMsg, null);
            }
        });
    }

    public void changePassword(String oldPwd, String newPwd) {
        if (user == null) {
            return;
        }
        RequestParams params = new RequestParams();
        params.put("userid", user.getUserid());
        params.put("messageId", getToken());
        params.put("password", oldPwd);
        params.put("newPassword", newPwd);
        NetManager.getInstance().postRequest(CommonUrl.Hinge_User_ChangePwd, Cmd_UserChangePwd, params, new INetUiThreadCallBack() {
            @Override
            public void onResp(Request req, Packet resp) {
                if (resp.status) {
                }
                UiObserverManager.getInstance().dispatchEvent(req.cmd, resp.status, resp.errorMsg, null);
            }
        });
    }

    public void setToken(final String cmd){
        RequestParams params = new RequestParams();
     //   User user = UserLogic.getInstance().getUser();
        if (user == null){
            return;
        }

        if (!TextUtils.isEmpty(user.email) && !user.email.startsWith("testadmin")) {
            params.put("email", user.email);
        }

        params.put("bind_id", user.userid);
        params.put("bind_region", "seeed");
        params.put("token", user.token);
        params.put("secret", "!@#$%^&*RG)))))))JM<==TTTT==>((((((&^HVFT767JJH");
        NetManager.getInstance().post(CommonUrl.OTA_SERVER_URL+CommonUrl.Hinge_Set_Token, Cmd_SetToken, params, new INetUiThreadCallBack() {
            @Override
            public void onResp(Request req, Packet resp) {
                if (resp.status) {
                    UiObserverManager.getInstance().dispatchEvent(cmd, resp.status, resp.errorMsg, null);
                } else {
                    UiObserverManager.getInstance().dispatchEvent(cmd, resp.status, resp.errorMsg, null);
                }
            }
        });
    }

    public void logOut() {
        this.user = null;
        App.getSp().edit().putString(Constant.USER_INFO, "").commit();
    }

    public void setUser(User user) {
        this.user = user;
        try {
            Gson gson = new Gson();
            String userJson = gson.toJson(this.user);
            App.getSp().edit().putString(Constant.USER_INFO, userJson).commit();
            App.getSp().edit().putString(Constant.USER_TEST_INFO, userJson).commit();
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


}
