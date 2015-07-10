package cn.edu.sjtu.jicapstone.bloodpressure;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

/**
 * This class is to build up another thread for connecting the monitor.
 * It extends from AsyncTask and will return a BluetoothSocket.
 * It tries to connect with a device. 
 * This class first creates a ProgressDialog and after connects with the monitor, the ProgressDialog dismisses.
 * @author Shaoxiang Su
 *
 */
public class ConnectThread extends AsyncTask<Void, Void, BluetoothSocket>{

	static private String TAG = "ConnectThread";
	
	private DataObtainable activity;
	private BluetoothDevice device;
	private BluetoothSocket socket = null;
	
	public ConnectThread(DataObtainable a, BluetoothDevice d) {
		activity = a;
		device = d;
	}
	
	@Override
	protected void onPreExecute() {
		Log.i(TAG, "onPreExecute");
		activity.startProgressDialog("connecting...");
	}
	
	@Override
	protected BluetoothSocket doInBackground(Void... params) {
		try {
			UUID uuid = device.getUuids()[0].getUuid();
			socket = device.createRfcommSocketToServiceRecord(uuid);
		} catch (IOException e) {
			Log.i(TAG, "doInBackground:create socket error");
			e.printStackTrace();
		}
		try {
			socket.connect();
		} catch (IOException e) {
			socket = null;
			Log.i(TAG, "doInBackground:connect socket error");
			e.printStackTrace();
		}
		Log.i(TAG, "doInBackground:set up socket");
		if (socket == null) {
			Log.i(TAG, "doInBackground:socket is still null");
		}
		return socket;
	}
	
	@Override
	protected void onPostExecute(BluetoothSocket socket) {
		activity.obtainSocket(socket);
		activity.endProgressDialog();
	}
}
