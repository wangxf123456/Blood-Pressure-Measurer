package cn.edu.sjtu.jicapstone.bloodpressure;

import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseObject;

/**
 * This class is the measure activity.
 * It extends from Activity. And it implements DataObtainable interface to receive data from other thread.
 * @author Shaoxiang Su
 *
 */
public class MeasureActivity extends Activity 
							implements DataObtainable{
	private static String TAG = "MeasureActivity";
	

	
	private ProgressDialog dialog;
	
	private View startMeasure;
	private View record;
	
	private int measuredDbp = -1;
	private int measuredSbp = -1;
	private int measuredRate = -1;
	private Date measuredDate;
	boolean isRecorded = false;
	
	private BluetoothAdapter mAdapter;
	private BroadcastReceiver mReceiver;
	
	private BluetoothDevice mDevice;
	private BluetoothSocket mSocket;
	
	private ImageView sbpHundred;
	private ImageView sbpTen;
	private ImageView sbpDigit;
	private ImageView dbpHundred;
	private ImageView dbpTen;
	private ImageView dbpDigit;
	private ImageView rateHundred;
	private ImageView rateTen;
	private ImageView rateDigit;
	
	private boolean isPrintNotFoundMessage = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_measure);
		
		startMeasure = findViewById(R.id.start_measure);
		record = findViewById(R.id.record_measure);
		
		sbpHundred = (ImageView)findViewById(R.id.imageView1);
		sbpTen = (ImageView)findViewById(R.id.imageView2);
		sbpDigit = (ImageView)findViewById(R.id.imageView3);
		dbpHundred = (ImageView)findViewById(R.id.imageView01);
		dbpTen = (ImageView)findViewById(R.id.imageView02);
		dbpDigit = (ImageView)findViewById(R.id.imageView03);
		rateHundred = (ImageView)findViewById(R.id.imageView11);
		rateTen = (ImageView)findViewById(R.id.imageView12);
		rateDigit = (ImageView)findViewById(R.id.imageView13);

		
		
		startMeasure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// could record for another time
				isRecorded = false;
				if (mSocket == null) {
					Toast.makeText(MeasureActivity.this, "the bluetooth is not connected", Toast.LENGTH_SHORT).show();
					Log.i(TAG, "click startMeasure, socket not setup");
				} else {
					new ReceiveThread(MeasureActivity.this, mSocket).execute();
				}
				measuredDate = new Date();
			}
		});
		
		record.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// if already record
				if (isRecorded) {
					Toast.makeText(MeasureActivity.this, "repeated record", Toast.LENGTH_SHORT).show();
					return;
				} else if (measuredDbp == -1) {
					Toast.makeText(MeasureActivity.this, "not yet measure", Toast.LENGTH_SHORT).show();
					return;
				} else {
					Toast.makeText(MeasureActivity.this, "record successfully", Toast.LENGTH_SHORT).show();
					DataAccess.getInstance().writeData(measuredDbp, measuredSbp, measuredRate, measuredDate);
					isRecorded = true;
				}
			}
			
		});

		
		
		// connect to the device
		// first check if the Bluetooth is available
		mAdapter = BluetoothAdapter.getDefaultAdapter();
				
		if (mAdapter == null) {
			Log.i(TAG, "mAdapter is null");
		}
		
		Log.i(TAG, "set up bt, mAdapter obtained");
		if (!mAdapter.isEnabled()) {
			Log.i(TAG, "bt is not opened");
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
					
					BluetoothDevice tempDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					// check if the macAddr is equal to this
					Log.i(TAG, "the mac address is " + tempDevice.getAddress().toString());
					if (tempDevice.getAddress().equals(Parameters.MAC_ADDR)) {
						Log.i(TAG, "BroadcastReceiver:onReceive:receive the needed one");
						mDevice = tempDevice;
						mAdapter.cancelDiscovery();
						endProgressDialog();
						Log.i(TAG, "endProgressDialog");
						if (mDevice == null) {
							Log.i(TAG, "find the device but mDevice is still null");
						}
						new ConnectThread(MeasureActivity.this, mDevice).execute();
					}
				}
				if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					if (!isPrintNotFoundMessage && mDevice == null) {
						Toast.makeText(MeasureActivity.this, "did not find the pi pei device", Toast.LENGTH_SHORT).show();
						mAdapter.cancelDiscovery();
						endProgressDialog();
						isPrintNotFoundMessage = true;
					}
				}
				
			}
		};
		
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(mReceiver, filter);
		Log.i(TAG, "registerReceiver end");
		
		new SearchThread(MeasureActivity.this, mAdapter).execute();	
	}	

	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mSocket != null) {
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		unregisterReceiver(mReceiver);
	}
	
	@Override
	public void obtainData(int dbpValue, int sbpValue, int heartRate) {
		// if some value is -1, measurement fail
			
		if (dbpValue < 0 || sbpValue < 0 || heartRate < 0) {
			Toast.makeText(MeasureActivity.this, "move, fail, test again", Toast.LENGTH_SHORT).show();
			return;
		}
		measuredDbp = convert(dbpValue); 
		measuredSbp = convert(sbpValue);
		measuredRate = heartRate;
		Log.i(TAG, "heartRate is: " + heartRate);
		ParseObject record = new ParseObject("Record");
		record.put("user", "tiatia");
		record.put("highPressure", measuredSbp);
		record.put("lowPressure", measuredDbp);
		record.put("heartRate", heartRate);

		Date date = new Date();
		record.put("date", date);

		record.saveInBackground();
		int measuredSbpHundred = measuredSbp / 100;
		int measuredSbpTen = (measuredSbp % 100) / 10;
		int measuredSbpDigit = measuredSbp % 10;
		sbpHundred.setImageResource(getNumberPic(measuredSbpHundred));
		sbpTen.setImageResource(getNumberPic(measuredSbpTen));
		sbpDigit.setImageResource(getNumberPic(measuredSbpDigit));
		
		int measuredDbpHundred = measuredDbp / 100;
		int measuredDbpTen = (measuredDbp % 100) / 10;
		int measuredDbpDigit = measuredDbp % 10;
		dbpHundred.setImageResource(getNumberPic(measuredDbpHundred));
		dbpTen.setImageResource(getNumberPic(measuredDbpTen));
		dbpDigit.setImageResource(getNumberPic(measuredDbpDigit));
		

		int measuredRateHundred = measuredRate / 100;
		int measuredRateTen = (measuredRate % 100) / 10;
		int measuredRateDigit = measuredRate % 10;
		rateHundred.setImageResource(getNumberPic(measuredRateHundred));
		rateTen.setImageResource(getNumberPic(measuredRateTen));
		rateDigit.setImageResource(getNumberPic(measuredRateDigit));
	}

	@Override
	public void socketErrorHandler() {
		Log.i(TAG, "socket close");
		Toast.makeText(MeasureActivity.this, "bluetooth out of connect",	Toast.LENGTH_SHORT).show();
		Intent intent = new Intent();
		intent.setClass(MeasureActivity.this, MainActivity.class);
		MeasureActivity.this.startActivity(intent);
		return;
	}
	
	@Override
	public void obtainDevice(BluetoothDevice device) {
		mDevice = device;
	}
	
	@Override
	public void obtainSocket(BluetoothSocket socket) {
		mSocket = socket;
	}
	
	@Override
	public void startProgressDialog(String message) {
		dialog = ProgressDialog.show(MeasureActivity.this, "wait...", message);
	}

	@Override
	public void endProgressDialog() {
		dialog.dismiss();
	}
	
	/**
	 * This function is to set the right picture resources.
	 * @param num the number that is going to be displayed.
	 * @return the picture resoruce id.
	 */
	private int getNumberPic(int num) {
		switch(num) {
		case 0:
			return R.drawable.number0;
		case 1:
			return R.drawable.number1;
		case 2:
			return R.drawable.number2;
		case 3:
			return R.drawable.number3;
		case 4:
			return R.drawable.number4;
		case 5: 
			return R.drawable.number5;
		case 6:
			return R.drawable.number6;
		case 7: 
			return R.drawable.number7;
		case 8:
			return R.drawable.number8;
		case 9:
			return R.drawable.number8;
		default:
			return R.drawable.number0;	
		}
	}
	
	/**
	 * This function is to convert the signal into the blood pressure
	 * @param val the signal obtained from monitor
	 * @return the value in mmHg
	 */
	public int convert(int val) {
		return (int)(Parameters.SLOP * (double) val + Parameters.BASE);
	}
}
