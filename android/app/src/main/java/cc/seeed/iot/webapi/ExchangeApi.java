package cc.seeed.iot.webapi;

import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import java.lang.reflect.Modifier;
import java.security.cert.CertificateException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.MainThreadExecutor;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

//import cc.seeed.iot.storge.MySharedPreference;

/**
 * Created by tenwong on 15/6/23.
 */
public class ExchangeApi {
    private static String data_exchange_url = "http://192.168.21.48:8080/v1";
//    private static String iot_url = "https://iot.seeed.cc/v1";

    private final IotService mIotService;

    private String mAccessToken;

//    public IotApi(Executor httpExecutor, Executor callbackExecutor) {
//        mIotService = init(httpExecutor, callbackExecutor);
//    }

    public static void SetServerUrl(String url) {
        data_exchange_url = url;
    }

    public ExchangeApi() {

        Executor httpExecutor = Executors.newSingleThreadExecutor();
        MainThreadExecutor callbackExecutor = new MainThreadExecutor();
        mIotService = init(httpExecutor, callbackExecutor);
    }


    private IotService init(Executor httpExecutor, Executor callbackExecutor) {
        OkHttpClient client = getUnsafeOkHttpClient();
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setExecutors(httpExecutor, callbackExecutor)
                .setEndpoint(data_exchange_url)
                .setRequestInterceptor(new WebApiAuthenticator())
                .setClient(new OkClient(client))
                .setConverter(new GsonConverter(new GsonBuilder()
                        .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
                        .serializeNulls()
                        .create()))
                .build();

        return restAdapter.create(IotService.class);
    }

    /**
     * Do not check certificate,Todo use keyStore
     */
    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] chain, String authType)
                                throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setSslSocketFactory(sslSocketFactory);
            okHttpClient.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public IotService getService() {
        return mIotService;
    }


    public ExchangeApi setAccessToken(String accessToken) {
        mAccessToken = accessToken;
        return this;
    }


    private class WebApiAuthenticator implements RequestInterceptor {
        @Override
        public void intercept(RequestFacade request) {
            if (mAccessToken != null) {
                request.addHeader("Authorization", "token " + mAccessToken);
            }
        }
    }

}
