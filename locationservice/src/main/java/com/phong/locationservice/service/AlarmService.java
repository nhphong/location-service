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
import com.phong.locationservice.database.model.Task;
import com.phong.locationservice.event.DetectLocationEvent;
import com.phong.locationservice.event.ReachTargetEvent;
import com.phong.locationservice.utility.Utils;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Phong Nguyen on 7/25/15.
 */
public class AlarmService extends Service implements LocationListener {

    public static final String TAG = AlarmService.class.getSimpleName();
    private LocationServiceApiClient mLocationServiceApiClient;

    public static void start(Context context) {
        context.startService(new Intent(context, AlarmService.class));
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, AlarmService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

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
        RealmResults<Task> taskList = getAllTask(this);
        if (taskList == null || taskList.isEmpty()) {
            stopSelf();
            return;
        }

        for (final Task task : taskList) {
            switch (task.getType()) {
                case Task.GET_CURRENT_LOCATION:
                    FetchAddressIntentService.start(this, location, new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            String address = (resultCode == Constants.RESULT_SUCCESS) ? resultData.getString(Constants.RESULT_MSG) : "";
                            DetectLocationEvent.fire(task.getId(), task.getCreatedAt(), location.getLatitude(), location.getLongitude(), address);
                            cancelTask(AlarmService.this, task.getId());
                        }
                    });
                    break;
                case Task.ADD_TARGET:
                    if (location.distanceTo(task.getLocation()) < 60) {
                        ReachTargetEvent.fire(task.getId(), task.getCreatedAt(), task.getLatitude(), task.getLongitude());
                        cancelTask(this, task.getId());
                    }
                    break;
            }
        }
    }

    public static String getCurrentLocation(Context context) {
        Realm realm = Realm.getInstance(context);
        realm.beginTransaction();
        Task task = realm.createObject(Task.class);
        task.setId(UUID.randomUUID().toString());
        task.setCreatedAt(System.currentTimeMillis());
        task.setType(Task.GET_CURRENT_LOCATION);
        realm.commitTransaction();

        if (!Utils.isServiceRunning(context, AlarmService.class)) {
            AlarmService.start(context);
        }

        return task.getId();
    }

    public static String addTarget(Context context, double latitude, double longitude) {
        Realm realm = Realm.getInstance(context);
        realm.beginTransaction();
        Task task = realm.createObject(Task.class);
        task.setId(UUID.randomUUID().toString());
        task.setCreatedAt(System.currentTimeMillis());
        task.setType(Task.ADD_TARGET);
        task.setLatitude(latitude);
        task.setLongitude(longitude);
        realm.commitTransaction();

        if (!Utils.isServiceRunning(context, AlarmService.class)) {
            AlarmService.start(context);
        }

        return task.getId();
    }

    public static void cancelTask(Context context, String taskId) {
        Realm realm = Realm.getInstance(context);
        Task task = realm.where(Task.class).equalTo("id", taskId).findFirst();
        if (task != null) {
            realm.beginTransaction();
            task.removeFromRealm();
            realm.commitTransaction();

            if (noTaskLeft(context) && Utils.isServiceRunning(context, AlarmService.class)) {
                AlarmService.stop(context);
            }
        }
    }

    public static void cancelAllTask(Context context) {
        RealmResults<Task> taskList = getAllTask(context);
        Realm realm = Realm.getInstance(context);

        if (taskList != null && !taskList.isEmpty()) {
            realm.beginTransaction();
            for (Task task : taskList) {
                task.removeFromRealm();
            }
            realm.commitTransaction();
        }

        if (Utils.isServiceRunning(context, AlarmService.class)) {
            AlarmService.stop(context);
        }
    }

    public static RealmResults<Task> getAllTask(Context context) {
        return Realm.getInstance(context).where(Task.class).findAll();
    }

    public static boolean noTaskLeft(Context context) {
        RealmResults<Task> taskList = getAllTask(context);
        return taskList == null || taskList.isEmpty();
    }
}
