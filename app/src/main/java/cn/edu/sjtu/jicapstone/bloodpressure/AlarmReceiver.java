package cn.edu.sjtu.jicapstone.bloodpressure;


import java.util.Date;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;
import android.text.format.DateFormat;

public class AlarmReceiver extends WakefulBroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Alarmed at " + System.currentTimeMillis());
        Toast.makeText(context, "Recurring", Toast.LENGTH_SHORT).show();
        if (RepeatActivity.repeatPIntent != null) {
            RepeatActivity.lastMeasurement = System.currentTimeMillis();
            // AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if(RepeatActivity.timeTextView != null){
                Date nDate = new Date(RepeatActivity.lastMeasurement);
                RepeatActivity.timeTextView.setText(DateFormat.getTimeFormat(context.getApplicationContext()).format(nDate));
            }
            System.out.println("Set at " + (RepeatActivity.lastMeasurement + RepeatActivity.interval));
            // manager.set(AlarmManager.RTC_WAKEUP, RepeatActivity.lastMeasurement + RepeatActivity.interval, RepeatActivity.repeatPIntent);
        }
        Intent service = new Intent(context, BackgroundMeasureService.class);
        startWakefulService(context, service);
    }

}
