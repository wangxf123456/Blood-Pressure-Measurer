package cn.edu.sjtu.jicapstone.bloodpressure;

import java.util.Date;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.content.Intent;
import android.widget.Toast;

import com.parse.ParseObject;

import java.io.IOException;

public class BackgroundMeasureService extends IntentService implements DataObtainable{
    private BluetoothAdapter mAdapter;
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    BroadcastReceiver mReceiver;

    private Date measuredDate;
    private static String TAG = "BackgroundMeasureService";

    private int dbp;
    private int sbp;
    private int hrate;

    public BackgroundMeasureService() {
        super("BackgroundMeasureService");
    }

    private Intent callIntent;

    @Override
    protected void onHandleIntent(Intent intent) {
        callIntent = intent;
        try {
            mAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mAdapter == null) {
                Log.i(TAG, "mAdapter is null");
                throw new RuntimeException("Bluetooth not available");
            }

            Log.i(TAG, "set up bt, mAdapter obtained");
            if (!mAdapter.isEnabled()) {
                Log.i(TAG, "bt is not opened");
                throw new RuntimeException("Bluetooth not opened");
            }

            Log.i(TAG, "check bt, bt is turned on");

            // create broadcast
            mReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    // if find one
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                        Log.i(TAG, "BroadcastReceiver:onReceive:receive one device");
                        try {
                            BluetoothDevice tempDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            // check if the macAddr is equal to this
                            if (tempDevice.getName().equals("HC-05")) {
                                Log.i(TAG, "BroadcastReceiver:onReceive:receive the needed one");
                                mDevice = tempDevice;
                                mAdapter.cancelDiscovery();
                                endProgressDialog();
                                Log.i(TAG, "endProgressDialog");
                                new ConnectThread(BackgroundMeasureService.this, mDevice).execute();
                            }
                        }
                        catch (Throwable t) {
                            AlarmReceiver.completeWakefulIntent(intent);
                            Log.e(TAG, "Error encountered processing bluetooth find intent", t);
                        }
                    }
                    if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                        AlarmReceiver.completeWakefulIntent(intent);
                    }
                }

            };
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mReceiver, filter);
            Log.d(TAG, "registerReceiver end");

            new SearchThread(this, mAdapter).execute();
        }
        catch (Throwable t) {
            unregisterReceiver(mReceiver);
            AlarmReceiver.completeWakefulIntent(intent);
            Log.e("BackgroundMeasure", "Error Handling intent.", t);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSocket = null;
        }
        unregisterReceiver(mReceiver);
    }

    public void measure() {
        new ReceiveThread(this, mSocket).execute();
        measuredDate = new Date();
    }

    @Override
    public void obtainSocket(BluetoothSocket socket) {
        mSocket = socket;

    }

    @Override
    public void obtainDevice(BluetoothDevice device) {
        mDevice = device;

    }

    @Override
    public void obtainData(int dbpValue, int sbpValue, int heartRate) {
        dbp = dbpValue;
        sbp = sbpValue;
        hrate = heartRate;

    }

    private String status;
    @Override
    public void startProgressDialog(String message) {
        status = message;
    }

    @Override
    public void endProgressDialog() {
        System.out.println(status);
        System.out.println(status.equals("connecting..."));
        if (status.equals("Searching...")) { // SearchThread
            System.out.println("in searching ");
            if (mDevice == null) {
                AlarmReceiver.completeWakefulIntent(callIntent);
            }
        }
        else if (status.equals("connecting...")) { // ConnectThread
            System.out.println("in connect ");
            if (mSocket == null) {
                Log.d(TAG, "Connecting error");
                AlarmReceiver.completeWakefulIntent(callIntent);
            }
            else {
                measure();
            }
        }
        else if (status.equals("Measuring")) { // ReceiveThread
            System.out.println("in measure ");
            if (dbp < 0) {
                Log.w(TAG, "Measurement error");
                AlarmReceiver.completeWakefulIntent(callIntent);
            }

            else {
                AlarmReceiver.completeWakefulIntent(callIntent);
                // Record data

                if (MainActivity.userid.length() > 0) {
                    ParseObject record = new ParseObject("Record");
                    record.put("userid", MainActivity.userid);
                    record.put("highPressure", sbp);
                    record.put("lowPressure", dbp);
                    record.put("heartRate", hrate);

                    record.put("date", measuredDate);

                    record.saveInBackground();
                }

            }
        }
        else {
            System.out.println("in else");
            AlarmReceiver.completeWakefulIntent(callIntent);
            throw new RuntimeException("Wrong message");
        }
    }

    @Override
    public void socketErrorHandler() {
        AlarmReceiver.completeWakefulIntent(callIntent);
    }



}
