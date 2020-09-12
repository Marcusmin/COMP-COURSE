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
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SingleToneDetecctor extends AppCompatActivity {
    private static final String FILENAME = "MicRecord";
    private static final int SAMPLEFREQUENCY = 44100;
    private static final int RECORDTIME = 3000;
    //    private static final int N = 100;
    private AudioRecord mAudioRecorder;
    private File sampleFile;
    private int bufferSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task1);
        Button recorderButton = findViewById(R.id.RecordBtn);
        recorderButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startRecord();
                        recordAudio();
                    }
                }
        );
    }
    // create sample file
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
            TextView status = findViewById(R.id.StatusText);
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
            double freq = Goertzel.getFrequency(buffer);
            TextView freqText = findViewById(R.id.FrequencyText);
            freqText.setText("Freq: "+freq);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stop() {
        TextView textView = findViewById(R.id.StatusText);
        textView.setText("Over");
        mAudioRecorder.stop();
    }
}