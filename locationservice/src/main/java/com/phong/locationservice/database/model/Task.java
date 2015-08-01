package com.phong.locationservice.database.model;

import android.location.Location;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Created by nguyenphong on 7/28/15.
 */
public class Task extends RealmObject {

    @Ignore
    public static final String GET_CURRENT_LOCATION = "get_current_location";
    @Ignore
    public static final String WATCH_TARGET = "watch_target";

    private String id;
    private long createdAt;
    private String type;
    private double latitude;
    private double longitude;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}
