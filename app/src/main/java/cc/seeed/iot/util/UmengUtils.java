package cc.seeed.iot.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.MessageDialog;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;

import java.util.Map;

import cc.seeed.iot.App;
import cc.seeed.iot.R;

/**
 * Created by seeed on 2016/3/10.
 */
public class UmengUtils {
    static ShareAction shareAction;
    static Activity activity;
    static UMShareAPI mShareAPI;

    public static void initUmengShare() {

        //微信 appid appsecret
        //  PlatformConfig.setWeixin("wx43881f3372d0a832", "f0a59af4fa6b7381af44ffb45bbe62f3");
        PlatformConfig.setTwitter("W7dkMSuiGFzCrfETuri3giEov", "Wp93a4FFrCFlzhMiwXBy7QKX9KjhgCbhgrHEz8QCsqGe4pg5Sv");
//         PlatformConfig.setTwitter("739753776953712640", "koX7vp9UBAbekl2xPBEhHy5aYdaERxdgHtPQgdHfLA8tk");
        //Twitter appid appkey
    }

    public static UMShareAPI getUMShareAPI() {
        if (mShareAPI == null) {
            mShareAPI = UMShareAPI.get(activity);
        }

        return mShareAPI;
    }

    public static void share(Activity activity, SHARE_MEDIA share_media, String title, String content, String url, String imgUrl) {
        if (mShareAPI == null)
            mShareAPI = UMShareAPI.get(activity);
        UMImage image = null;
        if (!mShareAPI.isInstall(activity, share_media)) {
            if (share_media == SHARE_MEDIA.WHATSAPP && ToolUtil.isInstallByread("com.whatsapp")) {

            }else  if (share_media == SHARE_MEDIA.GOOGLEPLUS && ToolUtil.isInstallByread("com.google.android.apps.plus")) {

            }else {
                App.showToastShrot("App not installed");
                return;
            }
        }

        if (ToolUtil.isNetworkAvailable() && imgUrl != null) {
            image = new UMImage(activity, imgUrl);
        } else {
            image = new UMImage(activity, BitmapFactory.decodeResource(activity.getResources(), R.drawable.logo));
        }

        if (share_media.equals(SHARE_MEDIA.WEIXIN_CIRCLE)) {
            new ShareAction(activity)
                    .setPlatform(share_media)
                    .setCallback(umShareListener)
                    .withText(title + " " + content)
                    .withTargetUrl(url)
                    .withTitle(title + " " + content)
                    .withMedia(image)
                    .share();
        } else {
            new ShareAction(activity)
                    .setPlatform(share_media)
                    .setCallback(umShareListener)
                    .withText(content)
                    .withTitle(title)
                    .withTargetUrl(url)
                            //  .withMedia(image)
                    .share();
        }

    }

    public static void shareToTwitter(Activity activity, String content) {
        new ShareAction(activity).setPlatform(SHARE_MEDIA.TWITTER).setCallback(umShareListener)
                .withText(content)
                .share();
     /*   new ShareAction(activity).setDisplayList(SHARE_MEDIA.TWITTER, SHARE_MEDIA.FACEBOOK, SHARE_MEDIA.GOOGLEPLUS)
                .withText("来自友盟分享面板")
                .setCallback(umShareListener)
                .open();*/
    }

    public static void shareFacebookMessenger(Activity activity, String url, String imgUrl) {
       /* Uri imgUri = new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                .path(String.valueOf(R.mipmap.logo))
                .build();*/

        Uri imgUri = Uri.parse(imgUrl);

        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(url))
                .setContentDescription(url)
                .setContentTitle("MarkerMap")
                        // .setImageUrl(imgUri)
                .build();
        MessageDialog.show(activity, content);
    }

    static UMShareListener  umShareListener = new UMShareListener() {
        @Override
        public void onResult(SHARE_MEDIA platform) {
            // Toast.makeText(ShareActivity.this,platform + " 分享成功啦", Toast.LENGTH_SHORT).show();
            App.showToastShrot("Success");
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            //  Toast.makeText(ShareActivity.this,platform + " 分享失败啦", Toast.LENGTH_SHORT).show();
            App.showToastShrot("Fail");
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            //  Toast.makeText(ShareActivity.this,platform + " 分享取消了", Toast.LENGTH_SHORT).show();
            App.showToastShrot("Cancel");
        }
    };

    public static void login(Activity activity) {
        mShareAPI = UMShareAPI.get(activity);
        SHARE_MEDIA platform = SHARE_MEDIA.WEIXIN;
        mShareAPI.doOauthVerify(activity, platform, umAuthListener);
    }

    private static UMAuthListener umAuthListener = new UMAuthListener() {
        @Override
        public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
            App.showToastShrot("Authorize succeed");
        }

        @Override
        public void onError(SHARE_MEDIA platform, int action, Throwable t) {
            App.showToastShrot("Authorize fail");
        }

        @Override
        public void onCancel(SHARE_MEDIA platform, int action) {
            App.showToastShrot("Authorize cancel");
        }
    };
}
