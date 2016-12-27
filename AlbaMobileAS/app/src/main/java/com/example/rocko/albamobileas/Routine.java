package com.example.rocko.albamobileas;

/**
 * Created by rocko on 2016/12/19.
 */

public class Routine {

    public String CreateSQL(String sql){
        sql += "create table GPSTable(";
        sql += "Time integer primary key";
        sql += ",Pressure text";
        sql += ",Latitude text";
        sql += ",Longitude text";
        sql += ",Speed text";
        sql += ",Accuracy text);";


        sql += "create table AcceTable(";
        sql += "Time integer primary key";
        sql += ",AcceX text";
        sql += ",AcceY text";
        sql += ",AcceZ text);";

        sql += "create table GyroTable(";
        sql += "Time integer primary key";
        sql += ",GyroX text";
        sql += ",GyroY text";
        sql += ",GyroZ text);";

        sql += "create table BlueTable(";
        sql += "Time integer primary key";
        sql += ",AirSpeed text";
        sql += ",Roll);";

        return sql;
    }
}
