package com.example.soundreceiver;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class ReceiveDualToneDigits extends AppCompatActivity {
    private static final String FILENAME = "MicRecord";
    private static final int SAMPLEFREQUENCY = 44100;
    private static final int RECORDTIME = 3000;
    private static final int step=20;
    private AudioRecord mAudioRecorder;
    private File sampleFile;
    private int bufferSize;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task3_receiver);
        Button start = findViewById(R.id.Task3RStartBtn);
        start.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startRecord();
                        recordAudio();
                    }
                }
        );
    }
    private void startRecord() {
        try {
            sampleFile = new File(getFilesDir()+"/"+FILENAME);
            if (sampleFile.exists()) {
                if (!sampleFile.delete()) {
                    Log.e("Start Fail", "Delete Fail");
                    return;
                }
            }
            if (!sampleFile.createNewFile()) {
                Log.e("Start Fail", "Create Fail");
            }
            TextView status = findViewById(R.id.FreqDisplay);
            status.setText(getFilesDir()+"/"+FILENAME);
        } catch (IOException e) {
            return;
        }
    }
    // record the audio
    public void recordAudio() {
        bufferSize = AudioRecord.getMinBufferSize(
                SAMPLEFREQUENCY,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSize > 0) {
            mAudioRecorder = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLEFREQUENCY,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
            );
            mAudioRecorder.startRecording();
            new Thread(new AndioRecordThread()).start();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    stop();
                    frequencyAnalyse();
                }
            }, RECORDTIME);
        }
    }

    private class AndioRecordThread implements Runnable {
        @Override
        public void run() {
            short[] audioData = new short[bufferSize/2];
            DataOutputStream fos = null;
            try {
                fos = new DataOutputStream(new FileOutputStream(sampleFile));
                int readSize;
                while (
                        mAudioRecorder.getRecordingState()
                                == AudioRecord.RECORDSTATE_RECORDING
                ) {
                    readSize = mAudioRecorder.read(
                            audioData,
                            0,
                            audioData.length
                    );
                    if (mAudioRecorder.ERROR_INVALID_OPERATION != readSize) {
                        for(int i = 0; i < readSize; i++) {
                            fos.writeShort(audioData[i]);
                            fos.flush();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mAudioRecorder.release();
                mAudioRecorder = null;
            }
        }
    }

    private void frequencyAnalyse() {
        if (sampleFile == null) {
            return;
        }
        try {
            DataInputStream inputStream = new DataInputStream(
                    new FileInputStream(sampleFile)
            );
            // 1 sec length data
            short[] buffer = new short[SAMPLEFREQUENCY];
            for(int i = 0; i < buffer.length; i++) {
                buffer[i] = inputStream.readShort();
            }
//            Log.e("Frequency 1", ""+freqs[0]);
//            Log.e("Frequency 2", ""+freqs[1]);
            Switch aSwitch = findViewById(R.id.t3rswitch);
            int freq;
            if (aSwitch.isChecked()) {
                int[] freqs = Goertzel.getTopTwpFreq(buffer);
                freq = getDigit(freqs);
            } else {
                int [] freqs = Goertzel.getUTopTwpFreq(buffer);
                freq = getUDigit(freqs);
            }
            TextView freqText = findViewById(R.id.FreqDisplay);
            freqText.setText("freq: "+freq);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private int getDigit(int[] freqs) {
        int row, col;
        if (freqs[0] == 1209) {
            col = 0;
        } else if (freqs[0] == 1336) {
            col = 1;
        } else {
            col = 2;
        }
        if (freqs[1] == 697) {
            row = 0;
        } else if (freqs[1] == 770) {
            row = 1;
        } else {
            row = 2;
        }
        int res = row*3+col;
        return res+1;
    }
    private int getUDigit(int[] freqs) {
        int row, col;
        if (freqs[0] == 17000) {
            col = 0;
        } else if (freqs[0] == 18000) {
            col = 1;
        } else {
            col = 2;
        }
        if (freqs[1] == 16970) {
            row = 0;
        } else if (freqs[1] == 17700) {
            row = 1;
        } else {
            row = 2;
        }
        int res = row*3+col;
        return res+1;
    }
    private void stop() {
        TextView textView = findViewById(R.id.FreqDisplay);
        textView.setText("Over");
        mAudioRecorder.stop();
    }
}
