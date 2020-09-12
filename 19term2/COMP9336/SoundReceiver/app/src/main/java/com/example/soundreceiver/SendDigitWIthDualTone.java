package com.example.soundreceiver;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

public class SendDigitWIthDualTone extends AppCompatActivity{
    private Thread runningTread;
    private ToneProducer toneProducer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task3_sender);
        runningTread = null;
        final Switch aSwitch = findViewById(R.id.t3switch);
        Button btn1 = findViewById(R.id.btn1);
        btn1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            toneProducer.stop();
                            runningTread.interrupt();
                            runningTread = null;
                            toneProducer = null;
                        }
                        if (aSwitch.isChecked()) {
                            toneProducer = new ToneProducer(1209, 697);
                        } else {
                            toneProducer = new ToneProducer(17000, 16970);
                        }
                        runningTread =  new Thread(toneProducer);
                        runningTread.start();
                    }
                }
        );

        Button btn2 = findViewById(R.id.btn2);
        btn2.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            toneProducer.stop();
                            runningTread.interrupt();
                            runningTread = null;
                            toneProducer = null;
                        }
                        if (aSwitch.isChecked()) {
                            toneProducer = new ToneProducer(1336, 697);
                        } else {
                            toneProducer = new ToneProducer(18000, 16970);
                        }
                        runningTread =  new Thread(toneProducer);
                        runningTread.start();
                    }
                }
        );
        Button btn3 = findViewById(R.id.btn3);
        btn3.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            toneProducer.stop();
                            runningTread.interrupt();
                            runningTread = null;
                            toneProducer = null;
                        }
                        if (aSwitch.isChecked()) {
                            toneProducer = new ToneProducer(1477, 697);
                        } else {
                            toneProducer = new ToneProducer(19000, 16970);
                        }
                        runningTread =  new Thread(toneProducer);
                        runningTread.start();
                    }
                }
        );
        Button btn4 = findViewById(R.id.btn4);
        btn4.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            toneProducer.stop();
                            runningTread.interrupt();
                            runningTread = null;
                            toneProducer = null;
                        }
                        if (aSwitch.isChecked()) {
                            toneProducer = new ToneProducer(1209, 770);
                        } else {
                            toneProducer = new ToneProducer(17000, 17700);
                        }
                        runningTread =  new Thread(toneProducer);
                        runningTread.start();
                    }
                }
        );
        Button btn5 = findViewById(R.id.btn5);
        btn5.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            toneProducer.stop();
                            runningTread.interrupt();
                            runningTread = null;
                            toneProducer = null;
                        }
                        if (aSwitch.isChecked()) {
                            toneProducer = new ToneProducer(1363, 770);
                        } else {
                            toneProducer = new ToneProducer(18000, 17700);
                        }
                        runningTread =  new Thread(toneProducer);
                        runningTread.start();
                    }
                }
        );
        Button btn6 = findViewById(R.id.btn6);
        btn6.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            toneProducer.stop();
                            runningTread.interrupt();
                            runningTread = null;
                            toneProducer = null;
                        }
                        if (aSwitch.isChecked()) {
                            toneProducer = new ToneProducer(1477, 770);
                        } else {
                            toneProducer = new ToneProducer(19000, 17700);
                        }
                        runningTread = new Thread(toneProducer);
                        runningTread.start();
                    }
                }
        );
        Button btn7 = findViewById(R.id.btn7);
        btn7.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            toneProducer.stop();
                            runningTread.interrupt();
                            runningTread = null;
                            toneProducer = null;
                        }
                        if (aSwitch.isChecked()) {
                            toneProducer = new ToneProducer(1209, 852);
                        } else {
                            toneProducer = new ToneProducer(17000, 18520);
                        }
                        runningTread = new Thread(toneProducer);
                        runningTread.start();
                    }
                }
        );
        Button btn8 = findViewById(R.id.btn8);
        btn8.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            toneProducer.stop();
                            runningTread.interrupt();
                            runningTread = null;
                            toneProducer = null;
                        }
                        if (aSwitch.isChecked()) {
                            toneProducer = new ToneProducer(1336, 852);
                        } else {
                            toneProducer = new ToneProducer(18000, 18520);
                        }
                        runningTread = new Thread(toneProducer);
                        runningTread.start();
                    }
                }
        );
        Button btn9 = findViewById(R.id.btn9);
        btn9.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            toneProducer.stop();
                            runningTread.interrupt();
                            runningTread = null;
                            toneProducer = null;
                        }
                        if (aSwitch.isChecked()) {
                            toneProducer = new ToneProducer(1477, 852);
                        } else {
                            toneProducer = new ToneProducer(19000, 18520);
                        }
                        runningTread = new Thread(toneProducer);
                        runningTread.start();
                    }
                }
        );
        Button stopBtn = findViewById(R.id.stopsound);
        stopBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (runningTread != null) {
                            toneProducer.stop();
                            runningTread.interrupt();
                            toneProducer = null;
                            runningTread = null;
                        }
                    }
                }
        );
    }

    private class ToneProducer implements Runnable {
        private int freq1;
        private int freq2;
        private AudioTrack audioTrack;
        private int bufferSize;
        private boolean isSinging;
        public ToneProducer(int freq1, int freq2) {
            this.freq1 = freq1;
            this.freq2 = freq2;
            isSinging = true;
        }

        @Override
        public void run() {
            bufferSize = AudioTrack.getMinBufferSize(44100,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
            );
            audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    44100,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM
            );
            while (true) {
                try {
                    audioTrack.play();
                    break;
                } catch (IllegalStateException e) {
                    Log.e("UNEXPECTED", "play() trigger illegal state exception");
                }
            }
            double ph1 = 0.0;
            double ph2 = 0.0;
            short[] buffer = new short[bufferSize];
            while (isSinging) {
                for(int i = 0; i <  bufferSize; i++) {
                    buffer[i] = (short) (
                            (Short.MAX_VALUE/2)*Math.sin(ph1)+
                                    Short.MAX_VALUE/2*Math.sin(ph2)
                    );
                    ph1 += 8*Math.atan(1.0)*freq1/44100;
                    ph2 += 8*Math.atan(1.0)*freq2/44100;
                }
                audioTrack.write(buffer, 0, buffer.length);
            }
            audioTrack.stop();
            audioTrack.release();
        }
        public void stop() {
            isSinging = false;
        }
    }
}
