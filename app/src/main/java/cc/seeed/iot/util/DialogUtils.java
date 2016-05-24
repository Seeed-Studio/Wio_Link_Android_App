package cc.seeed.iot.util;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Message;
import android.os.Process;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

import cc.seeed.iot.App;
import cc.seeed.iot.R;
import cc.seeed.iot.adapter.set_node.GroveI2cListRecyclerAdapter;
import cc.seeed.iot.ui_setnode.model.PinConfig;
import cc.seeed.iot.view.FontButton;
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

    public static Dialog showSelectServer(final Activity context,String defUrl, final ButtonClickListenter listenter) {
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


        // final String serverUrl = App.getSp().getString(Constant.SP_SERVER_URL, "");
        final String serverUrl;
        if (TextUtils.isEmpty(defUrl)){
            serverUrl = App.getApp().getOtaServerUrl();
        }else {
            serverUrl = defUrl;
        }
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
                    getIpAddress(context, dialog, url, mTvCustomServer, listenter);
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
        void okClick(String url, String ip);

        void cancelClick();
    }

    public static void getIpAddress(Activity context, final Dialog dialog, final String url, final EditText editText, final ButtonClickListenter listenter) {
        final Dialog progressDialog = showProgressDialog(context, context.getResources().getString(R.string.hint_get_host_address));
        NetworkUtils.getIpAddress(context, NetworkUtils.getDomainName(url), new NetworkUtils.OnIpCallback() {
            @Override
            public void okCallback(String ip) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }

                if (listenter != null) {
                    listenter.okClick(url, ip);
                }
                if (dialog != null) {
                    dialog.dismiss();
                }
            }

            @Override
            public void failCallback(String error) {
                if (progressDialog != null) {
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


    public static Dialog showEditOneRowDialog(Context context,String title, final ButtonEditClickListenter listenter) {

        final Dialog dialog = new Dialog(context, R.style.DialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_wifi_pwd, null);
        FontTextView mTvTitle = (FontTextView) view.findViewById(R.id.mTvTitle);
        final FontEditView mEtPwd = (FontEditView) view.findViewById(R.id.mEtPwd);
        FontTextView mTvCancel = (FontTextView) view.findViewById(R.id.mTvCancel);
        FontTextView mTvSubmit = (FontTextView) view.findViewById(R.id.mTvSubmit);

        if (!TextUtils.isEmpty(title)){
            mTvTitle.setText(title);
        }
        mTvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listenter != null) {
                    listenter.okClick(dialog,mEtPwd.getText().toString().trim());
                }
            }
        });

        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();
        dialog.setContentView(view);
        return dialog;
    }

    public interface ButtonEditClickListenter {
        void okClick(Dialog dialog,String content);
    }

    public static Dialog showEditNodeNameDialog(Context context, final String defaultName, final ButtonEditClickListenter listenter) {

        final Dialog dialog = new Dialog(context, R.style.DialogStyle);
        dialog.setCanceledOnTouchOutside(false);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_node_name, null);

        FontTextView mTvTitle = (FontTextView) view.findViewById(R.id.mTvTitle);
        FontTextView mTvHint = (FontTextView) view.findViewById(R.id.mTvHint);
        final FontEditView mEtName = (FontEditView) view.findViewById(R.id.mEtName);
        FontButton mTvSubmit = (FontButton) view.findViewById(R.id.mTvSubmit);

        mEtName.setHint(defaultName);
        mTvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listenter != null) {
                    String name = mEtName.getText().toString().trim();
                    if (TextUtils.isEmpty(name)){
                        name  = defaultName;
                    }
                    listenter.okClick(dialog,name);
                }
            }
        });

        dialog.show();
        dialog.setContentView(view);
        return dialog;
    }

    public static void showMenuPopWindow(Activity activity, View targetView, List<String> list, final OnMenuItemChickListener listener) {
        if (list == null) {
            return;
        }

        View view = LayoutInflater.from(activity).inflate(R.layout.popwindow_menu, null);
        final PopupWindow popWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popWindow.dismiss();
            }
        });

        LinearLayout mLLMenuContainer = (LinearLayout) view.findViewById(R.id.mLLMenuContainer);
        for (int i = 0; i < list.size(); i++) {
            View itemView = LayoutInflater.from(activity).inflate(R.layout.item_popwindow_menu, null);
            FontTextView mTVItem = (FontTextView) itemView.findViewById(R.id.mTVItem);
            View mDivider = itemView.findViewById(R.id.mDivider);

            mTVItem.setText(list.get(i));
            if (i == 0 && list.size() == 1){
                mDivider.setVisibility(View.GONE);
            }else if (i == list.size()-1){
                mDivider.setVisibility(View.GONE);
            }else {
                mDivider.setVisibility(View.VISIBLE);
            }

            final int finalI = i;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.chickItem(v, finalI);
                    }
                    popWindow.dismiss();
                }
            });
            mLLMenuContainer.addView(itemView);
        }

      //  mLLMenuContainer.setBackgroundResource(R.drawable.withe_shadow_bg);

        popWindow.setFocusable(true);
        popWindow.setOutsideTouchable(true);
        popWindow.setBackgroundDrawable(new BitmapDrawable());
        popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //   popWindow.showAtLocation(targetView, Gravity.NO_GRAVITY,0 ,targetView.getBottom());
        popWindow.showAsDropDown(targetView);
    }

    public interface OnMenuItemChickListener{
        void chickItem(View v,int position);
    }

    public static Dialog showErrorDialog(Context context,String title,String okName,String cancelName,String content, final OnErrorButtonClickListenter listenter){
        final Dialog dialog = new Dialog(context, R.style.DialogStyle);
        dialog.setCanceledOnTouchOutside(false);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_show_error, null);
        FontTextView mTvTitle = (FontTextView) view.findViewById(R.id.mTvTitle);
        FontTextView mTvDesc = (FontTextView) view.findViewById(R.id.mTvDesc);
        FontTextView mTvCancel = (FontTextView) view.findViewById(R.id.mTvCancel);
        FontTextView mTvSubmit = (FontTextView) view.findViewById(R.id.mTvSubmit);

        if (!TextUtils.isEmpty(title)){
            dialog.setTitle(title);
        }else {
            mTvTitle.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(okName)) {
            mTvSubmit.setText(okName);
        }else {
            mTvSubmit.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(cancelName)){
            mTvCancel.setText(cancelName);
        }else {
            mTvCancel.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(content)){
            mTvDesc.setText(content);
        }else {
            mTvDesc.setVisibility(View.GONE);
        }

        mTvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listenter != null) {
                    listenter.okClick();
                }
                dialog.dismiss();
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

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK ) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        dialog.show();
        dialog.setContentView(view);
        return dialog;
    }

    public interface OnErrorButtonClickListenter {
        void okClick();
        void cancelClick();
    }

    public static Dialog showRemoveGroveDialog(Context context, List<PinConfig> pinConfigs, final OnItemRemoveClickListenter listenter) {

        final Dialog dialog = new Dialog(context, R.style.DialogStyle);
        //  dialog.setCanceledOnTouchOutside(true);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_remove_grove, null);

        RecyclerView mRvGrove = (RecyclerView) view.findViewById(R.id.mRvGrove);

        if (mRvGrove != null) {
            mRvGrove.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRvGrove.setLayoutManager(layoutManager);
            GroveI2cListRecyclerAdapter mGroveI2cListAdapter = new GroveI2cListRecyclerAdapter(pinConfigs);
            mGroveI2cListAdapter.setOnRemoveItemListener(new GroveI2cListRecyclerAdapter.OnRemoveItemListener() {
                @Override
                public void onRemoveItem(PinConfig pinConfig, int position, int totalPin) {
                    if (listenter != null){
                        listenter.onRemoveItem(dialog,pinConfig, position, totalPin);
                    }
                }
            });
            mRvGrove.setAdapter(mGroveI2cListAdapter);
        }
        dialog.show();
        dialog.setContentView(view);
        return dialog;
    }

    public interface OnItemRemoveClickListenter {
        void onRemoveItem( Dialog dialog,PinConfig pinConfig, int position, int totalPin);
    }
}
