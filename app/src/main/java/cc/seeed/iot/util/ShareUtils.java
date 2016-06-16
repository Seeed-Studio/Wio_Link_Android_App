package cc.seeed.iot.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.HashMap;

import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.view.FontTextView;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.google.GooglePlus;

/**
 * author: Jerry on 2016/6/3 14:27.
 * description:
 */
public class ShareUtils {
    private static PopupWindow popWindow;
    private static Activity activity;
    private static String shareTitle;
    private static String shareUrl;
    private static String shareImgUrl;
    private static boolean isWiki = false;

    public static void initShare() {
        /*TwitterAuthConfig authConfig =  new TwitterAuthConfig("jWP91R6MLroH8BSA32Clr9plO", "bJo6j3EtsD9Hby4UQmYzJwDgDOWxiwAduOSOqSvH3vTmKs2nOR");
        Fabric.with(this, new TwitterCore(authConfig), new TweetComposer());*/
    }

    public static PopupWindow show(Activity context, String title, String url, String imgUrl) {
        shareTitle = "";
        shareUrl = "";
        shareTitle = title;
        shareUrl = url;
        shareImgUrl = imgUrl;
        if (activity != null && activity != context) {
            popWindow = null;
        }
        activity = context;
        if (popWindow == null) {
            View view = LayoutInflater.from(context).inflate(R.layout.popwindow_share, null);
            popWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            initPop(view);
        }

        popWindow.setFocusable(true);
        popWindow.setBackgroundDrawable(new BitmapDrawable());
        popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popWindow.showAtLocation(new View(context), Gravity.CENTER, 0, 0);
        return popWindow;
    }

    public static void setIsWiki(boolean iswiki) {
        isWiki = iswiki;
    }

    public static void initPop(View view) {
        LinearLayout mLlCopy = (LinearLayout) view.findViewById(R.id.mLlCopy);
        LinearLayout mLlSafari = (LinearLayout) view.findViewById(R.id.mLlSafari);
        LinearLayout mLlMail = (LinearLayout) view.findViewById(R.id.mLlMail);
        LinearLayout mLlMessage = (LinearLayout) view.findViewById(R.id.mLlMessage);
        LinearLayout mLlTwitter = (LinearLayout) view.findViewById(R.id.mLlTwitter);
        LinearLayout mLlGoogle = (LinearLayout) view.findViewById(R.id.mLlGoogle);
        LinearLayout mLlWhatsapp = (LinearLayout) view.findViewById(R.id.mLlWhatsapp);
        LinearLayout mLlFacebook = (LinearLayout) view.findViewById(R.id.mLlFacebook);
        FontTextView mTvCancel = (FontTextView) view.findViewById(R.id.mTvCancel);

        mLlCopy.setOnClickListener(listener);
        mLlSafari.setOnClickListener(listener);
        mLlMail.setOnClickListener(listener);
        mLlMessage.setOnClickListener(listener);
        mLlTwitter.setOnClickListener(listener);
        mLlGoogle.setOnClickListener(listener);
        mLlWhatsapp.setOnClickListener(listener);
        mLlFacebook.setOnClickListener(listener);
        mTvCancel.setOnClickListener(listener);
    }

    static View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.mLlCopy:
                    SystemUtils.copy(getShareUrl(null), activity);
                    break;
                case R.id.mLlSafari:
                    Uri uri = Uri.parse(getShareUrl(null));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    activity.startActivity(intent);
                    break;
                case R.id.mLlMail:
                    SystemUtils.sendEmail(activity, TextUtils.isEmpty(shareTitle) ? getShareUrl(null) : shareTitle + " " + getShareUrl(null), "");
                    break;
                case R.id.mLlMessage:
                    SystemUtils.sendSmsWithBody(activity, "", TextUtils.isEmpty(shareTitle) ? getShareUrl(null) : shareTitle + " " + getShareUrl(null));
                    break;
                case R.id.mLlTwitter:
                    UmengUtils.share(activity, SHARE_MEDIA.TWITTER, shareTitle, getShareUrl("twitter"), getShareUrl("twitter"), shareImgUrl);
//                       UmengUtils.shareToTwitter(activity,shareUrl);
                    // shareToTwitter();
                    //   showShare(activity, ShareSDK.getPlatform(Twitter.NAME).getName(), false);
                    break;
                case R.id.mLlGoogle:
                    //  UmengUtils.share(activity, SHARE_MEDIA.GOOGLEPLUS, shareTitle, shareUrl, shareUrl, shareImgUrl);
                    shareToGoogle(getShareUrl("google"),getShareUrl("google"));
                    break;
                case R.id.mLlWhatsapp:
                    UmengUtils.share(activity, SHARE_MEDIA.WHATSAPP, shareTitle,getShareUrl("whatsapp"), getShareUrl("whatsapp"), shareImgUrl);
                    break;
                case R.id.mLlFacebook:
                    UmengUtils.share(activity, SHARE_MEDIA.FACEBOOK, shareTitle, getShareUrl("facebook"),  getShareUrl("facebook"), shareImgUrl);
                    // UmengUtils.shareFacebookMessenger(activity, shareUrl, shareImgUrl);
                    break;
                case R.id.mTvCancel:
                    if (popWindow != null) {
                        popWindow.dismiss();
                    }
                    break;
            }

            if (popWindow != null) {
                popWindow.dismiss();
            }
        }
    };

    private static String getShareUrl(String utm_content) {
        String url = "";
        if (isWiki) {
            if (!TextUtils.isEmpty(utm_content)) {
                url = shareUrl + "?utm_source=Wio&utm_medium=Android&utm_content=" + utm_content + "&utm_campaign=WIKI";
            }else {
                url = shareUrl+"?utm_source=Wio&utm_medium=Android&utm_campaign=WIKI";
            }
        } else {
            url = shareUrl;
        }
        return url;
    }

    private static void shareToGoogle(String context, String url) {
        if (!ToolUtil.isInstallByread("com.google.android.apps.plus")) {
            App.showToastShrot("App not installed");
            return;
        }
        GooglePlus.ShareParams sp = new GooglePlus.ShareParams();
        sp.setText(context);
        sp.setUrl(url);

        Platform weibo = ShareSDK.getPlatform(GooglePlus.NAME);
        weibo.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                App.showToastShrot("Success");
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                App.showToastShrot("Fail");
            }

            @Override
            public void onCancel(Platform platform, int i) {
                App.showToastShrot("Cancel");
            }
        }); // 设置分享事件回调
// 执行图文分享
        weibo.share(sp);
    }

  /*  private static void shareToTwitter() {
        Platform.ShareParams sp = new Platform.ShareParams();
        sp.setText("测试分享的文本");

        Platform twitter = ShareSDK.getPlatform(Twitter.NAME);
        twitter.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {

            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {

            }

            @Override
            public void onCancel(Platform platform, int i) {

            }
        }); // 设置分享事件回调
// 执行图文分享
        twitter.share(sp);
    }
*/
  /*  public static void showShare(Context context, String platformToShare, boolean showContentEdit) {
        OnekeyShare oks = new OnekeyShare();
        oks.setSilent(!showContentEdit);
        if (platformToShare != null) {
            oks.setPlatform(platformToShare);
        }
        //ShareSDK快捷分享提供两个界面第一个是九宫格 CLASSIC  第二个是SKYBLUE
        oks.setTheme(OnekeyShareTheme.CLASSIC);
        // 令编辑页面显示为Dialog模式
//        oks.setDialogMode();
        // 在自动授权时可以禁用SSO方式
//        oks.disableSSOWhenAuthorize();
        //oks.setAddress("12345678901"); //分享短信的号码和邮件的地址
        oks.setTitle("ShareSDK--Title");
        oks.setTitleUrl("http://mob.com");
        oks.setText("ShareSDK--文本");
        //oks.setImagePath("/sdcard/test-pic.jpg");  //分享sdcard目录下的图片
        oks.setUrl("http://www.mob.com"); //微信不绕过审核分享链接
        //oks.setFilePath("/sdcard/test-pic.jpg");  //filePath是待分享应用程序的本地路劲，仅在微信（易信）好友和Dropbox中使用，否则可以不提供
        oks.setComment("分享"); //我对这条分享的评论，仅在人人网和QQ空间使用，否则可以不提供
        oks.setSite("ShareSDK");  //QZone分享完之后返回应用时提示框上显示的名称
        oks.setSiteUrl("http://mob.com");//QZone分享参数
        oks.setVenueName("ShareSDK");
        oks.setVenueDescription("This is a beautiful place!");
        oks.setCallback(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                App.showToastShrot("onComplete");
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                App.showToastShrot(throwable.toString());
            }

            @Override
            public void onCancel(Platform platform, int i) {
                App.showToastShrot("onCancel");
            }
        });
        // 将快捷分享的操作结果将通过OneKeyShareCallback回调
        //oks.setCallback(new OneKeyShareCallback());
        // 去自定义不同平台的字段内容
        //oks.setShareContentCustomizeCallback(new ShareContentCustomizeDemo());
        // 在九宫格设置自定义的图标
      *//*  Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_mini);
        String label = "ShareSDK";
        View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View v) {

            }
        };
        oks.setCustomerLogo(logo, label, listener);*//*
        oks.show(context);
    }*/

  /*  @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mLlCopy:
                SystemUtils.copy(shareUrl, activity);
                break;
            case R.id.mLlSafari:
                Uri uri = Uri.parse(shareUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(intent);
                break;
            case R.id.mLlMail:
                SystemUtils.sendEmail(activity, TextUtils.isEmpty(shareTitle) ? shareUrl : shareTitle + " " + shareUrl, "");
                break;
            case R.id.mLlMessage:
                SystemUtils.sendSmsWithBody(activity, "", TextUtils.isEmpty(shareTitle) ? shareUrl : shareTitle + " " + shareUrl);
                break;
            case R.id.mLlTwitter:
                // UmengUtils.share(activity, SHARE_MEDIA.WEIXIN, shareTitle, shareUrl, shareUrl, shareImgUrl);
                break;
            case R.id.mLlGoogle:
                //  UmengUtils.share(activity, SHARE_MEDIA.WEIXIN_CIRCLE, shareTitle, shareUrl, shareUrl, shareImgUrl);
                break;
            case R.id.mLlWhatsapp:
                UmengUtils.share(activity, SHARE_MEDIA.WHATSAPP, shareTitle, shareUrl, shareUrl, shareImgUrl);
                break;
            case R.id.mLlFacebook:
                // UmengUtils.share(activity, SHARE_MEDIA.FACEBOOK, shareTitle, shareUrl);
                // UmengUtils.shareFacebookMessenger(activity, shareUrl, shareImgUrl);
                break;
            case R.id.mTvCancel:
                if (popWindow != null) {
                    popWindow.dismiss();
                }
                break;
        }

        if (popWindow != null) {
            popWindow.dismiss();
        }
    }
*/
  /*  public static void shareToGoogle(Context context){
        Intent shareIntent = new PlusShare.Builder(context)
                .setType("text/plain")
                .setText("Welcome to the Google+ platform.")
                .setContentUrl(Uri.parse("https://developers.google.com/+/"))
                .getIntent();

        startActivityForResult(shareIntent, 0);
    }*/
}
