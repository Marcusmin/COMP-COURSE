package com.example.soundreceiver;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_LONG;

public final class Task2S extends AppCompatActivity {
    private Thread runningTread;
    private AudioSendSingleTone audioSendSingleTone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task2s);
        final Switch aSwitch = findViewById(R.id.t2switch);
        Button btn1 = findViewById(R.id.t2btn1);
        btn1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            audioSendSingleTone.stopThread();
                            runningTread.interrupt();
                            runningTread = null;
                            audioSendSingleTone = null;
                        }
                        if (aSwitch.isChecked()) {
                            Toast.makeText(getApplicationContext(), "unaudible", LENGTH_LONG).show();
                            audioSendSingleTone = new AudioSendSingleTone(10500);
                        } else {
                            audioSendSingleTone = new AudioSendSingleTone(550);
                        }
                        runningTread =  new Thread(audioSendSingleTone);
                        runningTread.start();
                    }
                }
        );

        Button btn2 = findViewById(R.id.t2btn2);
        btn2.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            audioSendSingleTone.stopThread();
                            runningTread.interrupt();
                            runningTread = null;
                            audioSendSingleTone = null;
                        }
                        if (aSwitch.isChecked()) {
                            Toast.makeText(getApplicationContext(), "unaudible", LENGTH_LONG).show();
                            audioSendSingleTone = new AudioSendSingleTone(11500);
                        } else {
                            audioSendSingleTone = new AudioSendSingleTone(650);
                        }
                        runningTread =  new Thread(audioSendSingleTone);
                        runningTread.start();
                    }
                }
        );
        Button btn3 = findViewById(R.id.t2btn3);
        btn3.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            audioSendSingleTone.stopThread();
                            runningTread.interrupt();
                            runningTread = null;
                            audioSendSingleTone = null;
                        }
                        if (aSwitch.isChecked()) {
                            Toast.makeText(getApplicationContext(), "unaudible", LENGTH_LONG).show();
                            audioSendSingleTone = new AudioSendSingleTone(12500);
                        } else {
                            audioSendSingleTone = new AudioSendSingleTone(750);
                        }
                        runningTread =  new Thread(audioSendSingleTone);
                        runningTread.start();
                    }
                }
        );
        Button btn4 = findViewById(R.id.t2btn4);
        btn4.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            audioSendSingleTone.stopThread();
                            runningTread.interrupt();
                            runningTread = null;
                            audioSendSingleTone = null;
                        }
                        if (aSwitch.isChecked()) {
                            Toast.makeText(getApplicationContext(), "unaudible", LENGTH_LONG).show();
                            audioSendSingleTone = new AudioSendSingleTone(13500);
                        } else {
                            audioSendSingleTone = new AudioSendSingleTone(850);
                        }
                        runningTread =  new Thread(audioSendSingleTone);
                        runningTread.start();
                    }
                }
        );
        Button btn5 = findViewById(R.id.t2btn5);
        btn5.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            audioSendSingleTone.stopThread();
                            runningTread.interrupt();
                            runningTread = null;
                            audioSendSingleTone = null;
                        }
                        if (aSwitch.isChecked()) {
                            Toast.makeText(getApplicationContext(), "unaudible", LENGTH_LONG).show();
                            audioSendSingleTone = new AudioSendSingleTone(14500);
                        } else {
                            audioSendSingleTone = new AudioSendSingleTone(950);
                        }
                        runningTread =  new Thread(audioSendSingleTone);
                        runningTread.start();
                    }
                }
        );
        Button btn6 = findViewById(R.id.t2btn6);
        btn6.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            audioSendSingleTone.stopThread();
                            runningTread.interrupt();
                            runningTread = null;
                            audioSendSingleTone = null;
                        }
                        if (aSwitch.isChecked()) {
                            Toast.makeText(getApplicationContext(), "unaudible", LENGTH_LONG).show();
                            audioSendSingleTone = new AudioSendSingleTone(15500);
                        } else {
                            audioSendSingleTone = new AudioSendSingleTone(1050);
                        }
                        runningTread = new Thread(audioSendSingleTone);
                        runningTread.start();
                    }
                }
        );
        Button btn7 = findViewById(R.id.t2btn7);
        btn7.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            audioSendSingleTone.stopThread();
                            runningTread.interrupt();
                            runningTread = null;
                            audioSendSingleTone = null;
                        }
                        if (aSwitch.isChecked()) {
                            Toast.makeText(getApplicationContext(), "unaudible", LENGTH_LONG).show();
                            audioSendSingleTone = new AudioSendSingleTone(16500);
                        } else {
                            audioSendSingleTone = new AudioSendSingleTone(1150);
                        }
                        runningTread = new Thread(audioSendSingleTone);
                        runningTread.start();
                    }
                }
        );
        Button btn8 = findViewById(R.id.t2btn8);
        btn8.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            audioSendSingleTone.stopThread();
                            runningTread.interrupt();
                            runningTread = null;
                            audioSendSingleTone = null;
                        }
                        if (aSwitch.isChecked()) {
                            Toast.makeText(getApplicationContext(), "unaudible", LENGTH_LONG).show();
                            audioSendSingleTone = new AudioSendSingleTone(17500);
                        } else {
                            audioSendSingleTone = new AudioSendSingleTone(1250);
                        }
                        runningTread = new Thread(audioSendSingleTone);
                        runningTread.start();
                    }
                }
        );
        Button btn9 = findViewById(R.id.t2btn9);
        btn9.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            audioSendSingleTone.stopThread();
                            runningTread.interrupt();
                            runningTread = null;
                            audioSendSingleTone = null;
                        }
                        if (aSwitch.isChecked()) {
                            Toast.makeText(getApplicationContext(), "unaudible", LENGTH_LONG).show();
                            audioSendSingleTone = new AudioSendSingleTone(18500);
                        } else {
                            audioSendSingleTone = new AudioSendSingleTone(1350);
                        }
                        runningTread = new Thread(audioSendSingleTone);
                        runningTread.start();
                    }
                }
        );
        Button stopBtn = findViewById(R.id.t2stopsound);
        stopBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            audioSendSingleTone.stopThread();
                            runningTread.interrupt();
                            audioSendSingleTone = null;
                            runningTread = null;
                        }
                    }
                }
        );
    }

}
