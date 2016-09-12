package cc.seeed.iot.logic;

import android.text.TextUtils;
import android.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cc.seeed.iot.App;
import cc.seeed.iot.entity.User;
import cc.seeed.iot.mgr.UiObserverManager;
import cc.seeed.iot.net.INetUiThreadCallBack;
import cc.seeed.iot.net.NetManager;
import cc.seeed.iot.net.Packet;
import cc.seeed.iot.net.Request;
import cc.seeed.iot.ui_setnode.model.PinConfigDBHelper;
import cc.seeed.iot.util.Common;
import cc.seeed.iot.util.CommonUrl;
import cc.seeed.iot.util.Constant;
import cc.seeed.iot.util.DBHelper;
import cc.seeed.iot.util.MLog;
import cc.seeed.iot.webapi.IotApi;
import cc.seeed.iot.webapi.IotService;
import cc.seeed.iot.webapi.model.LoginResponse;
import cc.seeed.iot.webapi.model.SuccessResponse;
import cc.seeed.iot.webapi.model.UserResponse;
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

    public void login(String email, String pwd) {
        if (App.getApp().isDefaultServer()) {
            loginDefault(email, pwd);
        } else {
            loginCustom(email, pwd);
        }
    }

    public void regist(String email, String pwd) {
        if (App.getApp().isDefaultServer()) {
            regiestDefault(email, pwd);
        } else {
            registCustom(email, pwd);
        }
    }

    public void changePwd(String oldPwd, String newPwd) {
        if (App.getApp().isDefaultServer()) {
            changePwdDefault(oldPwd, newPwd);
        } else {
            changePwdCustom(newPwd);
        }
    }

    public void sendCheckCodeToEmail(String email) {
        if (App.getApp().isDefaultServer()) {
            sendCheckCodeToEmailDefault(email);
        } else {
            sendCheckCodeToEmailCustom(email);
        }
    }


    public void loginCustom(final String email, String pwd) {
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

                    saveUser(user);

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

    public void loginDefault(String email, String pwd) {
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
                            saveUser(user);
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

    public void loginOther(String id, int type, String name, String avatar,String email) {
        RequestParams params = new RequestParams();

        params.put("platformID", id);
        params.put("platformType", type);
        params.put("toBind", 0);
        params.put("platformNickname", name);
        params.put("platformAvatar", avatar);
        params.put("platformEmail", email);
        NetManager.getInstance().postRequest(CommonUrl.Hinge_User_OtherLoginUrl, Cmd_UserOtherLogin, params, new INetUiThreadCallBack() {
            @Override
            public void onResp(Request req, Packet resp) {
                if (resp.status) {
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = new JSONObject(resp.data);
                        int toBind = jsonObj.getInt("toBind");
                        if (toBind == 1) {
                            //  App.showToastShrot("toBind");
                            UiObserverManager.getInstance().dispatchEvent(Cmd_UserBindEmail, resp.status, resp.errorMsg, null);
                        }
                    } catch (Exception e) {
                        Gson gson = new Gson();
                        user = gson.fromJson(resp.data, User.class);
                        if (user != null) {
                            saveUser(user);
                            setToken(Cmd_UserOtherLogin);
                        } else {
                            UiObserverManager.getInstance().dispatchEvent(req.cmd, false, "", null);
                        }
                    }

                } else {
                    UiObserverManager.getInstance().dispatchEvent(req.cmd, resp.status, resp.errorMsg, null);
                }
            }
        });
    }

    public void registCustom(final String email, String pwd) {
        IotApi api = new IotApi();
        IotService iot = api.getService();
        iot.userCreate(email, pwd, new Callback<UserResponse>() {
            @Override
            public void success(UserResponse userResponse, retrofit.client.Response response) {
                if (user == null) {
                    user = new User();
                }

                user.setEmail(email);
                user.setToken(userResponse.token);
                saveUser(user);
                UiObserverManager.getInstance().dispatchEvent(Cmd_UserRegiest, true, "", null);
            }

            @Override
            public void failure(RetrofitError error) {
                UiObserverManager.getInstance().dispatchEvent(Cmd_UserRegiest, false, error.toString(), null);
            }
        });
    }

    public void regiestDefault(String email, String pwd) {
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
                            saveUser(user);
                            setToken(Cmd_UserRegiest);
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


    public void sendCheckCodeToEmailDefault(String email) {
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

    public void sendCheckCodeToEmailCustom(String email) {
        IotApi api = new IotApi();
        IotService iot = api.getService();
        iot.userRetrievePassword(email, new Callback<SuccessResponse>() {
            @Override
            public void success(SuccessResponse successResponse, Response response1) {
                UiObserverManager.getInstance().dispatchEvent(Cmd_UserForgetPwd, true, successResponse.result, null);
            }

            @Override
            public void failure(RetrofitError error) {
                UiObserverManager.getInstance().dispatchEvent(Cmd_UserForgetPwd, false, error.toString(), null);
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

    public void changePwdDefault(String oldPwd, String newPwd) {
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

    public void changePwdCustom(String pwd) {
        IotApi api = new IotApi();
        IotService iot = api.getService();
        api.setAccessToken(user.token);
        iot.userChangePassword(pwd, new Callback<UserResponse>() {
            @Override
            public void success(UserResponse userResponse, retrofit.client.Response response) {
                if (userResponse != null) {
                    user.setToken(userResponse.token);
                    saveUser(user);
                }
                UiObserverManager.getInstance().dispatchEvent(Cmd_UserChangePwd, true, "Password changed successfully.", null);
            }

            @Override
            public void failure(RetrofitError error) {
                UiObserverManager.getInstance().dispatchEvent(Cmd_UserChangePwd, true, "Password changed failed", null);
            }
        });
    }


    public void setToken(final String cmd) {
        RequestParams params = new RequestParams();
        //   User user = UserLogic.getInstance().getUser();
        if (user == null) {
            return;
        }

        if (!TextUtils.isEmpty(user.email) && !user.email.startsWith("testadmin")) {
            params.put("email", user.email);
        }

        params.put("bind_id", user.userid);
        params.put("bind_region", "seeed");
        params.put("token", user.token);
        params.put("secret", "wiolinkseverything");
        NetManager.getInstance().post(App.getApp().getOtaServerUrl() + CommonUrl.Hinge_Set_Token, Cmd_SetToken, params, new INetUiThreadCallBack() {
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
    public void changeUserInfo(final String tag, final String content) {
        if (user == null) {
            return;
        }
        RequestParams params = new RequestParams();
        params.put(tag, content);
        params.put("messageId", getToken());
        params.put("userid", user.getUserid());
        NetManager.getInstance().postRequest(CommonUrl.Hinge_User_ChangeUserInfoUrl, Cmd_Change_User_Info, params, new INetUiThreadCallBack() {
            @Override
            public void onResp(Request req, Packet resp) {
                if (resp.status) {
                    if (Common.ChangeEmail.equals(tag)) {
                        user.setEmail(content);
                    } else if (Common.ChangeNickname.equals(tag)) {
                        user.setNickname(content);
                    } else if (Common.ChangeAvatar.equals(tag)) {
                        user.setAvatar(content);
                    }
                    saveUser(user);
                }
                UiObserverManager.getInstance().dispatchEvent(req.cmd, resp.status, resp.errorMsg, null);
            }
        });
    }

    public void bindOtherPlatform(final String id, final int type, final String nickname, final String avatar) {
        RequestParams params = new RequestParams();
        params.put("userid", user.getUserid());
        params.put("platformID", id);
        params.put("platformType", type);
        params.put("messageId", getToken());
        params.put("platformNickname", nickname);
        params.put("platformAvatar", avatar);
        NetManager.getInstance().postRequest(CommonUrl.Hinge_UserBindPlatformUrl, Cmd_UserBindPlatform, params, new INetUiThreadCallBack() {
            @Override
            public void onResp(Request req, Packet resp) {
                if (resp.status) {
                    App.showToastShrot(resp.data);
                } else {
                    App.showToastShrot(resp.errorMsg);
                }
                UiObserverManager.getInstance().dispatchEvent(req.cmd, resp.status, resp.errorMsg, null);
                Log.d("TAG", resp.data);
            }
        });
    }

  /*  public void unBindOtherPlatform(final int type) {
        RequestParams params = new RequestParams();
        String time = "" + System.currentTimeMillis() / 1000;

        params.put("api_key", Constant.MakerMapAppId);
        params.put("timestamp", time);
        params.put("sign", getSign(ConstantUrl.UserUnBindPlatformUrl, time));
        params.put("source", "2");
        params.put("messageId", getToken());
        params.put("userid", userBean.getUserid());
        params.put("platformType", type);
        NetManager.getInstance().postRequest(ConstantUrl.User_UnBindPlatform_Url.getVal(), UserUnBindPlatform, params, new INetUiThreadCallBack() {
            @Override
            public void onResp(Request req, Packet resp) {
                if (resp.status) {

                    if (type == Constant.PlatformWithFaceBook) {
                        if (!TextUtils.isEmpty(userBean.facebook_id)) {
                            userBean.setFacebook_id("");
                        }
                    } else if (type == Constant.PlatformWithWeChat) {
                        if (!TextUtils.isEmpty(userBean.webchat_id)) {
                            userBean.setWebchat_id("");
                        }
                    }
                    DBUtils.insertOrUpdata(userBean.getUserid(), userBean);
                }
                UiObserverManager.getInstance().dispatchEvent(req.cmd, resp.status, resp.errorMsg, null);
                Log.d("TAG", resp.data);
            }
        });
    }
*/


    public void logOut() {
        this.user = null;
        App.getApp().setFirstUseState(true);
        DBHelper.delNodesAll();
        DBHelper.delGrovesAll();
        PinConfigDBHelper.delPinConfigAll();
        App.getSp().edit().putString(Constant.SP_USER_INFO, "").commit();
    }

    public void saveUser(User user) {
        this.user = user;
        try {
            Gson gson = new Gson();
            String userJson = gson.toJson(this.user);
            App.getSp().edit().putString(Constant.SP_USER_INFO, userJson).commit();
            App.getSp().edit().putString(Constant.SP_USER_TEST_INFO, userJson).commit();
        } catch (Exception e) {
            MLog.e(UserLogic.this, e.toString());
        }
    }

    public User getUser() {
        if (user == null) {
            String userJson = App.getSp().getString(Constant.SP_USER_INFO, "");
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
        if (user == null || TextUtils.isEmpty(user.token)) {
            return false;
        } else {
            return true;
        }
    }


}
