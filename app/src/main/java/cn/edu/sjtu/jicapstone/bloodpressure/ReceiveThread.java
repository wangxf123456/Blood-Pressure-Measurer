package cn.edu.sjtu.jicapstone.bloodpressure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

/**
 * This class is a thread that receives the data from monitor.
 * It extends from AsycTask.
 * @author Shaoxiang Su
 *
 */
public class ReceiveThread extends AsyncTask<Void, Void, Void> {

	private static String TAG = "ReceiveThread";

	private BluetoothSocket socket;
	private DataObtainable activity;

	private InputStream mInputStream;
	private OutputStream mOutputStream;

	private int sbp = -1;
	private int dbp = -1;
	private int heartRate = -1;

	private static int riseHeight = 450;

	private static int decendHeight = 100;

	// check if the socket is error
	private boolean socketError = false;

	public ReceiveThread (DataObtainable a, BluetoothSocket s) {
		activity = a;
		socket = s;
		try {
			mInputStream = s.getInputStream();
			mOutputStream = s.getOutputStream();
		} catch (IOException e) {
			Log.i(TAG, "constructor: getSteam error");
			e.printStackTrace();
		}
	}

	@Override
	protected void onPreExecute() {
		Log.i(TAG, "onPreExecute");
		activity.startProgressDialog("²âÁ¿ÖÐ");
	}


	@Override
	protected Void doInBackground(Void... params) {

		Log.i(TAG, "doInBackground:send start tag");
		try {
			Log.i(TAG, "RECEIVE THREAD: send: " + Parameters.START_INFLATE);
			mOutputStream.write(Parameters.START_INFLATE);
		} catch (IOException e) {
			Log.i(TAG, "send 1: IOException");
			e.printStackTrace();
		}
		// the current data
		int measuredData = -1;

		// while loop flag, when the measurement is ending, flag becomes false
		boolean flag = true;


		boolean rising = true;

		List<Integer> recvData = new ArrayList<Integer>();

		long startTimeMills = 0;

		BPCalc calc = new BPCalc();

		while (flag) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(mInputStream));
				String numString;
				while ((numString = br.readLine()).length() == 0) {
					Log.i(TAG, "RECEIVE THREAD: recv: " + numString);
				}
				try {
					measuredData = Integer.valueOf(numString);
				} catch (NumberFormatException e) {
				}

				if (rising && measuredData > riseHeight) {
					rising = false;
					mOutputStream.write(Parameters.COMPLETE_DEFLATE);
					startTimeMills = System.currentTimeMillis();
				}

				if (!rising && measuredData > decendHeight) {
					recvData.add(measuredData);
				}

				if (!rising && measuredData <= decendHeight) {
					mOutputStream.write(Parameters.END_INFLATE);
					flag = false;

					long elapsedTimeMills = System.currentTimeMillis() - startTimeMills;

					int[] dataConvert = new int[recvData.size()];
					for (int i = 0; i < recvData.size(); i++) {
						dataConvert[i] = recvData.get(i);
					}

					float sampleRateHz = (float)recvData.size() / elapsedTimeMills * 1000;

					calc.calculate(dataConvert, sampleRateHz);

					sbp = (int) calc.getSbp();
					dbp = (int) calc.getDbp();
					heartRate = 0;
				}

			} catch (IOException e) {
				e.printStackTrace();
				socketError = true;
				return null;
			}
			// while end
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void arg) {
		if (socketError) {
			activity.socketErrorHandler();
		} else {
			activity.obtainData(dbp, sbp, heartRate);
		}
		activity.endProgressDialog();
	}
}
