package com.phong.locationservice.event;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.phong.locationservice.Constants;

/**
 * Created by Phong Nguyen on 7/30/15.
 */
public class LocationEvent {

    public static final String TAG = Constants.TAG + LocationEvent.class.getSimpleName();

    public static void fire(Context context, String taskId, long taskCreatedAt, double latitude, double longitude) {
        Log.d(TAG, "Broadcast location event");
        Intent intent = new Intent(Constants.LOCATION_EVENT_SIGNATURE);
        intent.putExtra(Constants.EXTRA_TASK_ID, taskId);
        intent.putExtra(Constants.EXTRA_TASK_CREATED_AT, taskCreatedAt);
        intent.putExtra(Constants.EXTRA_LATITUDE, latitude);
        intent.putExtra(Constants.EXTRA_LONGITUDE, longitude);
        context.sendBroadcast(intent);
    }
}
