package com.example.rocko.albamobileas;

import android.content.Context;
import android.content.res.Resources;
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
import java.util.logging.Handler;

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
    SensorManager sensor;
    //endregion

    //region 加速度計
    @Override
    protected void onResume() {
        super.onResume();

        sensor = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sensor.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (0 < sensors.size()) {
            sensor.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensor.unregisterListener(this);
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
        AcceX.setText(String.valueOf(event.values[0]));
        AcceY.setText(String.valueOf(event.values[1]));
        AcceZ.setText(String.valueOf(event.values[2]));
    }
//endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    }

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
