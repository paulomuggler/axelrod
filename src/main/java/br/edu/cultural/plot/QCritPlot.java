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

public class QCritPlot extends StandAlonePlot {

	public static void start_from_dialog(JFrame parent){
		JPanel setup = new JPanel(new MigLayout("fillx"));
		
		final JComboBox simulation_type_in = new JComboBox();
		simulation_type_in.setRenderer(new ClassNameComboBoxRenderer());
		for (Class<? extends CultureDisseminationSimulation> cl : CultureDisseminationSimulation.subclasses()) {
			simulation_type_in.addItem(cl);
		}
		setup.add(simulation_type_in, "span 2, wrap");
		
		final JCheckBox periodic_boundary_in = new JCheckBox("Periodic network boundary:");
		periodic_boundary_in.setSelected(true);
		setup.add(periodic_boundary_in, "span 2, wrap");
		
		SpinnerNumberModel size_spinner_model = new SpinnerNumberModel(240, 2, Integer.MAX_VALUE, 1);
		final JSpinner size_in = new JSpinner(size_spinner_model);
		setup.add(new JLabel("Size: "), "split 2");
		setup.add(size_in, "");
		
		SpinnerNumberModel features_in_model = new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1);
		final JSpinner features_in = new JSpinner(features_in_model);
		setup.add(new JLabel("Features: "), "split 2");
		setup.add(features_in, "wrap");
		
		SpinnerNumberModel traits_lower_spinner_model = new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1);
		final JSpinner traits_lower_in = new JSpinner(traits_lower_spinner_model);
		setup.add(new JLabel("Q lower bound: "), "split 2");
		setup.add(traits_lower_in, "");
		
		SpinnerNumberModel traits_upper_spinner_model = new SpinnerNumberModel(60, 2, Integer.MAX_VALUE, 1);
		final JSpinner traits_upper_in = new JSpinner(traits_upper_spinner_model);
		setup.add(new JLabel("Q upper bound: "), "split 2");
		setup.add(traits_upper_in, "wrap");
		
		SpinnerNumberModel simulation_count_model = new SpinnerNumberModel(10, 1, Integer.MAX_VALUE, 1);
		final JSpinner simulation_count_in = new JSpinner(simulation_count_model);
		setup.add(new JLabel("Average from: "), "split 3, span 2");
		setup.add(simulation_count_in, "");
		setup.add(new JLabel(" simulations."), "wrap");

		final JDialog bam = new JDialog(parent, "New Plot...");
		
		final JButton go = new JButton();
		go.setAction(new AbstractAction("Start.") {
			private static final long serialVersionUID = -9175379487336433833L;
			@Override
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unchecked")
				QCritPlot pl = new QCritPlot((Class<? extends CultureDisseminationSimulation>)simulation_type_in.getSelectedItem(), 
						periodic_boundary_in.isSelected(),
						((Number)size_in.getValue()).intValue(), 
						((Number)features_in.getValue()).intValue(),
						((Number)traits_lower_in.getValue()).intValue(),
						((Number)traits_upper_in.getValue()).intValue(),
						((Number)simulation_count_in.getValue()).intValue());
				new Thread(pl).start();
				bam.setVisible(false);
				bam.dispose();
			}
		});
		setup.add(go, "span 2, al center");
		
		bam.getContentPane().add(setup);
		bam.pack();
		bam.setVisible(true);
	}
	
	private static final int NW_REFRESH_ADJUST = 100;
	
	Class<? extends CultureDisseminationSimulation> simulation_type;
	boolean periodic_boundary;
	int network_size;
	int features;
	int traits_lower;
	int traits_upper;
	int simulation_count;
	
	public QCritPlot(Class<? extends CultureDisseminationSimulation> simulation_type,
			boolean periodic_boundary, int network_size, int features,
			int traits_lower, int traits_upper, int simulation_count) {
		super();
		this.simulation_type = simulation_type;
		this.periodic_boundary = periodic_boundary;
		this.network_size = network_size;
		this.features = features;
		this.traits_lower = traits_lower;
		this.traits_upper = traits_upper;
		this.simulation_count = simulation_count;
	}



	@Override
	public void run() {
		
			double[][] series = new double[2][(traits_upper - traits_lower + 1)];
			int si = 0;
			ScatterPlotter plotter = null;
			for (int q = traits_lower; q <= traits_upper; q++) {
				System.out.println("q: " + q);
				double[][] cSizes = new double[2][simulation_count];
				for (int i = 0; i < simulation_count; i++) {
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
