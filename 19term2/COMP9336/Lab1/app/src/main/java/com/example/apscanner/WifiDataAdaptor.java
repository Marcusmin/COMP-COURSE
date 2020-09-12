package com.example.apscanner;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

//import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;

public class WifiDataAdaptor extends ArrayAdapter<ScanResult> {
    private List<ScanResult> wifidata;
    private Context listContext;
    public WifiDataAdaptor(Context context, List<ScanResult> scanResults) {
        super(context, 0, scanResults);
        wifidata = scanResults;
        listContext = context;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        ScanResult scanResult = wifidata.get(pos);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.wifidata, parent, false);
        }
        // Lookup view for data population
        TextView wifiESSID = convertView.findViewById(R.id.ESSID);
        TextView wifiBSSID = convertView.findViewById(R.id.BSSID);
        TextView wifiSS = convertView.findViewById(R.id.SignalStength);
        TextView wifiEnMode = convertView.findViewById(R.id.encMode);
        // Populate the data into the template view using the data object
        wifiESSID.setText(scanResult.SSID);
        wifiBSSID.setText(scanResult.BSSID);
        wifiSS.setText(""+scanResult.level+"dBm");
        wifiEnMode.setText(scanResult.capabilities);
        // Return the completed view to render on screen
        return convertView;

    }
}
