package cn.edu.sjtu.jicapstone.bloodpressure;

/**
 * This class contains all the parameters in the project
 * @author Shaoxiang Su
 *
 */
public class Parameters {
	// the mac address of the monitor
	public static String MAC_ADDR = "20:14:05:04:37:02";
	// the uuid of bluetooth
	public static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	
	// the value of sbp and dbp for higher blood pressure and lower blood pressure
	public static int SBP_HIGHER = 160;
	public static int DBP_HIGHER = 90;
	public static int SBP_LOWER = 90;
	public static int DBP_LOWER = 60;
	
	// the factor that for heart beat loss
	public static double HEART_BEAT_SUPPLEMENT_FACTOR = 1.1;
	
	// sbp is the first pulse that SBP_FACTOR times max amplitude
	// dbp is the last pulse that DBP_FACTOR times max amplitude
	public static double SBP_FACTOR = 0.58;
	public static double DBP_FACTOR = 0.66;
	
	// the linear formula that convert signal to actual pressure
	public static double SLOP = 0.396;
	public static double BASE = 5.0;
	
	// end inflate level
	public static int START_MEASURE_FLAG = 550;
	public static int START_MEASURE_FOR_EXTRA_INFLATE_FLAG = 700;
	public static int END_MEASURE_FLAG = 110;
	
	// first step average amplitude for extra inflate
	public static double EXTRA_INFLATE_AMPLITUDE = 4;
	
	// control protocol
	public static char START_INFLATE = '1';
	public static char END_INFLATE = '2';
	public static char START_DEFLATE = '6';
	public static char COMPLETE_DEFLATE = '4';
	
}
