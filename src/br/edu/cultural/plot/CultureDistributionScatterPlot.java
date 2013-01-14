package br.edu.cultural.plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;

import br.edu.cultural.simulation.CultureDisseminationSimulation;

public class CultureDistributionScatterPlot extends Plot<CultureDisseminationSimulation, DefaultXYDataset> {

	private static final int SERIES_UPDATE_INTERVAL  = 10;

	@Override
	public JFreeChart createPlot(CultureDisseminationSimulation sim) {
		this.sim = sim;
		dataset = new DefaultXYDataset();
		dataset.addSeries(1, new double [2][0]);
		chart = ChartFactory.createScatterPlot("Culture sizes distribution "+simInfo(), "S", "% of culture sizes >= S", dataset,
		PlotOrientation.VERTICAL, false, true, false);
		LogarithmicAxis xAxis = new LogarithmicAxis("S");
		LogarithmicAxis yAxis = new LogarithmicAxis("% of culture sizes >= S");
		XYPlot pl = (XYPlot) chart.getPlot();
		pl.setDomainAxis(xAxis);
		pl.setRangeAxis(yAxis);
		return chart;
	}
	
	public void epoch() {
		plot();
	}

	public void plot() {
		// defer plotting for a little while... too busy!
		if(sim.current_epoch() % SERIES_UPDATE_INTERVAL != 0){
			return;
		}
		
		// let's retrieve the size of each culture
		List<Integer> culture_sizes = new ArrayList<Integer>(sim.nw.count_cultures().values());
		
		// zeros are a no-no on a log scale
		while (culture_sizes.contains(new Integer(0))) {
			culture_sizes.remove(new Integer(0));
		}
		Collections.sort(culture_sizes);

		// i-th element contains accumulated size of the i smallest cultures in the network
		int[] cumulative_size = new int[culture_sizes.size() + 1];
		cumulative_size[0] = 0;
		for (int i = 0; i < culture_sizes.size(); i++) {
			cumulative_size[i+1] = cumulative_size[i] + culture_sizes.get(i);
		}

		int diff = 0;
		int y = sim.nw.n_nodes;
		int diff_threshold = 2;

		int count = 1;

		double result[][] = new double[2][cumulative_size.length];
		result[0][0] = 1;
		result[1][0] = 1;

		for (int i = 0; i < cumulative_size.length - 1; i++) {
			diff = cumulative_size[i + 1] - cumulative_size[i];
			if (diff < diff_threshold) {
				y = y - diff;
			} else {
				result[0][count] = diff_threshold;
				result[1][count++] = ((double) y) / (sim.nw.n_nodes);
				diff_threshold = diff + 1;
				y = y - diff;
			}
		}

		double[][] series = new double[2][count];
		for (int i = 0; i < count; i++) {
			series[0][i] = result[0][i];
			series[1][i] = result[1][i];
		}
		dataset.addSeries(1, series);
	}
}
