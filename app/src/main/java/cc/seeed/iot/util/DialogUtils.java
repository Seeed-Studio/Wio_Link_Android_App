package cc.seeed.iot.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.os.Process;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
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
    private static void hideKeyboard(Context context, View view) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static Dialog showMsgInfoDialogOutsideCheck(Context context, String title, String msg, String okName, final View.OnClickListener listener) {
        final Dialog dialog = new Dialog(context, R.style.DialogStyle);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_err, null);

        TextView mTvTitle = (TextView) view.findViewById(R.id.tv_title);
        TextView mTVOk = (TextView) view.findViewById(R.id.tv_ok);
        TextView mTVMsg = (TextView) view.findViewById(R.id.tv_msg);

        if (TextUtils.isEmpty(title)){
            mTvTitle.setVisibility(View.GONE);
        }else {
            mTvTitle.setVisibility(View.VISIBLE);
        }

        if (okName != null) {
            mTVOk.setText(okName);
        }
        mTVMsg.setText(msg);
        mTVOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onClick(v);
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.setContentView(view);
        return dialog;
    }


    /**
     * 退出提示
     *
     * @param context
     */
    public static void showQuitDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getResources().getString(R.string.confirm_quit));
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Process.killProcess(Process.myPid());
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    static boolean isShow = false;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Dialog showSelectServer(final Activity context, String serverUrl, final ButtonClickListenter listenter) {

        final Dialog dialog = new Dialog(context, R.style.DialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_select_server, null);
        FontTextView mTvTitle = (FontTextView) view.findViewById(R.id.mTvTitle);
        FontTextView mTvDesc = (FontTextView) view.findViewById(R.id.mTvDesc);
        final RadioButton mRbDefaultServer = (RadioButton) view.findViewById(R.id.mRbDefaultServer);
        final FontTextView mTvDefaultServer = (FontTextView) view.findViewById(R.id.mTvDefaultServer);
        final RadioButton mRbCustomServer = (RadioButton) view.findViewById(R.id.mRbCustomServer);
        final FontEditView mEtCustomServer = (FontEditView) view.findViewById(R.id.mEtCustomServer);
        final View mCustomServer = (View) view.findViewById(R.id.mCustomServer);
        final RelativeLayout mRlHelp = (RelativeLayout) view.findViewById(R.id.mRlHelp);
        FontTextView mTvCancel = (FontTextView) view.findViewById(R.id.mTvCancel);
        FontTextView mTvSubmit = (FontTextView) view.findViewById(R.id.mTvSubmit);

        if (serverUrl == null) {
            serverUrl = "";
        }
        // final String serverUrl = App.getSp().getString(Constant.SP_SERVER_URL, "");
        if (CommonUrl.OTA_SERVER_URL.equals(serverUrl)) {
            mRbDefaultServer.setChecked(true);
            mRbCustomServer.setChecked(false);
            mEtCustomServer.setEnabled(false);
            mRlHelp.setVisibility(View.GONE);
        } else {
            mRbDefaultServer.setChecked(false);
            mRbCustomServer.setChecked(true);
            mEtCustomServer.setEnabled(true);
            mEtCustomServer.setText(serverUrl);
            mEtCustomServer.setSelection(serverUrl.length());
            mRlHelp.setVisibility(View.VISIBLE);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShow = false;
                mEtCustomServer.setError(null);
            }
        });

        mRlHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShow) {
                    mEtCustomServer.setError(null);
                } else {
                    mEtCustomServer.setError(context.getResources().getString(R.string.website_format_hint), context.getDrawable(R.mipmap.edit_help));
                }
                isShow = !isShow;
            }
        });
        mTvDefaultServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRbCustomServer.setChecked(false);
                mRbDefaultServer.setChecked(true);
                mEtCustomServer.setEnabled(false);
                mCustomServer.setVisibility(View.VISIBLE);
                mRlHelp.setVisibility(View.GONE);
                mEtCustomServer.setError(null);
            }
        });

        mCustomServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCustomServer.setVisibility(View.GONE);
                mRbDefaultServer.setChecked(false);
                mRbCustomServer.setChecked(true);
                mEtCustomServer.setEnabled(true);
                mRlHelp.setVisibility(View.VISIBLE);
            }
        });

        mRbDefaultServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCustomServer.setVisibility(View.VISIBLE);
                mRbCustomServer.setChecked(false);
                mEtCustomServer.setEnabled(false);
                mRlHelp.setVisibility(View.GONE);
                mEtCustomServer.setError(null);
            }
        });
        mRbCustomServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCustomServer.setVisibility(View.GONE);
                mRbDefaultServer.setChecked(false);
                mEtCustomServer.setEnabled(true);
                mRlHelp.setVisibility(View.VISIBLE);
            }
        });

        mTvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRbCustomServer.isChecked()) {
                    String url = mEtCustomServer.getText().toString().trim();
                    if (!RegularUtils.isWebsite(url)) {
                        //  App.showToastShrot(context.getString(R.string.website_format_error));
                        mEtCustomServer.setError(context.getResources().getString(R.string.website_format_hint), context.getDrawable(R.mipmap.edit_help));
                        return;
                    }
                    getIpAddress(context, dialog, url, mEtCustomServer, listenter);
                } else {
                    getIpAddress(context, dialog, CommonUrl.OTA_SERVER_URL, mEtCustomServer, listenter);
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

    public static void getIpAddress(final Activity context, final Dialog dialog, final String url, final EditText editText, final ButtonClickListenter listenter) {
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

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void failCallback(String error) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                if (editText != null) {
                    editText.setError(error, context.getDrawable(R.mipmap.edit_help));
                }
            }
        });
    }

    public static ProgressDialog showProgressDialog(Context context, String str) {
        ProgressDialog progressDialog = new ProgressDialog(context, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(str);
        progressDialog.show();
        return progressDialog;
    }


    public static Dialog showEditOneRowDialog(final Context context, String title,String content, final ButtonEditClickListenter listenter) {

        final Dialog dialog = new Dialog(context, R.style.DialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_wifi_pwd, null);
        FontTextView mTvTitle = (FontTextView) view.findViewById(R.id.mTvTitle);
        final FontEditView mEtPwd = (FontEditView) view.findViewById(R.id.mEtPwd);
        FontTextView mTvCancel = (FontTextView) view.findViewById(R.id.mTvCancel);
        FontTextView mTvSubmit = (FontTextView) view.findViewById(R.id.mTvSubmit);

        if (!TextUtils.isEmpty(title)) {
            mTvTitle.setText(title);
        }else {
            mTvTitle.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(content)){
            mEtPwd.setText(content);
            mEtPwd.setSelection(content.length());
        }
        mTvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listenter != null) {
                    listenter.okClick(dialog, mEtPwd.getText().toString().trim());
                    hideKeyboard(context, mEtPwd);
                }
            }
        });

        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                hideKeyboard(context, mEtPwd);
            }
        });


        dialog.show();
        dialog.setContentView(view);
        return dialog;
    }

    public interface ButtonEditClickListenter {
        void okClick(Dialog dialog, String content);
    }

    public static Dialog showEditNodeNameDialog(Context context, final String defaultName, final ButtonEditClickListenter listenter) {

        final Dialog dialog = new Dialog(context, R.style.DialogStyle);
        dialog.setCancelable(false);
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
                    if (TextUtils.isEmpty(name)) {
                        name = defaultName;
                    }
                    listenter.okClick(dialog, name);
                }
            }
        });

        dialog.show();
        dialog.setContentView(view);
        return dialog;
    }

    public static Dialog showWarningDialog(Context context,String content, final View.OnClickListener listener) {

        final Dialog dialog = new Dialog(context,R.style.DialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_warning, null);

        FontTextView mTvHint = (FontTextView) view.findViewById(R.id.mTvHint);
        FontButton mTvSubmit = (FontButton) view.findViewById(R.id.mTvSubmit);
        mTvHint.setText(Html.fromHtml(content));

        mTvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null){
                    listener.onClick(v);
                }
                dialog.dismiss();
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
            if (i == 0 && list.size() == 1) {
                mDivider.setVisibility(View.GONE);
            } else if (i == list.size() - 1) {
                mDivider.setVisibility(View.GONE);
            } else {
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

    public interface OnMenuItemChickListener {
        void chickItem(View v, int position);
    }

    public static Dialog showErrorDialog(Context context, String title, String okName, String cancelName, String content, final OnErrorButtonClickListenter listenter) {
        final Dialog dialog = new Dialog(context, R.style.DialogStyle);
        dialog.setCanceledOnTouchOutside(false);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_show_error, null);
        FontTextView mTvTitle = (FontTextView) view.findViewById(R.id.mTvTitle);
        FontTextView mTvDesc = (FontTextView) view.findViewById(R.id.mTvDesc);
        FontTextView mTvCancel = (FontTextView) view.findViewById(R.id.mTvCancel);
        FontTextView mTvSubmit = (FontTextView) view.findViewById(R.id.mTvSubmit);

        if (!TextUtils.isEmpty(title)) {
            dialog.setTitle(title);
        } else {
            mTvTitle.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(okName)) {
            mTvSubmit.setText(okName);
        } else {
            mTvSubmit.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(cancelName)) {
            mTvCancel.setText(cancelName);
        } else {
            mTvCancel.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(content)) {
            mTvDesc.setText(content);
        } else {
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
                if (keyCode == KeyEvent.KEYCODE_BACK) {
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
                    if (listenter != null) {
                        listenter.onRemoveItem(dialog, pinConfig, position, totalPin);
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
        void onRemoveItem(Dialog dialog, PinConfig pinConfig, int position, int totalPin);
    }

    /**
     * 分享功能
     * @param context       上下文
     * @param activityTitle Activity的名字
     * @param msgTitle      消息标题
     * @param msgText       消息内容
     */
    public static void showShare(Context context, String activityTitle, String msgTitle, String msgText) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain"); // 纯文本
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, activityTitle));
    }
}
