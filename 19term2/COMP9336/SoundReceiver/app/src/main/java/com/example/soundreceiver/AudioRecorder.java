package com.example.soundreceiver;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;


public class AudioRecorder implements Runnable {
    private final int SAMPLEFREQUENCY = 44100;
    private int BUFFERSIZE = 0;
    private AudioRecord audioRecord;
    private short[] buffer;
    private AppCompatActivity app;
    public AudioRecorder(AppCompatActivity activity) {
         BUFFERSIZE = AudioRecord.getMinBufferSize(
                SAMPLEFREQUENCY,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
         buffer = new short[BUFFERSIZE];
         if (BUFFERSIZE > 0) {
             audioRecord = new AudioRecord(
                     MediaRecorder.AudioSource.MIC,
                     SAMPLEFREQUENCY,
                     AudioFormat.CHANNEL_IN_MONO,
                     AudioFormat.ENCODING_PCM_16BIT,
                     BUFFERSIZE);
         }
         this.app = activity;
        audioRecord.startRecording();
    }

    @Override
    public void run() {
        while(audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            int readSize = audioRecord
                    .read(buffer, 0, buffer.length, AudioRecord.READ_NON_BLOCKING);
            if (readSize != AudioRecord.ERROR_INVALID_OPERATION) {
                double frequency = Goertzel.getFrequency(buffer);
            }
        }
    }
}
