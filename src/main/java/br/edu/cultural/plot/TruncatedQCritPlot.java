package br.edu.cultural.plot;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;
import br.edu.cultural.gui.ClassNameComboBoxRenderer;
import br.edu.cultural.network.CulturalNetwork;
import br.edu.cultural.network.Utils;
import br.edu.cultural.simulation.CultureDisseminationSimulation;

// NOT YET IMPLEMENTED
public class TruncatedQCritPlot extends StandAlonePlot {

	public static void start_from_dialog(JFrame parent){
	}
	
	private static final int NW_REFRESH_ADJUST = 100;
	
	Class<? extends CultureDisseminationSimulation> simulation_type;
	boolean periodic_boundary;
	int network_size;
	int features;
	int traits_lower;
	int traits_upper;
	
	public TruncatedQCritPlot(Class<? extends CultureDisseminationSimulation> simulation_type,
			boolean periodic_boundary, int network_size, int features,
			int traits_lower, int traits_upper) {
		super();
		this.simulation_type = simulation_type;
		this.periodic_boundary = periodic_boundary;
		this.network_size = network_size;
		this.features = features;
		this.traits_lower = traits_lower;
		this.traits_upper = traits_upper;
	}



	@Override
	public void run() {
		
			int numSims = 10;

			double[][] series = new double[2][(traits_upper - traits_lower + 1)];
			int si = 0;
			ScatterPlotter plotter = null;
			for (int q = traits_lower; q <= traits_upper; q++) {
				System.out.println("q: " + q);
				double[][] cSizes = new double[2][numSims];
				for (int i = 0; i < numSims; i++) {
					CultureDisseminationSimulation sim =
						CultureDisseminationSimulation.factory(
								(Class<? extends CultureDisseminationSimulation>) this.simulation_type,
								new CulturalNetwork(network_size, features, q, this.periodic_boundary, NW_REFRESH_ADJUST));
					sim.start();
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
							.format("L = %d, F = %d", network_size, features), series);
					plotter.mostra();
				}
				plotter.setSeries(series);
			}
	}
}
