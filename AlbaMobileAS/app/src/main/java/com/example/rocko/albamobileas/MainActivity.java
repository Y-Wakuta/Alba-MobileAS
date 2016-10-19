package com.example.rocko.albamobileas;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    //region GPS用オブジェクト
    private LocationManager locationManager;
    TextView Latitude;
    TextView Longitude;
    TextView Speed;
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
    }

    @Override

    public void onStart() {
        super.onStart();

        //region GPS
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                new LocationListener() {
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
                });
        //endregion
    }
}
