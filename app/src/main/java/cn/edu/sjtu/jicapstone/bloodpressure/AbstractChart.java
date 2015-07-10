package cn.edu.sjtu.jicapstone.bloodpressure;

import java.util.Date;
import java.util.List;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import android.content.Context;
import android.graphics.Paint.Align;
import android.view.View;

/**
 * This class is the abstract class of all the charts. 
 * It first sets some general factors of all the charts
 * @author Shaoxiang Su
 *
 */
public abstract class AbstractChart {

	protected UserDataVectorDecorator decorator;
	
	/**
	 * Constructor
	 */
	public AbstractChart(UserDataVectorDecorator decorator) {
		this.decorator = decorator;
	}
	
	/**
	 * This function is to build up the data that is going to put into the chart.
	 * @return dataset that contains the data in XYMultipleSeriesDataset form
	 */
	protected XYMultipleSeriesDataset buildDateDataset() {  
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();  
	    int length = decorator.getTitles().length;
	    List<Date> dates = decorator.getDates();
	    for (int i = 0; i < length; i++) {
			System.out.println("gg " + i);
	    	TimeSeries series = new TimeSeries(decorator.getTitles()[i]); 
	    	List<Integer> yValues = decorator.getValueLists().get(i);  
	    	int seriesLength = dates.size();
	    	for (int j = 0; j < seriesLength; j++) {  
	    		series.add(dates.get(j), yValues.get(j));
	    	}  
	    	dataset.addSeries(series);  
	    }  
	    return dataset;  
	}
	
	/**
	 * This function is to build the renderer of a multiple series
	 * @param colors an array of colors that for different lines
	 * @param styles an array of point style which indicates the shape of points of different lines
	 * @return XYMultipleSeriesRenderer for constructing the chart.
	 */
	protected XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles) {  
	    XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();  
	    setRenderer(renderer, colors, styles);  
	    return renderer;  
	  }  
	  
	/**
	 * This function is to set up a renderer
	 * @param renderer the render that is to be changed
	 * @param colors an array of colors that for different lines
	 * @param styles an array of point style
	 */
	protected void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles) {  
		  
	    renderer.setAxisTitleTextSize(26);  
	    renderer.setChartTitleTextSize(35);
	    renderer.setLabelsTextSize(25);  
	    renderer.setLegendTextSize(30);
	    renderer.setPointSize(10f);
	    renderer.setMargins(new int[] { 45, 50, 35, 50 });
	    
	    int length = colors.length;  
	    for (int i = 0; i < length; i++) {  
	      XYSeriesRenderer r = new XYSeriesRenderer();  
	      r.setColor(colors[i]);  
	      r.setPointStyle(styles[i]);  
	      r.setFillPoints(true);
	      r.setLineWidth(6);
	      renderer.addSeriesRenderer(r);  
	    }  
	}  
	 
	/**
	 * This function is to set some general setting of the chart
	 * @param renderer the renderer that contains all the chart settings
	 * @param xMin min value of the x axis
	 * @param xMax max value of the x axis
	 * @param yMin min value of the y axis
	 * @param yMax max value of the y axis
	 * @param axesColor the color of all the axis
	 * @param labelsColor the color of label
	 * @param backgroundColor the chart background color
	 * @param marginsColor the margin color of the chart
	 */
	protected void setChartSettings(XYMultipleSeriesRenderer renderer, 
									double xMin, double xMax, 
									double yMin, double yMax, 
									int axesColor, int labelsColor, 
									int backgroundColor, int marginsColor) {  
	    renderer.setXAxisMin(xMin);
	    renderer.setXAxisMax(xMax);
	    renderer.setYAxisMin(yMin);  
	    renderer.setYAxisMax(yMax);  
	    renderer.setAxesColor(axesColor);
	    renderer.setLabelsColor(labelsColor);
	    renderer.setXLabelsColor(labelsColor);
	    renderer.setYLabelsColor(0, labelsColor);
	    renderer.setYLabelsAlign(Align.RIGHT);
	    renderer.setApplyBackgroundColor(true);
	    renderer.setBackgroundColor(backgroundColor);
	    renderer.setMarginsColor(marginsColor);
	}
	  
	/**
	 * The abstract function that construct the chart.
	 * Concrete class may do some extra settings inside the execute.
	 * @param context the context that will draw the chart
	 * @return the view of the chart
	 */
	public abstract View execute(Context context);
}
 	