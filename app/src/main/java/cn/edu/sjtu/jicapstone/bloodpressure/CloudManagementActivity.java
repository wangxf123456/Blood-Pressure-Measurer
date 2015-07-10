package cn.edu.sjtu.jicapstone.bloodpressure;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;


public class CloudManagementActivity extends Activity {

    private TextView startDateView;
    private TextView endDateView;
    private Date startDate;
    private Date endDate;
    private ImageView searchView;

    private ImageView measureView;
    private ImageView settingView;

    private ListView resultView;
    private ListItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_management);

        startDateView = (TextView)findViewById(R.id.textView1);
        endDateView = (TextView)findViewById(R.id.textView2);
        searchView = (ImageView)findViewById(R.id.imageView1);
        measureView = (ImageView)findViewById(R.id.imageView3);
        settingView = (ImageView)findViewById(R.id.imageView2);

        resultView = (ListView)findViewById(R.id.ListView1);
        final Date today = new Date();
        final DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        String dateString = df.format(today);
        startDateView.setText(dateString);
        endDateView.setText(dateString);
        startDate = new Date();
        endDate = new Date();

        startDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(CloudManagementActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                                  int dayOfMonth) {
                                startDate = new Date(year-1900, monthOfYear, dayOfMonth);
                                startDateView.setText(df.format(startDate));
                            }
                        }, today.getYear() + 1900, today.getMonth(), today.getDate()).show();
            }
        });

        endDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(CloudManagementActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                                  int dayOfMonth) {
                                endDate = new Date(year - 1900, monthOfYear, dayOfMonth, 23, 59, 59);
                                endDateView.setText(df.format(endDate));
                            }
                        }, today.getYear() + 1900, today.getMonth(), today.getDate()).show();
            }
        });

        final Vector<UserData> dataVector;
        dataVector = new Vector<UserData>();
        final String username = getIntent().getStringExtra("username");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Record");
        query.whereEqualTo("user", username);
        query.orderByAscending("date");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> rList, ParseException e) {
                if (e == null) {
                    for (ParseObject r : rList) {
                        int h = r.getInt("highPressure");
                        int l = r.getInt("lowPressure");
                        int rate = r.getInt("heartRate");
                        Date d = r.getDate("date");
                        UserData newRecord = new UserData(d, l, h, rate, username);
                        dataVector.add(newRecord);
                    }
                }
                System.out.println(dataVector.size());
                for (int i = 0; i < dataVector.size(); i++) {
                    System.out.printf("%d, %d, %d, %s\n", dataVector.get(i).getHeartRate(),
                            dataVector.get(i).getDbpValue(),
                            dataVector.get(i).getSbpValue(),
                            dataVector.get(i).getUsername());
                }
                configureUI(dataVector);
            }
        });
    }

    public void configureUI(final Vector<UserData> dataVector) {
        final Date today = new Date();
        UserDataVectorDecorator decorator = new SingleDecorator(dataVector, today);

        resultView.setClickable(false);

        // set up the listview
        adapter = new ListItemAdapter(CloudManagementActivity.this, decorator.getDates(), decorator.getValueLists().get(0),
                decorator.getValueLists().get(1), decorator.getRateList(), "HH:mm");
        resultView.setAdapter(adapter);

        searchView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!endDate.after(startDate)) {
                    Toast.makeText(CloudManagementActivity.this, "�������ô���", Toast.LENGTH_SHORT).show();
                } else {
                    UserDataVectorDecorator decorator = new RangeDecorator(dataVector, startDate, endDate);
                    adapter = new ListItemAdapter(CloudManagementActivity.this, decorator.getDates(), decorator.getValueLists().get(0),
                            decorator.getValueLists().get(1), decorator.getRateList(), "yyyy-MM-dd");
                    resultView.setAdapter(adapter);
                    resultView.setClickable(true);

                    final List<Date> tempDateList = decorator.getDates();
                    resultView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent,
                                                View view, int position, long id) {
                            UserDataVectorDecorator tempDecorator = new SingleDecorator(dataVector, tempDateList.get(position));

                            // set up the listview
                            adapter = new ListItemAdapter(CloudManagementActivity.this, tempDecorator.getDates(), tempDecorator.getValueLists().get(0),
                                    tempDecorator.getValueLists().get(1), tempDecorator.getRateList(), "HH:mm");
                            resultView.setAdapter(adapter);
                            resultView.setOnItemClickListener(null);
                        }

                    });

                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cloud_management, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
