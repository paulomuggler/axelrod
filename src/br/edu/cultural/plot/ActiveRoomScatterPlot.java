package br.edu.cultural.plot;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;

import br.edu.cultural.simulation.CultureDisseminationSimulation;

public class ActiveRoomScatterPlot extends Plot<CultureDisseminationSimulation, DefaultXYDataset> {
	
	private Integer last_node_to_change;
	List<double[]> seriesList;
	
	@Override
	public JFreeChart createPlot(CultureDisseminationSimulation s) {
		seriesList = new ArrayList<double[]>();
		last_node_to_change = 0;
		this.sim = s;
		for (Integer node : s.nw.nodes_to_listen) {
			if (s.nw.interactiveNodes.contains(node)) {
				last_node_to_change = node;
				break;
			}
		}
		dataset = new DefaultXYDataset();
		dataset.addSeries(DEFAULT_SERIES_KEY, new double[2][0]);
		chart = ChartFactory.createScatterPlot("Active rooms over time: "+simInfo(), "time", "nodes", dataset,
		PlotOrientation.VERTICAL, false, true, false);
		plot();
		return chart;
	}
	
	@Override
	public void interaction(int i, int j, int[] oldState, int[] newState){
		Integer nbr = sim.nw.size * i + j;
		if (sim.nw.nodes_to_listen.contains(nbr)
				&& !nbr.equals(last_node_to_change)) {
			plot();
			last_node_to_change = nbr;
		}
	}

	@Override
	public void plot() {
		reallocate_series();
		double[] point = { sim.iterations(), sim.nw.nodes_to_listen.indexOf(last_node_to_change) };
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
}
