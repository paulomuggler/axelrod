package br.edu.cultural.plot;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;

import br.edu.cultural.simulation.CultureDisseminationSimulation;

public class ActiveEdgesScatterPlot extends Plot<CultureDisseminationSimulation, DefaultXYDataset> {
	
	private static final int SERIES_UPDATE_INTERVAL  = 2;
	List<double[]> seriesList;
	
	@Override
	public JFreeChart createPlot(CultureDisseminationSimulation s) {
		this.sim = s;
		seriesList= new ArrayList<double[]>();
		dataset = new DefaultXYDataset();
		dataset.addSeries(DEFAULT_SERIES_KEY, new double [2][0]);
		chart = ChartFactory.createScatterPlot("Interactive edges over time: "+simInfo(), "time", "edges", dataset,
		PlotOrientation.VERTICAL, false, true, false);
		return chart;
	}

	public void plot() {
		if(seriesList.size() % SERIES_UPDATE_INTERVAL == 0){
			reallocate_series();
		}
		double[] point = { sim.current_epoch() == 0? 1 : sim.current_epoch(), 
						   sim.nw.count_interactive_edges()/(2.0*(2*sim.nw.n_nodes)) };
		seriesList.add(point);
	}

	private void reallocate_series() {
		double[][] series = new double[2][seriesList.size()];
		int i = 0;
		for (double[] point: seriesList) {
			series[0][i] = point[0];
			series[1][i++] = point[1];
		}
		dataset.addSeries(DEFAULT_SERIES_KEY, series);
	}

	public void epoch() {
		plot();
	}

}
