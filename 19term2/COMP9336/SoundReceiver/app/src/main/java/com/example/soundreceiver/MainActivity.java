package com.example.soundreceiver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button triggerTaskBtn1 = findViewById(R.id.Task1Button);
        triggerTaskBtn1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startTask1(v);
                    }
                }
        );
        Button triggerTaskBtn1S = findViewById(R.id.Task1S);
        triggerTaskBtn1S.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startTask1S(v);
                    }
                }
        );
        Button triggerTask2Btn = findViewById(R.id.Task2Button);
        triggerTask2Btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startTask2(v);
                    }
                }
        );
        Button triggerTaskBtn2S = findViewById(R.id.Task2S);
        triggerTaskBtn2S.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startTask2S(v);
                    }
                }
        );
        Button triggerTask3Btn = findViewById(R.id.Task3Button);
        triggerTask3Btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startTask3(v);
                    }
                }
        );
        Button triggerTask3RBtn = findViewById(R.id.Task3_R);
        triggerTask3RBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startTask3R(view);
                    }
                }
        );
        Button triggerTask4Btn = findViewById(R.id.Task4);
        triggerTask4Btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startTask4(view);
                    }
                }
        );
        Button triggerTask4RBtn = findViewById(R.id.Task4_R);
        triggerTask4RBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startTask4R(view);
                    }
                }
        );
    }

    private void startTask1S(View view) {
        Intent intent = new Intent(this, Task1S.class);
        startActivity(intent);
    }

    private void startTask2S(View view) {
        Intent intent = new Intent(this, Task2S.class);
        startActivity(intent);
    }
    private void startTask1(View view) {
        Intent intent = new Intent(this, SingleToneDetecctor.class);
        startActivity(intent);
    }
    private void startTask2(View view) {
        Intent intent = new Intent(this, SendDigitWIthSingleTone.class);
        startActivity(intent);
    }
    private void startTask3(View view) {
        Intent intent = new Intent(this, SendDigitWIthDualTone.class);
        startActivity(intent);
    }
    private void startTask3R(View view) {
        Intent intent = new Intent(this, ReceiveDualToneDigits.class);
        startActivity(intent);
    }
    private void startTask4(View view) {
        Intent intent = new Intent(this, SendMessageWithSound.class);
        startActivity(intent);
    }
    private void startTask4R(View view) {
        Intent intent = new Intent(this, ReceiveMessageWithSound.class);
        startActivity(intent);
    }
}