package com.example.rocko.albamobileas;

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import android.content.Context;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
/**
 * Created by rocko on 2016/12/19.
 */

public class Routine {
    public static void SaveData(List<FullEntity> FEntities,FileOutputStream fos){
        for(FullEntity FE :FEntities) {
            String saveString = FE.Time + ","
                    + FE.acceEntity.AcceX + "," + FE.acceEntity.AcceY + "," + FE.acceEntity.AcceZ + ","
                    + FE.gyroEntity.GyroX + "," + FE.gyroEntity.GyroY + "," + FE.gyroEntity.GyroZ + ","
                    + FE.gpsEntity.Latitude + "," + FE.gpsEntity.Longitude + "," + FE.gpsEntity.Speed + "," + FE.gpsEntity.Accuracy + ","
                    + FE.pressure + "\n\r";
            try {
                fos.write(saveString.getBytes());
                fos.flush();
                fos.close();

            }catch (Exception exc){
                exc.printStackTrace();
            }
        }

    }

}
