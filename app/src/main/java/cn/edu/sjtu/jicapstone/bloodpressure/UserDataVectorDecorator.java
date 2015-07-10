package cn.edu.sjtu.jicapstone.bloodpressure;

import java.util.Vector;
import java.util.Date;
import java.util.List;

/**
 * This class is the abstract class for all the vector decorators.
 * It uses a vector of UserData an transform it into several vectors, dates and values.
 * @author Shaoxiang Su
 *
 */
public abstract class UserDataVectorDecorator {
	protected Vector<UserData> userDataVector;
	
	protected Vector<Date> rangeDate;
	protected Vector<Vector<Integer>> averageValue;
	protected Vector<Integer> averageRate;
	
	protected double startTime;
	protected double endTime;
	
	protected double startValue;
	protected double endValue;
	
	private String titles[];
	public UserDataVectorDecorator(Vector<UserData> userDataVector) {
		this.userDataVector = userDataVector;
		titles = new String[] {"舒张压", "收缩压"};
		
		// construct the data
		rangeDate = new Vector<Date> ();
		averageValue = new Vector<Vector<Integer>> ();			
		// two value vectors 
		averageValue.add(new Vector<Integer>());
		averageValue.add(new Vector<Integer>());
		// heart rate vector
		averageRate = new Vector<Integer> ();
	}
	/**
	 * This function gets the start time
	 * @return the start time
	 */
	public double getStartTime() {
		return startTime;
	}
	/**
	 * This function sets the start time
	 * @param startTime the time chart starts to display
	 */
	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}
	/**
	 * This function gets the end time
	 * @return the end time
	 */
	public double getEndTime() {
		return endTime;
	}
	/**
	 * This function sets the end time
	 * @param endTime the end time of the chart
	 */
	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}
	/**
	 * This function gets the start value
	 * @return the start value
	 */
	public double getStartValue() {
		return startValue;
	}
	/**
	 * This function sets the start value
	 * @param startValue the start value of the chart
	 */
	public void setStartValue(double startValue) {
		this.startValue = startValue;
	}
	/**
	 * This function gets the end value
	 * @return the end value
	 */
	public double getEndValue() {
		return endValue;
	}
	/**
	 * This function sets the end value
	 * @param startValue the end value of the chart
	 */
	public void setEndValue(double endValue) {
		this.endValue = endValue;
	}
	/**
	 * This function gets the titles of different line
	 * @return the title vector
	 */
	String[] getTitles() {
		return titles;
	}
	/**
	 * This function gets the date vector
	 * @return date vector
	 */
	public List<Date> getDates() {
		return rangeDate;
	}
	/**
	 * This function gets the value vectors
	 * @return the value vectors
	 */
	public List<Vector<Integer>> getValueLists() {
		return averageValue;
	}
	/**
	 * This function gets the heart beat rate vector
	 * @return heart beat rate values
	 */
	public List<Integer> getRateList() {
		return averageRate;
	}
	/**
	 * This is an abstract function that returns the time format of different chart
	 * @return time format in String
	 */
	public abstract String getTimeFormat();
	
}
