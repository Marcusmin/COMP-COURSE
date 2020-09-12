package com.example.lab4;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> devices;
    private final int REQUEST_ENABLE_BT=1;
    private final MainActivity me = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // init field bluetoothAdaptor
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // init field neighbors
        devices = new HashSet<>();
        // task 1: check the status of bluetooth
        Button button = findViewById(R.id.CheckBluetoothStatusButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextView status = findViewById(R.id.StatusText);
                if (bluetoothAdapter == null) {
                    //device doesn't support bluetooth
                    status.setText("BLUETOOTH UNSUPPORT");
                } else if (bluetoothAdapter.isEnabled() ){
                    status.setText("BLUETOOTH IS ENABLED");
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                BroadcastReceiver afterEnableBT = new CheckBtSatus(me);
                registerReceiver(afterEnableBT, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            }
        });
        // task2: discover devices
        // create a broadcast looking for neighbors
        final Button discoverButton = findViewById(R.id.DiscoverButton);
        discoverButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BroadcastReceiver searchNeighborReceiver =
                                new DiscoverDeviceReceiver(me, devices);
                        IntentFilter discoverIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                        registerReceiver(searchNeighborReceiver, discoverIntent);
                        boolean discovering = bluetoothAdapter.startDiscovery();
                        if (!discovering) {
                            Toast errorInfo = Toast.makeText(
                                    getApplicationContext(),
                                    "Start Discovery Fail",
                                    Toast.LENGTH_LONG
                            );
                        }
                    }
                }
        );
        // also make self discoverable
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        // use default duration
        startActivity(discoverableIntent);

        // task 3: connect to the target device
        // fetch uuid from target device
        ListView devicesList = findViewById(R.id.DeviceInfoListView);
        devicesList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void
                    onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // find target device, stop discovery
                        boolean error = bluetoothAdapter.cancelDiscovery();
                        if (!error) {
                            Toast toast = Toast.makeText(
                                    getApplicationContext(),
                                    "Warnning: cannot cancel discovery",
                                    Toast.LENGTH_LONG
                            );
                            toast.show();
                        }
                        BluetoothDevice device =
                                (BluetoothDevice) parent.getItemAtPosition(position);
                        fetchDeviceUuid(device);
                    }
                }
        );
    }

    private void fetchDeviceUuid(BluetoothDevice device) {
        device.fetchUuidsWithSdp();
    }
}
