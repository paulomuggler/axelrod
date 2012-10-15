package br.edu.cultural.plot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.edu.cultural.network.CulturalNetwork;
import br.edu.cultural.simulation.CultureDisseminationSimulation;

public class CultureSizesDistributed extends StandAlonePlot {
	
	int series_i = 0;
	double[][] series;
	
	public CultureSizesDistributed(
			Class<? extends CultureDisseminationSimulation> simulation_type,
			Boolean periodic_boundary, Integer network_size, Integer invar_param,
			Integer var_param_lower, Integer var_param_upper,
			Boolean is_features_variable, Integer simulation_count, Integer vary_in_steps_of, Long max_epochs) {
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
		series = new double[2][network_size*network_size];
	}
	
	protected void run_with_variable_features() {
		int f_low = var_param_lower;
		int f_hi = var_param_upper;
		int traits = invar_param;
		
		plotter = new ScatterPlotter("Not implemented", String
				.format("Careful! L = %d, q = %d, Truncate = 10^%d, %d Ensembles ", network_size, traits, (int)Math.log10(max_epochs), simulation_count), series, "F", "Smax/N");
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
		int features = invar_param;
		
		plotter = new ScatterPlotter("Culture Sizes Histogram", String
				.format("L = %d, F = %d, q = %d, Truncate = 10^%d, %d Ensembles", network_size, invar_param, q_low, (int)Math.log10(max_epochs), simulation_count), series, "domain size", "frequency");
		
		addStopPlotWindowListener();
		plotter.pack();
		plotter.setVisible(true);
		
		plot_point_traits(features, q_low);
	
	}

	private void plot_point_features(int features, int traits) {
		double cSize = -1.0;
		for (int i = 0; i < simulation_count && !plot_aborted; i++) {
			CultureDisseminationSimulation sim =
				CultureDisseminationSimulation.factory(
						(Class<? extends CultureDisseminationSimulation>) this.simulation_type,
						new CulturalNetwork(network_size, features, traits, this.periodic_boundary, NW_REFRESH_ADJUST));
			sim.stop_after_epochs(max_epochs);
			sim.setDefer_update(true);
			sim.run();
			Integer[] culture_sizes = new ArrayList<Integer>(sim.nw
					.count_cultures().values()).toArray(new Integer[0]);
			Arrays.sort(culture_sizes);
			Integer largest_culture = (Integer) culture_sizes[culture_sizes.length - 1];
			cSize = ((double) largest_culture) / (sim.nw.size * sim.nw.size);
		}
		series[0][series_i] = series_i + 1;
		series[1][series_i] = cSize;
		series_i++;
		plotter.setSeries(series);
	}
	private void plot_point_traits(int features, int traits) {
		
		double[] domains = new double[network_size*network_size];
		for (int m = 0; m < network_size*network_size; m++){
			domains[m] = 0;
		}
		for(int s = 0; s < this.simulation_count; s++){
			System.out.println("simulation: " + s);
			CultureDisseminationSimulation sim =
				CultureDisseminationSimulation.factory((Class<? extends CultureDisseminationSimulation>) this.simulation_type, 
						new CulturalNetwork(network_size, features, traits, this.periodic_boundary, NW_REFRESH_ADJUST));
			sim.stop_after_epochs(max_epochs);
			sim.setDefer_update(true);
			sim.run();
			System.out.println("simulation stopped...");
			List<Integer> FirstList = new ArrayList<Integer>();  
			List<Integer> SecondList = new ArrayList<Integer>();  
			List<Integer> ClusterSizes = new ArrayList<Integer>(); 
			Integer node;
			Integer count = 0;
			int nbr;
			int nm;
			
			
			for (node = 0; node < sim.nw.n_nodes; node++){
				FirstList.add(node);
			}
			
			while(!FirstList.isEmpty()){
				node = FirstList.remove(0);
				SecondList.add(node);
				while(!SecondList.isEmpty()){
					node = SecondList.remove(0);
					for (int nbr_idx = 0; nbr_idx < sim.nw.degree(node.intValue()); nbr_idx++){
						nbr = sim.nw.adj_matrix[node.intValue()][nbr_idx];
						if(sim.nw.overlap(node.intValue(), nbr) == sim.nw.features){
							if(FirstList.remove(new Integer(nbr)) == true){
								SecondList.add(new Integer(nbr));
							}
						}
					}
					count++;
				}
				ClusterSizes.add(count);
				count = 0;
			}
			while(!ClusterSizes.isEmpty()){
				nm = ClusterSizes.remove(0).intValue();
				domains[nm - 1] += (double)nm/(this.simulation_count*sim.nw.n_nodes);
			}
		System.out.println("Clusters added");
		}
		for(int i = 0; i < network_size*network_size; i++){
			series[0][i] = i+1;
			series[1][i] = (double)domains[i];	
		}
		plotter.setSeries(series);
	}
}
