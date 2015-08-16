package com.phong.sample;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.phong.locationservice.Constants;
import com.phong.locationservice.service.FetchAddressIntentService;
import com.phong.locationservice.service.FetchLatLngIntentService;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mMarker != null) {
            mMarker.remove();
            mMarker = null;
        }
        mMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Hello World"));
        String address = "duong 51, phuong tan tao, quan binh tan";
        FetchLatLngIntentService.start(this, address, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == Constants.RESULT_SUCCESS) {
                    LatLng d = resultData.getParcelable(Constants.RESULT_MSG);
                    Location l = new Location("Abc");
                    l.setLatitude(d.latitude);
                    l.setLongitude(d.longitude);
                    FetchAddressIntentService.start(MapsActivity.this, l, new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            Toast.makeText(MapsActivity.this, resultData.getString(Constants.RESULT_MSG), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(MapsActivity.this, resultData.getString(Constants.RESULT_MSG), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
