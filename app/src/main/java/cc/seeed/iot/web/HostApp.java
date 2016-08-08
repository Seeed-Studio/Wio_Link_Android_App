/**
 * Summary: js脚本所能执行的函数空间
 * Version 1.0
 * Date: 13-11-20
 * Time: 下午4:40
 * Copyright: Copyright (c) 2013
 */

package cc.seeed.iot.web;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.webkit.WebView;

//HostJsScope中需要被JS调用的函数，必须定义成public static，且必须包含WebView这个参数
public class HostApp {

    public static void getImei(WebView webView, JsCallback jsCallback) {
        String imei = ((TelephonyManager) webView.getContext().getSystemService(Context.TELEPHONY_SERVICE)).getSubscriberId();
        jsCallback.apply(imei);
    }

    /*public static void isLogin(WebView webView, JsCallback jsCallback) {
        jsCallback.apply(String.valueOf(UserLogic.getInstance().isLogin()));
    }

    public static void getToken(WebView webView, final JsCallback jsCallback) {
        ThreadManager.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                jsCallback.apply(UserLogic.getInstance().getToken());
            }
        }, 0);
    }


    public static void delayJsCallback(WebView view, Object l, Object str, Object d, final JsCallback jsCallback) {
        QLog.d("HostApp", "l:" + Util.toLong(l) + " d:" + Util.toDouble(d));
        ThreadManager.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                jsCallback.apply("data from java");
            }
        }, Util.toLong(l));
    }

    public static void nativePopTip(WebView view, Object j, Object l, JsCallback jsCallback) {
        QLog.d("HostApp", "text:" + j.toString());
        try {
            App.showToast(j.toString());
            jsCallback.apply("");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    //h5 跳转到app页面
    public static void toNativePage(WebView webView, Object action, Object params) {
        QLog.d("HostApp", "action:" + action.toString() + " params:" + params.toString());
        if (action == null) {
            return;
        }

        String act = action.toString();
        Context context = webView.getContext();
        Intent intent = null;
        JSONObject argJson = null;
        boolean hasParams = false;

        try {
            if (params != null) {
                argJson = new JSONObject(params.toString());
                hasParams = true;
            }
            if (act.equals("nativeLogin")) {  //登录页
                intent = new Intent(context, LoginAct.class);
                intent.putExtra(LoginAct.Return_Back, true);
                intent.putExtra(LoginAct.IsNeedBindPhone, true);
                App.showToast(R.string.not_login_hint);

            } else if (act.equals("nativeSearch")) {  //搜索页
                boolean isGoSearch = true;
                if (hasParams && !argJson.toString().equals("{}")) {
                    try {
                        String keyword = argJson.optString("keyword");
                        if (!TextUtils.isEmpty(keyword)) {
                            SearchKeyLogic.getInstance().saveKeyword(context, keyword);
                            intent = new Intent(context, SearchResultActivity.class);
                            intent.putExtra("keyword", keyword);
                            isGoSearch = false;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (isGoSearch) {
                    intent = new Intent(context, SearchKeyActivity.class);
                }
            } else if (act.equals("nativeRoomDetail") && hasParams) {   //跳转到房源详情
                long roomID = argJson.optLong("roomID");
                intent = new Intent(context, RoomDetailAct.class);
                intent.putExtra(RoomDetailAct.KEY_ROOM_DETAIL_ID, roomID);

            } else if (act.equals("nativeApartmentList") && hasParams) { //跳转的房源列表
                String keyword = argJson.optString("keyword");
                String province = argJson.optString("province");
                String city = argJson.optString("city");
                String region = argJson.optString("region");
                int priceMin = argJson.optInt("priceMin", -1);
                int priceMax = argJson.optInt("priceMax", -1);

                intent = new Intent(context, RoomListAct.class);
                intent.putExtra("keyword", keyword);
                intent.putExtra("keyword", keyword);
                intent.putExtra("province", province);
                intent.putExtra("city", city);
                intent.putExtra("region", region);
                intent.putExtra("priceMin", priceMin);
                intent.putExtra("priceMax", priceMax);
            } else if (act.equals("nativeBusinessRoomDetail")) {
                String roomID = argJson.optString("roomGenId");
                intent = new Intent(context, WebEntry.class);
                String url = String.format(RunProfile.Business_Room_Url.getVal(), roomID);
                QLog.d(context, "load business room " + url);
                intent.putExtra(WebEntry.Param_Url, url);
                intent.putExtra(WebEntry.Room_GenId, roomID);
                intent.putExtra(WebEntry.Param_CanShare, true);
            } else if (act.equals("openWebWindow")) {
                openWebWindow(webView, params);
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (intent != null) {
            context.startActivity(intent);
        }
    }

    public static void nativeLogin(WebView view, Object params) {
        Context context = view.getContext();
        Intent intent = new Intent(view.getContext(), LoginAct.class);
        intent.putExtra(LoginAct.Return_Back, true);
        intent.putExtra(LoginAct.IsNeedBindPhone, true);
        App.showToast(R.string.not_login_hint);
        context.startActivity(intent);
    }


    public static void nativeSearch(WebView view, Object params) {
        Context context = view.getContext();
        JSONObject argJson = null;
        try {
            argJson = new JSONObject(params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            argJson = null;
        }

        if (argJson == null || argJson.toString().equals("{}") || TextUtils.isEmpty(argJson.optString("keyword"))) {
            Intent intent = new Intent(context, SearchKeyActivity.class);
            context.startActivity(intent);
        } else {
            String keyword = argJson.optString("keyword");
            SearchKeyLogic.getInstance().saveKeyword(context, keyword);
            Intent intent = new Intent(context, SearchResultActivity.class);
            intent.putExtra("keyword", keyword);
            context.startActivity(intent);
        }
    }

    public static void nativeRoomDetail(WebView view, Object params) {
        JSONObject argJson = null;
        Context context = view.getContext();
        long id = 0;
        try {
            argJson = new JSONObject(params.toString());
            id = argJson.getLong("roomID");
        } catch (JSONException e) {
            e.printStackTrace();
            argJson = null;
        }
        Intent intent = new Intent(context, RoomDetailAct.class);
        intent.putExtra(RoomDetailAct.KEY_ROOM_DETAIL_ID, id);
        context.startActivity(intent);
    }

    public static void nativeApartmentList(WebView view, Object params) {
        JSONObject argJson = null;
        Context context = view.getContext();
        try {
            argJson = new JSONObject(params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            argJson = null;
        }
        if (argJson != null) {
            String keyword = argJson.optString("keyword");
            String province = argJson.optString("province");
            String city = argJson.optString("city");
            String region = argJson.optString("region");
            int priceMin = argJson.optInt("priceMin", -1);
            int priceMax = argJson.optInt("priceMax", -1);

            Intent intent = new Intent(context, RoomListAct.class);
            intent.putExtra("keyword", keyword);
            intent.putExtra("keyword", keyword);
            intent.putExtra("province", province);
            intent.putExtra("city", city);
            intent.putExtra("region", region);
            intent.putExtra("priceMin", priceMin);
            intent.putExtra("priceMax", priceMax);
            context.startActivity(intent);
        }

    }

    public static void nativeBusinessRoomDetail(WebView view, Object params) {
        JSONObject argJson = null;
        Context context = view.getContext();
        try {
            argJson = new JSONObject(params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            argJson = null;
        }
        if (argJson != null) {
            String roomID = argJson.optString("roomGenId");
            Intent intent = new Intent(context, WebEntry.class);
            String url = String.format(RunProfile.Business_Room_Url.getVal(), roomID);
            QLog.d(context, "load business room " + url);
            intent.putExtra(WebEntry.Param_Url, url);
            intent.putExtra(WebEntry.Room_GenId, roomID);
            intent.putExtra(WebEntry.Param_CanShare, true);
            context.startActivity(intent);
        }
    }

    public static void subletDetail(WebView view, Object params) {
        JSONObject argJson = null;
        Context context = view.getContext();
        try {
            argJson = new JSONObject(params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            argJson = null;
        }
        if (argJson != null) {
            long subletId = argJson.optLong("id");
            Intent intent = new Intent(context, PersonRoomDetailAct.class);
            intent.putExtra(RoomDetailAct.KEY_ROOM_DETAIL_ID, subletId);
            context.startActivity(intent);
        }
    }

    public static void c2cChat(WebView view, Object params) {
        JSONObject argJson = null;
        Context context = view.getContext();
        try {
            argJson = new JSONObject(params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            argJson = null;
        }
        if (argJson != null) {
            long peerUin = argJson.optLong("peerUin");
            Intent intent = new Intent(context, ChatNewActivity.class);
            intent.putExtra("chatType", ChatNewActivity.CHATTYPE_C2C);
            intent.putExtra("userName", "" + peerUin);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
    }

    public static void groupChat(WebView view, Object params) {
        JSONObject argJson = null;
        final Context context = view.getContext();
        try {
            argJson = new JSONObject(params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            argJson = null;
        }
        if (argJson != null) {
            final String groupUin = argJson.optString("groupID");
            if (UserInfoManagerNew.getInstance().getGroupID2Info().containsKey(groupUin)) {
                startAct(context, groupUin);
                return;
            }
            TIMGroupManager.getInstance().applyJoinGroup(groupUin, "somereason", new TIMCallBack() {
                        @Override
                        public void onError(int code, String desc) {
                            if (code == 10013) {
                                startAct(context, groupUin);
                            }
                            QLog.d(this, "applyJoinGroup " + "  code: " + code);
                            App.showToast(desc);
                        }

                        @Override
                        public void onSuccess() {
                            startAct(context, groupUin);
                        }
                    }

            );

        }
    }

    static void startAct(Context context, String groupUin) {
        Intent intent = new Intent(context, ChatNewActivity.class);
        intent.putExtra("chatType", ChatNewActivity.CHATTYPE_GROUP);
        intent.putExtra("groupID", groupUin);
        intent.putExtra("groupType", Constant.TYPE_CHAT_ROOM);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void userInfo(WebView view, Object params) {
        JSONObject argJson = null;
        Context context = view.getContext();
        try {
            argJson = new JSONObject(params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            argJson = null;
        }
        if (argJson != null) {
            long uin = argJson.optLong("uin");
            Intent intent = new Intent(context, UserInfoAct.class);
            RentSimpleUserInfo info = new RentSimpleUserInfo.Builder().uin(uin).build();
            intent.putExtra(UserInfoAct.USER_INFO, info);
            context.startActivity(intent);
        }
    }

    public static void closeWindow(WebView view, Object params) {
        Context context = view.getContext();
        Activity act = (Activity) context;
        act.finish();
    }

    public static void openWebWindow(WebView view, Object params) {
        JSONObject argJson = null;
        Context context = view.getContext();
        try {
            argJson = new JSONObject(params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            argJson = null;
        }
        if (argJson != null) {
            String url = argJson.optString("url");
            String showTitleBar = argJson.optString("hiddenNavbar");
            Intent intent = new Intent(context, WebEntry.class);
            intent.putExtra(WebEntry.Param_Url, url);
            if ("YES".equals(showTitleBar)) {
                intent.putExtra(WebEntry.ShowTitleBar, false);
            } else if ("NO".equals(showTitleBar)) {
                intent.putExtra(WebEntry.ShowTitleBar, true);
            }

            context.startActivity(intent);
        }
    }

    public static ServerApi serverApi = new ServerApi();

    public static void nativePay(WebView view, final Object params, final JsCallback onSucess,
                                 final JsCallback onFail) {
        final Activity context = (Activity) view.getContext();
        JSONObject argJson = null;

        try {
            argJson = new JSONObject(params.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            argJson = null;
        }

        if (argJson == null || argJson.toString().equals("{}")) {
            App.showToast("数据错误!");
        } else {

          *//*  RechargeReq req = new RechargeReq.Builder().uin(UserLogic.getInstance().getUid()).amount(0.01).channel("alipay").address("127.0.0.1").build();
            serverApi.sendCmd(CmdConst.ACCOUNT_Recharge, req, new INetCallback() {
                @Override
                public void onResp(Request req, Packet resp) {
                    Packet packet = req.packet;
                    String cmd = packet.cmd;
                    if (CmdConst.ACCOUNT_Recharge.equals(cmd)) {
                        if (resp.ret == 0) {
                            RechargeResp rechargeResp = serverApi.getResp(resp, RechargeResp.class);
                            String charge = rechargeResp.charge;

                            if (!TextUtils.isEmpty(charge)) {
                                Intent intent = new Intent();
                                String packageName = context.getPackageName();
                                ComponentName componentName = new ComponentName(packageName, packageName + ".wxapi.WXPayEntryActivity");

                                intent.setComponent(componentName);
                                intent.putExtra(PaymentActivity.EXTRA_CHARGE, charge);
                                context.startActivityForResult(intent, PayAct.REQUEST_CODE_PAYMENT);

                                WebEntry.setPayResultListener(new WebEntry.PayResultListener() {
                                    @Override
                                    public void onSuccess(String channel) {
                                        onSucess.apply(params.toString());
                                        App.showToast("onSuccess: " + channel);
                                    }

                                    @Override
                                    public void onFail(String channel, String errMsg) {
                                        Map<String,String> failMag= new HashMap<String, String>();
                                        failMag.put("payParams",params.toString());
                                        failMag.put("errCode","-1");
                                        failMag.put("failMsg",errMsg);
                                        failMag.put("extraMsg","");

                                        onFail.apply(failMag.toString());
                                        App.showToast("onFail: " + errMsg);
                                    }
                                });
                            }
                        }
                    }
                }
            });*//*


            String charge = argJson.optString("charge");
            int orderId = argJson.optInt("orderId", -1);

            if (!TextUtils.isEmpty(charge)) {
                Intent intent = new Intent();
                String packageName = context.getPackageName();
                ComponentName componentName = new ComponentName(packageName, packageName + ".wxapi.WXPayEntryActivity");

                intent.setComponent(componentName);
                intent.putExtra(PaymentActivity.EXTRA_CHARGE, charge);
                context.startActivityForResult(intent, PayAct.REQUEST_CODE_PAYMENT);

                WebEntry.setPayResultListener(new WebEntry.PayResultListener() {
                    @Override
                    public void onSuccess(String channel) {
                        onSucess.apply(params.toString());
                        //     App.showToast("onSuccess: " + channel);
                    }

                    @Override
                    public void onFail(String channel, String errMsg) {
                        Map<String, String> failMag = new HashMap<String, String>();
                        failMag.put("payParams", params.toString());
                        failMag.put("errCode", "-1");
                        failMag.put("failMsg", errMsg);
                        failMag.put("extraMsg", "");

                        onFail.apply(failMag.toString());
                        //     App.showToast("onFail: " + errMsg);
                    }
                });
            }

        }
    }


    public static void nativeAction(final WebView webView, Object action, Object params) {
        if (action == null) {
            return;
        }

        String act = action.toString();
        final Activity context = (Activity) webView.getContext();
        Intent intent = null;
        JSONObject argJson = null;

        try {
            if (params != null && !params.toString().equals("null")) {
                argJson = new JSONObject(params.toString());
            }
            if (act.equals("socialShare")) {  //登录页
                if (argJson != null) {
                    String url = argJson.optString("url");
                    String title = argJson.optString("title");
                    String content = argJson.optString("content");
                    String imageURL = argJson.optString("imageURL");
                    UMImage headImg = null;
                    if (!TextUtils.isEmpty(imageURL)) {
                        headImg = new UMImage(context, imageURL);
                    }
                    new UmengUtil().initShare(context, content, url, headImg, title);

                    new UmengUtil().openShare(context);
                }
            } else if (act.equals("webViewGoBack")) {
                ThreadManager.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (webView.canGoBack()) {
                            webView.goBack();
                        } else {
                            context.finish();
                        }
                    }
                }, 0);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void nativeEventReq(WebView webView, Object action) {
        if (action == null) {
            return;
        }

        String act = action.toString();
        if (act.equals("YES")){
            webView.getParent().requestDisallowInterceptTouchEvent(true);
        }else if (act.equals("NO")){
            webView.getParent().requestDisallowInterceptTouchEvent(false);
        }
    }
*/
}