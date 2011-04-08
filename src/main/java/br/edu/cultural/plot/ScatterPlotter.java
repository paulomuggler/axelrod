/**
 * 
 */
package br.edu.cultural.plot;

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
public class ScatterPlotter extends JFrame{
	
	private static final long serialVersionUID = -5160449133785015019L;
	
	DefaultXYDataset ds;
	public JFreeChart chart;
	
	public ScatterPlotter(String applicationTitle, String chartTitle, double[][] dataset) {
		super(applicationTitle);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		chart = createChart(dataset, chartTitle);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
		setContentPane(chartPanel);
	}
	
	/**
	 * Creates a chart
	 */
	private JFreeChart createChart(double[][] dataset, String title) {
		ds = new DefaultXYDataset();
		ds.addSeries(1, dataset);
		JFreeChart chart = ChartFactory.createScatterPlot(title, "q", "Smax/N", ds,
		PlotOrientation.VERTICAL, false, false, false);
		return chart;
	}
	
	public void mostra(){
		this.pack();
		this.setVisible(true);
	}
	
	public void setSeries(double[][] series){
		ds.addSeries(1, series);
	}


}