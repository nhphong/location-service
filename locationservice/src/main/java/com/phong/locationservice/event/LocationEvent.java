package com.phong.locationservice.event;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.phong.locationservice.Constants;
import com.phong.locationservice.database.model.Task;

/**
 * Created by Phong Nguyen on 7/30/15.
 */
public class LocationEvent {

    public static final String TAG = Constants.TAG + LocationEvent.class.getSimpleName();

    public static void fire(Context context, String taskId, long taskCreatedAt, double latitude, double longitude, String address, String type) {
        Intent intent = null;
        if (Task.GET_CURRENT_LOCATION.equals(type)) {
            Log.d(TAG, "Send Broadcast with " + Constants.LOCATION_DETECTED_EVENT_SIGNATURE + " signature");
            intent = new Intent(Constants.LOCATION_DETECTED_EVENT_SIGNATURE);
        } else if (Task.WATCH_TARGET.equals(type)) {
            Log.d(TAG, "Send Broadcast with " + Constants.NEAR_TARGET_EVENT_SIGNATURE + " signature");
            intent = new Intent(Constants.NEAR_TARGET_EVENT_SIGNATURE);
        }

        if (intent != null) {
            intent.putExtra(Constants.EXTRA_TASK_ID, taskId);
            intent.putExtra(Constants.EXTRA_TASK_CREATED_AT, taskCreatedAt);
            intent.putExtra(Constants.EXTRA_LATITUDE, latitude);
            intent.putExtra(Constants.EXTRA_LONGITUDE, longitude);
            if (address != null) {
                intent.putExtra(Constants.EXTRA_ADDRESS, address);
            }
            context.sendBroadcast(intent);
        }
    }
}
