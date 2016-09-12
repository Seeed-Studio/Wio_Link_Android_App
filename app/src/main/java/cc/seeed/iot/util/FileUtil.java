package cc.seeed.iot.util;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

import cc.seeed.iot.logic.CmdConst;
import cc.seeed.iot.mgr.UiObserverManager;
import cz.msebera.android.httpclient.Header;

/**
 * Created by seeed on 2016/3/22.
 */
public class FileUtil {

   /* public static void downFile(String downPath, String savePath) {
        FinalHttp fh = new FinalHttp();
        //调用download方法开始下载
        HttpHandler handler = fh.download(downPath, //这里是下载的路径
                savePath, //这是保存到本地的路径
                true,//true:断点续传 false:不断点续传（全新下载）
                new AjaxCallBack<File>() {
                    @Override
                    public void onSuccess(File file) {
                        // super.onSuccess(file);
                        App.showToast("" + file.getName());
                    }

                    @Override
                    public void onFailure(Throwable t, int errorNo, String strMsg) {
                        // super.onFailure(t, errorNo, strMsg);
                    }
                });


        //调用stop()方法停止下载
        //  handler.stop();
    }*/

    public static void uploadOneFile(String serverUrl, String filePath) {
        RequestParams params = new RequestParams();
        AsyncHttpClient client = new AsyncHttpClient();
        try {
            params.put("file", new File(filePath)); // Upload a File
            client.post(serverUrl, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String s = new String(responseBody);
                    Log.d("TAG", "onSuccess: " + s);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.d("TAG", "onFailure");
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    super.onProgress(bytesWritten, totalSize);
                    Log.d("TAG", "onProgress: " + bytesWritten / (float) totalSize * 100 + "%");
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static final String TAG = "TAG";
    private static final int TIME_OUT = 10 * 1000;   //超时时间
    private static final String CHARSET = "utf-8"; //设置编码
    private static DataOutputStream dos;

    public static void uploadFile(String cmd, final String filePath, final String RequestURL) {

        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            File file = new File(filePath);
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);  //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false);  //不允许使用缓存
            conn.setRequestMethod("POST");  //请求方式
            conn.setRequestProperty("Charset", CHARSET);  //设置编码
            //  conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-TYPE", "multipart/form-data;");
            conn.setRequestProperty("Content-Length", "" + file.length());

            // fileparams.put("menu_item_bg",file);
            if (file != null) {
                dos = new DataOutputStream(conn.getOutputStream());

                InputStream is = new FileInputStream(file);
//                writeFileParams();
                byte[] bytes = new byte[3096];
                int len = 0;
                while ((len = is.read(bytes)) != -1) {
                    Log.e(TAG, "progress: " + len);
                //    UiObserverManager.getInstance().dispatchEvent(CmdConst.UpdateProgress, true, "" + len, new String[]{});
                    dos.write(bytes, 0, len);
                }
                is.close();
                dos.flush();
                int res = conn.getResponseCode();
                Log.e(TAG, "response code:" + res);
                if (res == 200) {
                    Log.e(TAG, "request success");
                    InputStream input = conn.getInputStream();
                    StringBuffer sb1 = new StringBuffer();
                    int ss;
                    while ((ss = input.read()) != -1) {
                        sb1.append((char) ss);
                    }
                    Log.e(TAG, "result : " + sb1.toString());
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = new JSONObject(sb1.toString());
                        String data = jsonObj.getString("data");
                        jsonObj = new JSONObject(data);
                        String path = jsonObj.getString("path");
                        UiObserverManager.getInstance().dispatchEvent(cmd, true, "Update success", new String[]{path});
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e(TAG, "request error");
                    UiObserverManager.getInstance().dispatchEvent(cmd, false, "Update fail", null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            UiObserverManager.getInstance().dispatchEvent(cmd, false, "Update fail", null);
        }
    }

   /* public static void uploadFile(final String filePath, UpdateListener listener) {
        final String RequestURL = ConstantUrl.Update_Img_Url.getVal();
        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            File file = new File(filePath);
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);  //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false);  //不允许使用缓存
            conn.setRequestMethod("POST");  //请求方式
            conn.setRequestProperty("Charset", CHARSET);  //设置编码
            //  conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-TYPE", "multipart/form-data;");
            conn.setRequestProperty("Content-Length", "" + file.length());

            // fileparams.put("menu_item_bg",file);
            if (file != null) {
                dos = new DataOutputStream(conn.getOutputStream());

                InputStream is = new FileInputStream(file);
//                writeFileParams();
                int progress = 0;
                byte[] bytes = new byte[1024 * 5];
                int len = 0;
                long startTime = System.currentTimeMillis();
                while ((len = is.read(bytes)) != -1) {
                    //   Log.e(TAG, "progress: " + len);
                    progress += len;
                    dos.write(bytes, 0, len);
                    if (listener != null) {
                        listener.onProgress(progress, file.length());
                    }
                }
                is.close();
                dos.flush();
                long endTime = System.currentTimeMillis();
                //未来视觉体验,这里对上传的文件做时间补偿
                if (endTime - startTime < 1000){
                    Thread.sleep(1000 - (endTime -startTime));
                }
                int res = conn.getResponseCode();
                //      Log.e(TAG, "response code:" + res);
                if (res == 200) {
                    //      Log.e(TAG, "request success");
                    InputStream input = conn.getInputStream();
                    StringBuffer sb1 = new StringBuffer();
                    int ss;
                    while ((ss = input.read()) != -1) {
                        sb1.append((char) ss);
                    }
                    //       Log.e(TAG, "result : " + sb1.toString());
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = new JSONObject(sb1.toString());
                        String data = jsonObj.getString("data");
                        jsonObj = new JSONObject(data);
                        String path = jsonObj.getString("path");
                        if (listener != null) {
                            listener.onSuccess(path);
                        }
                    } catch (JSONException e) {
                        if (listener != null) {
                            listener.onFail(e.toString());
                        }
                        e.printStackTrace();
                    }

                } else {
                    if (listener != null) {
                        listener.onFail("request error");
                    }
                    Log.e(TAG, "request error");
                }
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onFail(e.toString());
            }
            e.printStackTrace();
        }
    }
*/
    public interface UpdateListener {
        void onSuccess(String serverPath);

        void onFail(String errInfo);

        void onProgress(long progress, long total);
    }
/*

    public static void updateMoreFile(final String cmd, final List<String> path) {
        MyThreadPool threadPool = new MyThreadPool();
        for (int i = 0; i < path.size(); i++) {
            final int finalI = i;
            threadPool.submit(new Runnable() {
                @Override
                public void run() {
                    uploadFile(cmd, path.get(finalI), ConstantUrl.Update_Img_Url.getVal());
//                    uploadFile(path.get(finalI));
                }
            });
        }
    }
*/

    public static String fileMD5(String inputFile) {
        // 缓冲区大小（这个可以抽出一个参数）
        int bufferSize = 256 * 1024;
        FileInputStream fileInputStream = null;
        DigestInputStream digestInputStream = null;
        try {
            // 拿到一个MD5转换器（同样，这里可以换成SHA1）
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            // 使用DigestInputStream
            fileInputStream = new FileInputStream(inputFile);
            digestInputStream = new DigestInputStream(fileInputStream, messageDigest);
            // read的过程中进行MD5处理，直到读完文件
            byte[] buffer = new byte[bufferSize];
            while (digestInputStream.read(buffer) > 0) ;
            // 获取最终的MessageDigest
            messageDigest = digestInputStream.getMessageDigest();
            // 拿到结果，也是字节数组，包含16个元素
            byte[] resultByteArray = messageDigest.digest();
            // 同样，把字节数组转换成字符串
            return bytesToHex(resultByteArray);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                digestInputStream.close();
            } catch (Exception e) {
            }
            try {
                fileInputStream.close();
            } catch (Exception e) {
            }
        }
    }

    final protected static char[] hexArray = "0123456789abcdef".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String saveImage(Intent data) {

        Bundle bundle = data.getExtras();
        Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
        FileOutputStream b = null;
        File file = new File("/sdcard/seeed/img");
        if (!file.exists()) {
            file.mkdirs();// 创建文件夹
        }
        //生成随机数，命名图片
        String uuid = UUID.randomUUID().toString();

        String picName = uuid + ".jpg";
        String fileName = "/sdcard/seeed/img/" + picName;

        try {
            b = new FileOutputStream(fileName);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
            return fileName;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fileName = "";
        } finally {
            try {
                b.flush();
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
                fileName = "";
            }
        }

        return fileName;
    }

}
