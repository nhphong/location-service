package com.phong.locationservice.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.phong.locationservice.api.LocationServiceApiClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Phong Nguyen on 7/25/15.
 */
public class AlarmService extends Service implements LocationListener {

    public static final String TAG = AlarmService.class.getSimpleName();
    public static final String ONLY_GET_CURRENT_LOCATION = "only_get_current_location";

    private LocationServiceApiClient mLocationServiceApiClient;
    private boolean mOnlyGetTheCurrentLocation;
    private ResultReceiver mResultReceiver;
    private static List<Location> mTargets = new ArrayList<>();

    // Be careful: the latest call overrides all the previous calls
    public static void start(Context context, boolean onlyGetCurrentLocation, ResultReceiver resultReceiver) {
        Intent i = new Intent(context, AlarmService.class);
        i.putExtra(ONLY_GET_CURRENT_LOCATION, onlyGetCurrentLocation);
        i.putExtra(Constants.EXTRA_RESULT_RECEIVER, resultReceiver);
        context.startService(i);
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, AlarmService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand (OnlyGetCurrentLocation = " + intent.getBooleanExtra(ONLY_GET_CURRENT_LOCATION, false) + ")");
        mOnlyGetTheCurrentLocation = intent.getBooleanExtra(ONLY_GET_CURRENT_LOCATION, false);
        mResultReceiver = intent.getParcelableExtra(Constants.EXTRA_RESULT_RECEIVER);

        if (mLocationServiceApiClient == null) {
            mLocationServiceApiClient = new LocationServiceApiClient(this, 5000, 5000, LocationRequest.PRIORITY_HIGH_ACCURACY, this);
            mLocationServiceApiClient.connectAndStartLocationUpdates();
        } else if (mLocationServiceApiClient.isConnected()) {
            mLocationServiceApiClient.startLocationUpdates();
        } else {
            mLocationServiceApiClient.connectAndStartLocationUpdates();
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationServiceApiClient.stopLocationUpdates();
        mLocationServiceApiClient.disconnect();
        mLocationServiceApiClient = null;
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onLocationChanged(final Location location) {
        Log.d(TAG, "onLocationChanged");

        if (mOnlyGetTheCurrentLocation) {
            FetchAddressIntentService.start(this, location, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    if (mResultReceiver != null) {
                        resultData.putString(Constants.RESULT_TAG, location.getLatitude() + " : " + location.getLongitude());
                        mResultReceiver.send(resultCode, resultData);
                    }
                }
            });
            stopSelf();
        }
//        else if (reachAnyTarget(location)) {
//            Intent intent = new Intent(this, ???);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//            stopSelf();
//        }
    }

//    private boolean reachAnyTarget(Location loc) {
//        if (mTargets == null || mTargets.isEmpty()) {
//            return false;
//        }
//        for (Location target : mTargets) {
//            if (loc.distanceTo(target) < 100) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static void addTarget(Location target) {
//        mTargets.add(target);
//    }
//
//    public static void removeTarget(Location target, Context context) {
//        if (mTargets == null || mTargets.isEmpty()) {
//            AlarmService.stop(context);
//            return;
//        }
//        for (Location candidate : mTargets) {
//            if (candidate.distanceTo(target) == 0) {
//                mTargets.remove(candidate);
//                break;
//            }
//        }
//
//        if (mTargets.isEmpty()) {
//            AlarmService.stop(context);
//        }
//    }
//
//    public static void removeAllTargets(Context context) {
//        mTargets.clear();
//        AlarmService.stop(context);
//    }
}
