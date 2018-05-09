package woopy.domain.com.woopy;

/*-------------------------------

    - woopy -

    Created by cubycode @2017
    All Rights reserved

-------------------------------*/


import android.*;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MapScreen extends AppCompatActivity implements OnMapReadyCallback {

    /* Views */
    GoogleMap mapView;



    /* Variables */
    double latitude, longitude;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_screen);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();

        // Init Google Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.msMapView);
        mapFragment.getMapAsync(this);
        MapsInitializer.initialize(this);



        // MARK: - CLOSE BUTTON ------------------------------------
        Button closeButt = findViewById(R.id.msCloseButt);
        closeButt.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              Configs.chosenLocation = null;
              finish();
        }});



        // MARK: - OK BUTTON ------------------------------------
        Button okButt = findViewById(R.id.msOkButt);
        okButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (latitude != 0) {
                    // Set chosenLocation's coordinates
                    Configs.chosenLocation= new Location("provider");
                    Configs.chosenLocation.setLatitude(latitude);
                    Configs.chosenLocation.setLongitude(longitude);

                    finish();

                // No Location set
                } else { Configs.simpleAlert(getResources().getString(R.string.toast_You_must_move_the_Map_and_to_choose_Location_or_just_close_this_Map_screen), MapScreen.this); }
        }});


    }// end onCreate





    // MARK: - ON MAP READY ----------------------------------------------------------------
    @Override public void onMapReady(final GoogleMap googleMap) {
        mapView = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // MapView settings
        mapView.setMyLocationEnabled(false);
        mapView.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Zoom the Google Map to Deafult Location
        mapView.moveCamera(CameraUpdateFactory.newLatLng(Configs.DEFAULT_LOCATION));
        mapView.animateCamera(CameraUpdateFactory.zoomTo(10));


        // Move Map to change Location's coordinates
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                CameraPosition cp = googleMap.getCameraPosition();
                Log.i("log-", "NEW LATITUDE: " + String.valueOf(cp.target.latitude));
                Log.i("log-", "NEW LONGITUDE: " +  String.valueOf(cp.target.longitude));

                latitude = cp.target.latitude;
                longitude = cp.target.longitude;
        }});
    }



}//@end
