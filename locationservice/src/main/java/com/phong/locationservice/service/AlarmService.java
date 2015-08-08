package com.phong.locationservice.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.phong.locationservice.Constants;
import com.phong.locationservice.api.LocationServiceApiClient;
import com.phong.locationservice.database.model.Task;
import com.phong.locationservice.event.LocationEvent;
import com.phong.locationservice.utility.Utils;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Phong Nguyen on 7/25/15.
 */
public class AlarmService extends Service implements LocationListener {

    public static final String TAG = Constants.TAG + AlarmService.class.getSimpleName();
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
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        Realm realm = Realm.getInstance(this);
        RealmResults<Task> taskList = getAllTask(this);
        if (taskList == null || taskList.isEmpty()) {
            stopSelf();
            return;
        }

        realm.beginTransaction();
        for (int i = 0; i < taskList.size(); ++i) {
            Task task = taskList.get(i);

            switch (task.getType()) {
                case Task.GET_CURRENT_LOCATION:
                    LocationEvent.fire(this, task.getId(), task.getCreatedAt(), location.getLatitude(), location.getLongitude(), null, Task.GET_CURRENT_LOCATION);
                    task.removeFromRealm();
                    if (noTaskLeft(this)) {
                        stopSelf();
                    }
                    break;
                case Task.WATCH_TARGET:
                    Location target = new Location("target");
                    target.setLatitude(task.getLatitude());
                    target.setLongitude(task.getLongitude());

                    if (location.distanceTo(target) < 100) {
                        LocationEvent.fire(this, task.getId(), task.getCreatedAt(), task.getLatitude(), task.getLongitude(), task.getAddress(), Task.WATCH_TARGET);
                        task.removeFromRealm();
                        if (noTaskLeft(this)) {
                            stopSelf();
                        }
                    }
                    break;
            }
        }
        realm.commitTransaction();
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

    public static String addTarget(Context context, double latitude, double longitude, String address) {
        Realm realm = Realm.getInstance(context);
        realm.beginTransaction();
        Task task = realm.createObject(Task.class);
        task.setId(UUID.randomUUID().toString());
        task.setCreatedAt(System.currentTimeMillis());
        task.setType(Task.WATCH_TARGET);
        task.setLatitude(latitude);
        task.setLongitude(longitude);
        task.setAddress(address);
        realm.commitTransaction();

        if (!Utils.isServiceRunning(context, AlarmService.class)) {
            AlarmService.start(context);
        }

        return task.getId();
    }

    public static void cancelTask(Context context, String taskId) {
        if (taskId == null) {
            return;
        }

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

    public static boolean isTaskDone(Context context, String taskId) {
        if (taskId == null) {
            return true;
        }

        Realm realm = Realm.getInstance(context);
        Task task = realm.where(Task.class).equalTo("id", taskId).findFirst();
        return task == null;
    }
}
