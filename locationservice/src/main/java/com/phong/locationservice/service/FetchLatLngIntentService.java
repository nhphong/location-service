package com.phong.locationservice.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.phong.locationservice.Constants;
import com.phong.locationservice.R;
import com.phong.locationservice.utility.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Phong Nguyen on 8/16/15.
 */
public class FetchLatLngIntentService extends IntentService {

    public static final String TAG = Constants.TAG + FetchLatLngIntentService.class.getSimpleName();

    public static void start(Context context, String address, ResultReceiver resultReceiver) {
        Intent i = new Intent(context, FetchLatLngIntentService.class);
        i.putExtra(Constants.EXTRA_ADDRESS, address);
        i.putExtra(Constants.EXTRA_RESULT_RECEIVER, resultReceiver);
        context.startService(i);
    }

    public FetchLatLngIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        List<Address> addresses = null;
        String errorMessage = "";
        Bundle bundle = new Bundle();

        // Get the address passed to this service through an extra.
        String address = intent.getStringExtra(Constants.EXTRA_ADDRESS);
        // Get the result receiver
        ResultReceiver resultReceiver = intent.getParcelableExtra(Constants.EXTRA_RESULT_RECEIVER);

        if (Geocoder.isPresent() && Utils.checkInternetConnection(this)) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                addresses = geocoder.getFromLocationName(address, 1);
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                errorMessage = getString(R.string.location_service_is_not_available);
                Log.e(TAG, errorMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid address.
                errorMessage = getString(R.string.invalid_address_parameter);
                Log.e(TAG, errorMessage + ". " +
                        "Address = \"" + address + "\"", illegalArgumentException);
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
            double latitude = addresses.get(0).getLatitude();
            double longitude = addresses.get(0).getLongitude();

            Log.d(TAG, getString(R.string.address_found));
            bundle.putParcelable(Constants.RESULT_MSG, new LatLng(latitude, longitude));
            resultReceiver.send(Constants.RESULT_SUCCESS, bundle);
        }
    }
}