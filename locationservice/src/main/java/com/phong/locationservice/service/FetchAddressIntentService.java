package com.phong.locationservice.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.phong.locationservice.R;
import com.phong.locationservice.utility.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Phong Nguyen on 7/25/15.
 */

public class FetchAddressIntentService extends IntentService {

    public static final String TAG = FetchAddressIntentService.class.getSimpleName();

    public static void start(Context context, Location location, ResultReceiver resultReceiver) {
        Intent i = new Intent(context, FetchAddressIntentService.class);
        i.putExtra(Constants.EXTRA_LOCATION_DATA, location);
        i.putExtra(Constants.EXTRA_RESULT_RECEIVER, resultReceiver);
        context.startService(i);
    }

    public FetchAddressIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        List<Address> addresses = null;
        String errorMessage = "";
        Bundle bundle = new Bundle();

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(Constants.EXTRA_LOCATION_DATA);
        // Get the result receiver
        ResultReceiver resultReceiver = intent.getParcelableExtra(Constants.EXTRA_RESULT_RECEIVER);

        if (Geocoder.isPresent() && Utils.checkInternetConnection(this)) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                errorMessage = getString(R.string.location_service_is_not_available);
                Log.e(TAG, errorMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid latitude or longitude values.
                errorMessage = getString(R.string.invalid_latitude_longitude);
                Log.e(TAG, errorMessage + ". " +
                        "Latitude = " + location.getLatitude() +
                        ", Longitude = " + location.getLongitude(), illegalArgumentException);
            }
        } else {
            // Geocoder or Internet unavailable
            errorMessage = getString(R.string.geocoder_or_internet_unavailable);
            Log.e(TAG, errorMessage);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }

            bundle.putString(Constants.RESULT_MSG, errorMessage);
            resultReceiver.send(Constants.RESULT_FAILURE, bundle);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }

            Log.d(TAG, getString(R.string.address_found));
            bundle.putString(Constants.RESULT_MSG, TextUtils.join(", ", addressFragments));
            resultReceiver.send(Constants.RESULT_SUCCESS, bundle);
        }
    }
}