package cc.seeed.iot.util;

import android.app.Activity;
import android.content.Context;
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

import cc.seeed.iot.R;
import cc.seeed.iot.view.FontTextView;

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
                //    UmengUtils.share(activity, SHARE_MEDIA.WHATSAPP, shareTitle, shareUrl, shareUrl, shareImgUrl);
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
    };

  /*  public static void shareToGoogle(Context context){
        Intent shareIntent = new PlusShare.Builder(context)
                .setType("text/plain")
                .setText("Welcome to the Google+ platform.")
                .setContentUrl(Uri.parse("https://developers.google.com/+/"))
                .getIntent();

        startActivityForResult(shareIntent, 0);
    }*/
}
