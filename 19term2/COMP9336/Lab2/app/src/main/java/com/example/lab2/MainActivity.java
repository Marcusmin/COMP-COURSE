package com.example.lab2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    WifiManager wifiManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // lab2
        boolean isSupport;
        final TextView textView = findViewById(R.id.isSupport);
        wifiManager =
                (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        isSupport = wifiManager.is5GHzBandSupported();
        if (isSupport) {
            textView.setText("5GHZ is supported");
        } else {
            textView.setText("5GHZ is not supported");
        }
        // a change information when wifi state change
        if (wifiManager.isWifiEnabled()) {
            TextView connectionInfo = findViewById(R.id.ConnectionInfo);
            TextView speed = findViewById(R.id.SpeedBps);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int frequency = wifiInfo.getFrequency();
            speed.setText("Link speed is "+wifiInfo.getLinkSpeed()+"Mbps");
            String freqInfo = "Frequency is "+frequency/1000.0+"GHZ";
            connectionInfo.setText(freqInfo);
        } else {
            TextView connectionInfo = findViewById(R.id.ConnectionInfo);
            connectionInfo.setText("Wifi is not enabled");
        }
        BroadcastReceiver wifiInformation = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TextView textView = findViewById(R.id.ConnectionInfo);
                TextView speed = findViewById(R.id.SpeedBps);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int frequency = wifiInfo.getFrequency();
                speed.setText("Link speed is "+wifiInfo.getLinkSpeed()+"Mbps");
                String freqInfo = "Frequency is "+frequency/1000.0+"GHZ";
                textView.setText(freqInfo);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(wifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(wifiInformation, intentFilter);
        // check if wifi is connected
    }
}
