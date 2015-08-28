package com.phong.locationservice.utility;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.phong.locationservice.Constants;
import com.phong.locationservice.R;

/**
 * Created by Phong Nguyen on 7/25/15.
 */
public class Utils {

    public static final String TAG = Constants.TAG + Utils.class.getSimpleName();

    public static boolean checkGooglePlayServices(final Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        activity.finish();
                    }
                }).show();
            } else {
                Log.e(TAG, "This device does not support Google Play Services SDK");
                activity.finish();
            }
            return false;
        }
        return true;
    }

    public static boolean isGPSEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    public static void showDialog(Context context, String message, String positiveBtn, DialogInterface.OnClickListener positiveAction) {
        showDialog(context, message, false, positiveBtn, positiveAction, context.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
    }

    public static void showDialog(Context context, String message, boolean cancelable,
                                  String positiveBtn, DialogInterface.OnClickListener positiveAction,
                                  String negativeBtn, DialogInterface.OnClickListener negativeAction) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setCancelable(cancelable)
                .setPositiveButton(positiveBtn, positiveAction)
                .setNegativeButton(negativeBtn, negativeAction)
                .create().show();
    }

    private static ProgressDialog progressDialog;
    public static synchronized void showProgressDialog(Context context, CharSequence message) {
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = ProgressDialog.show(context, "", message != null ? message : "", true);
        }
    }

    public static synchronized void showProgressDialogWithoutDimBk(Context context, CharSequence message) {
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = new ProgressDialog(context, R.style.ProgressDialogWithoutDimBk);
            progressDialog.setTitle("");
            progressDialog.setMessage(message != null ? message : "");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setOnCancelListener(null);
            progressDialog.show();
        }
    }

    public static synchronized void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    /**
     * Checks if the device has Internet connection.
     *
     * @return <code>true</code> if the phone is connected to the Internet.
     */
    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null && wifiNetwork.isConnected()) {
            return true;
        }

        NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null && mobileNetwork.isConnected()) {
            return true;
        }

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        }

        return false;
    }

    /**
     * Checks if a Service is still running.
     */
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d(TAG, serviceClass + " is running");
                return true;
            }
        }
        Log.d(TAG, serviceClass + " is NOT running");
        return false;
    }

    public static void goToLocationSettings(Context context) {
        context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }
}
