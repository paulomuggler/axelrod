package br.edu.axelrod.plot;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;

import br.edu.axelrod.simulation.FacilitatedDisseminationWithSurfaceTension;

public class ActiveRoomScatterPlot extends Plot<FacilitatedDisseminationWithSurfaceTension> {
	
	private Integer lastMonitoredNodeToChange;
	List<double[]> seriesList;
	
	DefaultXYDataset ds;
	
	@Override
	public JFreeChart createPlot(FacilitatedDisseminationWithSurfaceTension sim) {
		seriesList = new ArrayList<double[]>();
		lastMonitoredNodeToChange = 0;
		this.simulation = sim;
		for (Integer node : simulation.nw.monitorNodes) {
			if (simulation.nw.interactiveNodes.contains(node)) {
				lastMonitoredNodeToChange = node;
				break;
			}
		}
		ds = new DefaultXYDataset();
		ds.addSeries(1, new double[2][0]);
		chart = ChartFactory.createScatterPlot("Active rooms over time: "+simInfo(), "time", "nodes", ds,
		PlotOrientation.VERTICAL, false, true, false);
		plot();
		return chart;
	}
	
	public void interaction(int i, int j){
		Integer nbr = simulation.nw.size * i + j;
		if (simulation.nw.monitorNodes.contains(nbr)
				&& !nbr.equals(lastMonitoredNodeToChange)) {
			plot();
			lastMonitoredNodeToChange = nbr;
		}
	}

	@Override
	public void plot() {
		reallocate_series();
		double[] point = { simulation.iterations(), simulation.nw.monitorNodes.indexOf(lastMonitoredNodeToChange) };
		seriesList.add(point);
	}
	
	private void reallocate_series() {
		double[][] series = new double[2][seriesList.size()];
		int i = 0;
		for (double[] point: seriesList) {
			series[0][i] = point[0];
			series[1][i++] = point[1];
		}
		ds.addSeries(1, series);
	}
}
