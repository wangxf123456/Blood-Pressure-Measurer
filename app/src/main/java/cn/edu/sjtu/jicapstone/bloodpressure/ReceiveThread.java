package cn.edu.sjtu.jicapstone.bloodpressure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Vector;

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
		activity.startProgressDialog("measureing");
	}


	@Override
	protected Void doInBackground(Void... params) {
		
		Log.i(TAG, "doInBackground:send start tag");
		try {
			mOutputStream.write(Parameters.START_INFLATE);
		} catch (IOException e) {
			Log.i(TAG, "send 1: IOException");
			e.printStackTrace();
		}
		// the current data
		int measuredData = -1;
		// for finding the max and min
		Vector<Integer> valVector = new Vector<Integer>();
		
		// while loop flag, when the measurement is ending, flag becomes false
		boolean flag = true;
		// after inflating, set startSearch to true
		boolean startSearch = false;
		
		// flags for dynamic inflate
		// when receive the first step average, set alreadyArrivedFirstStep to true
		// check value and whether we need to set extraInflateFlag to true
		boolean extraInflateFlag = false;
		boolean alreadyArrivedFirstStep = false;
		boolean extraInflatePressureFlag = false;
		
		// delete the first step data as an measurement error
		boolean ifDeleteFirstNum = false;
		
		// flag to find min and max
		int minFlag = 1;
		int maxFlag = 1;
		
		// the min and max, which comes one by one for measuring the amplitude
		int min = -1;
		int max = -1;
		
		// the last max id and min id
		// the current id which is used to check if we really achieve a new max or min
		// or just a noise
		int maxId = -1;
		int minId = -1;
		int id = 0;
		
		// check if we arrive at a new min, then we can try to calculate the amplitude
		boolean isFindNewMin = false;
		// a temp min which is used to record the old min, so we can compare two mins and
		// check if we arrive at a new step
		int localMin = -1;
	
		// the number of heart beat during the measuring time
		int beatTime = 0;
		// the starting time
		long startTime = 0;
		// the ending time
		long endTime = 0;
		
		// if isInflatedEnded false and the measuredData is bigger than 520
		// then we stop inflation, start deflate and start measure
		boolean isInflateEnded = false;
		
		// record both the average amplitude and the average value of one step
		Vector <Double> amplitudeList = new Vector<Double>();
		Vector <Integer> valueList = new Vector<Integer>();

		// record the all the amplitude and the average value or every pulse in a single step 
		Vector <Integer> maxMinusMin = new Vector<Integer>();
		Vector <Integer> maxPlusMin = new Vector<Integer>();
		
		// the max amplitude in amplitudeList
		double maxAmplitude = -1;
		// the corresponding index of max amplitude
		int maxAmplitudeIndex = -1;
		
		
		
		while (flag) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(mInputStream));
				String numString;
				while ((numString = br.readLine()).length() == 0);
				try {
					measuredData = Integer.valueOf(numString);
				} catch (NumberFormatException e) {
				}
				if (measuredData > 900 || measuredData < 100)
					continue;
				
				// if measuredData > 550 and it is not extra inflate,
				// or measuredData > 700 and it is extra inflate,
				// then end inflate.
				if (!isInflateEnded &&
						((measuredData > Parameters.START_MEASURE_FLAG && !extraInflatePressureFlag) ||
						 (measuredData > Parameters.START_MEASURE_FOR_EXTRA_INFLATE_FLAG && extraInflatePressureFlag))) {
					Log.i(TAG, "end inflate");
					// end inflate
					mOutputStream.write(Parameters.END_INFLATE);
					isInflateEnded = true;
					// wait for 2 sec
					/*
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					*/
					// start deflate
					mOutputStream.write(Parameters.START_DEFLATE);
					// start search
					startSearch = true;
				}
				
				// if we need one extra inflate
				if (extraInflateFlag) {
					Log.i(TAG, "extra inflate setting");
					extraInflateFlag = false;
					
					// tell the inflate condition that we need extraInflatePressureFlag
					extraInflatePressureFlag = true;
					
					// start inflate
					mOutputStream.write(Parameters.START_INFLATE);
					// set startSearch to false, no subsequent operations in while loop
					startSearch = false;
					// set isInflateEnded to false, meaning we needs to inflate again
					isInflateEnded = false;
					// reset all the values used before
					id = 0;
					maxId = -1;
					minId = -1;
					
					minFlag = 1;
					maxFlag = 1;
					
					min = -1;
					max = -1;
					
					valVector.clear();
					
					isFindNewMin = false;
					localMin = -1;
					
					beatTime = 0;
					startTime = 0;
					
					ifDeleteFirstNum = false;
				}
				
				
				if (startSearch) {
					++id;
					minFlag = 1;
					maxFlag = 1;
					valVector.add(measuredData);
					if (valVector.size() == 15) {
						valVector.remove(0);
						for (int i = 0; i < valVector.size(); ++i) {
							if (valVector.elementAt(8) < valVector.elementAt(i))
								maxFlag = 0;
							if (valVector.elementAt(8) > valVector.elementAt(i))
								minFlag = 0;
						}
						isFindNewMin = false;
						
						if (maxFlag == 1 && (maxId == -1 || id - maxId >= 10)) {
							maxId = id;
							// store in local max
							max = valVector.elementAt(8);
							Log.i(TAG, "local max obtained: " + max);
							if (startTime == 0) 
								startTime = System.currentTimeMillis();
							beatTime ++;
							
						}
						if (minFlag == 1 && (minId == -1 || id - minId >= 10)) {
							minId = id;
							localMin = min;
							min = valVector.elementAt(8);							
							Log.i(TAG, "local min obtained: " + min);
							isFindNewMin = true;
						}
					}
				
					if (max != -1 && isFindNewMin) {
						int diff = max - min;
						Log.i(TAG, "diff: " + diff);
						
						if (localMin - min > 10) {
							Log.i(TAG, "arrive at a new step");
							// reset max
							max = -1;
							
							// find the average of two local vectors and clear them
							int maxMinusMinSum = 0;
							int maxPlusMinSum = 0;
							for (int i = 0; i < maxMinusMin.size(); ++i) {
								maxMinusMinSum += maxMinusMin.elementAt(i);
								maxPlusMinSum += maxPlusMin.elementAt(i);
							}
							// find the average
							if (maxMinusMinSum != 0) {
								double maxMinusMinAverage = (double)maxMinusMinSum / maxMinusMin.size();
								int maxPlusMinAverage = maxPlusMinSum / maxMinusMin.size() / 2;
								Log.i(TAG, "maxMinusMinAverage: " + maxMinusMinAverage);
								
								if (!ifDeleteFirstNum) {
									Log.i(TAG, "delete the first step as an error");
									ifDeleteFirstNum = true;
									// clear them
									maxMinusMin.clear();
									maxPlusMin.clear();
									valVector.clear();
									continue;
								}
									
								// if this is the first step, we check if we needs to extra inflate
								if (!alreadyArrivedFirstStep && ifDeleteFirstNum) {
									Log.i(TAG, "first step");
									alreadyArrivedFirstStep = true;
									if (maxMinusMinAverage > Parameters.EXTRA_INFLATE_AMPLITUDE) {
										Log.i(TAG, "prepare for the extraInflateFlag");
										extraInflateFlag = true;
										// clear them
										maxMinusMin.clear();
										maxPlusMin.clear();
										valVector.clear();
										continue;
									}
								}
								
								
								amplitudeList.add(maxMinusMinAverage);
								valueList.add(maxPlusMinAverage);
								
								int amplitudeListSize = amplitudeList.size();
								if (amplitudeListSize > 2) {
									Log.i(TAG, "check physical movement");
									if (amplitudeList.elementAt(amplitudeListSize - 1) >
											amplitudeList.elementAt(amplitudeListSize - 2) + 5) {
										Log.i(TAG, "delete one data");
										amplitudeList.remove(amplitudeListSize - 1);
										valueList.remove(amplitudeListSize - 1);
									}
								}
								
								
								// check list size, if larger than or equal to 7, start to find the max number
								int currentSize = amplitudeList.size();
								if (currentSize >= 7) {
									double potentialMaxAmplitude = amplitudeList.elementAt(currentSize - 4);
									if (potentialMaxAmplitude >= amplitudeList.elementAt(currentSize - 7)
											&& potentialMaxAmplitude >= amplitudeList.elementAt(currentSize - 6)
											&& potentialMaxAmplitude >= amplitudeList.elementAt(currentSize - 5)
											&& potentialMaxAmplitude > amplitudeList.elementAt(currentSize - 3)
											&& potentialMaxAmplitude > amplitudeList.elementAt(currentSize - 2)
											&& potentialMaxAmplitude > amplitudeList.elementAt(currentSize - 1)) {
										// find max amplitude
										maxAmplitude = potentialMaxAmplitude;
										maxAmplitudeIndex = currentSize - 4;
									}
								}
								
							}
							// clear them
							maxMinusMin.clear();
							maxPlusMin.clear();
							
							valVector.clear();
							
						} else {
							if (diff >= 0) {
								maxMinusMin.add(diff);
								maxPlusMin.add(max + min);
							}
						}
					}
	
					if (maxAmplitudeIndex > 0) {
						for (int i = maxAmplitudeIndex + 1; i < amplitudeList.size() - 1; ++i) {
							double dbpStandard = maxAmplitude * Parameters.DBP_FACTOR;
							if (amplitudeList.elementAt(i) > dbpStandard
									&& amplitudeList.elementAt(i + 1) < dbpStandard) {
								dbp = valueList.elementAt(i);
							}
						}
					}
					
					// already find dbp
					// end measure
					if (dbp > 0) {
						// go out of the loop since we find all the value
						flag = false;
						// deflate immediately
						mOutputStream.write(Parameters.COMPLETE_DEFLATE);
						
						// for testing
						for (double num : amplitudeList)
							Log.i(TAG, "amplitude value: " + num);
						Log.i(TAG, "amplitude size: " + amplitudeList.size());
						for (int num : valueList)
							Log.i(TAG, "value value: " + num);
						Log.i(TAG, "value size: " + valueList.size());
						
						Log.i(TAG, "biggest: " + maxAmplitude);
						
						// find the sbp
						try {
							for (int i = 0; ; ++i) {		
								double sbpStandard = maxAmplitude * Parameters.SBP_FACTOR;
								if (amplitudeList.elementAt(i) <= sbpStandard 
										&& amplitudeList.elementAt(i + 1) >= sbpStandard) {
									sbp = valueList.elementAt(i + 1);
									break;
								}
							}
						} catch (ArrayIndexOutOfBoundsException e) {
							// out of bound
							// treat as measurement error
							// give message in measure activity
							Log.i(TAG, "out of bounds");
						}
						Log.i(TAG, "find dbp: " + dbp);
						Log.i(TAG, "find sbp: " + sbp);
						// calculate the heartRate
						endTime = System.currentTimeMillis();
						double timeRangeInMin = ((double)(endTime - startTime)) / 60000;
						heartRate = (int)((double)beatTime * Parameters.HEART_BEAT_SUPPLEMENT_FACTOR / timeRangeInMin);
					}
					
					if (measuredData < Parameters.END_MEASURE_FLAG) {
						// still not find dbp
						mOutputStream.write(Parameters.COMPLETE_DEFLATE);
						flag = false;
					}
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
