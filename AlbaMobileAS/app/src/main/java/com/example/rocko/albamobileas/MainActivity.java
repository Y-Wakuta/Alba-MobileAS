package com.example.rocko.albamobileas;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //region GPS用オブジェクト
    private LocationManager locationManager;
    TextView Latitude;
    TextView Longitude;
    TextView Speed;
    LocationListener listener;
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

    //region 温度計用オブジェクト
    TextView temp;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //画面の向きを立て向きに固定
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //region GPS用オブジェクト
        Latitude = (TextView) findViewById(R.id.textViewLatitude);
        Longitude = (TextView) findViewById(R.id.textViewLongitude);
        Speed = (TextView) findViewById(R.id.textViewSpeed);
        //endregion

        //region 加速度計用オブジェクト
        AcceX = (TextView) findViewById(R.id.textViewAccelermeterX);
        AcceY = (TextView) findViewById(R.id.textViewAccelermeterY);
        AcceZ = (TextView) findViewById(R.id.textViewAccelermeterZ);
        //endregion

        //region ジャイロセンサ用オブジェクト
        GyroX = (TextView)findViewById(R.id.textViewGyroX);
        GyroY = (TextView)findViewById(R.id.textViewGyroY);
        GyroZ = (TextView)findViewById(R.id.textViewGyroZ);
        //endregion

        //region 温度用オブジェクト
        temp = (TextView)findViewById(R.id.textViewTemperature);
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
        gyroSensor = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> gyroSensors = acceSensor.getSensorList(Sensor.TYPE_GYROSCOPE);
        if(gyroSensors.size() > 0){
            gyroSensor.registerListener(this,gyroSensors.get(0),SensorManager.SENSOR_DELAY_UI);
        }
        //endregion

    }

    @Override
    protected void onPause() {
        super.onPause();
        acceSensor.unregisterListener(this);
        gyroSensor.unregisterListener(this);
    }

    //region 加速度計
    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        AcceX.setText(String.valueOf(event.values[0]));
        AcceY.setText(String.valueOf(event.values[1]));
        AcceZ.setText(String.valueOf(event.values[2]));

        GyroX.setText(String.valueOf(event.values[0]));
        GyroY.setText(String.valueOf(event.values[1]));
        GyroZ.setText(String.valueOf(event.values[2]));

    }
//endregion

    //region GPS
    @Override
    public void onStart() {
        super.onStart();

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Latitude.setText(String.valueOf(location.getLatitude()));
                Longitude.setText(String.valueOf(location.getLongitude()));
                Speed.setText(String.valueOf(location.getSpeed()));
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);

    }

    public void onStop() {
        super.onStop();
        locationManager.removeUpdates(listener);
    }
    //endregion

}
