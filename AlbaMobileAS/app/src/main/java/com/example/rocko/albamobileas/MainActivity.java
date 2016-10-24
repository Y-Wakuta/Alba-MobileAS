package com.example.rocko.albamobileas;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    //region GPS用オブジェクト
    private LocationManager locationManager;
    TextView Latitude;
    TextView Longitude;
    TextView Speed;
    TextView Accuracy;
    LocationListener GPSListener;
    //endregion

    //region 加速度計用オブジェクト
    TextView AcceX;
    TextView AcceY;
    TextView AcceZ;
    SensorManager acceSensor;
    //endregion

    //region ジャイロ用オブジェクト
    TextView GyroX;
    TextView GyroY;
    TextView GyroZ;
    SensorManager gyroSensor;
    //endregion

    //region 気圧計用オブジェクト
    TextView press;
    SensorManager pressSensor;
    //endregion

    //region Bluetooth用オブジェクト

    Button connect;

    private static final String TAG = "AlbaMobile";

    private BluetoothAdapter _blueAdapter = BluetoothAdapter.getDefaultAdapter();

    private BluetoothDevice _blueDevice;

    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final String DEVICE_NAME = "HC-05";

    private BluetoothSocket _blueSocket;

    private Thread _blueThread;

    private boolean isRunning;

    private TextView BlueStatus;

    TextView AirSpeed;

    private static final int VIEW_STATUS = 0;

    private static final int VIEW_INPUT = 1;

    private boolean connectFlg = false;

    OutputStream mmOutputStream = null;


    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //画面の向きを縦向きに固定
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //region GPS用オブジェクト
        Latitude = (TextView) findViewById(R.id.textViewLatitude);
        Longitude = (TextView) findViewById(R.id.textViewLongitude);
        Speed = (TextView) findViewById(R.id.textViewSpeed);
        Accuracy = (TextView) findViewById(R.id.textViewAccuracy);
        //endregion

        //region 加速度計用オブジェクト
        AcceX = (TextView) findViewById(R.id.textViewAccelermeterX);
        AcceY = (TextView) findViewById(R.id.textViewAccelermeterY);
        AcceZ = (TextView) findViewById(R.id.textViewAccelermeterZ);
        //endregion

        //region ジャイロセンサ用オブジェクト
        GyroX = (TextView) findViewById(R.id.textViewGyroX);
        GyroY = (TextView) findViewById(R.id.textViewGyroY);
        GyroZ = (TextView) findViewById(R.id.textViewGyroZ);
        //endregion

        //region 気圧計用オブジェクト
        press = (TextView) findViewById(R.id.textViewPressure);
        //endregion

        //region Bluetooth用オブジェクト
        AirSpeed = (TextView) findViewById(R.id.textViewAirSpeed);
        BlueStatus = (TextView) findViewById(R.id.textViewBlueStatus);

        AirSpeed.setText("");
        BlueStatus.setText("");
        //endregion

        //region Bluetooth準備
        connect = (Button) findViewById(R.id.connectButton);

        AirSpeed.setAllCaps(false);
        _blueAdapter = BluetoothAdapter.getDefaultAdapter();
        BlueStatus.setText("Searching Device.");
        Set<BluetoothDevice> devices = _blueAdapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            if (device.getName().equals(DEVICE_NAME)) {
                BlueStatus.setText("find:" + device.getName());
                _blueDevice = device;
            }
        }

        connect.setOnClickListener(this);
        //endregion
    }

    Handler blueHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AirSpeed.clearComposingText();
            int action = msg.what;
            String msgStr = (String) msg.obj;
            if (action == VIEW_INPUT) {
                AirSpeed.setText(String.valueOf(msgStr));
            } else if (action == VIEW_STATUS) {
                BlueStatus.setText(msgStr);
            }
        }
    };


    @Override
    public void onClick(View v) {
        if (v.equals(connect)) {
            if (!connectFlg) {
                BlueStatus.setText("Try connect.");
                //Threadを起動
                _blueThread = new Thread() {
                    @Override
                    public void run() {
                        InputStream mmInStream = null;

                        Message valueMsg = new Message();
                        valueMsg.what = VIEW_STATUS;
                        valueMsg.obj = "connecting...";
                        blueHandler.sendMessage(valueMsg);

                        try {
                            _blueSocket = _blueDevice.createRfcommSocketToServiceRecord(MY_UUID);
                            _blueSocket.connect();
                            mmInStream = _blueSocket.getInputStream();
                            mmOutputStream = _blueSocket.getOutputStream();

                            byte[] buffer = new byte[1024];

                            int bytes;
                            valueMsg = new Message();
                            valueMsg.what = VIEW_STATUS;
                            valueMsg.obj = "connected.";
                            blueHandler.sendMessage(valueMsg);

                            connectFlg = true;

                            while (isRunning) {
                                //inPutStreamの読み込み
                                bytes = mmInStream.read(buffer);
                                Log.i(TAG, "bytes=" + bytes);
                                String readMsg = new String(buffer, 0, bytes);

                                if (readMsg.trim() != null && !readMsg.trim().equals("")) {
                                    Log.i(TAG, "value= " + readMsg.trim());
                                    valueMsg = new Message();
                                    valueMsg.what = VIEW_INPUT;
                                    valueMsg.obj = readMsg;
                                    blueHandler.sendMessage(valueMsg);
                                } else {

                                }
                            }
                        } catch (Exception exc) {
                            valueMsg = new Message();
                            valueMsg.what = VIEW_STATUS;
                            valueMsg.obj = "ERROR1:" + exc;
                            blueHandler.sendMessage(valueMsg);
                            try {
                                _blueSocket.close();
                            } catch (Exception e) {
                                isRunning = false;
                                connectFlg = false;
                            }
                        }
                    }
                };

                isRunning = true;
                _blueThread.start();
            }
            if (connectFlg)
                BlueStatus.setText("Already Connected.");
        }
        //endregion
    }

    @Override
    protected void onResume() {
        super.onResume();
        //region 加速計用
        acceSensor = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> acceSensors = acceSensor.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (0 < acceSensors.size()) {
            acceSensor.registerListener(this, acceSensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }
        //endregion

        //region ジャイロ
        gyroSensor = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> gyroSensors = gyroSensor.getSensorList(Sensor.TYPE_GYROSCOPE);
        if (gyroSensors.size() > 0) {
            gyroSensor.registerListener(this, gyroSensors.get(0), SensorManager.SENSOR_DELAY_UI);
        }
        //endregion

        //region 気圧計
        pressSensor = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> pressSensors = pressSensor.getSensorList(Sensor.TYPE_PRESSURE);
        if (0 < pressSensors.size()) {
            pressSensor.registerListener(this, pressSensors.get(0), SensorManager.SENSOR_DELAY_UI);
        }
        //endregion
    }

    @Override
    protected void onPause() {
        super.onPause();
        acceSensor.unregisterListener(this);
        gyroSensor.unregisterListener(this);
        pressSensor.unregisterListener(this);

        //region Bluetooth用処理
        isRunning = false;
        try {
            _blueSocket.close();
            BlueStatus.setText("");
            AirSpeed.setText("");
        } catch (Exception exc) {
        }
        //endregion
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER: //加速度計
                AcceX.setText(String.valueOf(event.values[0]));
                AcceY.setText(String.valueOf(event.values[1]));
                AcceZ.setText(String.valueOf(event.values[2]));
                break;
            case Sensor.TYPE_GYROSCOPE:  //Gyroセンサー
                GyroX.setText(String.valueOf(event.values[0]));
                GyroY.setText(String.valueOf(event.values[1]));
                GyroZ.setText(String.valueOf(event.values[2]));
                break;
            case Sensor.TYPE_PRESSURE:  //気圧計
                press.setText(String.valueOf(event.values[0]));
        }
    }

    //region GPS
    @Override
    public void onStart() {
        super.onStart();

        GPSListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Latitude.setText(String.valueOf(location.getLatitude()));
                Longitude.setText(String.valueOf(location.getLongitude()));
                Speed.setText(String.valueOf(location.getSpeed()));
                Accuracy.setText(String.valueOf(location.getAccuracy()));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, GPSListener);

    }

    public void onStop() {
        super.onStop();
        locationManager.removeUpdates(GPSListener);
    }
    //endregion

}
