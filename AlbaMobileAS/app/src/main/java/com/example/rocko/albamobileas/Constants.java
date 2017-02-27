package com.example.rocko.albamobileas;

import android.widget.TextView;

import java.util.UUID;

/**
 * Created by rocko on 2016/11/01.
 */

public class Constants {
   public static double MpuDefault = 0.0;
   public static double MpuMoveDeg = 2.0;

   public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

   public static final String DEVICE_NAME = "HC-05";

   private static final String TAG = "AlbaMobile";

   public static final int VIEW_STATUS = 0;

   public static final int VIEW_INPUT_MPU = 1;

   public static final int VIEW_INPUT_AIRSPEED = 2;

   public static final int VIEW_SCREEN = 3;

   public static final int VIEW_MPU_PROGRESS_LEFT = 4;

   public static final int VIEW_MPU_PROGRESS_RIGHT = 5;
}
