package cn.edu.sjtu.jicapstone.bloodpressure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import android.os.Environment;
import android.util.Log;

/**
 * This class is a singleton that access the data file.
 * It can save a new data entry, restore data from the file and clear all the data. 
 * @author Shaoxiang Su
 *
 */
public class DataAccess {
	private static String TAG = "DataAccess";
	
	private static DataAccess instance = null;
	
	private static String path = "/data/cn.edu.sjtu.jicapstone.bloodpressure/";

	public static DataAccess getInstance() {
		if (instance == null)
			instance = new DataAccess();
		return instance;
	}
	
	/**
	 * This function will clear all the data in the user file.
	 */
	public void clearData () {
		File dir = Environment.getDataDirectory();
		File file = new File(dir, path + "userdata.txt");
		if (file.exists()) {
			Log.i(TAG, "clearData: file exists and going to be deleted");
			file.delete();
		} else {
			Log.i(TAG, "clearData: file does not exist");
		}
	}
	
	/**
	 * This function writes a new data entry into the file.
	 * @param dbpValue the dbp value
	 * @param sbpValue the sbp value
	 * @param heartRate the heart rate
	 * @param date the current time
	 */
	public void writeData (int dbpValue, int sbpValue, int heartRate, Date date) {
		try {
			File dir = Environment.getDataDirectory();
			File outFile = new File(dir, path + "userdata.txt");
			
			if (!outFile.createNewFile()) 
				Log.i(TAG, "writeData: file exists");
			
			Log.i(TAG, "writeData: write data");
			FileWriter fw = new FileWriter(outFile, true);
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateString = df.format(date);
			Log.i(TAG, "save data: date-" + dateString + " dbp value-" + String.valueOf(dbpValue) 
					+ " sbpValue-" + String.valueOf(sbpValue) + " rateValue-" + String.valueOf(heartRate));
			fw.write(dateString + "|" + String.valueOf(dbpValue) + "|" + String.valueOf(sbpValue) + "|" + String.valueOf(heartRate) + "\t\n");
			fw.flush();
			fw.close();
			
		} catch (IOException e) {
			Log.i(TAG, "writeData: IOException");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * This function reads all the data from the file and saves into a vector
	 * @return a vector of UserData that contains all the records.
	 */
	public Vector<UserData> readData () { 
		try {
			Vector<UserData> result = new Vector<UserData>();
			File dir = Environment.getDataDirectory();
			File inFile = new File(dir, path + "userdata.txt");
			
			if (!inFile.createNewFile())
				Log.i(TAG, "readData: file exists");
			
			FileReader fr = new FileReader(inFile);
			BufferedReader bufferedReader = new BufferedReader(fr);
			String inString = null;
			while ((inString = bufferedReader.readLine()) != null) {
				Log.i(TAG, "readData getLine: " + inString);				
				int firstDelimiterPos = inString.indexOf('|');
				int secondDelimiterPos = -1;
				for (int i = firstDelimiterPos + 1; ; ++i) {
					if (inString.charAt(i) == '|') {
						secondDelimiterPos = i;
						break;
					}
				}
				int lastDelimiterPos = inString.lastIndexOf('|');
				Log.i(TAG, "first delimiter position: " + String.valueOf(firstDelimiterPos));
				Log.i(TAG, "second delimiter position: " + String.valueOf(secondDelimiterPos));
				Log.i(TAG, "last delimiter position: " + String.valueOf(lastDelimiterPos));
				Log.i(TAG, "date is: " + inString.substring(0, firstDelimiterPos));
				Log.i(TAG, "dbp is: " + inString.substring(firstDelimiterPos + 1, secondDelimiterPos));
				Log.i(TAG, "sbp is: " + inString.substring(secondDelimiterPos + 1, lastDelimiterPos));
				Log.i(TAG, "heart rate is: " + inString.substring(lastDelimiterPos + 1));
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try {
					result.add(new UserData(df.parse(inString.substring(0, firstDelimiterPos)),
							Integer.parseInt(inString.substring(firstDelimiterPos + 1, secondDelimiterPos)), 
							Integer.parseInt(inString.substring(secondDelimiterPos + 1, lastDelimiterPos)), 
							Integer.parseInt(inString.substring(lastDelimiterPos + 1).trim()), null));
				} catch (NumberFormatException e) {
					Log.i(TAG, "readData: bad number format");
					e.printStackTrace();
				} catch (ParseException e) {
					Log.i(TAG, "readData: bad parse");
					e.printStackTrace();
				}
			}
			bufferedReader.close();
			return result;
			
		} catch (IOException e) {
			Log.i(TAG, "readData: IOException");
			e.printStackTrace();
		}
		return null;
	}
}
