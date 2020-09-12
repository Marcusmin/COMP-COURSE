package com.example.lab5;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private LocationManager locationManager;
    private Location location;
    private final long TWOMIN = 1000 * 60 * 2;
    private final int ACCESS_LOCATION_CODE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) this.getSystemService(
                getApplicationContext().LOCATION_SERVICE
        );
        Button getGPSStatusButton = findViewById(R.id.GetGPSStatus);
        getGPSStatusButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getGPSStatus();
                    }
                }
        );
        Button getLocationButton = findViewById(R.id.GetLocation);
        getLocationButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getLocation();
                    }
                }
        );
    }

    private void getLocation() {
        LocationListener networkLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                makeUseNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        LocationListener gpsLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                makeUseNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                0,
                networkLocationListener
        );
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0,
                gpsLocationListener
        );
    }
    private void makeUseNewLocation(Location newLocation) {
        int accuracyDiff;
        long timeDiff;
        if (this.location == null) {
            this.location = newLocation;
        } else {
            accuracyDiff = (int) (this.location.getAccuracy() - newLocation.getAccuracy());
            timeDiff = (int) newLocation.getTime() - this.location.getTime();
            boolean isTooOld = timeDiff > TWOMIN;
            boolean isMoreAccuracy = accuracyDiff > 0;  // newLocation's accuracy is smaller
            if (isTooOld) {
                this.location = newLocation;
            } else if (isMoreAccuracy){
                this.location = newLocation;
            }
        }
        Date date = new Date(this.location.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String[] outputField = new String[6];
        outputField[0] = "Data/Time: "+sdf.format(date);
        outputField[1] = "Provider: "+this.location.getProvider();
        outputField[2] = "Accuracy: "+this.location.getAccuracy();
        // may not support altitude
        if (this.location.hasAltitude()) {
            outputField[3] = "Altitude: "+ this.location.getAltitude();
        } else {
            outputField[3] = "Altitude: "+"null";
        }
        outputField[4] = "Latitude: "+this.location.getLatitude();
        outputField[5] = "Speed: "+this.location.getSpeed();
        String outputText = new String();
        for(String s: outputField) {
            outputText += s + "\n";
        }
        TextView positoinInfo = findViewById(R.id.LocationInfo);
        positoinInfo.setText(outputText);
    }
    // Define a listener that responds to location updates
    private void getGPSStatus() {
        boolean isGPSEnabled =
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        TextView gpsStatus = findViewById(R.id.GPSStatus);
        if (!isGPSEnabled) {
            gpsStatus.setText("GPS is not active");
            SettingGPSDialog();
        } else {
            gpsStatus.setText("GPS is active");
        }
    }

    private void SettingGPSDialog() {
        AlertDialog.Builder alertDialog =
                new AlertDialog.Builder(this);
        String alertText = "GPS is not enabled. " +
                "Do you want to go to setting the menu";
        alertDialog.setMessage(alertText)
                .setTitle("Setting the GPS")
                .setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", null);
        alertDialog.show();
    }
}
