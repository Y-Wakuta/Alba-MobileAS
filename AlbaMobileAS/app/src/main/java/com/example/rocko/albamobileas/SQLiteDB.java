package com.example.rocko.albamobileas;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by rocko on 2016/12/24.
 */

public class SQLiteDB extends SQLiteOpenHelper {
   private final static String DB_NAME = "AlbaDB";

   private final static int DB_VER = 1;

   private String _sql =null;


   public SQLiteDB(Context context,String sql){
      super(context,DB_NAME,null,DB_VER);

      _sql = sql;
   }

   @Override
   public void onCreate(SQLiteDatabase db){
      db.execSQL(_sql);
   }

   @Override
   public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){

   }
}