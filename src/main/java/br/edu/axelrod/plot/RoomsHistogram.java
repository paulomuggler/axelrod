/**
 * 
 */
package br.edu.axelrod.plot;

import java.util.Random;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;

import br.edu.axelrod.simulation.FacilitatedDisseminationWithSurfaceTension;

/**
 * @author muggler
 *
 */
public class RoomsHistogram extends Plot<FacilitatedDisseminationWithSurfaceTension>{

	private static final long serialVersionUID = -5160449133785015019L;
	
	private Integer lastMonitoredNodeToChange;
	private Integer timeOfLastChange;
	
	SimpleHistogramDataset ds;
	public JFreeChart chart;
	
	final static int MIN_VAL = 0;
	final static int MAX_VAL = 3000000;
//	final static int BIN_SIZE = 10000;
//	final static int NUM_BINS = (MAX_VAL-MIN_VAL)/BIN_SIZE + 1;
	final static int NUM_BINS = 30;
	final static int BIN_SIZE = (MAX_VAL-MIN_VAL)/NUM_BINS;
	
	public JFreeChart createPlot(FacilitatedDisseminationWithSurfaceTension sim) {
		this.simulation = sim;
		lastMonitoredNodeToChange = 0;
		timeOfLastChange = 0;
		ds = new SimpleHistogramDataset(1);
		for (int i = 0; i < NUM_BINS; i++) {
			ds.addBin(new SimpleHistogramBin(i * BIN_SIZE, (i+1)*BIN_SIZE, true, false));
		}
		chart = ChartFactory.createHistogram("Active Rooms Histogram: "+simInfo(), "time", "frequency", ds, PlotOrientation.VERTICAL, false, true, false);
		return chart;
	}
	
	public void interaction(int i, int j){
		Integer nbr = simulation.nw.size * i + j;
		if (simulation.nw.monitorNodes.contains(nbr)
				&& !nbr.equals(lastMonitoredNodeToChange)) {
			plot();
			lastMonitoredNodeToChange = nbr;
			plot();
			timeOfLastChange = simulation.epoch();
		}
	}
	
	@Override
	public void plot() {
		Integer delta = simulation.epoch() - timeOfLastChange;
		ds.addObservation(delta);
		ds.seriesChanged(null);
	}
	
	public static void main(String[] args){
		int bin_width = 10;
		Random rand = new Random();
		SimpleHistogramDataset ds = new SimpleHistogramDataset(1);
		for (int i = 0; i < 10; i++) {
			SimpleHistogramBin bin = new SimpleHistogramBin(i*bin_width, bin_width * (i+1), true, false);
			ds.addBin(bin);
		}
		JFrame plot = new JFrame("Test histogram");
		plot.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		for (int i = 0; i < 5; i++) {
			ds.addObservation(rand.nextInt(100));
		}
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
