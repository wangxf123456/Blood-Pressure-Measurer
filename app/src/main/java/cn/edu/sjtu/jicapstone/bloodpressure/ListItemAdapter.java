package cn.edu.sjtu.jicapstone.bloodpressure;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.edu.sjtu.jicapstone.bloodpressure.R.drawable;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This class is to construct an adapter that insert the data into ListView item
 * @author Shaoxiang Su
 *
 */
public class ListItemAdapter extends BaseAdapter {
	private static String TAG = "ListItemAdapter";
	
	private final Activity activity;
	private final List <Date> dateList;
	private final List <Integer> dbpList;
	private final List <Integer> sbpList;
	private final List <Integer> rateList;
	
	private String timeFormat;
	
	private static LayoutInflater inflater = null;
	
	public ListItemAdapter (Activity a, List <Date> d, List <Integer> dbp, 
			List <Integer> sbp, List<Integer> rate, String tf) {
		activity = a;
		dateList = d;
		dbpList = dbp;
		sbpList = sbp;
		rateList = rate;
		timeFormat = tf;
		Log.i(TAG, "constructor: list size: " + d.size() + " " + dbp.size() + " " + sbp.size());
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return dbpList.size();
	}

	@Override
	public Object getItem(int position) {
		return new String(dateList.get(position).toString() + " " + dbpList.get(position) + " " + sbpList.get(position));
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.i(TAG, "getView: position: " + position);
		
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listview_item, null);
			holder = new ViewHolder();
			
			holder.statusView = (ImageView)convertView.findViewById(R.id.imageView2);
			holder.dateView = (TextView)convertView.findViewById(R.id.textView1);
			holder.dbpView = (TextView)convertView.findViewById(R.id.textView2);
			holder.sbpView = (TextView)convertView.findViewById(R.id.textView3);
			holder.rateView = (TextView)convertView.findViewById(R.id.textView4);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		
		int dbpValue = dbpList.get(position);
		int sbpValue = sbpList.get(position);

		// check the status and find the right picture to paste
		if (sbpValue >= Parameters.SBP_HIGHER || dbpValue >= Parameters.DBP_HIGHER) {
			Resources resources = activity.getResources();
			Drawable drawable = resources.getDrawable(R.drawable.status_higher);
			holder.statusView.setImageDrawable(drawable);
		} else if (sbpValue <= Parameters.SBP_LOWER || dbpValue <= Parameters.DBP_LOWER) {
			Resources resources = activity.getResources();
			Drawable drawable = resources.getDrawable(R.drawable.status_lower);
			holder.statusView.setImageDrawable(drawable);
		} else {
			Resources resources = activity.getResources();
			Drawable drawable = resources.getDrawable(R.drawable.status_normal);
			holder.statusView.setImageDrawable(drawable);
		}
		
		DateFormat df = new SimpleDateFormat(timeFormat);
		holder.dateView.setText("时间:" + df.format(dateList.get(position)));
		
		holder.dbpView.setText("舒张压:" + String.valueOf(dbpValue));
		holder.sbpView.setText("收缩压:" + String.valueOf(sbpValue));

		holder.rateView.setText("");
		return convertView;
	}
	
	class ViewHolder {
		public ImageView statusView;
		public TextView dateView;
		public TextView dbpView;
		public TextView sbpView;
		public TextView rateView;
	}
}
