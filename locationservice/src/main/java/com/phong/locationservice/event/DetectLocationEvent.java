package com.phong.locationservice.event;

import de.greenrobot.event.EventBus;

/**
 * Created by nguyenphong on 7/30/15.
 */
public class DetectLocationEvent {

    private String taskId;
    private long taskCreatedAt;
    private double latitude;
    private double longitude;
    private String address;

    private DetectLocationEvent(String taskId, long taskCreatedAt, double latitude, double longitude, String address) {
        this.taskId = taskId;
        this.taskCreatedAt = taskCreatedAt;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public static void fire(String taskId, long taskCreatedAt, double latitude, double longitude, String address) {
        DetectLocationEvent event = new DetectLocationEvent(taskId, taskCreatedAt, latitude, longitude, address);
        EventBus.getDefault().post(event);
    }
}
