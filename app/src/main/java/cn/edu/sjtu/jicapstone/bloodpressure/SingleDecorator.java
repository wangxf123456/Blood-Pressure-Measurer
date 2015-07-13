package cn.edu.sjtu.jicapstone.bloodpressure;

import java.util.Date;
import java.util.Vector;

/**
 * This class is the decorator for the single day.
 * It extends from UserDataVectorDecorator.
 * @author Shaoxiang Su
 *
 */
public class SingleDecorator extends UserDataVectorDecorator{
	
	private Date singleDate;
	private Vector<String> idVector;
	
	public SingleDecorator (Vector<UserData> userDataVector, Date singleDate) {
		super(userDataVector);
		this.singleDate = singleDate;
		idVector = new Vector<String>();
		
		int maxValue = -1;
		int minValue = 10000;
		long minDate = -1;
		long maxDate = -1;
		
		for (UserData ud : userDataVector) {
			Date tempDate = ud.getDate();
			if (tempDate.getYear() == singleDate.getYear() && 
					tempDate.getMonth() == singleDate.getMonth() && 
					tempDate.getDate() == singleDate.getDate()) {
				if (minDate == -1)
					minDate = tempDate.getTime();
				maxDate = tempDate.getTime();
				
				rangeDate.add(tempDate);
				averageValue.get(0).add(ud.getDbpValue());
				averageValue.get(1).add(ud.getSbpValue());
				averageRate.add(ud.getHeartRate());
				idVector.add(ud.getItemid());
				
				if (ud.getSbpValue() > maxValue)
					maxValue = ud.getSbpValue();
				if (ud.getDbpValue() < minValue)
					minValue = ud.getDbpValue();
			}
		}
		long dateSpace;
		if (maxDate == minDate) 
			dateSpace = 500000;
		else 
			dateSpace = (maxDate - minDate) / 10;
		startValue = minValue - 5;
		endValue = maxValue + 5;
		startTime = minDate - dateSpace;
		endTime = maxDate + dateSpace;
	}

	public String getIdAtLocation(int location) {
		return idVector.get(location);
	}

	@Override
	public String getTimeFormat() {
		return "HH:mm";
	}
}
