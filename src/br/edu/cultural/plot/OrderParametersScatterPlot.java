package br.edu.cultural.plot;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;

import br.edu.cultural.simulation.CultureDisseminationSimulation;

public class OrderParametersScatterPlot extends Plot<CultureDisseminationSimulation, DefaultXYDataset> {
	
	private static final int SERIES_UPDATE_INTERVAL  = 3;
	private static final String[] series_keys = {"0: overlap = 0", "1: 0 < overlap < f", "2: overlap = f"};
	List<List<double[]>> serieses = new ArrayList<List<double[]>>();
	double[] points;
	
	@Override
	public JFreeChart createPlot(CultureDisseminationSimulation sim) {
		this.sim = sim;
		for(int s = 0; s < 3; s++){
			serieses.add(new ArrayList<double[]>());
		}
		dataset = new DefaultXYDataset();
		chart = ChartFactory.createScatterPlot("Size of cultural regions: "+simInfo(), "time", "ro 0, ro a, ro F", dataset,
		PlotOrientation.VERTICAL, true, true, false);
		return chart;
	}

	public void plot() {
		if(sim.current_epoch() % SERIES_UPDATE_INTERVAL == 0){
			reallocate_series();
		}
		addPoints(sim.current_epoch());
	}

	private void reallocate_series() {
		int f = 0;
		for (List<double[]> series : serieses) {
			double[][] s = new double[2][series.size()];
			int i = 0;
			for (double[] point: series) {
				s[0][i] = point[0];
				s[1][i++] = point[1];
			}
			dataset.removeSeries(series_keys[f]);
			dataset.addSeries(series_keys[f++], s);
		}
	}
	
	public void started(){
		points = new double[3];
		for(int nd = 0; nd < sim.nw.n_nodes; nd++){
			for(int nbr_idx = 0; nbr_idx < sim.nw.degree(nd); nbr_idx++){
				int nbr = sim.nw.node_neighbor(nd, nbr_idx);
				int overlap = 0;
				for (int f = 0; f < sim.nw.features; f++){
					if (sim.nw.states[nd][f] == sim.nw.states[nbr][f]){
						overlap++;
					}
				}
				if(overlap == 0){
					points[0]++;
				}else if(overlap == sim.nw.features){
					points[2]++;
				}else{
					points[1]++;
				}
			}
		}
		
		for (int i = 0; i < points.length; i++) {
			points[i] /= 2.0;
		}
		
		addPoints(1);
		reallocate_series();
	}

	private void addPoints(double time) {
		for (int i = 0; i < points.length; i++) {
			double[] point = {time, points[i]/sim.nw.n_edges()};
			serieses.get(i).add(point);
		}
	}
	
	@Override
	public void interaction(int i, int j, int[] oldState, int[] newState){
		int node = sim.nw.size * i + j;
		for(int nbr_idx = 0; nbr_idx < sim.nw.degree(node); nbr_idx++){
			int nbr = sim.nw.node_neighbor(node, nbr_idx);
			
			int oldOverlap = 0;
			int newOverlap = 0;
			
			for (int f = 0; f < sim.nw.features; f++){
				if (oldState[f] == sim.nw.states[nbr][f]){
					oldOverlap++;
				}
				if (newState[f] == sim.nw.states[nbr][f]){
					newOverlap++;
				}
			}
			if(oldOverlap == 0){
				points[0]--;
			}else if(oldOverlap == sim.nw.features){
				points[2]--;
			}else{
				points[1]--;
			}
			if(newOverlap == 0){
				points[0]++;
			}else if(newOverlap == sim.nw.features){
				points[2]++;
			}else{
				points[1]++;
			}
		}
	}

	public void epoch() {
		plot();
	}
}
