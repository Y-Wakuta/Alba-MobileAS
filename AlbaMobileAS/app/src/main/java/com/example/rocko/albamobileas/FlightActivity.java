package com.example.rocko.albamobileas;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FlightActivity extends AppCompatActivity {

    TextView DebugButton;
    ProgressBar MpuLeft;
    ProgressBar MpuRight;
    private int _progressBarStatusLeft = 0;
    private Handler handler = new Handler();
    private BluetoothEntities blueEntity = new BluetoothEntities();

    FlightActivity(BluetoothEntities be) {
        blueEntity.AirSpeed = be.AirSpeed;
        blueEntity.MpuRoll = be.MpuRoll;
        blueEntity.msg = be.msg;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight);

        MpuLeft = (ProgressBar)findViewById(R.id.MpuLeft);
        MpuLeft.setMax(100);
        MpuLeft.setMinimumHeight(0);



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
                while(_progressBarStatusLeft < 100){
                    _progressBarStatusLeft++;
                    if(_progressBarStatusLeft == 100)
                        _progressBarStatusLeft = 0;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MpuLeft.setProgress(_progressBarStatusLeft);
                        }
                    });
                    try {
                        Thread.sleep(10);
                    }catch(Exception exc){
                    }
                }
            }
        }).start();

        for(int i = 0;i < 100;i++)
            MpuLeft.setProgress(i);
    }


}
