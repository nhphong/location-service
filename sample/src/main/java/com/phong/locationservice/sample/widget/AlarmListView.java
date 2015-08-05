package com.phong.locationservice.sample.widget;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.phong.locationservice.Constants;
import com.phong.locationservice.sample.R;
import com.phong.locationservice.sample.database.model.Alarm;
import com.phong.locationservice.service.AlarmService;
import com.phong.locationservice.service.FetchAddressIntentService;
import com.phong.locationservice.utility.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;

public class AlarmListView extends ScrollView implements View.OnClickListener {

    public static final String TAG = AlarmListView.class.getSimpleName();
    private static final int GET_LOCATION_TIMEOUT = 15000;
    private static final int ON_TICK = 3000;

    private Context mContext;
    private LinearLayout mContent;
    private CountDownTimer mCountDownTimer;
    private BroadcastReceiver mBroadcastReceiver;
    private String mGetCurrentLocationTaskId;

    public AlarmListView(Context context) {
        super(context);
        init();
    }

    public AlarmListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AlarmListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mContext = getContext();
        mContent = new LinearLayout(mContext);
        mContent.setOrientation(LinearLayout.VERTICAL);
        mContent.setLayoutTransition(createCustomLayoutTransition("scaleY", 0, 1, false));
        this.addView(mContent);

        mCountDownTimer = new CountDownTimer(GET_LOCATION_TIMEOUT, ON_TICK) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, millisUntilFinished + " milliseconds before " + AlarmService.TAG + " timeout");
            }

            @Override
            public void onFinish() {
                Log.e(TAG, AlarmService.TAG + " timeout");
                AlarmService.cancelTask(mContext, mGetCurrentLocationTaskId);
                Utils.dismissProgressDialog();
                Toast.makeText(mContext, mContext.getString(R.string.cannot_get_your_location), Toast.LENGTH_LONG).show();
            }
        };

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCountDownTimer.cancel();
                final Location loc = new Location("Current Location");
                loc.setLatitude(intent.getDoubleExtra(Constants.EXTRA_LATITUDE, -1));
                loc.setLongitude(intent.getDoubleExtra(Constants.EXTRA_LONGITUDE, -1));

                FetchAddressIntentService.start(mContext, loc, new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        Utils.dismissProgressDialog();
                        String tag = loc.getLatitude() + ":" + loc.getLongitude();
                        createNewItem(resultCode == Constants.RESULT_SUCCESS ? resultData.getString(Constants.RESULT_MSG) : tag, tag);
                    }
                });
            }
        };

        refreshData();
    }

    public void refreshData() {
        //TODO: Reload everything from the database here
    }

    private List<View> getAllItems() {
        List<View> result = new ArrayList<>();
        for (int i = 0; i < mContent.getChildCount(); ++i) {
            result.add(mContent.getChildAt(i));
        }
        return result;
    }

    public boolean enterEditMode() {
        List<View> items = getAllItems();
        if (!items.isEmpty()) {
            for (View v : items) {
                v.setOnClickListener(this);
                v.findViewById(R.id.switch_view).setVisibility(GONE);
                v.findViewById(R.id.btn_delete).setVisibility(VISIBLE);
            }
            return true;
        }
        return false;
    }

    public void exitEditMode() {
        List<View> items = getAllItems();
        for (View v : items) {
            v.setOnClickListener(null);
            v.setClickable(false);
            v.findViewById(R.id.switch_view).setVisibility(VISIBLE);
            v.findViewById(R.id.btn_delete).setVisibility(GONE);
        }
    }

    //TODO: Should check for air-plane mode
    public void addItem() {
        if (!Utils.isLocationEnabled(mContext)) {
            Utils.showDialog(mContext, mContext.getString(R.string.require_location),
                    mContext.getString(R.string.go_to_settings), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Utils.goToLocationSettings(mContext);
                        }
                    });
            return;
        }

        if (!Utils.checkInternetConnection(mContext)) {
            //TODO: Advise user to use the application outdoor
        }

        if (Utils.isGPSEnabled(mContext)) {
            Log.d(TAG, "GPS is ON");
        } else {
            Log.d(TAG, "GPS is OFF");
        }

        fetchCurrentLocation();
    }

    private void fetchCurrentLocation() {
        Log.d(TAG, " Starting countdown timer");
        mCountDownTimer.cancel();
        mCountDownTimer.start();

        Utils.showProgressDialog(mContext, mContext.getString(R.string.fetching_your_location));
        mGetCurrentLocationTaskId = AlarmService.getCurrentLocation(mContext);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mContext.registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.LOCATION_EVENT_SIGNATURE));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContext.unregisterReceiver(mBroadcastReceiver);
        mCountDownTimer.cancel();
        Utils.dismissProgressDialog();
        AlarmService.cancelTask(mContext, mGetCurrentLocationTaskId);
    }

    private void createNewItem(String title, String tag) {
        exitEditMode();
        View v = LayoutInflater.from(mContext).inflate(R.layout.alarm_view, null);
        mContent.addView(v, 0);
        ViewGroup.LayoutParams params = v.getLayoutParams();
        params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 85, getResources().getDisplayMetrics());
        v.setLayoutParams(params);

        final TextView locationTv = (TextView) v.findViewById(R.id.location_tv);
        locationTv.setText(title);
        locationTv.setTag(tag);

        View btnDelete = v.findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(this);
        btnDelete.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        ((LinearLayout) v.findViewById(R.id.main_content))
                .setLayoutTransition(createCustomLayoutTransition("translationX", btnDelete.getMeasuredWidth(), 0, true));

        Switch switchView = (Switch) v.findViewById(R.id.switch_view);
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //TODO: Permanently, add this change to the database. Maybe we don't need
                String[] tags = locationTv.getTag().toString().split(":");
                double latitude = Double.parseDouble(tags[0]);
                double longitude = Double.parseDouble(tags[1]);

                if (isChecked) {
                    //TODO: Assign task to the Location service
                } else {
                    //TODO: Cancel task from the Location service
                }
            }
        });

        String[] tags = tag.split(":");
        createNewAlarmInDatabase(mContext, title, Double.parseDouble(tags[0]), Double.parseDouble(tags[1]));
        //TODO: assign back alarm Id to the textview tag
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_delete) {
            View container = findItemContaining(v);
            if (container != null) {
                mContent.removeView(container);
                //TODO: cancel the task, if set
                //TODO: Permanently, remove this view from the database
                //TODO: Exit Edit mode if there is no Alarm left
            }
        } else {
            Toast.makeText(mContext, mContent.indexOfChild(v) + "", Toast.LENGTH_SHORT).show();
        }
    }

    private View findItemContaining(View v) {
        List<View> items = getAllItems();
        ViewParent parent = v.getParent();

        if (items == null || items.isEmpty() || parent == null) {
            return null;
        }
        ViewParent grandParent = parent.getParent();

        for (int i = 0; i < items.size(); ++i) {
            View item = items.get(i);
            if (item == parent || item == grandParent) {
                return item;
            }
        }
        return null;
    }

    private static LayoutTransition createCustomLayoutTransition(String property, float from, float to, boolean animateWhenGone) {
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setAnimator(LayoutTransition.APPEARING, ObjectAnimator.ofPropertyValuesHolder(layoutTransition,
                PropertyValuesHolder.ofFloat(property, from, to), PropertyValuesHolder.ofFloat("pivotY", 0)));
        layoutTransition.setStartDelay(LayoutTransition.APPEARING, 0);

        if (animateWhenGone) {
            layoutTransition.setAnimator(LayoutTransition.DISAPPEARING, ObjectAnimator.ofPropertyValuesHolder(layoutTransition,
                    PropertyValuesHolder.ofFloat(property, to, from), PropertyValuesHolder.ofFloat("pivotY", 0)));
            layoutTransition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        }
        return layoutTransition;
    }

    private static String createNewAlarmInDatabase(Context context, String address, double latitude, double longitude) {
        Realm realm = Realm.getInstance(context);
        realm.beginTransaction();
        Alarm alarm = realm.createObject(Alarm.class);
        alarm.setId(UUID.randomUUID().toString());
        alarm.setCreatedAt(System.currentTimeMillis());
        alarm.setAddress(address);
        alarm.setLatitude(latitude);
        alarm.setLongitude(longitude);
        realm.commitTransaction();
        Log.d(TAG, "Create new Alarm in the database: address='" + address + "', latitude=" + latitude + ", longitude=" + longitude);
        return alarm.getId();
    }

    private static void deleteAlarmInDatabase(Context context, String alarmId) {
        Realm realm = Realm.getInstance(context);
        Alarm alarm = realm.where(Alarm.class).equalTo("id", alarmId).findFirst();
        if (alarm != null) {
            realm.beginTransaction();
            alarm.removeFromRealm();
            realm.commitTransaction();
            Log.d(TAG, "Delete Alarm in the database: alarmId=" + alarmId + ", address='" + alarm.getAddress() + "'");
        }
    }

    private static void updateAlarmInDatabase(Context context, String whichAlarmId, String newAddress, double newLatitude, double newLongitude, String newLatestTaskId) {
        Realm realm = Realm.getInstance(context);
        Alarm alarm = realm.where(Alarm.class).equalTo("id", whichAlarmId).findFirst();
        if (alarm != null) {
            realm.beginTransaction();
            alarm.setAddress(newAddress);
            alarm.setLatitude(newLatitude);
            alarm.setLongitude(newLongitude);
            alarm.setLatestTaskId(newLatestTaskId);
            realm.commitTransaction();
            Log.d(TAG, "Update Alarm in the database: alarmId=" + whichAlarmId + ", newAddress='" + newAddress + "', " +
                    "newLatitude=" + newLatitude + ", newLongitude=" + newLongitude + ", newLatestTaskId=" + newLatestTaskId);
        }
    }

    private static void updateAlarmInDatabase(Context context, String whichAlarmId, String newLatestTaskId) {
        Realm realm = Realm.getInstance(context);
        Alarm alarm = realm.where(Alarm.class).equalTo("id", whichAlarmId).findFirst();
        if (alarm != null) {
            realm.beginTransaction();
            alarm.setLatestTaskId(newLatestTaskId);
            realm.commitTransaction();
            Log.d(TAG, "Update Alarm in the database: alarmId=" + whichAlarmId + ", newLatestTaskId=" + newLatestTaskId);
        }
    }

    private static void updateAlarmInDatabase(Context context, String whichAlarmId, String newAddress, double newLatitude, double newLongitude) {
        Realm realm = Realm.getInstance(context);
        Alarm alarm = realm.where(Alarm.class).equalTo("id", whichAlarmId).findFirst();
        if (alarm != null) {
            realm.beginTransaction();
            alarm.setAddress(newAddress);
            alarm.setLatitude(newLatitude);
            alarm.setLongitude(newLongitude);
            realm.commitTransaction();
            Log.d(TAG, "Update Alarm in the database: alarmId=" + whichAlarmId + ", newAddress='" + newAddress + "', " +
                    "newLatitude=" + newLatitude + ", newLongitude=" + newLongitude);
        }
    }
}
