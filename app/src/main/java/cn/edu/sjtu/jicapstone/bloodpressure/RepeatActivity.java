package cn.edu.sjtu.jicapstone.bloodpressure;

import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;


public class RepeatActivity extends Activity {

    static PendingIntent repeatPIntent = null;

    static long lastMeasurement = -1;
    static long interval = 2 * 60 * 1000;//AlarmManager.INTERVAL_FIFTEEN_MINUTES; // half hour

    private AlarmManager manager;

    static public TextView timeTextView;
    private View scheduleRepeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeat);

        manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        scheduleRepeat = findViewById(R.id.toggleStartStop);
        timeTextView = (TextView)findViewById(R.id.textViewTimeChange);

        if (repeatPIntent == null) {
            ((TextView)scheduleRepeat).setText(R.string.start_repeat_english);
            timeTextView.setText(R.string.time_undefined);
        }
        else {
            ((TextView)scheduleRepeat).setText(R.string.stop_repeat_english);
            long nextMeasurement = lastMeasurement + interval;
            Date nDate = new Date(nextMeasurement);
            timeTextView.setText(DateFormat.getTimeFormat(getApplicationContext()).format(nDate));
        }

        scheduleRepeat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Clicked");
                if (repeatPIntent != null) {
                    System.out.println("Cancelling");
                    manager.cancel(repeatPIntent);
                    repeatPIntent = null;
                    lastMeasurement = -1;
                    ((TextView)scheduleRepeat).setText(R.string.start_repeat_english);
                    timeTextView.setText(R.string.time_undefined);
                }
                else {
                    Intent alarmIntent = new Intent(RepeatActivity.this, AlarmReceiver.class);
                    repeatPIntent = PendingIntent.getBroadcast(RepeatActivity.this,
                            0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    lastMeasurement = System.currentTimeMillis();
                    System.out.println(interval);
                    System.out.println("Set at " + (lastMeasurement + interval));
                    manager.setRepeating(AlarmManager.RTC_WAKEUP, lastMeasurement, interval, repeatPIntent);
                    ((TextView)scheduleRepeat).setText(R.string.stop_repeat_english);

                    long nextMeasurement = lastMeasurement + interval;
                    Date nDate = new Date(nextMeasurement);
                    timeTextView.setText(DateFormat.getTimeFormat(getApplicationContext()).format(nDate));
                }
            }
        });
    }

}
