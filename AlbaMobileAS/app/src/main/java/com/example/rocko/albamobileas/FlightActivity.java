package com.example.rocko.albamobileas;

import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FlightActivity extends AppCompatActivity {

    TextView DebugButton;
    ProgressBar MpuLeft;
    private int _progressBarStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight);

        MpuLeft = (ProgressBar)findViewById(R.id.MpuLeft);

        DebugButton = (TextView) findViewById(R.id.DebugButton);
        DebugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(_progressBarStatus < 100){
                    _progressBarStatus++;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MpuLeft.setProgress(_progressBarStatus);
                        }
                    });
                    try {
                        Thread.sleep(200);
                    }catch(Exception exc){
                    }
                }
            }
        }).start();
        MpuLeft.setMax(100);
        for(int i = 0;i < 100;i++)
            MpuLeft.setProgress(i);
        MpuLeft.setSecondaryProgress(70);
    }


}
