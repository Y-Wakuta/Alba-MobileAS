package com.example.rocko.albamobileas;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.*;
import java.io.OutputStreamWriter;

import android.app.Activity;

import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SensorEventListener, View.OnClickListener {

    private double _cadenceFlight = 0;
    private double _progressBarStatusRight = 0;
    TextView DebugButton;
    ProgressBar CadenceProgress;
    TextView FlightAirSpeed;
    double flightAirSpeed = 0.0;

    boolean _threadRunning = true;
    boolean isFlight = false;

    Timer timer = new Timer(true);

    //region GPS用オブジェクト
    private LocationManager locationManager;
    TextView Latitude;
    TextView Longitude;
    TextView Speed;
    TextView Accuracy;
    LocationListener GPSListener;
    //endregion

    //region 加速度計用オブジェクト
    SensorManager acceSensor;
    //endregion

    //region ジャイロ用オブジェクト
    SensorManager gyroSensor;
    //endregion

    //region 気圧計用オブジェクト
    TextView press;
    SensorManager pressSensor;
    //endregion

    Button Flight;

    //region Bluetooth用オブジェクト
    TextView AirSpeed;
    TextView MpuRoll;

    Button connect;

    Button NextButton;

    public TextView BlueStatus;

    private boolean connectFlg = false;

    private Thread _blueThread;

    public BlueTooth blueTooth = new BlueTooth();
    private boolean isRunning = true;

    private List<FullEntity> FEList = new ArrayList<>();

    private int SaveCounter = 0;

    private BluetoothAdapter _blueAdapter = BluetoothAdapter.getDefaultAdapter();

    private BluetoothDevice _blueDevice;

    private BluetoothSocket _blueSocket;

    public String StatusText;

    //region Entities
    FullEntity fullEntity = new FullEntity();

    GPSEntity gpsEntity = new GPSEntity();

    AcceEntity acceEntity = new AcceEntity();

    GyroEntity gyroEntity = new GyroEntity();

    String pressure;

    BluetoothEntity bluetoothEntity = new BluetoothEntity();
    //endregion

    FileOutputStream fos = null;

    long startTime;

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        startMainScreen();
        startTime = System.currentTimeMillis();
        //  Flight.setEnabled(false);
        try {
            _blueAdapter = BluetoothAdapter.getDefaultAdapter();
            BlueStatus.setText("Searching Device.");
            Set<BluetoothDevice> devices = _blueAdapter.getBondedDevices();
            for (BluetoothDevice device : devices) {
                if (device.getName().equals(Constants.DEVICE_NAME)) {
                    _blueDevice = device;
                }
            }
        } catch (Exception exc) {
            BlueStatus.setText("failed");
            connectFlg = false;
        }
        _blueThread = new Thread() {
            @Override
            public void run() {
                while (_threadRunning) {
                    Message valueMsg;
                    try {
                        try {
                            _blueSocket.close();
                        } catch (Exception e) {
                        }
                        _blueSocket = _blueDevice.createRfcommSocketToServiceRecord(Constants.MY_UUID);
                        Thread.sleep(500);
                        try {
                            _blueSocket.connect();
                            valueMsg = Message.obtain(blueHandler, Constants.VIEW_STATUS, "connected");
                            blueHandler.sendMessage(valueMsg);
                            //       Flight.setEnabled(true);
                        } catch (IOException e) {
                            try {
                                _blueSocket = (BluetoothSocket) _blueDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(_blueDevice, 1);
                                _blueSocket.connect();
                                valueMsg = Message.obtain(blueHandler, Constants.VIEW_STATUS, "connected");
                                blueHandler.sendMessage(valueMsg);
                                // Flight.setEnabled(true);
                                connectFlg = true;
                            } catch (IOException ie) {
                                valueMsg = Message.obtain(blueHandler, Constants.VIEW_STATUS, "connection failed");
                                blueHandler.sendMessage(valueMsg);
                            }
                            return;
                        }
                        InputStream mmInStream = null;
                        try {
                            mmInStream = _blueSocket.getInputStream();

                            byte[] buffer = new byte[1024];
                            int bytes = 0;
                            BluetoothEntity _blue = new BluetoothEntity();
                            while (isRunning) {
                                try {
                                    //inPutStreamの読み込み
                                    bytes = mmInStream.read(buffer);
                                    String readMsg = new String(buffer, 0, bytes);
                                    if (readMsg.trim() != null && !readMsg.trim().equals("")) {
                                        String[] msgline = readMsg.split(",\n,", 0);
                                        if (msgline.length > 1) {
                                            for (int i = 0; i < msgline.length && i < 1; i++) {
                                                String[] msgs = msgline[i].split(",", 0);
                                                if (4 == msgs.length) {

                                                    _blue.MpuRoll = msgs[0];
                                                    _blue.AirSpeed = msgs[1];
                                                    _blue.Cadence = msgs[2];
                                                    _blue.msg = msgs[3];
                                                    bluetoothEntity.AirSpeed = _blue.AirSpeed;
                                                    bluetoothEntity.MpuRoll = _blue.MpuRoll;

                                                    valueMsg = Message.obtain(blueHandler, Constants.VIEW_INPUT_MPU, msgs[0]);
                                                    blueHandler.sendMessage(valueMsg);
                                                    valueMsg = Message.obtain(blueHandler, Constants.VIEW_INPUT_AIRSPEED, msgs[1]);
                                                    blueHandler.sendMessage(valueMsg);

                                                    SetFlight(_blue);
                                                }
                                            }
                                        }
                                    }
                                } catch (NumberFormatException NExc) {
                                    break;
                                }
                                //ここにsleepを入れないと画面が固まる
                                Thread.sleep(450);
                            }
                        } finally {
                            mmInStream.close();
                        }
                    } catch (Exception exc) {
                        valueMsg = Message.obtain(blueHandler, Constants.VIEW_STATUS, exc.getMessage());
                        blueHandler.sendMessage(valueMsg);
                        stopThread();
                        //  isRunning = false;
                        // connectFlg = false;
                        return;
                    }
                }
            }
        };
        InitLocalFile();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fullEntity.gpsEntity = gpsEntity;
                fullEntity.acceEntity = acceEntity;
                fullEntity.gyroEntity = gyroEntity;
                fullEntity.pressure = pressure;
                fullEntity.bluetoothEntity = bluetoothEntity;
                long endTime = System.currentTimeMillis();
                fullEntity.Time = String.valueOf(endTime - startTime);
                FullEntity fullEndEntity;
                fullEndEntity = fullEntity.Clone();
                FEList.add(fullEndEntity);
            }
        }, 3500, 350);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Routine.SaveData(FEList, fos);
                FEList.clear();
            }
        }, 5000, 10000);
    }


    private void stopThread() {
        _threadRunning = false;
    }

    protected void startMainScreen() {

        setContentView(R.layout.activity_main);

        //region GPS用オブジェクト
        Latitude = (TextView) findViewById(R.id.textViewLatitude);
        Longitude = (TextView) findViewById(R.id.textViewLongitude);
        Speed = (TextView) findViewById(R.id.textViewSpeed);
        Accuracy = (TextView) findViewById(R.id.textViewAccuracy);
        //endregion

        //region 気圧計用オブジェクト
        press = (TextView) findViewById(R.id.textViewPressure);
        //endregion

        //region Bluetooth用オブジェクト
        AirSpeed = (TextView) findViewById(R.id.textViewAirSpeed);
        BlueStatus = (TextView) findViewById(R.id.textViewBlueStatus);
        MpuRoll = (TextView) findViewById(R.id.textViewMpuRoll);
        //endregion

        //region 画面遷移用オブジェクト
        Flight = (Button) findViewById(R.id.FlightButton);
        //endregion

        //region Bluetooth準備
        connect = (Button) findViewById(R.id.connectButton);

        AirSpeed.setAllCaps(false);

        connect.setOnClickListener(this);
        Flight.setOnClickListener(this);
        //endregion
    }

    @Override
    public void onClick(View v) {
        if (v.equals(connect)) {
            BlueStatus.setText("Try connect.");
            //Threadを起動
            isRunning = true;
            _threadRunning = true;
            _blueThread.start();
            connect.setEnabled(false);
        } else if (v.equals(Flight)) {
            isFlight = true;
            setFlightScreen();
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

        try {
            fos.flush();
            fos.close();
            if (_blueSocket != null)
                _blueSocket.close();
        } catch (IOException exc) {
        }

        //region Bluetooth用処理
        isRunning = false;
        //endregion
    }

    @Override
    protected void onDestroy() {
        try {
            _blueSocket.close();

        } catch (Exception exc) {
            BlueStatus.setText("");
            AirSpeed.setText("");
        }
        super.onDestroy();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER: //加速度計
                acceEntity.AcceX = String.valueOf(event.values[0]);
                acceEntity.AcceY = String.valueOf(event.values[1]);
                acceEntity.AcceZ = String.valueOf(event.values[2]);
                break;
            case Sensor.TYPE_GYROSCOPE:  //Gyroセンサー
                gyroEntity.GyroX = String.valueOf(event.values[0]);
                gyroEntity.GyroY = String.valueOf(event.values[1]);
                gyroEntity.GyroZ = String.valueOf(event.values[2]);
                break;
            case Sensor.TYPE_PRESSURE:  //気圧計
                pressure = String.valueOf(event.values[0]);
                press.setText(pressure);
        }
    }

    //region GPS
    @Override
    public void onStart() {
        super.onStart();

        GPSListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                gpsEntity.Latitude = String.valueOf(location.getLatitude());
                Latitude.setText(gpsEntity.Latitude);
                gpsEntity.Longitude = String.valueOf(location.getLongitude());
                Longitude.setText(gpsEntity.Longitude);
                gpsEntity.Speed = String.valueOf(location.getSpeed());
                Speed.setText(gpsEntity.Speed);
                gpsEntity.Accuracy = String.valueOf(location.getAccuracy());
                Accuracy.setText(gpsEntity.Accuracy);
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
        //endregion
    }

    Handler blueHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int action = msg.what;
            String msgStr = (String) msg.obj;
            if (action == Constants.VIEW_INPUT_MPU) {
                MpuRoll.setText(msgStr);
            } else if (action == Constants.VIEW_INPUT_AIRSPEED) {
                AirSpeed.setText(msgStr);
            } else if (action == Constants.VIEW_STATUS) {
                BlueStatus.setText(msgStr);
            } else if (action == Constants.VIEW_SCREEN) {
            }
        }
    };

    Handler FlightHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int action = msg.what;
            if (action == Constants.VIEW_CADENCE) {
                double msgDouble = (double) msg.obj;
                int msgInt = (int) msgDouble;
                CadenceProgress.setProgress(msgInt);
            } else if (action == Constants.VIEW_INPUT_AIRSPEED) {
                double msgDouble = (double) msg.obj;
                FlightAirSpeed.setText(String.valueOf(msgDouble));
            }
        }
    };

    //region フライト中画面
    private void setFlightScreen() {
        setContentView(R.layout.activity_flight);

        //region プログレスバーの設定
        CadenceProgress = (ProgressBar) findViewById(R.id.Cadence);
        CadenceProgress.setMax(100);
        CadenceProgress.setMinimumHeight(0);

        //endregion
        FlightAirSpeed = (TextView) findViewById(R.id.FlightAirSpeed);

        DebugButton = (Button) findViewById(R.id.DebugButton);
        DebugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFlight = false;
                startMainScreen();
                connect.setEnabled(false);
            }
        });

        NextButton = (Button) findViewById(R.id.NextButton);
        NextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isFlight) {
                    Message valueMsg;
                    valueMsg = Message.obtain(FlightHandler, Constants.VIEW_CADENCE, _cadenceFlight);
                    FlightHandler.sendMessage(valueMsg);
                    valueMsg = Message.obtain(FlightHandler, Constants.VIEW_INPUT_AIRSPEED, flightAirSpeed);
                    FlightHandler.sendMessage(valueMsg);
                    try {
                        Thread.sleep(90);
                    } catch (Exception exc) {
                    }
                }
            }
        }).start();
    }
    //endregion

    //ロール用のprogressBarの値をセットします
    void SetFlight(BluetoothEntity bt) {
        try {
            double airSpeed = Double.parseDouble(bt.AirSpeed);
            double cadence = Double.parseDouble(bt.Cadence);

            if (airSpeed <= 6.5)
                FlightAirSpeed.setBackgroundColor(Color.BLACK);
            else if (airSpeed > 6.5 && airSpeed < 7.6)
                FlightAirSpeed.setBackgroundColor(Color.YELLOW);
            else if (airSpeed >= 7.6)
                FlightAirSpeed.setBackgroundColor(Color.GREEN);

            if (_cadenceFlight < 0)
                _cadenceFlight = 0;
            if (_cadenceFlight >= 100)
                _cadenceFlight = 100;
            if (_cadenceFlight < 70)
                CadenceProgress.setBackgroundColor(Color.YELLOW);
            else if (_cadenceFlight >= 70)
                CadenceProgress.setBackgroundColor(Color.GREEN);
            else
                CadenceProgress.setProgress(Color.BLUE);

            _cadenceFlight = cadence;
            flightAirSpeed = airSpeed;
        } catch (Exception exc) {
            return;
        }
    }

    public void InitLocalFile() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM'_'DD'_'kk'_'mm");
        String sdPath = Environment.getExternalStorageDirectory().getPath() + "/Data" + sdf.format(date).toString() + ".csv";
        String sdCardState = Environment.getExternalStorageState();
        File file = new File(sdPath);
        file.getParentFile().mkdir();


        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                fos = new FileOutputStream(sdPath);
                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            } catch (IOException e) {
            }
        }
    }

}