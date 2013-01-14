/**
 * 
 */
package br.edu.cultural.plot;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;

import br.edu.cultural.ui.MainApplicationFrame;

/**
 * @author muggler
 *
 */
public class ScatterPlotter extends JFrame{
	
	private static final long serialVersionUID = -5160449133785015019L;
	
	DefaultXYDataset ds;
	public JFreeChart chart;
	ChartPanel chartPanel;
	
	public ScatterPlotter(String applicationTitle, String chartTitle, double[][] dataset, String xAx, String yAx) {
		super(applicationTitle);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		chart = createChart(dataset, chartTitle, xAx, yAx);
		chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
		addExportToCsvOption();
		setContentPane(chartPanel);
	}
	
	/**
	 * Creates a chart
	 */
	private JFreeChart createChart(double[][] dataset, String title, String xAx, String yAx) {
		ds = new DefaultXYDataset();
		ds.addSeries(1, dataset);
		JFreeChart chart = ChartFactory.createScatterPlot(title, xAx, yAx, ds,
		PlotOrientation.VERTICAL, true, true, true);
		return chart;
	}
	
	public void setSeries(double[][] series){
		ds.addSeries(1, series);
	}
	
	@SuppressWarnings("rawtypes")
	public void addSeries(double[][] series, Comparable series_index){
		ds.addSeries(series_index, series);
	}
	
	private void addExportToCsvOption(){
		JMenuItem exportToCsv = new JMenuItem(new AbstractAction("Export to .csv...") {
			private static final long serialVersionUID = -7965277270950293468L;
			public void actionPerformed(ActionEvent e) {
				int select = MainApplicationFrame.getFileChooser().showSaveDialog(ScatterPlotter.this);
				if (select == JFileChooser.APPROVE_OPTION) {
					File f = MainApplicationFrame.getFileChooser().getSelectedFile();
					try {
						ScatterPlotter.this.export_series_to_csv(f);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(ScatterPlotter.this,
								"Error saving to file: " + e1.getMessage(),
								"Error!", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		this.chartPanel.getPopupMenu().add(exportToCsv, 3);
	}
	
	public void export_series_to_csv(File f) throws IOException {
		if(!f.exists()) f.createNewFile();
		if(f.canWrite()){
			FileWriter fw = new FileWriter(f);
			fw.write(chart.getTitle().getText());
			fw.write('\n');
			for (int s = 0; s < ds.getSeriesCount(); s++) {
				fw.write(String.format("Series %s:\n", ds.getSeriesKey(s)));
				fw.write(((XYPlot) chart.getPlot()).getDomainAxis().getLabel());
				fw.write(',');
				fw.write(((XYPlot) chart.getPlot()).getRangeAxis().getLabel());
				fw.write('\n');
				for (int i = 0; i < ds.getItemCount(s); i++) {
					fw.write(Double.toString(ds.getXValue(s, i)));
					fw.write(',');
					fw.write(Double.toString(ds.getYValue(s, i)));
					fw.write('\n');
				}
			}
			fw.close();
		}
	}

}
