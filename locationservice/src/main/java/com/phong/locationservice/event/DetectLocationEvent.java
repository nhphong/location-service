package com.phong.locationservice.event;

import android.content.Context;
import android.content.Intent;

import com.phong.locationservice.Constants;

/**
 * Created by Phong Nguyen on 7/30/15.
 */
public class DetectLocationEvent {

    public static void fire(Context context, String taskId, long taskCreatedAt, double latitude, double longitude, String address) {
        Intent intent = new Intent(Constants.DETECT_LOCATION_EVENT_SIGNATURE);
        intent.putExtra(Constants.EXTRA_TASK_ID, taskId);
        intent.putExtra(Constants.EXTRA_TASK_CREATED_AT, taskCreatedAt);
        intent.putExtra(Constants.EXTRA_LATITUDE, latitude);
        intent.putExtra(Constants.EXTRA_LONGITUDE, longitude);
        intent.putExtra(Constants.EXTRA_ADDRESS, address);
        context.sendBroadcast(intent);
    }
}
