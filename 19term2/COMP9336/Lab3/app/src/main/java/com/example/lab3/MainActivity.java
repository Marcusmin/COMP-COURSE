package com.example.lab3;

import android.content.Context;
import android.content.IntentFilter;

import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    WifiP2pManager manager;
    Channel channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //register the broadcast
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        // for task 1, simple check wifip2p state
        final Button checkWifiDirectButton = findViewById(R.id.CheckWifiP2pButton);
        checkWifiDirectButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkWifiDirect(v);
                    }
                }
        );
        final Button discoverPeerButton = findViewById(R.id.PeerDiscoveryButton);
        discoverPeerButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        discoverPeer(v);
                    }
                }
        );
    }

    // function to check if wifi p2p is enabled
    private void checkWifiDirect(View v) {
        // register the broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        CheckWifiAvailable checkWifiAvailable =
                new CheckWifiAvailable(manager, channel, this);
        registerReceiver(checkWifiAvailable, intentFilter);

    }

    // funciton to discover the peers
    private void discoverPeer(View v) {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast toast =
                        Toast.makeText(getApplicationContext(), "Scanning", Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            public void onFailure(int reason) {
                Toast toast =
                        Toast.makeText(getApplicationContext(), "Discover Fail", Toast.LENGTH_LONG);
                toast.show();

            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        registerReceiver(
                new DiscoverPeersBroadcastReceiver(manager, channel, this),
                intentFilter);
    }
}
