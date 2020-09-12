package com.example.lab4;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DiscoverDeviceReceiver extends BroadcastReceiver {
    private Set<BluetoothDevice> devices;
    private MainActivity activity;
    private final int ACCESS_COARSE_LOCATION_RQ = 2;
    public DiscoverDeviceReceiver(MainActivity activity, Set<BluetoothDevice> devices) {
        super();
        this.devices = devices;
        this.activity = activity;
        //request gps permission otherwise discovery not work
        int gpsPermissionCheck = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
        );
        if (gpsPermissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_COARSE_LOCATION_RQ
            );
        }
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int preSizeOfNeighbors = devices.size();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // allocate the device into set
            BluetoothDevice device =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            devices.add(device);
            // if a new device found, refresh the list view
            if (devices.size() != preSizeOfNeighbors) {
                // refresh listview
                List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>(devices);
                ListView deviceInfo = activity.findViewById(R.id.DeviceInfoListView);
                DeviceListAdaptor deviceListAdaptor =
                        new DeviceListAdaptor(activity.getApplicationContext(), deviceList);
                deviceInfo.setAdapter(deviceListAdaptor);
            }
        }
    }
}
