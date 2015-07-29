package com.phong.locationservice.database.model;

import io.realm.RealmObject;

/**
 * Created by nguyenphong on 7/28/15.
 */
public class Task extends RealmObject {

    private String id;
    private long createdAt;
    private Type type;
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
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

    public enum Type {
        GET_CURRENT_LOCATION,
        ADD_TARGET
    }
}
