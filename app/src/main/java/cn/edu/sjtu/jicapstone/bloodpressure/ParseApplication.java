package cn.edu.sjtu.jicapstone.bloodpressure;

import android.app.Application;
import android.content.Intent;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Crash Reporting.
        ParseCrashReporting.enable(this);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Add your initialization code here
        Parse.initialize(this, "RynSlEE4Es0NUg9CwuCuCaO0ABk13lWpJWFPhmHA", "HYdbMYqVx1jExHQZJjXpR96I5ndxtzm3EniTKhZe");

        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
//        defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

//        createData();
    }

    public void createData() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Record");
        query.whereEqualTo("user", "tiatia");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> rList, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < rList.size(); i++) {
                        rList.get(i).deleteInBackground();
                    }
                }
            }
        });

        for (int i = 0; i < 5; i++) {
            ParseObject record = new ParseObject("Record");
            record.put("user", "tiatia");
            record.put("highPressure", 130 + Math.random() * 50);
            record.put("lowPressure", 80 + Math.random() * 50);
            record.put("heartRate", (int)(60 + Math.random() * 50));
            SimpleDateFormat sourceDateFormat = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");

            try {
                Date date = sourceDateFormat.parse("2015-07-10 00:00:00.0");
                date.setHours(i);
                record.put("date", date);
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }

            record.saveInBackground();
            System.out.println("record saved");
        }

    }
}

