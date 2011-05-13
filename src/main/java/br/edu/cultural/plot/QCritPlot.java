package br.edu.cultural.plot;

import java.util.ArrayList;
import java.util.Arrays;


import br.edu.cultural.network.CulturalNetwork;
import br.edu.cultural.network.Utils;
import br.edu.cultural.simulation.CultureDisseminationSimulation;

public class QCritPlot extends StandAlonePlot {
	
	public QCritPlot(
			Class<? extends CultureDisseminationSimulation> simulation_type,
			boolean periodic_boundary, int network_size, int invar_param,
			int var_param_lower, int var_param_upper,
			boolean is_features_variable, int simulation_count, int vary_in_steps_of, long max_iterations) {
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
		this.max_iterations = max_iterations;
	}
	
	protected void run_with_variable_features() {
		int f_low = var_param_lower;
		int f_hi = var_param_upper;
		int traits = invar_param;
		double[][] series = new double[2][(var_param_upper - var_param_lower + 1)];
		int si = 0;
		ScatterPlotter plotter = null;
		for (int f = f_low; f <= f_hi; f+=vary_in_steps_of) {
			System.out.println("F: " + f);
			double[][] cSizes = new double[2][simulation_count];
			for (int i = 0; i < simulation_count; i++) {
				CultureDisseminationSimulation sim =
					CultureDisseminationSimulation.factory(
							(Class<? extends CultureDisseminationSimulation>) this.simulation_type,
							new CulturalNetwork(network_size, f, traits, this.periodic_boundary, NW_REFRESH_ADJUST));
				sim.run();
				Integer[] culture_sizes = new ArrayList<Integer>(sim.nw
						.count_cultures().values()).toArray(new Integer[0]);
				Arrays.sort(culture_sizes);
				Integer largest_culture = (Integer) culture_sizes[culture_sizes.length - 1];
				cSizes[0][i] = f; // current q
				cSizes[1][i] = ((double) largest_culture)
						/ (sim.nw.size * sim.nw.size); // largest culture,
				// normalized
			}
			double average = Utils.array_average(cSizes[1]);
			series[0][si] = cSizes[0][0];
			series[1][si] = average;
			si++;
			if (plotter == null) {
				plotter = new ScatterPlotter("Axelrod Simulation Plot", String
						.format("L = %d, q = %d", network_size, traits), series, "F", "% overlap");
				plotter.mostra();
			}
			plotter.setSeries(series);
		}
		
	}

	protected void run_with_variable_traits() {
		double[][] series = new double[2][(var_param_upper - var_param_lower + 1)];
		int si = 0;
		ScatterPlotter plotter = null;
		for (int q = var_param_lower; q <= var_param_upper; q+=vary_in_steps_of) {
			System.out.println("q: " + q);
			double[][] cSizes = new double[2][simulation_count];
			for (int i = 0; i < simulation_count; i++) {
				CultureDisseminationSimulation sim =
					CultureDisseminationSimulation.factory(
							(Class<? extends CultureDisseminationSimulation>) this.simulation_type,
							new CulturalNetwork(network_size, invar_param, q, this.periodic_boundary, NW_REFRESH_ADJUST));
				sim.run();
				Integer[] culture_sizes = new ArrayList<Integer>(sim.nw
						.count_cultures().values()).toArray(new Integer[0]);
				Arrays.sort(culture_sizes);
				Integer largest_culture = (Integer) culture_sizes[culture_sizes.length - 1];
				cSizes[0][i] = q; // current q
				cSizes[1][i] = ((double) largest_culture)
						/ (sim.nw.size * sim.nw.size); // largest culture,
				// normalized
			}
			double average = Utils.array_average(cSizes[1]);
			series[0][si] = cSizes[0][0];
			series[1][si] = average;
			si++;
			if (plotter == null) {
				plotter = new ScatterPlotter("Axelrod Simulation Plot", String
						.format("L = %d, F = %d", network_size, invar_param), series, "q", "% overlap");
				plotter.mostra();
			}
			plotter.setSeries(series);
		}
	}
}
