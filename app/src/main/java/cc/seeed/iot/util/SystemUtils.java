package cc.seeed.iot.util;

import android.content.ClipboardManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;


import com.facebook.internal.PermissionType;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Enumeration;

import cc.seeed.iot.App;

/**
 * Created by seeed on 2016/3/8.
 */
public class SystemUtils {

    public static PackageInfo getPackageInfo() {
        PackageManager manager;
        PackageInfo info = null;
        manager = App.getApp().getPackageManager();
        try {
            info = manager.getPackageInfo(App.getApp().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }

    /**
     * 调用系统界面，给指定的号码发送短信，并附带短信内容
     *
     * @param context
     * @param number
     * @param body
     */
    public static void sendSmsWithBody(Context context, String number, String body) {
        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
        sendIntent.setData(Uri.parse("smsto:" + number));
        sendIntent.putExtra("sms_body", body);
        context.startActivity(sendIntent);
    }

    public static void sendEmail(Context context, String body, String address) {
       /* if (address == null) {
            Intent email = new Intent(Intent.ACTION_SEND);
            //  intent.putExtra(Intent.EXTRA_SUBJECT, filename);
            //intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filename)); ;
            email.setType("plain/text");
            //设置发送的内容
            email.putExtra(android.content.Intent.EXTRA_TEXT, body);
            context.startActivity(Intent.createChooser(email, "EMail"));
        } else {*/
        // 必须明确使用mailto前缀来修饰邮件地址,如果使用
        Uri uri = Uri.parse("mailto:" + address);
        String[] email = {address};
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
     //   intent.putExtra(Intent.EXTRA_CC, email); // 抄送人
        //     intent.putExtra(Intent.EXTRA_SUBJECT, "这是邮件的主题部分"); // 主题
        intent.putExtra(Intent.EXTRA_TEXT, body); // 正文
        context.startActivity(Intent.createChooser(intent, "Email"));
    }

    /**
     * 实现文本复制功能
     * add by wangqianzhou
     *
     * @param content
     */
    public static void copy(String content, Context context) {
        App.showToastShrot("URL Copied");
// 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }

    /**
     * 实现粘贴功能
     * add by wangqianzhou
     *
     * @param context
     * @return
     */
    public static String paste(Context context) {
// 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        return cmb.getText().toString().trim();
    }

    private static final String TAG = "Contacts";

    private static void insertDummyContact(Context context) {
        // Two operations are needed to insert a new contact.
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(2);

        // First, set up a new raw contact.
        ContentProviderOperation.Builder op =
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null);
        operations.add(op.build());

        // Next, set the name for the contact.
        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        "__DUMMY CONTACT from runtime permissions sample");
        operations.add(op.build());

        // Apply the operations.
        ContentResolver resolver = context.getContentResolver();
        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (RemoteException e) {
            Log.d(TAG, "Could not add a new contact: " + e.getMessage());
        } catch (OperationApplicationException e) {
            Log.d(TAG, "Could not add a new contact: " + e.getMessage());
        }
    }

    public static boolean checkPermission(String per){
        PackageManager pm = App.getApp().getPackageManager();
        return  (PackageManager.PERMISSION_GRANTED ==pm.checkPermission(per,getPackageInfo().packageName));

    }

}
