package cn.edu.sjtu.jicapstone.bloodpressure;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**
 * This interface contains functions for other threads transfer data to the activity thread.
 * @author Shaoxiang Su
 *
 */
public interface DataObtainable {
	
	public void obtainSocket (BluetoothSocket socket);
	
	public void obtainDevice (BluetoothDevice device);
	
	public void obtainData (int dbpValue, int sbpValue, int heartRate);
	
	public void startProgressDialog(String message);
	
	public void endProgressDialog();
	
	public void socketErrorHandler();
	
	
}
