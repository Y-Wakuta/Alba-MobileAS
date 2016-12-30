package com.example.rocko.albamobileas;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by rocko on 2016/12/19.
 */

public class BlueTooth {

    private static final String TAG = "AlbaMobile";

    private BluetoothAdapter _blueAdapter = BluetoothAdapter.getDefaultAdapter();

    private BluetoothDevice _blueDevice;

    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final String DEVICE_NAME = "HC-05";

    private static final int VIEW_STATUS = 0;

    private static final int VIEW_INPUT = 1;

    private BluetoothSocket _blueSocket;


    public String StatusText;

    InputStream mmInStream = null;

    OutputStream mmOutputStream = null;

    Handler blueHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int action = msg.what;
            if (action == VIEW_INPUT) {
            } else if (action == VIEW_STATUS) {
            }
        }
    };

    BlueTooth() {
        _blueAdapter = BluetoothAdapter.getDefaultAdapter();
        StatusText = "Searching Device.";
        Set<BluetoothDevice> devices = _blueAdapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            if (device.getName().equals(DEVICE_NAME)) {
                StatusText = "find:" + device.getName();
                _blueDevice = device;
            }
        }
    }

    void Connect() {
        try {
            _blueSocket = _blueDevice.createRfcommSocketToServiceRecord(MY_UUID);
            _blueSocket.connect();
            mmInStream = _blueSocket.getInputStream();
            mmOutputStream = _blueSocket.getOutputStream();
        } catch (Exception exc) {
        }
    }

    BluetoothEntity Read() {

        byte[] buffer = new byte[1024];

        int bytes = 0;

        String readMsg = null;

        BluetoothEntity _blue = new BluetoothEntity();

        //inPutStreamの読み込み
        try {
            bytes = mmInStream.read(buffer);
        }catch(Exception exc){
            exc.getMessage();
        }
        Log.i(TAG, "bytes=" + bytes);
        String tempReadMsg = new String(buffer, 0, bytes);
        readMsg += tempReadMsg;
        if (readMsg.trim() != null && !readMsg.trim().equals("")) {
            Log.i(TAG, "value= " + readMsg.trim());
            String[] msgline = readMsg.split("\n", 0);
            if (msgline.length > 1) {
                for (int i = 0; i < msgline.length; i++) {
                    String[] msgs = msgline[i].split(",", 0);

                    if (msgs.length == 3) {
                        _blue.MpuRoll = msgs[0];
                        _blue.AirSpeed = msgs[1];
                        _blue.msg = msgs[2];
                    }
                }
            }

        }
        return _blue;
    }

    void Close(){
        try {
            _blueSocket.close();
        }catch(Exception exc){}
    }

    void Send(){
        Message valueMsg = new Message();
        valueMsg.what = VIEW_STATUS;
        valueMsg.obj = "connecting...";
        blueHandler.sendMessage(valueMsg);
    }
}