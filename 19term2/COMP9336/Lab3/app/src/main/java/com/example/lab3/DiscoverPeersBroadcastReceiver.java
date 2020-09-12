package com.example.lab3;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DiscoverPeersBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager manager;
    private MainActivity activity;
    private Channel channel;
    private PeerListListener peerListListener;

    public DiscoverPeersBroadcastReceiver(final WifiP2pManager manager,
                                          final Channel channel,
                                          final MainActivity activity) {
        super();
        this.manager = manager;
        this.activity = activity;
        this.channel = channel;
        peerListListener = new PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                // get the device from peers
                Collection<WifiP2pDevice> devices = peers.getDeviceList();
                int permissionCheck =
                        ContextCompat.checkSelfPermission(
                                activity,
                                Manifest.permission.ACCESS_COARSE_LOCATION);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            activity,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            1
                    );
                }
                List<WifiP2pDevice> deviceList = new ArrayList<>(devices);
                DeviceListAdaptor arrayAdapter =
                        new DeviceListAdaptor(activity.getApplicationContext(), deviceList);
                ListView listView = activity.findViewById(R.id.DeviceList);
                listView.setAdapter(arrayAdapter);
                listView.setOnItemClickListener(
                        new AdapterView.OnItemClickListener() {
                            @Override
                            public void
                            onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                // connect to specific device
                                final WifiP2pDevice device =
                                        (WifiP2pDevice) parent.getItemAtPosition(position);
                                WifiP2pConfig config = new WifiP2pConfig();
                                config.deviceAddress = device.deviceAddress;
                                config.wps.setup = WpsInfo.PBC;
                                manager.connect(
                                        channel,
                                        config,
                                        new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        Toast toast =
                                                Toast.makeText(
                                                        activity.getApplicationContext(),
                                                        "Connected to "+device.deviceName,
                                                        Toast.LENGTH_LONG);
                                        toast.show();
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        Toast toast =
                                                Toast.makeText(
                                                        activity.getApplicationContext(),
                                                        "Fail",
                                                        Toast.LENGTH_LONG
                                                );
                                        toast.show();
                                    }
                                });

                            }
                        }
                );
            }
        };
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (manager != null) {
            manager.requestPeers(channel, peerListListener);
        }
    }
}
