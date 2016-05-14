package cc.seeed.iot.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Process;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

import butterknife.InjectView;
import butterknife.OnClick;
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

    public static Dialog showSelectServer(final Context context, final ButtonClickListenter listenter) {
        final Dialog dialog = new Dialog(context, R.style.DialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_select_server, null);
        FontTextView mTvTitle = (FontTextView) view.findViewById(R.id.mTvTitle);
        FontTextView mTvDesc = (FontTextView) view.findViewById(R.id.mTvDesc);
        final RadioButton mRbDefaultServer = (RadioButton) view.findViewById(R.id.mRbDefaultServer);
        FontTextView mTvDefaultServer = (FontTextView) view.findViewById(R.id.mTvDefaultServer);
        final RadioButton mRbCustomServer = (RadioButton) view.findViewById(R.id.mRbCustomServer);
        final FontEditView mTvCustomServer = (FontEditView) view.findViewById(R.id.mTvCustomServer);
        FontTextView mTvCancel = (FontTextView) view.findViewById(R.id.mTvCancel);
        FontTextView mTvSubmit = (FontTextView) view.findViewById(R.id.mTvSubmit);

        final String serverUrl = App.getSp().getString(Constant.SERVER_URL, "");
        if (CommonUrl.OTA_SERVER_URL.equals(serverUrl)){
          //  mRbDefaultServer.setSelected(true);
          //  mRbCustomServer.setSelected(false);
            mRbDefaultServer.setChecked(true);
            mRbCustomServer.setChecked(false);
            mTvCustomServer.setEnabled(false);
        }else {
          //  mRbDefaultServer.setSelected(false);
            mRbDefaultServer.setChecked(false);
            mRbCustomServer.setChecked(true);
           // mRbCustomServer.setSelected(true);
            mTvCustomServer.setEnabled(true);
            mTvCustomServer.setText(serverUrl);
            mTvCustomServer.setSelection(serverUrl.length());
        }

        mRbDefaultServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //    mRbDefaultServer.setSelected(true);
            //    mRbCustomServer.setSelected(false);
                mRbCustomServer.setChecked(false);
                mTvCustomServer.setEnabled(false);
            }
        });
        mRbCustomServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
         //       mRbDefaultServer.setSelected(false);
                mRbDefaultServer.setChecked(false);
          //      mRbCustomServer.setSelected(true);
                mTvCustomServer.setEnabled(true);
            }
        });
        mTvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRbCustomServer.isChecked()){
                    String url = mTvCustomServer.getText().toString().trim();
                    if (!RegularUtils.isWebsite(url)){
                      //  App.showToastShrot(context.getString(R.string.website_format_error));
                        mTvCustomServer.setError("e.g: https://wio.seeed.io or https://192.168.31.2");
                        return;
                    }
                    if (listenter != null){
                        listenter.okClick(url);
                    }
                }else {
                    if (listenter != null){
                        listenter.okClick(serverUrl);
                    }
                    dialog.dismiss();
                }
            }
        });

        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listenter != null){
                    listenter.cancelClick();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.setContentView(view);
        return dialog;
    }

    public interface ButtonClickListenter{
        void okClick(String url);
        void cancelClick();
    }
}
