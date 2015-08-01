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

        for (int i = 0; i < 12; i++) {
            ParseObject record = new ParseObject("Record");
            record.put("userid", ParseUser.getCurrentUser().getObjectId());
            if (i <= 8 || i >= 22) {
                record.put("highPressure", 100 + Math.random() * 20);
                record.put("lowPressure", 60 + Math.random() * 20);
            } else {
                record.put("highPressure", 100 + Math.random() * 40);
                record.put("lowPressure", 60 + Math.random() * 40);
            }
            record.put("heartRate", (int)(60 + Math.random() * 50));

            SimpleDateFormat sourceDateFormat = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");

            Date date = new Date();
            record.put("date", date);

            record.saveInBackground();
            System.out.println("record saved");
        }

    }
}

