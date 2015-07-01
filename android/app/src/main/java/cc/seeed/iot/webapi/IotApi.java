package cc.seeed.iot.webapi;

import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

//import cc.seeed.iot.storge.MySharedPreference;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.MainThreadExecutor;

/**
 * Created by tenwong on 15/6/23.
 */
public class IotApi {

//    private String IOT_WEB_API_ENDPOINT = "http://192.168.21.83:8080/v1";

    private String iot_url = "http://192.168.21.83:8080/v1";

    private final IotService mIotService;

    private String mAccessToken;

//    public IotApi(Executor httpExecutor, Executor callbackExecutor) {
//        mIotService = init(httpExecutor, callbackExecutor);
//    }


    public IotApi() {
//        iot_url = MySharedPreference.getSvrAdd();
        Executor httpExecutor = Executors.newSingleThreadExecutor();
        MainThreadExecutor callbackExecutor = new MainThreadExecutor();
        mIotService = init(httpExecutor, callbackExecutor);
    }


    private IotService init(Executor httpExecutor, Executor callbackExecutor) {
        Log.e("iot", "iot_url:" + iot_url);
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setExecutors(httpExecutor, callbackExecutor)
                .setEndpoint(iot_url)
                .setRequestInterceptor(new WebApiAuthenticator())
                .build();

        return restAdapter.create(IotService.class);
    }

    public IotService getService() {
        return mIotService;
    }


    public IotApi setAccessToken(String accessToken) {
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
