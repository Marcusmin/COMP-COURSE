package com.example.lab3;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class DeviceListAdaptor extends ArrayAdapter<WifiP2pDevice> {
    public DeviceListAdaptor(Context context, List<WifiP2pDevice> devices) {
        super(context, 0, devices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WifiP2pDevice device = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.deviceinfo, parent, false);
        }
        TextView deviceName = convertView.findViewById(R.id.name);
        TextView deviceMAC = convertView.findViewById(R.id.MACaddr);
        deviceName.setText(device.deviceName);
        deviceMAC.setText(device.deviceAddress);
        return convertView;
    }
}
