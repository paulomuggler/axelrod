package br.edu.cultural.plot;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;

import br.edu.cultural.simulation.CultureDisseminationSimulation;

public class ActiveNodesScatterPlot extends Plot<CultureDisseminationSimulation, DefaultXYDataset> {
	
	private static final int SERIES_UPDATE_INTERVAL  = 2;
	List<double[]> seriesList;
	
	@Override
	public JFreeChart createPlot(CultureDisseminationSimulation sim) {
		this.sim = sim;
		seriesList= new ArrayList<double[]>();
		dataset = new DefaultXYDataset();
		dataset.addSeries(1, new double [2][0]);
		chart = ChartFactory.createScatterPlot("Interactive nodes over time: "+simInfo(), "time", "active nodes", dataset,
		PlotOrientation.VERTICAL, false, true, false);
		return chart;
	}

	public void plot() {
		if(seriesList.size() % SERIES_UPDATE_INTERVAL == 0){
			reallocate_series();
		}
		double[] point = { sim.current_epoch() == 0? 1 : sim.current_epoch(), 
						   sim.nw.interactiveNodes.size()/(double)sim.nw.n_nodes };
		seriesList.add(point);
	}

	private void reallocate_series() {
		double[][] series = new double[2][seriesList.size()];
		int i = 0;
		for (double[] point: seriesList) {
			series[0][i] = point[0];
			series[1][i++] = point[1];
		}
		dataset.addSeries(1, series);
	}

	public void epoch() {
		plot();
	}
}
