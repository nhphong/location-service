package com.phong.locationservice.event;

import android.content.Context;
import android.content.Intent;

import com.phong.locationservice.Constants;

/**
 * Created by Phong Nguyen on 7/30/15.
 */
public class ReachTargetEvent {

    public static void fire(Context context, String taskId, long taskCreatedAt, double latitude, double longitude) {
        Intent intent = new Intent(Constants.REACH_TARGET_EVENT_SIGNATURE);
        intent.putExtra(Constants.EXTRA_TASK_ID, taskId);
        intent.putExtra(Constants.EXTRA_TASK_CREATED_AT, taskCreatedAt);
        intent.putExtra(Constants.EXTRA_LATITUDE, latitude);
        intent.putExtra(Constants.EXTRA_LONGITUDE, longitude);
        context.sendBroadcast(intent);
    }
}
