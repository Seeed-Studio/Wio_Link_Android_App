package cc.seeed.iot.util;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONObject;

import java.util.Arrays;

import cc.seeed.iot.App;
import cc.seeed.iot.logic.CmdConst;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.mgr.UiObserverManager;

/**
 * Created by seeed on 2016/3/31.
 */
public class OtherPlatformUtils {
    public static int LoginWithWechat = 1;
    public static int LoginWithFacebook = 2;
    public static int BindWithWechat = 3;
    public static int BindWithFacebook = 4;

    public static void getFacebookInfo(Activity activity, CallbackManager callbackManager, final int type) {
        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile", "email", "user_friends"));
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        //   App.showToastShrot("onSuccess");
                        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Profile profile = Profile.getCurrentProfile();
                                if (type == LoginWithFacebook) {
                                        UserLogic.getInstance().loginOther(profile.getId(), Common.PlatformWithFaceBook, profile.getName(), profile.getProfilePictureUri(300, 300).toString());
                                } else if (type == BindWithFacebook) {
                                    //   UserLogic.getInstance().bindOtherPlatform(profile.getId(), Constant.PlatformWithFaceBook, profile.getName(), profile.getProfilePictureUri(300, 300).toString());
                                }
                                Log.v("TAG", response.toString());
                            }
                        });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,gender, birthday");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        App.showToastShrot("Authorize cancel");
                        if (type == LoginWithFacebook) {
                            UiObserverManager.getInstance().dispatchEvent(CmdConst.Cmd_AuthorizeCancel, true, "", null);
                        }
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        if (type == LoginWithFacebook) {
                            UiObserverManager.getInstance().dispatchEvent(CmdConst.Cmd_AuthorizeCancel, true, "", null);
                        }
                        App.showToastShrot("Authorize fail");

                    }
                });
    }


  /*  public static void getWechatInfo(final Activity activity, final UMShareAPI mShareAPI, final int type) {
        if (!ToolUtil.isInstallByread("com.tencent.mm")){
            App.showToastShrot("You don't have to install WeChat");
            return;
        }
        mShareAPI.doOauthVerify(activity, SHARE_MEDIA.WEIXIN, new UMAuthListener() {
            @Override
            public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                mShareAPI.getPlatformInfo(activity, SHARE_MEDIA.WEIXIN, new UMAuthListener() {
                    @Override
                    public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> data) {
                        String unionid = data.get("unionid");
                        String nickname = data.get("nickname");
                        String headimgurl = data.get("headimgurl");
                        if (type == LoginWithWechat) {
                            UserLogic.getInstance().loginOther(unionid, Constant.PlatformWithWeChat, nickname, headimgurl);
                        } else if (type == BindWithWechat) {
                            UserLogic.getInstance().bindOtherPlatform(unionid, Constant.PlatformWithWeChat, nickname, headimgurl);
                        }

                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                        App.showToastShrot("Authorize fail");
                        if (type == LoginWithWechat) {
                            UiObserverManager.getInstance().dispatchEvent(CmdConst.Cmd_AuthorizeCancel, true, "", null);
                        }
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media, int i) {
                        App.showToastShrot("Authorize cancel");
                        if (type == LoginWithWechat) {
                            UiObserverManager.getInstance().dispatchEvent(CmdConst.Cmd_AuthorizeCancel, true, "", null);
                        }
                    }
                });
            }

            @Override
            public void onError(SHARE_MEDIA platform, int action, Throwable t) {

            }

            @Override
            public void onCancel(SHARE_MEDIA platform, int action) {

            }
        });
    }*/

}
