package com.example.lab3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.widget.TextView;

public class CheckWifiAvailable extends BroadcastReceiver {
    private MainActivity activity;
    private Channel channel;
    private WifiP2pManager manager;

    public CheckWifiAvailable(WifiP2pManager manager,
                                       Channel channel,
                                       MainActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        TextView textView = activity.findViewById(R.id.WifiStateResult);
        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            textView.setText("Wifi-Direct is available");
        } else {
            textView.setText("Wifi-Direct is unavailable");
        }

    }
}
