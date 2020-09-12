package com.example.gpspositioning;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private LocationManager locationManager;
    private final int ACCESS_LOCATION_CODE=0;
    private String[] output;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) this.getSystemService(
                getApplicationContext().LOCATION_SERVICE
        );
        trackLocation();
    }

    private void trackLocation() {
        LocationListener GPSLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                makeUseNewLocation(location);
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            public void onProviderEnabled(String provider) { }
            public void onProviderDisabled(String provider) { }
        };
        if (
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    ACCESS_LOCATION_CODE
            );
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0,
                GPSLocationListener
        );
    }
    private void makeUseNewLocation(Location location) {
        if (location.getSpeed() != 0) {
            output = new String[5];
            Date date = new Date(location.getTime());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String textofDate = sdf.format(date);
            output[0] = "Date/Time:"+textofDate;
            output[1] = "Longitude ="+location.getLongitude();
            output[2] = "Latitude = " + location.getLatitude();
            output[3] = "My Speed = " + location.getSpeed();
            output[4] = "GPS Accuracy = " + location.getAccuracy();
            String outputText = "My current location at " +output[0] + "is:\n";
            for(int i = 1; i < output.length; i++) {
                outputText += output[i] + "\n";
            }
            Toast toast = Toast.makeText(
                    getApplicationContext(),
                    outputText,
                    Toast.LENGTH_LONG
            );
            toast.show();
        }
    }
}
