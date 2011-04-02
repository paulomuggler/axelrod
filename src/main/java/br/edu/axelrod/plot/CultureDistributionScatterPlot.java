package br.edu.axelrod.plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;

import br.edu.axelrod.simulation.CultureDisseminationSimulation;

public class CultureDistributionScatterPlot extends Plot<CultureDisseminationSimulation> {

	private static final int SERIES_UPDATE_INTERVAL  = 10;
	DefaultXYDataset ds;
	
	@Override
	public JFreeChart createPlot(CultureDisseminationSimulation sim) {
		this.simulation = sim;
		ds = new DefaultXYDataset();
		ds.addSeries(1, new double [2][0]);
		chart = ChartFactory.createScatterPlot("Culture sizes distribution "+simInfo(), "S", "% of culture sizes >= S", ds,
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
		if(simulation.epoch() % SERIES_UPDATE_INTERVAL != 0){
			return;
		}
		
		List<Integer> cSizes = new ArrayList<Integer>(simulation.nw.count_cultures().values());
		while (cSizes.contains(new Integer(0))) {
			cSizes.remove(new Integer(0));
		}
		Collections.sort(cSizes);

		int[] cumulativeSizes = new int[cSizes.size() + 1];
		cumulativeSizes[0] = 0;

		for (int i = 0; i < cSizes.size(); i++) {
			cumulativeSizes[i + 1] = cumulativeSizes[i] + cSizes.get(i);
		}

		int diff = 0;
		int y = simulation.nw.n_nodes;
		int x = 2;

		int count = 1;

		double result[][] = new double[2][cumulativeSizes.length];
		result[0][0] = 1;
		result[1][0] = 1;

		for (int i = 0; i < cumulativeSizes.length - 1; i++) {
			diff = cumulativeSizes[i + 1] - cumulativeSizes[i];
			if (diff < x) {
				y = y - diff;
			} else {
				result[0][count] = x;
				result[1][count++] = ((double) y) / (simulation.nw.n_nodes);
				x = diff + 1;
				y = y - diff;
			}
		}

		double[][] series = new double[2][count];
		for (int i = 0; i < count; i++) {
			series[0][i] = result[0][i];
			series[1][i] = result[1][i];
		}
		ds.addSeries(1, series);
	}
	
}
