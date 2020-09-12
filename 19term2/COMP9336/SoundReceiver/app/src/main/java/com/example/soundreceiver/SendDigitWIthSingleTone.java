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

// Task2
public class SendDigitWIthSingleTone extends AppCompatActivity {
    private static final String FILENAME = "MicRecord";
    private static final int SAMPLEFREQUENCY = 44100;
    private static final int RECORDTIME = 3000;
    private static final int step=20;
    private Switch aSwitch;
    //    private static final int N = 100;
    private AudioRecord mAudioRecorder;
    private File sampleFile;
    private int bufferSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task2);
        aSwitch = findViewById(R.id.switchBtn);
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
        Switch audbileFlagButton = findViewById(R.id.switchBtn);
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
            int digit;
            TextView freqText = findViewById(R.id.FrequencyText);
            TextView digitText = findViewById(R.id.DigitText);
            if (aSwitch.isChecked()) {
                digit = getDigit(freq);
                digitText.setText("Unaudible: "+digit);
                freqText.setText("Freq: "+freq);
            } else {
                digit = getAudibleDigit(freq);
                digitText.setText("Audible: "+digit);
                freqText.setText("Feq: "+freq);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stop() {
        TextView textView = findViewById(R.id.StatusText);
        textView.setText("Over");
        mAudioRecorder.stop();
    }

    public int getAudibleDigit(double frequency) {
        if (frequency < 600 && frequency >= 500) {
            return 1;
        } else if (frequency < 700 && frequency >= 600) {
            return 2;
        } else if (frequency < 800 && frequency >= 700) {
            return 3;
        } else if (frequency < 900 && frequency >= 800) {
            return 4;
        } else if (frequency < 1000 && frequency >= 900) {
            return 5;
        } else if (frequency < 1100 && frequency >= 1000) {
            return 6;
        } else if (frequency < 1200 && frequency >= 1100) {
            return 7;
        } else if (frequency < 1300 && frequency >= 1200) {
            return 8;
        } else if (frequency < 1400 && frequency >= 1300) {
            return 9;
        }
        return 0;
    }

    public int getDigit(double frequency) {
        if (frequency < 11000 && frequency >= 10000) {
            return 1;
        } else if (frequency < 12000 && frequency >= 11000) {
            return 2;
        } else if (frequency < 13000 && frequency >= 12000) {
            return 3;
        } else if (frequency < 14000 && frequency >= 13000) {
            return 4;
        } else if (frequency < 15000 && frequency >= 14000) {
            return 5;
        } else if (frequency < 16000 && frequency >= 15000) {
            return 6;
        } else if (frequency < 17000 && frequency >= 16000) {
            return 7;
        } else if (frequency < 18000 && frequency >= 17000) {
            return 8;
        } else if (frequency < 19000 && frequency >= 18000) {
            return 9;
        }
        return 0;
    }
}