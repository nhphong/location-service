package com.phong.locationservice.sample.database.model;

import io.realm.RealmObject;

/**
 * Created by nguyenphong on 8/8/15.
 */
public class Alarm extends RealmObject {

    private String id;
    private long createdAt;
    private String address;
    private double latitude;
    private double longitude;
    private String latestTaskId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLatestTaskId() {
        return latestTaskId;
    }

    public void setLatestTaskId(String latestTaskId) {
        this.latestTaskId = latestTaskId;
    }
}
