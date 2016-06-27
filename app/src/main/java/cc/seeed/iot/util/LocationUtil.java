package cc.seeed.iot.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;


/**
 * author: Jerry on 2016/6/21 17:19.
 * description:
 */
public class LocationUtil {
    String TAG = this.getClass().getSimpleName();
    private Context context;
/*
    public void init(Context context) {
        this.context = context;
        mGoogleApiClient = new GoogleApiClient
                .Builder(context)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();
    }

    public void startLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                        Log.i(TAG, String.format("Place '%s' has likelihood: %g，%g",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getPlace().getLatLng().latitude,
                                placeLikelihood.getPlace().getLatLng().longitude,
                                placeLikelihood.getLikelihood()));
                    }
                    likelyPlaces.release();
                }
            });
            return;
        }

    }

    public void stopLocation() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    }*/

    LocationManager loctionManager;

    public Location location(Context context) {
        Location location = null;
        loctionManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);//高精度
        criteria.setAltitudeRequired(false);//不要求海拔
        criteria.setBearingRequired(false);//不要求方位
        criteria.setCostAllowed(true);//允许有花费
        criteria.setPowerRequirement(Criteria.POWER_LOW);//低功耗
        //从可用的位置提供器中，匹配以上标准的最佳提供器
        String provider = loctionManager.getBestProvider(criteria, true);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //获得最后一次变化的位置
            location = loctionManager.getLastKnownLocation(provider);
            if (location != null)
                Log.i(TAG, "Latitude：" + location.getLatitude() + " Longitude:" + location.getLongitude());
            //监听位置变化，2秒一次，距离10米以上
            //  loctionManager.requestLocationUpdates(provider, 100, 10, locationListener);
        }
        return location;
    }

    //位置监听器
    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i(TAG, "onStatusChanged: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(TAG, "onProviderDisabled: " + provider);
        }

        //当位置变化时触发
        @Override
        public void onLocationChanged(Location location) {
            //使用新的location更新TextView显示
            // updateWithNewLocation(location);
            if (location != null)
                Log.i(TAG, "Latitude：" + location.getLatitude() + " Longitude:" + location.getLongitude());
        }

    };
}
