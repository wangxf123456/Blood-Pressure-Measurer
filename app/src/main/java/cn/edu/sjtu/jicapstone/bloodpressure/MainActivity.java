package cn.edu.sjtu.jicapstone.bloodpressure;

import android.app.AlertDialog;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.parse.ParseAnalytics;
import com.parse.ParseObject;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * This class is the main activity.
 * It extends from Activity class.
 * @author Shaoxiang Su
 *
 */
public class MainActivity extends Activity {
	private static String TAG = "MainActivity";

	private static int REQUEST_ENABLE_BT = 2;
	
	private View measureView;
	private View historyView;
	private View loginView;
	private View cloudView;
	private View repeatView;
	private TextView loginStateText;
	private TextView loginStateEngText;

	public static String userid;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		measureView = findViewById(R.id.measureLayout);
		measureView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				BluetoothAdapter mAdapter = null;
				mAdapter = BluetoothAdapter.getDefaultAdapter();
				if (mAdapter == null) {
					// notify the user that there is no Bluetooth and exit the program
					Dialog dialog = new AlertDialog.Builder(MainActivity.this)
							.setTitle("not jian rong")
							.setMessage("did not find bluetooth")
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									MainActivity.this.finish();
								}
							})
							.create();
				}

				if (!mAdapter.isEnabled()) {
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				} else {
					Intent intent = new Intent();
					intent.putExtra("userid", getIntent().getStringExtra("userid"));
					intent.setClass(arg0.getContext(), MeasureActivity.class);
					MainActivity.this.startActivity(intent);
				}
			}

		});

		
		historyView = findViewById(R.id.recordLayout);
		historyView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(v.getContext(), ChartActivity.class);
				if (getIntent().getStringExtra("userid") != null) {
					intent.putExtra("userid", getIntent().getStringExtra("userid"));
					intent.putExtra("isOffline", false);
				} else {
					intent.putExtra("isOffline", true);
				}
				MainActivity.this.startActivity(intent);
			}
		});

		loginStateText = (TextView)findViewById(R.id.textView3);
		loginStateEngText = (TextView)findViewById(R.id.textView4);
		final String username = getIntent().getStringExtra("username");
		if (username != null) {
			loginStateText.setText("登出");
			loginStateEngText.setText("logout");
		}

		loginView = findViewById(R.id.loginLayout);
		loginView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getIntent().getStringExtra("userid") != null) {
					getIntent().removeExtra("username");
					getIntent().removeExtra("userid");
					loginStateText.setText("登录");
					loginStateEngText.setText("login");
				} else {
					Intent nextScreen = new Intent(getApplicationContext(), LoginActivity.class);
					startActivity(nextScreen);
				}
			}
		});

		cloudView = findViewById(R.id.cloudLayout);
		cloudView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getIntent().getStringExtra("userid") != null) {
					Intent nextScreen = new Intent(getApplicationContext(), CloudManagementActivity.class);
					nextScreen.putExtra("userid", getIntent().getStringExtra("userid"));
					startActivity(nextScreen);
				}
			}
		});

		repeatView = findViewById(R.id.repeatLayout);
		repeatView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(v.getContext(), RepeatActivity.class);
				MainActivity.this.startActivity(intent);

			}
		});
		ParseAnalytics.trackAppOpenedInBackground(getIntent());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
