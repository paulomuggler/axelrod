package br.edu.cultural.plot;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import br.edu.cultural.network.CulturalNetwork;
import br.edu.cultural.network.Utils;
import br.edu.cultural.simulation.CultureDisseminationSimulation;

public class TruncatedQCritPlot extends StandAlonePlot {
	
	public TruncatedQCritPlot(
			Class<? extends CultureDisseminationSimulation> simulation_type,
			Boolean periodic_boundary, Integer network_size, Integer invar_param,
			Integer var_param_lower, Integer var_param_upper,
			Boolean is_features_variable, Integer simulation_count, Integer vary_in_steps_of, Long max_iterations) {
		super();
		this.simulation_type = simulation_type;
		this.periodic_boundary = periodic_boundary;
		this.network_size = network_size;
		this.invar_param = invar_param;
		this.var_param_lower = var_param_lower;
		this.var_param_upper = var_param_upper;
		this.is_features_variable = is_features_variable;
		this.simulation_count = simulation_count;
		this.vary_in_steps_of = vary_in_steps_of;
	}
	
	protected void run_with_variable_features() {
		int f_low = var_param_lower;
		int f_hi = var_param_upper;
		int traits = invar_param;
		
		// 3 series of 2 axes
		double[][][] series = new double[3][2][(var_param_upper - var_param_lower + 1)];
		int si = 0;
		ScatterPlotter plotter = null;
		for (int cur_f = f_low; cur_f <= f_hi  && !plot_aborted; cur_f+=vary_in_steps_of) {
			System.out.println("F: " + cur_f);
			double[][] overlaps = new double[3][simulation_count];
			for (int i = 0; i < simulation_count  && !plot_aborted; i++) {
				CultureDisseminationSimulation sim =
					CultureDisseminationSimulation.factory(
							(Class<? extends CultureDisseminationSimulation>) this.simulation_type,
							new CulturalNetwork(network_size, cur_f, traits, this.periodic_boundary, NW_REFRESH_ADJUST));
				sim.run();
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
							overlaps[0][i]++;
						}else if(overlap == sim.nw.features){
							overlaps[2][i]++;
						}else{
							overlaps[1][i]++;
						}
					}
				}
			}
			double overlap_none_average = Utils.array_average(overlaps[0]);
			double overlap_some_average = Utils.array_average(overlaps[1]);
			double overlap_all_average = Utils.array_average(overlaps[2]);
			series[0][0][si] = cur_f;
			series[0][1][si] = overlap_none_average / (network_size*network_size);
			series[1][0][si] = cur_f;
			series[1][1][si] = overlap_some_average / (network_size*network_size);
			series[2][0][si] = cur_f;
			series[2][1][si] = overlap_all_average / (network_size*network_size);
			si++;
			if (plotter == null) {
				plotter = new ScatterPlotter("Truncated Criticality Plot", String
						.format("L = %d, Q = %d", network_size, traits), new double[2][2], "F", "% overlap");
				plotter.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						TruncatedQCritPlot.this.stopPlot();
					}
				});
				plotter.mostra();
			}
			for(int i = 0; i < 3; i++){
				plotter.addSeries(series[i], i);
			}
		}
	}

	protected void run_with_variable_traits() {
		int q_low = var_param_lower;
		int q_hi = var_param_upper;
		int features = invar_param;
		
		// 3 series of 2 axes
		double[][][] series = new double[3][2][(var_param_upper - var_param_lower + 1)];
		int si = 0;
		ScatterPlotter plotter = null;
		for (int cur_q = q_low; cur_q <= q_hi && !plot_aborted; cur_q+=vary_in_steps_of) {
			System.out.println("F: " + cur_q);
			double[][] overlaps = new double[3][simulation_count];
			for (int i = 0; i < simulation_count && !plot_aborted; i++) {
				CultureDisseminationSimulation sim =
					CultureDisseminationSimulation.factory(
							(Class<? extends CultureDisseminationSimulation>) this.simulation_type,
							new CulturalNetwork(network_size, features, cur_q, this.periodic_boundary, NW_REFRESH_ADJUST));
				sim.run();
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
							overlaps[0][i]++;
						}else if(overlap == sim.nw.features){
							overlaps[2][i]++;
						}else{
							overlaps[1][i]++;
						}
					}
				}
			}
			double overlap_none_average = Utils.array_average(overlaps[0]);
			double overlap_some_average = Utils.array_average(overlaps[1]);
			double overlap_all_average = Utils.array_average(overlaps[2]);
			series[0][0][si] = cur_q;
			series[0][1][si] = overlap_none_average / (network_size*network_size);
			series[1][0][si] = cur_q;
			series[1][1][si] = overlap_some_average / (network_size*network_size);
			series[2][0][si] = cur_q;
			series[2][1][si] = overlap_all_average / (network_size*network_size);
			si++;
			if (plotter == null) {
				plotter = new ScatterPlotter("Truncated Criticality Plot", String
						.format("L = %d, F = %d", network_size, features), new double[2][2], "Q", "% overlap");
				plotter.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						TruncatedQCritPlot.this.stopPlot();
					}
				});
				plotter.mostra();
			}
			for(int i = 0; i < 3; i++){
				plotter.addSeries(series[i], i);
			}
		}
	}
}
