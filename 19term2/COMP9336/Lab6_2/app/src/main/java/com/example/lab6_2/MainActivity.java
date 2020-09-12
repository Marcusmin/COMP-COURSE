package com.example.lab6_2;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor linearAC;
    private Sensor nonLinearAC;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        linearAC = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        nonLinearAC = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(
                this,
                nonLinearAC,
                SensorManager.SENSOR_DELAY_NORMAL
        );
        sensorManager.registerListener(
                this,
                linearAC,
                SensorManager.SENSOR_DELAY_NORMAL
        );
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor == linearAC) {
            float xValue = event.values[0];
            float yValue = event.values[1];
            float zValue = event.values[2];
            TextView info = findViewById(R.id.inAC);

            String res = new String(
                    "Acceleration force including gravity\n"
            );
            res += "X: "+xValue+"\n";
            res += "Y: "+yValue+"\n";
            res += "Z: "+zValue+"\n";
            info.setText(res);
        } else if (event.sensor == nonLinearAC) {
            float xValue = event.values[0];
            float yValue = event.values[1];
            float zValue = event.values[2];
            TextView textView = findViewById(R.id.exAC);
            String res = new String(
                    "Acceleration force excluding gravity\n"
            );
            res += "X: "+xValue+"\n";
            res += "Y: "+yValue+"\n";
            res += "Z: "+zValue+"\n";
            textView.setText(res);
            TextView pos = findViewById(R.id.orientation);
            if (zValue > 9) {
                pos.setText("On the Table");
            } else if (yValue > 9) {
                pos.setText("Default");
            } else if (yValue < -9) {
                pos.setText("Upside Down");
            } else if (xValue < -9 ) {
                pos.setText("Right");
            } else if (xValue > 9) {
                pos.setText("Left");
            } else {
            }
        }

    }
}
