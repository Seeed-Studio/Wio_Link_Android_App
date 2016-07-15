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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Arrays;

import cc.seeed.iot.App;
import cc.seeed.iot.logic.CmdConst;
import cc.seeed.iot.logic.UserLogic;
import cc.seeed.iot.mgr.UiObserverManager;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpVersion;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.conn.scheme.PlainSocketFactory;
import cz.msebera.android.httpclient.conn.scheme.Scheme;
import cz.msebera.android.httpclient.conn.scheme.SchemeRegistry;
import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.params.BasicHttpParams;
import cz.msebera.android.httpclient.params.HttpConnectionParams;
import cz.msebera.android.httpclient.params.HttpParams;
import cz.msebera.android.httpclient.params.HttpProtocolParams;
import cz.msebera.android.httpclient.protocol.HTTP;

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
                                String email = "";
                                try {
                                    email = response.getJSONObject().getString("email");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if (type == LoginWithFacebook) {
                                    UserLogic.getInstance().loginOther(profile.getId(), Constant.OtherPlatform.Facebook.getValue(), profile.getName(),
                                            profile.getProfilePictureUri(300, 300).toString(), email);
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

    public static void getGithubToken(String code) {
        String url = "https://github.com/login/oauth/access_token";
        RequestParams params = new RequestParams();
        params.put("code", code);
        params.put("client_id", Common.Github_Client_ID);
        params.put("redirect_uri", Common.Github_Redirect_Uri);
        params.put("client_secret", Common.Github_Client_Secret);
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (responseBody != null) {
                    String s = new String(responseBody);
                 //   getGithubUserInfo(s);
                    getInfo(s);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                MLog.e(error.getMessage());
            }
        });
    }

    public static void getGithubUserInfo(String token) {
        String url = "https://api.github.com/user?" + token;
//        String url = "https://api.github.com/user?access_token=bc9e085e450f88f8d642e5ac8d8176a98b59f289&scope=user&token_type=bearer";
        RequestParams params = new RequestParams();
        //   params.put("access_token", token.substring("access_token".length()));
        AsyncHttpClient client = new AsyncHttpClient();
      //  AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
//        client.addHeader("Authorization","token OAUTH-TOKEN");
        //   client.setSSLSocketFactory(NetManager.createSSLSocketFactory());
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (responseBody != null) {
                    String s = new String(responseBody);
                    JSONObject jsonObj = null;
                    String userId = "";
                    String avatar_url = "";
                    String name = "";
                    try {
                        jsonObj = new JSONObject(s);
                        userId = jsonObj.getString("id");
                        avatar_url = jsonObj.getString("avatar_url");
                        name = jsonObj.getString("login");

                        MLog.e("userId: " + userId + " avatar: " + avatar_url + " name: " + name);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                MLog.e(error.getMessage());
            }
        });
    }

    public static SchemeRegistry getSchemeRegistry() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 10000);
            HttpConnectionParams.setSoTimeout(params, 10000);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));
            return registry;
        } catch (Exception e) {
            return null;
        }
    }

    public static void getInfo(final String token) {
       new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = "https://api.github.com/user?" + token;
                    HttpClient client = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(path);
                    HttpResponse response = client.execute(httpGet); // 客户端执行请求
                    int code = response.getStatusLine().getStatusCode(); // 获取响应码
                    if (code == 200) {
                        InputStream is = response.getEntity().getContent(); // 获取实体内容
                         String result = streamToString(is); // 字节流转字符串
                        JSONObject jsonObj = null;
                        String userId = "";
                        String avatar_url = "";
                        String name = "";
                        String email = "";
                        try {
                            jsonObj = new JSONObject(result);
                            userId = jsonObj.getString("id");
                            avatar_url = jsonObj.getString("avatar_url");
                            name = jsonObj.getString("login");
                            email = jsonObj.getString("email");

                            MLog.e("userId: " + userId + " avatar: " + avatar_url + " name: " + name+" email:"+email);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        MLog.e(result);
                    } else {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public static String streamToString(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.close();
            is.close();
            byte[] byteArray = baos.toByteArray();
            return new String(byteArray);
        } catch (Exception e) {
         //   Log.e(tag, e.toString());
            return null;
        }
    }

}
