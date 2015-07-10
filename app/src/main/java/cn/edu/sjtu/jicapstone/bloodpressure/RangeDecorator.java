package cn.edu.sjtu.jicapstone.bloodpressure;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import android.util.Log;

/**
 * This class is a decorator for a range of date.
 * It extends from UserDataVectorDecorator.
 * @author Shaoxiang Su
 *
 */
public class RangeDecorator extends UserDataVectorDecorator {
	private Date startDate;
	private Date endDate;
	private int minValue = 1000;
	private int maxValue = -1;

	public RangeDecorator (Vector<UserData> userDataVector, Date startDate, Date endDate) {
		super(userDataVector);
		this.startDate = startDate;
		this.endDate = endDate;

		if (userDataVector.size() == 0)
			return;
		
		int index = 0;
		while (startDate.after(userDataVector.get(index).getDate())) 
			index++;
		
		Date currentDate = new Date(2000, 1, 1);
		int overallDbp = 0;
		int overallSbp = 0;
		int overallRate = 0;
		int count = 0;
		
		
		for(; index < userDataVector.size(); index++) {
			Date tempDate = userDataVector.get(index).getDate();
			if (endDate.before(tempDate)) {
				addList (overallDbp, overallSbp, overallRate, count, currentDate);
				break;
			}
			if (tempDate.getYear() == currentDate.getYear() && 
					tempDate.getMonth() == currentDate.getMonth() && 
					tempDate.getDate() == currentDate.getDate()) { 
				overallDbp += userDataVector.get(index).getDbpValue();
				overallSbp += userDataVector.get(index).getSbpValue();
				overallRate += userDataVector.get(index).getHeartRate();
				count++;
				
			} else {
				if (count != 0) {
					addList (overallDbp, overallSbp, overallRate, count, currentDate);
				}
				overallDbp = userDataVector.get(index).getDbpValue();
				overallSbp = userDataVector.get(index).getSbpValue();
				overallRate = userDataVector.get(index).getHeartRate();
				count = 1;
				currentDate = tempDate;	
			}
			
			if (index == userDataVector.size() - 1) {
				addList (overallDbp, overallSbp, overallRate, count, currentDate);
			}
		}
		startValue = minValue - 5;
		endValue = maxValue + 5;
		long dateSpace = (endDate.getTime() - startDate.getTime()) / 10;
		startTime = startDate.getTime() - dateSpace;
		endTime = endDate.getTime() + dateSpace;
		
	}
	@Override
	public String getTimeFormat() {
		return "MM/dd";
	}
	
	/**
	 * This function is to add a new average value into the result vector
	 * @param overallDbp the sum of dbp for the current day
	 * @param overallSbp the sum of sbp for the current day
	 * @param overallRate the sum of heart beat rate for the current day
	 * @param count the number of measurement in this single day
	 * @param currentDate the current date
	 */
	private void addList (int overallDbp, int overallSbp, int overallRate, int count, Date currentDate) {
		int averageDbp = overallDbp / count;
		int averageSbp = overallSbp / count;
		int averageHeartRate = overallRate / count;
		
		rangeDate.add(new Date(currentDate.getYear(), currentDate.getMonth(), currentDate.getDate()));
		averageValue.get(0).add(averageDbp);
		averageValue.get(1).add(averageSbp);
		averageRate.add(averageHeartRate);
		
		if (averageSbp > maxValue)
			maxValue = averageSbp;
		if (averageDbp < minValue)
			minValue = averageDbp;
	}
	
}
