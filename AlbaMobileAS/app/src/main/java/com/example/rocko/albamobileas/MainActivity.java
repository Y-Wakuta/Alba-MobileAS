package com.example.rocko.albamobileas;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import android.app.Activity;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends Activity implements SensorEventListener, View.OnClickListener {

    private double _progressBarStatusLeft = 0;
    private double _progressBarStatusRight = 0;
    TextView DebugButton;
    ProgressBar MpuLeft;
    ProgressBar MpuRight;
    TextView FlightAirSpeed;
    double flightAirSpeed = 0.0;
    PrintWriter writer;
    FullEntity fullEntity = new FullEntity();
    boolean _threadRunning = true;

    private Handler handler = new Handler();

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

    TextView Flight;

    //region Bluetooth用オブジェクト
    TextView AirSpeed;
    TextView MpuRoll;

    Button connect;

    public TextView BlueStatus;
    BluetoothEntity _blue;


    private boolean connectFlg = false;

    private Thread _blueThread;

    public BlueTooth blueTooth = new BlueTooth();
    private boolean isRunning = true;

    private static final String TAG = "AlbaMobile";

    private BluetoothAdapter _blueAdapter = BluetoothAdapter.getDefaultAdapter();

    private BluetoothDevice _blueDevice;

    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final String DEVICE_NAME = "HC-05";

    private static final int VIEW_STATUS = 0;

    private static final int VIEW_INPUT_MPU = 1;

    private static final int VIEW_INPUT_AIRSPEED = 2;

    private static final int VIEW_SCREEN = 3;

    private BluetoothSocket _blueSocket;

    public String StatusText;

    InputStream mmInStream = null;

    OutputStream mmOutputStream = null;

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        startMainScreen();

        try {
            _blueAdapter = BluetoothAdapter.getDefaultAdapter();
            BlueStatus.setText("Searching Device.");
            Set<BluetoothDevice> devices = _blueAdapter.getBondedDevices();
            for (BluetoothDevice device : devices) {
                if (device.getName().equals(DEVICE_NAME)) {
                    StatusText = "find:" + device.getName();
                    _blueDevice = device;
                }
            }

        } catch (Exception exc) {

            BlueStatus.setText("failed");
            connectFlg = false;
        }_blueThread = new Thread() {
            @Override
            public void run() {
                while(_threadRunning) {
                    Message valueMsg = new Message();
                    try {
                        _blueSocket = _blueDevice.createRfcommSocketToServiceRecord(MY_UUID);
                        if (_blueSocket.isConnected())
                            _blueSocket.close();
                        Thread.sleep(500);
                        try{
                            _blueSocket.connect();
                            valueMsg.what = VIEW_STATUS;
                            valueMsg.obj = "connected";
                            blueHandler.sendMessage(valueMsg);
                        }catch(IOException e){
                            try{
                                _blueSocket = (BluetoothSocket)_blueDevice.getClass().getMethod("createRfcommSocket",new Class[]{int.class}).invoke(_blueDevice,1);
                                _blueSocket.connect();
                                valueMsg.what = VIEW_STATUS;
                                valueMsg.obj = "connected";
                                blueHandler.sendMessage(valueMsg);
                                connectFlg = true;
                            }catch(IOException ie){  valueMsg = new Message();
                                valueMsg.what = VIEW_STATUS;
                                valueMsg.obj = "connection failed";
                                blueHandler.sendMessage(valueMsg);}
                        }

                        mmInStream = _blueSocket.getInputStream();
                        mmOutputStream = _blueSocket.getOutputStream();

                        byte[] buffer = new byte[1024];
                        int bytes = 0;
                        String readMsg = null;
                        BluetoothEntity _blue = new BluetoothEntity();
                        while (isRunning) {
                            try {
                                //inPutStreamの読み込み
                                try {
                                    bytes = mmInStream.read(buffer);
                                } catch (Exception exc) {
                                    exc.getMessage();
                                }
                                Log.i(TAG, "bytes=" + bytes);
                                String tempReadMsg = new String(buffer, 0, bytes);
                                readMsg += tempReadMsg;
                              /*  try {
                                    if (readMsg.length() > 100)
                                        readMsg = null;
                                }catch(Exception e) {
                                }*/
                                if (readMsg.trim() != null && !readMsg.trim().equals("")) {
                                    Log.i(TAG, "value= " + readMsg.trim());
                                    String[] msgline = readMsg.split(",\n,", 0);
                                    if (msgline.length > 1) {
                                        for (int i = 0; i < msgline.length; i++) {
                                            String[] msgs = msgline[i].split(",", 0);

                                            double[] value = new double[msgs.length];
                                            try {
                                                for (int j = 0; j < msgs.length - 1; j++) {
                                                    value[j] = Double.parseDouble(msgs[j]);
                                                }
                                            } catch (Exception exc) {
                                                continue;
                                            }
                                            if (msgs.length == 3) {

                                                _blue.MpuRoll = msgs[0];
                                                _blue.AirSpeed = msgs[1];
                                                _blue.msg = msgs[2];
                                                fullEntity.AirSpeed = _blue.AirSpeed;
                                                fullEntity.MpuRoll = _blue.MpuRoll;

                                                valueMsg = new Message();
                                                valueMsg.what = VIEW_INPUT_MPU;
                                                valueMsg.obj = msgs[0];
                                                blueHandler.sendMessage(valueMsg);

                                                valueMsg = new Message();
                                                valueMsg.what = VIEW_INPUT_AIRSPEED;
                                                valueMsg.obj = msgs[1];
                                                blueHandler.sendMessage(valueMsg);

                                                SetFlight(_blue);

                                                //これを入れないと上手く値が更新されない(これがないとView操作が重いから処理がたまってしまうとか原因か)
                                                Thread.sleep(500);
                                            }
                                        }
                                        readMsg = null;
                                    }
                                }
                            } catch (NumberFormatException NExc) {
                                continue;
                            }
                        }
                    } catch (Exception exc) {
                        valueMsg = new Message();
                        valueMsg.what = VIEW_STATUS;
                        valueMsg.obj = "connection failed";
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
                SaveData(fullEntity);
            }
        },3500,200);
    }

    private void stopThread(){
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
        MpuRoll = (TextView)findViewById(R.id.textViewMpuRoll);

        //endregion

        //region 画面遷移用オブジェクト
        Flight = (TextView) findViewById(R.id.FlightButton);
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
        } else if (v.equals(Flight)) {
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

    Handler blueHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int action = msg.what;
            String msgStr = (String) msg.obj;
            if (action == VIEW_INPUT_MPU) {
                MpuRoll.setText(msgStr);
            }else if(action== VIEW_INPUT_AIRSPEED){
                AirSpeed.setText(msgStr);
            }else if (action == VIEW_STATUS) {
                BlueStatus.setText(msgStr);
            }else if(action == VIEW_SCREEN){

            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        acceSensor.unregisterListener(this);
        gyroSensor.unregisterListener(this);
        pressSensor.unregisterListener(this);

        //region Bluetooth用処理
        isRunning = false;
        //endregion
    }

    @Override
    protected void onDestroy() {
        try {
            _blueSocket.close();
        }catch(Exception exc) {
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
                fullEntity.AcceX = String.valueOf(event.values[0]);
                AcceX.setText(fullEntity.AcceX);
                fullEntity.AcceY = String.valueOf(event.values[1]);
                AcceY.setText(fullEntity.AcceY);
                fullEntity.AcceZ = String.valueOf(event.values[2]);
                AcceZ.setText(fullEntity.AcceZ);
                break;
            case Sensor.TYPE_GYROSCOPE:  //Gyroセンサー
                fullEntity.GyroX = String.valueOf(event.values[0]);
                GyroX.setText(fullEntity.GyroX);
                fullEntity.GyroY = String.valueOf(event.values[1]);
                GyroY.setText(fullEntity.GyroY);
                fullEntity.GyroZ = String.valueOf(event.values[2]);
                GyroZ.setText(fullEntity.GyroZ);
                break;
            case Sensor.TYPE_PRESSURE:  //気圧計
                fullEntity.Pressure = String.valueOf(event.values[0]);
                press.setText(fullEntity.Pressure);
        }
    }

    //region GPS
    @Override
    public void onStart() {
        super.onStart();

        GPSListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                fullEntity.Latitude = String.valueOf(location.getLatitude());
                Latitude.setText(fullEntity.Latitude);
                fullEntity.Longitude = String.valueOf(location.getLongitude());
                Longitude.setText(fullEntity.Longitude);
                fullEntity.Speed = String.valueOf(location.getSpeed());
                Speed.setText(fullEntity.Speed);
                fullEntity.Accuracy = String.valueOf(location.getAccuracy());
                Accuracy.setText(fullEntity.Accuracy);
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

    //region フライト中画面
    private void setFlightScreen() {
        setContentView(R.layout.activity_flight);

        //region プログレスバーの設定
        MpuLeft = (ProgressBar) findViewById(R.id.MpuLeft);
        MpuLeft.setMax(100);
        MpuLeft.setMinimumHeight(0);

        MpuRight = (ProgressBar) findViewById(R.id.MpuRight);
        MpuRight.setMax(100);
        MpuRight.setMinimumHeight(0);
        //endregion
        FlightAirSpeed = (TextView) findViewById(R.id.FlightAirSpeed);

        DebugButton = (TextView) findViewById(R.id.DebugButton);
        DebugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainScreen();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MpuLeft.setProgress((int) (_progressBarStatusLeft));
                        MpuRight.setProgress((int) (_progressBarStatusRight));
                        FlightAirSpeed.setText(String.valueOf(flightAirSpeed));
                    }
                });
                try {
                    Thread.sleep(10);
                } catch (Exception exc) {
                }
            }
        }).start();
    }
    //endregion

    //ロール用のprogressBarの値をセットします
    void SetFlight(BluetoothEntity bt) {
        try {
            double roll = Double.parseDouble(bt.MpuRoll);
            double airSpeed = Double.parseDouble(bt.AirSpeed);
            double cadence = Double.parseDouble(bt.Cadence);
            if (-Constants.MpuMoveDeg < roll && roll < 0)
                _progressBarStatusLeft = (-roll - Constants.MpuDefault) * 100;
            else if (roll < -Constants.MpuMoveDeg)
                _progressBarStatusLeft = 100;
            else if (0 <= roll && roll < Constants.MpuMoveDeg)
                _progressBarStatusRight = (roll - Constants.MpuDefault) * 100;
            else if (Constants.MpuMoveDeg < roll)
                _progressBarStatusRight = 100;

            flightAirSpeed = airSpeed;
        }catch(Exception exc){return;}
    }

    public void InitLocalFile(){
        try{
            FileOutputStream out =getApplication().openFileOutput("test.csv",MODE_WORLD_READABLE);
            writer = new PrintWriter(new OutputStreamWriter(out,"UTF-8"));

        }catch(IOException exc){
            exc.printStackTrace();
        }
    }

    public void SaveData(FullEntity FE){
        String saveString = FE.Time + ","
                + FE.AcceX + "," + FE.AcceY + "," + FE.AcceZ + ","
                + FE.GyroX + "," + FE.GyroY + "," + FE.GyroZ + ","
                + FE.Latitude + "," + FE.Longitude + "," + FE.Speed + "," + FE.Accuracy + ","
                + FE.Pressure + "\n\r";
        try {
            writer.append(saveString);
            writer.close();

        }catch (Exception exc){
            exc.printStackTrace();
        }
    }
}