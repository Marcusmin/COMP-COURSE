package com.example.lab4;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

public class CheckBtSatus extends BroadcastReceiver {
    private MainActivity activity;
    public CheckBtSatus(MainActivity activity) {
        this.activity = activity;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            int state =
                    intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            TextView status = activity.findViewById(R.id.StatusText);
            if (state == BluetoothAdapter.STATE_ON) {
                status.setText("BLUETOOTH IS ENABLED");
            } else {
                status.setText("BLUETOOTH IS NOT ENABLED");
            }
        }
    }
}
