package cn.edu.sjtu.jicapstone.bloodpressure;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;

/**
 * This class is to draw a time line chart.
 * It extends from AbstractChart.
 * @author Shaoxiang Su
 *
 */
public class TimeLineChart extends AbstractChart {

	public TimeLineChart(UserDataVectorDecorator decorator) {
		super(decorator);
	}
	@Override
	public View execute(Context context) {
		
		int[] colors = new int[] { Color.rgb(61, 0, 121), Color.rgb(170, 0, 0) };
		PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND };
		
		XYMultipleSeriesDataset dataset = buildDateDataset();
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		
		setChartSettings(renderer, 
				decorator.getStartTime(), decorator.getEndTime(), 
				decorator.getStartValue(), decorator.getEndValue(), 
				Color.BLACK, Color.BLACK, 
				Color.parseColor("#CCFFFF"), Color.parseColor("#CCFFFF"));
		
		// disable zoom and pan
		renderer.setZoomEnabled(true, false);
		renderer.setPanEnabled(true, false);
		// Y label number
		renderer.setYLabels(10);
		// X label number
		renderer.setXLabels(10);
		
		GraphicalView chartView = ChartFactory.getTimeChartView(context, dataset, renderer, decorator.getTimeFormat());
		return chartView;
	}
}
