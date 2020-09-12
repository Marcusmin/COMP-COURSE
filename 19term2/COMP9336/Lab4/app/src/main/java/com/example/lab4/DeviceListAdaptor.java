package com.example.lab4;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class DeviceListAdaptor extends ArrayAdapter<BluetoothDevice> {
    public DeviceListAdaptor(Context context, List<BluetoothDevice> devices) {
        super(context, 0, devices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothDevice device = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.deviceinfo, parent, false);
        }
        TextView deviceName = convertView.findViewById(R.id.DeviceNameText);
        TextView deviceMacAddr = convertView.findViewById(R.id.DeviceMACText);
        deviceName.setText(device.getName());
        deviceMacAddr.setText(device.getAddress());
        return convertView;
    }
}
