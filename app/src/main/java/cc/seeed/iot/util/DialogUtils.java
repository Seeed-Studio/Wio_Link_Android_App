package cc.seeed.iot.util;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Process;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.view.FontEditView;
import cc.seeed.iot.view.FontTextView;

/**
 * author: Jerry on 2016/5/13 17:10.
 * description:
 */
public class DialogUtils {

    /**
     * 退出提示
     *
     * @param context
     */
    public static void showQuitDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getResources().getString(R.string.confirm_quit));
        builder.setNeutralButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Process.killProcess(Process.myPid());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    public static Dialog showSelectServer(final Activity context, final ButtonClickListenter listenter) {
        final Dialog dialog = new Dialog(context, R.style.DialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_select_server, null);
        FontTextView mTvTitle = (FontTextView) view.findViewById(R.id.mTvTitle);
        FontTextView mTvDesc = (FontTextView) view.findViewById(R.id.mTvDesc);
        final RadioButton mRbDefaultServer = (RadioButton) view.findViewById(R.id.mRbDefaultServer);
        FontTextView mTvDefaultServer = (FontTextView) view.findViewById(R.id.mTvDefaultServer);
        final RadioButton mRbCustomServer = (RadioButton) view.findViewById(R.id.mRbCustomServer);
        final FontEditView mTvCustomServer = (FontEditView) view.findViewById(R.id.mTvCustomServer);
        final View mCustomServer = (View) view.findViewById(R.id.mCustomServer);
        FontTextView mTvCancel = (FontTextView) view.findViewById(R.id.mTvCancel);
        FontTextView mTvSubmit = (FontTextView) view.findViewById(R.id.mTvSubmit);

        final String serverUrl = App.getSp().getString(Constant.SP_SERVER_URL, "");
        if (CommonUrl.OTA_SERVER_URL.equals(serverUrl)) {
            mRbDefaultServer.setChecked(true);
            mRbCustomServer.setChecked(false);
            mTvCustomServer.setEnabled(false);
        } else {
            mRbDefaultServer.setChecked(false);
            mRbCustomServer.setChecked(true);
            mTvCustomServer.setEnabled(true);
            mTvCustomServer.setText(serverUrl);
            mTvCustomServer.setSelection(serverUrl.length());
        }
        mTvDefaultServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRbCustomServer.setChecked(false);
                mRbDefaultServer.setChecked(true);
                mTvCustomServer.setEnabled(false);
                mCustomServer.setVisibility(View.VISIBLE);
            }
        });

        mCustomServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCustomServer.setVisibility(View.GONE);
                mRbDefaultServer.setChecked(false);
                mRbCustomServer.setChecked(true);
                mTvCustomServer.setEnabled(true);
            }
        });

        mRbDefaultServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCustomServer.setVisibility(View.VISIBLE);
                mRbCustomServer.setChecked(false);
                mTvCustomServer.setEnabled(false);
            }
        });
        mRbCustomServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCustomServer.setVisibility(View.GONE);
                mRbDefaultServer.setChecked(false);
                mTvCustomServer.setEnabled(true);
            }
        });

        mTvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRbCustomServer.isChecked()) {
                    String url = mTvCustomServer.getText().toString().trim();
                    if (!RegularUtils.isWebsite(url)) {
                        //  App.showToastShrot(context.getString(R.string.website_format_error));
                        mTvCustomServer.setError(context.getResources().getString(R.string.website_format_hint));
                        return;
                    }
                    getIpAddress(context,dialog, url, mTvCustomServer, listenter);
                } else {
                    getIpAddress(context, dialog, CommonUrl.OTA_SERVER_URL, mTvCustomServer, listenter);
                }
            }
        });

        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listenter != null) {
                    listenter.cancelClick();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.setContentView(view);
        return dialog;
    }

    public interface ButtonClickListenter {
        void okClick(String url,String ip);

        void cancelClick();
    }

    public static void getIpAddress(Activity context, final Dialog dialog, final String url, final EditText editText, final ButtonClickListenter listenter) {
        final Dialog progressDialog = showProgressDialog(context, context.getResources().getString(R.string.hint_get_host_address));
        NetworkUtils.getIpAddress(context, NetworkUtils.getDomainName(url), new NetworkUtils.OnIpCallback() {
            @Override
            public void okCallback(String ip) {
                if (progressDialog != null){
                    progressDialog.dismiss();
                }
                if (listenter != null) {
                    App.getApp().saveUrlAndIp(url,ip);
                    listenter.okClick(url,ip);
                }
                if (dialog != null) {
                    dialog.dismiss();
                }
            }

            @Override
            public void failCallback(String error) {
                if (progressDialog != null){
                    progressDialog.dismiss();
                }
                if (editText != null) {
                    editText.setError(error);
                }
            }
        });
    }

    public static Dialog showProgressDialog(Context context, String str) {
        ProgressDialog progressDialog = new ProgressDialog(context, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(str);
        progressDialog.show();
        return progressDialog;
    }
}
