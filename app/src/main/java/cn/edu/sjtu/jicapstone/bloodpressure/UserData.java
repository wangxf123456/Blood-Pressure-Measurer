package cn.edu.sjtu.jicapstone.bloodpressure;

import java.util.Date;

/**
 * This class is a POJO that contains all the data
 * @author Shaoxiang Su
 *
 */
public class UserData {
	private java.util.Date date;
	private int dbpValue;
	private int sbpValue;
	private int heartRate;
	private String userid;
	private String itemid;
	public UserData(Date date, int dbpValue, int sbpValue, int heartRate, String userid, String itemid) {
		super();
		this.date = date;
		this.dbpValue = dbpValue;
		this.sbpValue = sbpValue;
		this.heartRate = heartRate;
		this.userid = userid;
		this.itemid = itemid;
	}
	public java.util.Date getDate() {
		return date;
	}
	public void setDate(java.util.Date date) {
		this.date = date;
	}
	public int getDbpValue() {
		return dbpValue;
	}
	public void setDbpValue(int dbpValue) {
		this.dbpValue = dbpValue;
	}
	public int getSbpValue() {
		return sbpValue;
	}
	public void setSbpValue(int sbpValue) {
		this.sbpValue = sbpValue;
	}
	
	public int getHeartRate() {
		return heartRate;
	}
	public void setHeartRate(int heartRate) {
		this.heartRate = heartRate;
	}

	public String getUserid() {
		return userid;
	}
	public void setUsername(String username) {
		this.userid = userid;
	}

	public String getItemid() {
		return itemid;
	}
	public void setItemid(String itemid) {
		this.itemid = itemid;
	}
}
