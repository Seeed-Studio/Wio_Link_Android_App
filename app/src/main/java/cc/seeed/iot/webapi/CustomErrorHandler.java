package cc.seeed.iot.webapi;

import android.util.Log;

import cc.seeed.iot.webapi.model.ErrorResponse;
import retrofit.ErrorHandler;
import retrofit.RetrofitError;

/**
 * Created by tenwong on 15/12/28.
 */
public class CustomErrorHandler implements ErrorHandler {
    private static String TAG = "CustomErrorHandler";

    @Override
    public Throwable handleError(RetrofitError cause) {
        String errorDescription;

        if (cause.isNetworkError()) {
            errorDescription = "Connect wio server failure. Please check the network.";
        } else {
            if (cause.getResponse() == null) {
                errorDescription = "No response!";
            } else {

                // Error message handling - return a simple error to Retrofit handlers..
                try {
                    ErrorResponse errorResponse = (ErrorResponse) cause.getBodyAs(ErrorResponse.class);
                    errorDescription = errorResponse.error;
                } catch (Exception ex) {
                    errorDescription = "Exception error!";
                }
            }
        }

        return new Exception(errorDescription);
    }
}