package com.example.vbob_original;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.WellKnownTileServer;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

public class StudentTrackingActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap mapboxMap;

    private Marker busMarker;

    private TextView busNumberText;
    private TextView statusText;

    private String busNumber = "";


    private DatabaseReference busLocationReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(
                this,
                "",
                WellKnownTileServer.MapLibre
        );

        setContentView(R.layout.activity_student_tracking);

        mapView = findViewById(R.id.mapView);
        busNumberText = findViewById(R.id.busNumberText);
        statusText = findViewById(R.id.statusText);

        String regNum=getRegNumber();

        getbusNumber(regNum);


        busNumberText.setText("Bus: " + busNumber);


        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(map -> {

            mapboxMap = map;

            mapboxMap.setStyle(
                    new Style.Builder()
                            .fromUri("https://tiles.openfreemap.org/styles/liberty"),
                    style -> {

                        statusText.setText("Map loaded. Waiting for bus location");


                    }
            );
        });
    }



    private void startTrackingWhenReady() {

        if (mapboxMap != null
                && busNumber != null
                && !busNumber.isEmpty()) {

            listenToBusLocation();
        }
    }



    private void getbusNumber(String regNum) {

        DatabaseReference studentReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("Student_Detail")
                .child(regNum)
                .child("Bus");

        studentReference.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot
                    ) {

                        busNumber = snapshot.getValue(String.class);

                        if (busNumber == null || busNumber.isEmpty()) {

                            statusText.setText(
                                    "No bus assigned to this student"
                            );

                            busNumberText.setText(
                                    "Bus: Not Assigned"
                            );

                            return;
                        }

                        busNumberText.setText(
                                "Bus: " + busNumber
                        );

                        statusText.setText(
                                "Bus assigned: " + busNumber
                        );

                        startTrackingWhenReady();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error
                    ) {

                        statusText.setText(
                                "Failed to load bus details"
                        );
                    }
                }
        );
    }



    private String getRegNumber() {

        String email= FirebaseAuth.getInstance().getCurrentUser().getEmail().toString();
        int index=email.indexOf("@");
        return email.substring(0,index);


    }


    private void listenToBusLocation() {

        busLocationReference = FirebaseDatabase
                .getInstance()
                .getReference("bus_details")
                .child(busNumber);

        busLocationReference.addValueEventListener(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Double latitude =
                                snapshot.child("latitude")
                                        .getValue(Double.class);

                        Double longitude =
                                snapshot.child("longitude")
                                        .getValue(Double.class);

                        String lastUpdated=snapshot.child("last_updated")
                                .getValue(String.class);

                        if (latitude == null || longitude == null) {

                            statusText.setText(
                                    "Bus location not available"
                            );

                            return;
                        }



                        LatLng busLocation =
                                new LatLng(
                                        latitude,
                                        longitude
                                );

                        statusText.setText(
                                "Live tracking\n"+"last updated\n"+lastUpdated
                        );

                        updateBusMarker(busLocation);
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error
                    ) {

                        statusText.setText(
                                "Failed to load bus location"
                        );
                    }
                }
        );
    }

    private void updateBusMarker(
            LatLng busLocation
    ) {

        if (busMarker == null) {

            busMarker =
                    mapboxMap.addMarker(
                            new MarkerOptions()
                                    .position(busLocation)
                                    .title("Bus " + busNumber)
                    );

            mapboxMap.animateCamera(
                    CameraUpdateFactory
                            .newLatLngZoom(
                                    busLocation,
                                    15
                            )
            );

        } else {

            busMarker.setPosition(
                    busLocation
            );
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (mapView != null) {
            mapView.onDestroy();
        }
    }

}