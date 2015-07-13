package cn.edu.sjtu.jicapstone.bloodpressure;

import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.util.Log;

/**
 * This class is to search the device.
 * It extends from AsycTask
 * @author Shaoxiang Su
 *
 */
public class SearchThread extends AsyncTask<Void, Void, BluetoothDevice> {
	
	private static String TAG = "searchThread";
	
	private DataObtainable activity;
	private BluetoothDevice device = null;
	private BluetoothAdapter adapter;
	
	public SearchThread (DataObtainable a, BluetoothAdapter ba) {
		activity = a;
		adapter = ba;
	}
	
	@Override
	protected void onPreExecute() {
		Log.i(TAG, "onPreExecute");
		activity.startProgressDialog("Searching...");
	}


	@Override
	protected BluetoothDevice doInBackground(Void... params) {
		// find the paired
		Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
		for (BluetoothDevice d : pairedDevices) {
			if (d.getName().equals("HC-05")) {
				Log.i(TAG, "doInBackground : find paired");
				device = d;
				break;
			}
		}
		if (device == null) {
			Log.i(TAG, "doInBackground:startDiscovery");
			adapter.startDiscovery();
		}
		return device;
	}
	
	@Override
	protected void onPostExecute(BluetoothDevice device) {
		activity.obtainDevice(device);
		if (device != null) {
			activity.endProgressDialog();
			new ConnectThread(activity, device).execute();
		}
	}
}
