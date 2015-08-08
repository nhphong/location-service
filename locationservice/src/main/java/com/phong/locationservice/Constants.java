package com.phong.locationservice;

public class Constants {
    public static final String RESULT_MSG = "result_msg";
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_FAILURE = 0;

    public static final String EXTRA_RESULT_RECEIVER = "result_receiver";
    public static final String EXTRA_LOCATION_DATA = "location_data";
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_TASK_CREATED_AT = "task_created_at";
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    public static final String EXTRA_ADDRESS = "address";

    public static final String NEAR_TARGET_EVENT_SIGNATURE = "com.phong.locationservice.event.neartarget";
    public static final String LOCATION_DETECTED_EVENT_SIGNATURE = "com.phong.locationservice.event.locationdetected";
    public static final String TAG = "Location Service -- ";
}
