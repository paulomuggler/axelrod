/**
 * 
 */
package br.edu.cultural.plot;

import java.util.Random;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;

import br.edu.cultural.simulation.CultureDisseminationSimulation;

/**
 * @author muggler
 *
 */
public class ActiveRoomsHistogram extends Plot<CultureDisseminationSimulation, SimpleHistogramDataset>{

	private static final long serialVersionUID = -5160449133785015019L;
	
	private Integer lastMonitoredNodeToChange;
	private Integer timeOfLastChange;
	
	final static int MIN_VAL = 0;
	final static int MAX_VAL = 1000000;
	final static int BIN_SIZE = 250;
	final static int NUM_BINS = (MAX_VAL-MIN_VAL)/BIN_SIZE + 1;
//	final static int NUM_BINS = 30;
//	final static int BIN_SIZE = (MAX_VAL-MIN_VAL)/NUM_BINS;
	
	public JFreeChart createPlot(CultureDisseminationSimulation sim) {
		this.sim = sim;
		lastMonitoredNodeToChange = 0;
		timeOfLastChange = 0;
		for (Integer node : sim.nw.monitorNodes) {
			if (sim.nw.interactiveNodes.contains(node)) {
				lastMonitoredNodeToChange = node;
				break;
			}
		}
		dataset = new SimpleHistogramDataset(1);
		for (int i = 0; i < NUM_BINS; i++) {
			dataset.addBin(new SimpleHistogramBin((i * BIN_SIZE) + MIN_VAL, ((i+1)*BIN_SIZE) + MIN_VAL, true, false));
		}
		chart = ChartFactory.createHistogram("Active Rooms Histogram: "+simInfo(), "time", "frequency", dataset, PlotOrientation.VERTICAL, false, true, false);
		return chart;
	}
	
	private int iteration;
	
	@Override
	public void interaction(int i, int j, int[] oldState, int[] newState){
		Integer node = sim.nw.size * i + j;
		if (sim.nw.monitorNodes.contains(node)
				&& !node.equals(lastMonitoredNodeToChange)) {
			lastMonitoredNodeToChange = node;
			iteration = sim.iterations();
			plot();
			timeOfLastChange = iteration;
		}
	}
	
	@Override
	public void plot() {
		Integer delta = iteration - timeOfLastChange;
		dataset.addObservation((delta == 0 ? 1 : delta));
	}
	
	public static void main(String[] args) throws InterruptedException{
		Random rand = new Random();
		SimpleHistogramDataset ds = new SimpleHistogramDataset(1);
		for (int i = 0; i < NUM_BINS; i++) {
			ds.addBin(new SimpleHistogramBin(i * BIN_SIZE, (i+1)*BIN_SIZE, true, false));
		}
		JFreeChart chart = ChartFactory.createHistogram("Active Rooms Histogram:", "time", "frequency", ds, PlotOrientation.VERTICAL, false, true, false);
		
		JFrame plot = new JFrame("Test histogram");
		plot.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// we put the chart into a panel
		ChartPanel chartPanel = new ChartPanel(chart);
		// default size
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
		// add it to our application
		plot.setContentPane(chartPanel);
		plot.pack();
		plot.setVisible(true);
		for (int i = 0; i < 5000; i++) {
			ds.addObservation(rand .nextInt(10000));
			Thread.sleep(33);
		}
	}
}
