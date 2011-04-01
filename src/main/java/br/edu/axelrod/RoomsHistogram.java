/**
 * 
 */
package br.edu.axelrod;

import java.util.Random;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.statistics.HistogramDataset;

/**
 * @author muggler
 *
 */
public class RoomsHistogram extends JFrame{
	
	private static final long serialVersionUID = -5160449133785015019L;
	
	HistogramDataset ds;
	public JFreeChart chart;
	
	public RoomsHistogram(String applicationTitle, String chartTitle, String xCaption, String yCaption, double[] dataset) {
		super(applicationTitle);
		org.jfree.chart.renderer.xy.XYBarRenderer.setDefaultBarPainter(
		        new org.jfree.chart.renderer.xy.StandardXYBarPainter());
		// based on the dataset we create the chart
		chart = createChart(dataset, chartTitle, xCaption, yCaption);
		// we put the chart into a panel
		ChartPanel chartPanel = new ChartPanel(chart);
		// default size
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
		// add it to our application
		setContentPane(chartPanel);
	}
	
	final static int MIN_VAL = 0;
	final static int MAX_VAL = 3000000;
	final static int BIN_SIZE = 10;
	final static int NUM_BINS = (MAX_VAL-MIN_VAL)/BIN_SIZE + 1;
	
	/**
	 * Creates a chart
	 */
	private JFreeChart createChart(double[] dataset, String title, String xCaption, String yCaption) {
		ds = new HistogramDataset();
		ds.addSeries(1, dataset, NUM_BINS, MIN_VAL, MAX_VAL);
		JFreeChart chart = ChartFactory.createHistogram(title, xCaption, yCaption, ds, PlotOrientation.VERTICAL, false, true, false);
		return chart;
	}
	
	public void mostra(){
		this.pack();
		this.setVisible(true);
	}
	
	public void setSeries(double[] series){
		ds.addSeries(1, series, NUM_BINS, MIN_VAL, MAX_VAL);
	}
	
	public void seriesChanged(Object source){
		ds.seriesChanged(new SeriesChangeEvent(source));
	}
	
	public static void main(String[] args){
		Random rand = new Random();
		double[] dataset = new double[10000000];
		for (int i = 0; i < dataset.length; i++) {
			dataset[i] = rand.nextInt(8);
		}
		JFrame plot = new JFrame("Test histogram");
		HistogramDataset ds = new HistogramDataset();
		ds.addSeries(1, dataset, 8, 0, 8);
		JFreeChart chart = ChartFactory.createHistogram("Test", "element","frequency", ds, PlotOrientation.VERTICAL, false, true, false);
		// we put the chart into a panel
		ChartPanel chartPanel = new ChartPanel(chart);
		// default size
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
		// add it to our application
		plot.setContentPane(chartPanel);
		plot.pack();
		plot.setVisible(true);
		
	}


}
