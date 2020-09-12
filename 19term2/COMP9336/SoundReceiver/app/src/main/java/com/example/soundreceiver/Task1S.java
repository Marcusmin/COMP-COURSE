package com.example.soundreceiver;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Task1S extends AppCompatActivity {
    private AudioSendSingleTone audioSendSingleTone;
    private Thread runningThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task1s);
        Button start = findViewById(R.id.T1SStartBtn);
        start.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText editText = findViewById(R.id.freqInput);
                        audioSendSingleTone = new AudioSendSingleTone(
                                Integer.parseInt(editText.getText().toString())
                        );
                        runningThread = new Thread(audioSendSingleTone);
                        runningThread.start();
                    }
                }
        );
        Button stop = findViewById(R.id.T1SStopBtn);
        stop.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningThread != null) {
                            audioSendSingleTone.stopThread();
                            runningThread.interrupt();
                        }
                    }
                }
        );
    }
}
