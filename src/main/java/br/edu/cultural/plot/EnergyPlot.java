package br.edu.cultural.plot;

import br.edu.cultural.network.CulturalNetwork;
import br.edu.cultural.network.Utils;
import br.edu.cultural.simulation.CultureDisseminationSimulation;

public class EnergyPlot extends StandAlonePlot {
	
	int series_i = 0;
	double[][] series;
	
	public EnergyPlot(
			Class<? extends CultureDisseminationSimulation> simulation_type,
			Boolean periodic_boundary, Integer network_size, Integer invar_param,
			Integer var_param_lower, Integer var_param_upper,
			Boolean is_features_variable, Integer simulation_count, Integer vary_in_steps_of, Long max_epochs, Boolean adjust_time) {
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
		this.max_epochs = max_epochs;
		this.adjust_time = this.adjust_time;
		series = new double[2][(var_param_upper - var_param_lower + 1)/this.vary_in_steps_of];
	}
	
	protected void run_with_variable_features() {
		int f_low = var_param_lower;
		int f_hi = var_param_upper;
		int traits = invar_param;
		
		plotter = new ScatterPlotter("Energy Plot", String
				.format("L = %d, q = %d, Truncate = 10^%d, %d Ensembles ", network_size, traits, (int)Math.log10(max_epochs), simulation_count), series, "F", "Energy/node");
		addStopPlotWindowListener();
		plotter.pack();
		plotter.setVisible(true);
		
		for (int f = f_low; f <= f_hi  && !plot_aborted; f+=vary_in_steps_of) {
			System.out.println("F: " + f);
			plot_point_features(f, traits);
		}
		
	}
	
	protected void run_with_variable_traits() {
		int q_low = var_param_lower;
		int q_hi = var_param_upper;
		int features = invar_param;
		
		plotter = new ScatterPlotter("Energy Plot", String
				.format("L = %d, F = %d, Truncate = 10^%d, %d Ensembles", network_size, invar_param, (int)Math.log10(max_epochs), simulation_count), series, "q", "Energy/node");
		
		addStopPlotWindowListener();
		plotter.pack();
		plotter.setVisible(true);
		
		for (int q = q_low; q <= q_hi && !plot_aborted; q+=vary_in_steps_of) {
			System.out.println("q: " + q);
			plot_point_traits(features, q);
		}
	}

	private void plot_point_features(int features, int traits) {
		double[][] sTimes = new double[2][simulation_count];
		double energy;
		for (int i = 0; i < simulation_count && !plot_aborted; i++) {
			CultureDisseminationSimulation sim =
				CultureDisseminationSimulation.factory(
						(Class<? extends CultureDisseminationSimulation>) this.simulation_type,
						new CulturalNetwork(network_size, features, traits, this.periodic_boundary, NW_REFRESH_ADJUST));
			sim.stop_after_epochs(max_epochs);
			sim.setDefer_update(true);
			sim.run();
			energy = sim.nw.LyapunovPotential() + 2.*sim.nw.n_nodes*sim.nw.features;
//			absTime = sim.interactions();
			sTimes[0][i] = features;
			sTimes[1][i] = energy/sim.nw.n_nodes;
		}
		double average = Utils.array_average(sTimes[1]);
		series[0][series_i] = sTimes[0][0];
		series[1][series_i] = average;
		series_i++;
		plotter.setSeries(series);
	}
	private void plot_point_traits(int features, int traits) {
		double[][] sTimes = new double[2][simulation_count];
		double energy;
		for (int i = 0; i < simulation_count && !plot_aborted; i++) {
			CultureDisseminationSimulation sim =
				CultureDisseminationSimulation.factory(
						(Class<? extends CultureDisseminationSimulation>) this.simulation_type,
						new CulturalNetwork(network_size, features, traits, this.periodic_boundary, NW_REFRESH_ADJUST));
			sim.stop_after_epochs(max_epochs);
			sim.setDefer_update(true);
			sim.set_adjust_simulation_time(adjust_time);
			sim.run();
			energy = sim.nw.LyapunovPotential() + 2.*sim.nw.n_nodes*sim.nw.features;

			
			sTimes[0][i] = traits;
			sTimes[1][i] = (double)energy/sim.nw.n_nodes;
//			sTimes[1][i] = (double)energy/sim.nw.n_nodes;
		}
		double average = Utils.array_average(sTimes[1]);
		series[0][series_i] = sTimes[0][0];
		series[1][series_i] = average;
		series_i++;
		plotter.setSeries(series);
	}

}
