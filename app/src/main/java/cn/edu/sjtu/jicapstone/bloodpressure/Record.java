package cn.edu.sjtu.jicapstone.bloodpressure;

import java.util.Date;

/**
 * Created by wangxiaofei on 15/6/27.
 */
public class Record {
    String user; // belong to which user
    double highPressure;
    double lowPressure;
    Date date;

    public Record(String u, double h, double l, Date d) {
        user = u;
        highPressure = h;
        lowPressure = l;
        date = d;
    }
}
