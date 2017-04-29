package com.example.rocko.albamobileas;

/**
 * Created by rocko on 2016/12/27.
 */

public class FullEntity{
    public String Time  = "";
    public GPSEntity gpsEntity;
    public AcceEntity acceEntity;
    public GyroEntity gyroEntity;
    public BluetoothEntity bluetoothEntity;
    public String pressure;

    FullEntity Clone() {
        FullEntity fullEntity = new FullEntity();
        fullEntity.Time = this.Time;
        fullEntity.gpsEntity = this.gpsEntity;
        fullEntity.acceEntity = this.acceEntity;
        fullEntity.gyroEntity = this.gyroEntity;
        fullEntity.bluetoothEntity = this.bluetoothEntity;
        fullEntity.pressure = this.pressure;
        return fullEntity;
    }
}
