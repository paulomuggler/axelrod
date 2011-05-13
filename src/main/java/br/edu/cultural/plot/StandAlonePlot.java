package br.edu.cultural.plot;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import br.edu.cultural.gui.ClassNameComboBoxRenderer;
import br.edu.cultural.simulation.CultureDisseminationSimulation;

public abstract class StandAlonePlot implements Runnable {
	
	protected static final int NW_REFRESH_ADJUST = 100;

	@SuppressWarnings("serial")
	public static void start_from_dialog(JFrame parent, final Class<? extends StandAlonePlot> plot_type) {
		JPanel setup = new JPanel(new MigLayout("fillx"));
		
		final JComboBox simulation_type_in = new JComboBox();
		simulation_type_in.setRenderer(new ClassNameComboBoxRenderer());
		for (Class<?> cl : CultureDisseminationSimulation.simulationClasses) {
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
		
		final JLabel invar_in_lbl = new JLabel();
		final JLabel var_in_low_lbl = new JLabel();
		final JLabel var_in_hi_lbl = new JLabel();
		
		ButtonGroup variable_parameter_in = new ButtonGroup();

		final JRadioButton traits_is_variable = new JRadioButton();
		traits_is_variable.setAction(new AbstractAction("features") {
			public void actionPerformed(ActionEvent e) {
				invar_in_lbl.setText(traits_is_variable.isSelected()? "Features: " : "Traits");
				var_in_low_lbl.setText((traits_is_variable.isSelected()? "Features: " : "Traits")+" lower bound");
				var_in_hi_lbl.setText((traits_is_variable.isSelected()? "Features: " : "Traits")+" upper bound");
			}
		});
		final JRadioButton features_is_variable = new JRadioButton(new AbstractAction("features") {
			public void actionPerformed(ActionEvent e) {
				var_in_low_lbl.setText((traits_is_variable.isSelected()? "Features: " : "Traits")+" lower bound");
				var_in_hi_lbl.setText((traits_is_variable.isSelected()? "Features: " : "Traits")+" upper bound");
			}
		});
		variable_parameter_in.add(features_is_variable);
		variable_parameter_in.add(traits_is_variable);
		traits_is_variable.doClick();
		
		setup.add(new JLabel("Vary: "));
		setup.add(features_is_variable, "");
		setup.add(traits_is_variable, "wrap");
		
		SpinnerNumberModel invar_param_in_model = new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1);
		final JSpinner invar_in = new JSpinner(invar_param_in_model);
		setup.add(invar_in_lbl, "split 2");
		setup.add(invar_in, "wrap");
		
		SpinnerNumberModel var_param_lower_in_model = new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1);
		final JSpinner var_lower_in = new JSpinner(var_param_lower_in_model);
		setup.add(var_in_low_lbl, "split 2");
		setup.add(var_lower_in, "");
		
		SpinnerNumberModel var_param_upper_in_model = new SpinnerNumberModel(60, 2, Integer.MAX_VALUE, 1);
		final JSpinner invar_upper_in = new JSpinner(var_param_upper_in_model);
		setup.add(new JLabel(var_in_hi_lbl+" upper bound: "), "split 2");
		setup.add(invar_upper_in, "wrap");
		
		SpinnerNumberModel steps_in_model = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
		final JSpinner steps_in = new JSpinner(steps_in_model);
		setup.add(new JLabel("vary in steps of: "), "split 2");
		setup.add(steps_in, "wrap");
		
		SpinnerNumberModel simulation_count_model = new SpinnerNumberModel(10, 1, Integer.MAX_VALUE, 1);
		final JSpinner simulation_count_in = new JSpinner(simulation_count_model);
		setup.add(new JLabel("Average from: "), "split 3, span 2");
		setup.add(simulation_count_in, "");
		setup.add(new JLabel(" simulations."), "wrap");
		
		final SpinnerNumberModel stop_spinner_model = new SpinnerNumberModel(10, 0, (int)Math.log10(Long.MAX_VALUE), 1);
		JSpinner stop_after_iterations = new JSpinner(stop_spinner_model);
		setup.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");
		setup.add(new JLabel("Stop simulation after 10^"),"");
		setup.add(stop_after_iterations, "al left");
		setup.add(new JLabel("iterations."), "grow, wrap");
		setup.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");
	
		final JDialog bam = new JDialog(parent, "New Plot...");
		
		final JButton go = new JButton();
		go.setAction(new AbstractAction("Start.") {
			private static final long serialVersionUID = -9175379487336433833L;
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				StandAlonePlot pl = null;
				try {
					pl = plot_type.getConstructor(Class.class, Boolean.class, Integer.class, Integer.class,
							Integer.class, Integer.class,
							Boolean.class, Integer.class, Integer.class, Long.class)
							.newInstance((
								Class<? extends CultureDisseminationSimulation>)simulation_type_in.getSelectedItem(), 
								periodic_boundary_in.isSelected(),
								((Number)size_in.getValue()).intValue(), 
								((Number)invar_in.getValue()).intValue(),
								((Number)var_lower_in.getValue()).intValue(),
								((Number)invar_upper_in.getValue()).intValue(),
								(features_is_variable.isSelected()),
								((Number)simulation_count_in.getValue()).intValue(), 
								((Number)steps_in.getValue()).intValue(),
								(long)Math.pow(10, stop_spinner_model.getNumber().longValue()));
					
				// this stuff just hurts my eyes, really
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (SecurityException e1) {
					e1.printStackTrace();
				} catch (InstantiationException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				} catch (NoSuchMethodException e1) {
					e1.printStackTrace();
				}

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

	protected Class<? extends CultureDisseminationSimulation> simulation_type;
	protected boolean periodic_boundary;
	protected int network_size;
	protected int invar_param;
	protected int var_param_lower;
	protected int var_param_upper;
	protected int simulation_count;
	protected boolean is_features_variable;
	protected int vary_in_steps_of;
	protected long max_iterations;

	@Override
	public void run() {
		if(is_features_variable){
			run_with_variable_features();
		}else{
			run_with_variable_traits();
		}
	}

	protected abstract void run_with_variable_traits();

	protected abstract void run_with_variable_features();

}
