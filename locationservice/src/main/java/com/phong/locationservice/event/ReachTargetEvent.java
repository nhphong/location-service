package com.phong.locationservice.event;

import de.greenrobot.event.EventBus;

/**
 * Created by nguyenphong on 7/30/15.
 */
public class ReachTargetEvent {

    private String taskId;
    private long taskCreatedAt;
    private double latitude;
    private double longitude;

    private ReachTargetEvent(String taskId, long taskCreatedAt, double latitude, double longitude) {
        this.taskId = taskId;
        this.taskCreatedAt = taskCreatedAt;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static void fire(String taskId, long taskCreatedAt, double latitude, double longitude) {
        ReachTargetEvent event = new ReachTargetEvent(taskId, taskCreatedAt, latitude, longitude);
        EventBus.getDefault().post(event);
    }
}
