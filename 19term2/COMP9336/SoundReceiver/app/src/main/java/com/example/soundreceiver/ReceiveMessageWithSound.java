package com.example.soundreceiver;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;


public class ReceiveMessageWithSound extends AppCompatActivity {
    private Deque<Character> charList = new LinkedBlockingDeque<>();
    private List<Integer> bitList = new ArrayList<>();
    private Deque<Integer> freqList = new LinkedBlockingDeque<>();
    private List<Thread> threadList = new ArrayList<>();
    private AudioRecord audioRecord;
    Handler aedgen = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task4_receiver);
        Button start = findViewById(R.id.task4_r_startbtn);
        Button stop = findViewById(R.id.task4_r_stopbtn);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView = findViewById(R.id.task4_r_freq);
                textView.setText("");
                for(Thread t: threadList) {
                    t.interrupt();
                }
                threadList.clear();
                audioRecord.stop();
                audioRecord.release();
            }
        });
        start.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startListening(view);
                    }
                }
        );
    }

    public void startListening(View view) {
        AudioRecordThread audioRecordThread = new AudioRecordThread();
        DealWithTextThread dealWithTextThread = new DealWithTextThread();
        DisplayCharThread displayThread = new DisplayCharThread();
        Thread dthread = new Thread(dealWithTextThread);
        Thread athread = new Thread(audioRecordThread);
        Thread dsthread = new Thread(displayThread);
        threadList.add(dthread);
        threadList.add(athread);
        threadList.add(dsthread);
        athread.start();
        dthread.start();
        dsthread.start();
    }
    private class DisplayCharThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                if(!charList.isEmpty()) {
                    final char ch = charList.pollFirst();
                    final TextView textView = findViewById(R.id.task4_r_freq);
//                    textView.setText(textView.getText().toString()+ch);
                    aedgen.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(textView.getText().toString()+ch);
                        }
                    });

                }
            }
        }
    }
    private class DealWithTextThread implements Runnable {
        @Override
        public void run() {
            int count = 0;
            while(true) {
                if(freqList.size() == 8) {
                    byte c = (byte) 0b00000000;
                    for(int i = 0; i <= 7; i++) {
                        int freq = freqList.pollFirst();
                        if (freq == 13000) {
                            c = (byte) (c|(1<<(7-i)));
//                            Log.e("bits", ""+c);
                        }
                    }
                    char ch = (char)c;
                    charList.add(ch);
                }
            }
        }
    }

    private class AudioRecordThread implements Runnable {
        private final int SAMPLEFREQUENCY = 44100;
        private int BUFFERSIZE = 0;
        private short[] buffer;
        private AppCompatActivity app;
        public AudioRecordThread() {
            BUFFERSIZE = AudioRecord.getMinBufferSize(
                    SAMPLEFREQUENCY,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            buffer = new short[4248];
            if (BUFFERSIZE > 0) {
                audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        SAMPLEFREQUENCY,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        BUFFERSIZE);
            }
            audioRecord.startRecording();
        }

        @Override
        public void run() {
            while(audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                int readSize = audioRecord
                        .read(buffer, 0, buffer.length, AudioRecord.READ_BLOCKING);

                if (readSize != AudioRecord.ERROR_INVALID_OPERATION) {
                    double frequency = Goertzel.getFrequency(buffer);
//                    Log.e("freq", ""+frequency);
                    if (frequency <= 14000&&frequency>=12500) {
                        freqList.add(13000);
                    } else if (frequency >= 16500 && frequency <=17500){
                        freqList.add(17000);
                    }
                }
            }
        }

    }
}
