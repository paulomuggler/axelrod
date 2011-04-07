/**
 * 
 */
package br.edu.cultural;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;

/**
 * @author muggler
 * 
 */

public class MyPlotting extends JFrame {

	private static final long serialVersionUID = 1L;

	static DefaultXYDataset ds;

	public MyPlotting(String applicationTitle, String chartTitle) {
		super(applicationTitle);
		// This will create the dataset
		double[][] dataset = createDataset();
		// based on the dataset we create the chart
		JFreeChart chart = createChart(dataset, chartTitle);
		// we put the chart into a panel
		ChartPanel chartPanel = new ChartPanel(chart);
		// default size
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		// add it to our application
		setContentPane(chartPanel);
	}

	/**
	 * Creates a sample dataset
	 */
	private double[][] createDataset() {
		double[][] dataset = new double[2][6];
		for (int i = 0; i < 6; i++) {
			dataset[0][i] = i;
			dataset[1][i] = 2*i;
		}
		return dataset;

	}

	/**
	 * Creates a chart
	 */
	private JFreeChart createChart(double[][] dataset, String title) {
		ds = new DefaultXYDataset();
		ds.addSeries(1, dataset);
		JFreeChart chart = ChartFactory.createScatterPlot(title, "exu X", "exu Y", ds,
		PlotOrientation.VERTICAL, false, false, false);
		return chart;
	}

	public static void main(String[] args) throws InterruptedException {
		MyPlotting demo = new MyPlotting("Comparison",
		"Which operating system are you using?");
		
		demo.addWindowListener(new WindowListener(){
			public void windowOpened(WindowEvent e) {
			}
			
			public void windowIconified(WindowEvent e) {
			}
			
			public void windowDeiconified(WindowEvent e) {
			}
			
			public void windowDeactivated(WindowEvent e) {
			}
			
			public void windowClosing(WindowEvent e) {
			}
			
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
			
			public void windowActivated(WindowEvent e) {
			}
		});
		
		demo.pack();
		demo.setVisible(true);
		Thread.sleep(3000l);
		double[][] dataset = new double[2][6];
		for (int i = 0; i < 6; i++) {
			dataset[0][i] = 1;
			dataset[1][i] = 2*i;
		}
		ds.addSeries(2, dataset);
	}

}
