package com.example.vbob_original;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.Manifest;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class DriverPage extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    String busId,dateTime;

    int turnOnLocationSwitch;
    private TextView latTextView,busIdTextView,statusTextView,dateTimeTextView;

    private TextView lonTextView;
    private Button startButton;
    String driverID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_page);

        latTextView = findViewById(R.id.latitude);
        lonTextView = findViewById(R.id.longitude);
        startButton = findViewById(R.id.current_location);
        busIdTextView=findViewById(R.id.bus_id);
        statusTextView=findViewById(R.id.status);
        dateTimeTextView=findViewById(R.id.date_time);

        turnOnLocationSwitch=0;



        statusTextView.setText("NOT ACTIVE");




        String email=FirebaseAuth.getInstance().getCurrentUser().getEmail().toString();
        int index=email.indexOf("@");
        busId=email.substring(0,index).toUpperCase();

        busIdTextView.setText(busId);












        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        locationRequest = new LocationRequest.Builder(2000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .build();


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (turnOnLocationSwitch==0) {

                    if (checkLocationPermission() == false) {
                        ActivityCompat.requestPermissions(DriverPage.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);

                    }

                    if (checkLocationPermission() == false) return;
                    else {
                        startLocationUpdates();
                        turnOnLocationSwitch=1;
                        startButton.setText("STOP LOCATION TRACKING");

                    }

                }
                else {
                    stopLocationUpdates();
                    turnOnLocationSwitch=0;
                    startButton.setText("START LOCATION TRACKING");

                }

            }
        });

        setupLocationCallback();










        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private boolean checkLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(DriverPage.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            return false;
        }
        return true;
    }



    private void startLocationUpdates() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            statusTextView.setText("ACTIVE");
            statusTextView.setTextColor(Color.parseColor("#57bc22"));

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void setupLocationCallback() {

        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {

                Location location = locationResult.getLastLocation();

                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    latTextView.setText(""+latitude);
                    lonTextView.setText("" + longitude);



                    saveLocationToFirebaseDatabase(latitude,longitude);
                }
            }
        };
    }

    private void saveLocationToFirebaseDatabase(double latitude, double longitude) {

        LocalDateTime currentDateTime = LocalDateTime.now();

        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");


        dateTime=currentDateTime.format(formatter);


        dateTimeTextView.setText(dateTime);

        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();


        databaseReference.child("bus_details").child(busId).child("latitude").setValue(latitude);
        databaseReference.child("bus_details").child(busId).child("longitude").setValue(longitude);
        databaseReference.child("bus_details").child(busId).child("last_updated").setValue(dateTime);

        if (( latitude>=12.8405658-0.00044916 && latitude<=12.8405658+0.00044916) && (longitude>=80.1530578-0.00044916 && longitude<=80.1530578+0.00044916))
        {
            if ((latitude>= 12.8405658 - 0.00044916 &&
                    latitude <= 12.8405658 + 0.00044916) &&

                    (longitude >= 80.1530578 - 0.00044916 &&
                            longitude <= 80.1530578 + 0.00044916)) {

                String expectedTime = "08:00";

                DateTimeFormatter timeFormatter =
                        DateTimeFormatter.ofPattern("HH:mm");

                String currentTime =
                        currentDateTime.format(timeFormatter);

//                String currentTime="08:20";

                LocalTime expectedLocalTime =
                        LocalTime.parse(
                                expectedTime,
                                timeFormatter
                        );

                LocalTime currentLocalTime =
                        LocalTime.parse(
                                currentTime,
                                timeFormatter
                        );

                FirebaseDatabase.getInstance()
                        .getReference()
                        .child("bus_details")
                        .child(busId)
                        .child("time_Of_Arrival")
                        .setValue(currentTime);

                if (currentLocalTime.isAfter(expectedLocalTime)) {

                    DateTimeFormatter dateFormatter=DateTimeFormatter.ofPattern("dd-MM-yyyy");

                    String todayDate=currentDateTime.format(dateFormatter);
                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child("late_buses")
                            .child(todayDate).push()
                            .setValue(busId);
                }
            }
        }


    }



    private void stopLocationUpdates() {

        fusedLocationClient.removeLocationUpdates(locationCallback);

        statusTextView.setText("NOT ACTIVE");
        statusTextView.setTextColor(Color.parseColor("#D32F2F"));

    }





}




