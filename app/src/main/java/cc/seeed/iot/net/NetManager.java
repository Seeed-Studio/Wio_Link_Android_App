package cc.seeed.iot.net;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cc.seeed.iot.logic.BaseLogic;
import cc.seeed.iot.mgr.ThreadManager;
import cc.seeed.iot.util.Common;
import cc.seeed.iot.util.CommonUrl;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;

/**
 * Created by seeed on 2015/12/28.
 */
public class NetManager {

    static NetManager netManager;
    int NetTimeout = 1000 * 15;

    public static NetManager getInstance() {
        if (netManager == null) {
            synchronized (NetManager.class) {
                if (netManager == null) {
                    netManager = new NetManager();
                }
            }
        }
        return netManager;
    }

    public void getRequest(String url, String cmd, final INetCallback callback) {
        final Request request = new Request();
        request.callback = callback;
        request.cmd = cmd;
        request.url = url;
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(NetTimeout);
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String s = new String(responseBody);
                final Packet resp = new Packet();
                resp.code = statusCode;
                resp.data = s;
                resp.status = true;

                if (callback != null) {
                    if (callback instanceof INetUiThreadCallBack) {
                        ThreadManager.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResp(request, resp);
                            }
                        }, 0);
                    } else {
                        callback.onResp(request, resp);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace(System.out);
                //   String s = new String(responseBody);
                final Packet resp = new Packet();
                resp.code = statusCode;
                resp.data = "";
                resp.errorMsg = error.toString();
                resp.status = false;

                if (callback != null) {
                    if (callback instanceof INetUiThreadCallBack) {
                        ThreadManager.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResp(request, resp);
                            }
                        }, 0);
                    } else {
                        callback.onResp(request, resp);
                    }
                }
            }
        });
    }

    /**
     * 走seeedstudiio 服务器
     * @param uri
     * @param cmd
     * @param params
     * @param callback
     */
    public void postRequest(String uri, String cmd, RequestParams params, final INetCallback callback) {

        // LedShowNumberUrl = "https://cn.iot.seeed.cc/v1/node/Grove4Digit_UART/display_digits/0/" + angleStr + "?access_token=44b127d788c4069245ef591d0f6e0f9e";
        final Request request = new Request();
        request.callback = callback;
        request.cmd = cmd;

        if (params != null) {
            String time = "" + System.currentTimeMillis() / 1000;
            params.put("api_key", Common.WioLink_AppId);
            params.put("timestamp", time);
            params.put("sign", BaseLogic.getSign(uri, time));
            params.put("source", Common.WioLink_Source);
        }

        String url = "";
        if (uri.startsWith("http")){
           url = uri;
        }else {
            url = CommonUrl.Server_Prefix.getVal() + uri;
        }
        request.url = url;
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(NetTimeout);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String s = new String(responseBody);
                JSONObject jsonObj = null;
                int code = 0;
                String errMsg = "";
                String data = "";
                try {
                    jsonObj = new JSONObject(s);
                    errMsg = jsonObj.getString("msgs");
                    code = jsonObj.getInt("errorcode");
                    data = jsonObj.getString("data");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final Packet resp = new Packet();
                resp.code = code;
                resp.errorMsg = errMsg;
                resp.data = data;
                if (resp.code != 0) {
                    resp.status = false;
                } else {
                    resp.status = true;
                }

                if (callback != null) {
                    if (callback instanceof INetUiThreadCallBack) {
                        ThreadManager.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResp(request, resp);
                            }
                        }, 0);
                    } else {
                        callback.onResp(request, resp);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                final Packet resp = new Packet();
                if (responseBody != null) {
                    String s = new String(responseBody);
                    resp.code = statusCode;
                    resp.data = "";
                    if (error.toString().length() > 50) {
                        resp.errorMsg = "server error";
                    } else {
                        resp.errorMsg = error.toString();
                    }
                    resp.status = false;
                }

                if (callback != null) {
                    if (callback instanceof INetUiThreadCallBack) {
                        ThreadManager.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResp(request, resp);
                            }
                        }, 0);
                    } else {
                        callback.onResp(request, resp);
                    }
                }
            }
        });
    }

    public static SSLSocketFactory createSSLSocketFactory() {
        MySSLSocketFactory sf = null;
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sf;
    }

    public static class MySSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("SSL");

        public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);
            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
            injectHostname(socket, host);
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }

        private void injectHostname(Socket socket, String host) {
            try {
                Field field = InetAddress.class.getDeclaredField("hostName");
                field.setAccessible(true);
                field.set(socket.getInetAddress(), host);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 走wio link 服务器
     * @param url
     * @param cmd
     * @param params
     * @param callback
     */
    public void post(String url, String cmd, RequestParams params, final INetCallback callback) {

        // LedShowNumberUrl = "https://cn.iot.seeed.cc/v1/node/Grove4Digit_UART/display_digits/0/" + angleStr + "?access_token=44b127d788c4069245ef591d0f6e0f9e";
        final Request request = new Request();
        request.callback = callback;
        request.cmd = cmd;
        request.url = url;
        AsyncHttpClient client = new AsyncHttpClient();
        client.setSSLSocketFactory(createSSLSocketFactory());
        client.setTimeout(NetTimeout);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String s = new String(responseBody);
                JSONObject jsonObj = null;
                int code = 0;
                String errMsg = "";
                String data = "";
              /*  try {
                    jsonObj = new JSONObject(s);
                    errMsg = jsonObj.getString("msgs");
                    code = jsonObj.getInt("errorcode");
                    data = jsonObj.getString("data");
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
                final Packet resp = new Packet();
                resp.code = code;
                resp.errorMsg = errMsg;
                resp.data = s;
                if (resp.code != 0) {
                    resp.status = false;
                } else {
                    resp.status = true;
                }

                if (callback != null) {
                    if (callback instanceof INetUiThreadCallBack) {
                        ThreadManager.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResp(request, resp);
                            }
                        }, 0);
                    } else {
                        callback.onResp(request, resp);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                final Packet resp = new Packet();
                if (responseBody != null) {
                    String s = new String(responseBody);
                    resp.code = statusCode;
                    resp.data = "";
                    if (error.toString().length() > 50) {
                        resp.errorMsg = "server error";
                    } else {
                        resp.errorMsg = error.toString();
                    }
                    resp.status = false;
                }

                if (callback != null) {
                    if (callback instanceof INetUiThreadCallBack) {
                        ThreadManager.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResp(request, resp);
                            }
                        }, 0);
                    } else {
                        callback.onResp(request, resp);
                    }
                }
            }
        });
    }

    public void get(String url, String cmd, final INetCallback callback) {
        final Request request = new Request();
        request.callback = callback;
        request.cmd = cmd;
        request.url = url;
        AsyncHttpClient client = new AsyncHttpClient();
        client.setSSLSocketFactory(createSSLSocketFactory());
        client.setTimeout(NetTimeout);
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String s = new String(responseBody);
                final Packet resp = new Packet();
                resp.code = statusCode;
                resp.data = s;
                resp.status = true;

                if (callback != null) {
                    if (callback instanceof INetUiThreadCallBack) {
                        ThreadManager.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResp(request, resp);
                            }
                        }, 0);
                    } else {
                        callback.onResp(request, resp);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace(System.out);
                //   String s = new String(responseBody);
                final Packet resp = new Packet();
                resp.code = statusCode;
                resp.data = "";
                resp.errorMsg = error.toString();
                resp.status = false;

                if (callback != null) {
                    if (callback instanceof INetUiThreadCallBack) {
                        ThreadManager.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResp(request, resp);
                            }
                        }, 0);
                    } else {
                        callback.onResp(request, resp);
                    }
                }
            }
        });
    }
}
