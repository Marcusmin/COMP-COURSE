package com.example.lab6;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    // senor
    private SensorManager sensorManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.RView);

        // get access to sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        List<String[]> sensorInfo = new ArrayList<>();
        for(Sensor sensor: deviceSensors) {
            String[] info = new String[5];
            info[0] = "name: "+sensor.getName();
            info[1] = "vendor: "+sensor.getVendor();
            info[2] = "version: "+sensor.getVersion()+"";
            info[3] = "MaxinumRange: "+sensor.getMaximumRange()+" ";
            info[4] = "MinDelay: "+sensor.getMinDelay() + " sec";
            sensorInfo.add(info);
        }

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        MyAdaptor myAdaptor = new MyAdaptor(sensorInfo);
        recyclerView.setAdapter(myAdaptor);
    }
}
