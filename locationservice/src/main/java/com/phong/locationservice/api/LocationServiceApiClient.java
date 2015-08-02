package com.phong.locationservice.api;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.phong.locationservice.Constants;

/**
 * Created by Phong Nguyen on 7/25/15.
 */
public class LocationServiceApiClient implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = Constants.TAG + LocationServiceApiClient.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationListener mLocationListener;
    private boolean mStartLocationUpdatesAfterConnect;

    public LocationServiceApiClient(Context context, long interval, long fastestInterval, int priority, LocationListener listener) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = new LocationRequest()
                .setInterval(interval)
                .setFastestInterval(fastestInterval)
                .setPriority(priority);

        mLocationListener = listener;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        if (mStartLocationUpdatesAfterConnect) {
            mStartLocationUpdatesAfterConnect = false;
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended, trying to reconnect");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed with error code: " + connectionResult.getErrorCode());
    }

    public void connect() {
        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
            return;
        }
        mGoogleApiClient.connect();
    }

    public void connectAndStartLocationUpdates() {
        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
            return;
        }
        mStartLocationUpdatesAfterConnect = true;
        mGoogleApiClient.connect();
    }

    public void disconnect() {
        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.disconnect();
            Log.d(TAG, "onDisconnected");
        }
    }

    public boolean isConnected() {
        return mGoogleApiClient.isConnected();
    }

    public boolean isConnecting() {
        return mGoogleApiClient.isConnecting();
    }

    public void startLocationUpdates() {

        if (!isConnected()) {
            return;
        }

        Log.d(TAG, "startLocationUpdates");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);
    }

    public void stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
    }
}
