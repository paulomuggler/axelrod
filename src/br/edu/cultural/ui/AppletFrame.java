package br.edu.cultural.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintStream;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import br.edu.cultural.network.CulturalNetwork;
import br.edu.cultural.network.State;
import br.edu.cultural.simulation.CultureDisseminationSimulation;
import br.edu.cultural.simulation.CultureDisseminationSimulation.SimulationState;
import br.edu.cultural.simulation.SimulationEventListener.SimulationEventAdapter;

public class AppletFrame extends JFrame {

	/** */
	private static final long serialVersionUID = -2360890643572400835L;

	private static final int FRAME_WIDTH = (int) (Toolkit.getDefaultToolkit().getScreenSize().width*0.96);;
	private static final int FRAME_HEIGHT = (int) (Toolkit.getDefaultToolkit().getScreenSize().height*0.96);
	private static final int CANVAS_HEIGHT = (int) (Toolkit.getDefaultToolkit().getScreenSize().height*0.9);

	private static final String APP_TITLE = "Axelrod Simulation";

	JComboBox simulation_type_in;
	JCheckBox periodicBoundarySelect;

	JPanel controls = new JPanel(new MigLayout("fillx"));
	
	SpinnerNumberModel nw_size_in_model = new SpinnerNumberModel(80, 2, 9999, 1);
	JSpinner nw_size_in = new JSpinner(nw_size_in_model);
	
	SpinnerNumberModel f_in_model = new SpinnerNumberModel(2, 2, 9999, 1);
	JSpinner f_in = new JSpinner(f_in_model);
	
	SpinnerNumberModel q_in_model = new SpinnerNumberModel(2, 2, 9999, 1);
	JSpinner q_in = new JSpinner(q_in_model);

	JTextField paintTxtIn = new JTextField();
	PaintSample paintSample = new PaintSample();

	JButton reset_simulation_button = new JButton("Reset");
	JButton toggle_simulation_button = new JButton("Start");
	JButton step_simulation_button = new JButton("Step");
	
	ButtonGroup representations = new ButtonGroup();
	@SuppressWarnings("serial")
	JRadioButton colorRepresentation = new JRadioButton(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			AppletFrame.this.switchCanvas(new CultureColorsCanvas(CANVAS_HEIGHT, sim.nw));
		}
	});
	@SuppressWarnings("serial")
	JRadioButton bordersRepresentation = new JRadioButton(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			AppletFrame.this.switchCanvas(new CultureBordersCanvas(CANVAS_HEIGHT, sim.nw));
		}
	});
	
	JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
	JSlider networkRefreshRateSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, 10);

	JTextArea console_out;

	public CultureCanvas canvas;
	public CultureDisseminationSimulation sim;
	Thread simThr = new Thread(sim);

	public AppletFrame() {

		setTitle(APP_TITLE);
		
		simulation_type_in = new JComboBox();
		simulation_type_in.setRenderer(new ClassNameComboBoxRenderer());
		
		for (Class<?> cl : CultureDisseminationSimulation.simulationClasses) {
			simulation_type_in.addItem(cl);
		}
		
		periodicBoundarySelect = new JCheckBox("Periodic boundary condition");
		periodicBoundarySelect.setSelected(true);
		
		reset_simulation_button.addActionListener(reset_simulation);
		toggle_simulation_button.addActionListener(toggle_simulation);
		step_simulation_button.addActionListener(step_simulation);

		reset_simulation.actionPerformed(null);

		reset_simulation_button.setPreferredSize(new Dimension(80, 20));
		toggle_simulation_button.setPreferredSize(new Dimension(80, 20));
		step_simulation_button.setPreferredSize(new Dimension(80, 20));

		speedSlider.setPreferredSize(new Dimension(240, 18));
		networkRefreshRateSlider.setPreferredSize(new Dimension(240, 18));
		networkRefreshRateSlider.setValue(100);
		
		colorRepresentation.setText("Colors");
		representations.add(colorRepresentation);
		bordersRepresentation.setText("Borders");
		representations.add(bordersRepresentation);
		JPanel representations_panel = new JPanel();
		representations_panel.add(colorRepresentation);
		representations_panel.add(bordersRepresentation);

		paintTxtIn.setPreferredSize(new Dimension(320, 18));
		paintSample.setPreferredSize(new Dimension(16, 16));
		paintTxtIn.addActionListener(stateStrokeActionHandler());

		speedSlider.addChangeListener(speedSliderChangeHandler());
		speedSlider.setMajorTickSpacing(10);
		speedSlider.setMinorTickSpacing(1);
		speedSlider.setPaintTicks(true);
		speedSlider.setPaintLabels(true);
		
		networkRefreshRateSlider.addChangeListener(networkRefreshSliderHandler());
		networkRefreshRateSlider.setMajorTickSpacing(10);
		networkRefreshRateSlider.setMinorTickSpacing(1);
		networkRefreshRateSlider.setPaintTicks(true);
		networkRefreshRateSlider.setPaintLabels(true);
		
		controls.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");
		controls.add(simulation_type_in, "span 3, grow, wrap");
		
		controls.add(new JLabel("Size:"), "split 6");
		controls.add(nw_size_in, "");
		controls.add(new JLabel("F:"), "");
		controls.add(f_in, "");
		controls.add(new JLabel("q:"), "");
		controls.add(q_in, "wrap");
		
		controls.add(periodicBoundarySelect, "span 3, grow, wrap");
		controls.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");

		controls.add(new JLabel("State brush"), "wrap, gapbottom 3");
		controls.add(paintTxtIn, "span 3, split 2, growx");
		controls.add(paintSample, "wrap");
		controls.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");

		controls.add(new JLabel("Visualization"), "wrap");
		controls.add(representations_panel, "wrap");
		controls.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");

		controls.add(new JLabel("Speed:"), "wrap");
		controls.add(speedSlider, "span 3, grow, wrap, gapbottom 18");
		
		controls.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");

		controls.add(toggle_simulation_button, "span 3, split 3, growx");
		controls.add(step_simulation_button, "growx");
		controls.add(reset_simulation_button, "growx, wrap");

		Container pane = this.getContentPane();
		pane.add(controls, BorderLayout.WEST);

		final JPopupMenu outputMenu = new JPopupMenu("Output");
		outputMenu.add(new AbstractAction("Clear") {
			private static final long serialVersionUID = 5606674484787535063L;
			@Override
			public void actionPerformed(ActionEvent e) {
				console_out.setText("");
			}
		});
		console_out = new JTextArea(5, 30);
		console_out.setEditable(false);
		console_out.addMouseListener( new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON3){
					outputMenu.show(console_out, e.getX(), e.getY());
				}
			}
		});
		
		JScrollPane scrollOut = new JScrollPane(console_out);
		System.setOut(new PrintStream(new TextAreaOutputStream(console_out)));
		System.setErr(new PrintStream(new TextAreaOutputStream(console_out)));
		pane.add(scrollOut, BorderLayout.SOUTH);
	}

	private ChangeListener speedSliderChangeHandler() {
		return new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				AppletFrame.this.sim
						.setSpeed(((JSlider) e.getSource()).getValue());
			}
		};
	}
	
	private ChangeListener networkRefreshSliderHandler() {
		return new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				AppletFrame.this.sim.nw
						.setRefreshAdjust(((JSlider) e.getSource()).getValue());
			}
		};
	}

	private ActionListener stateStrokeActionHandler() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] state = State.parseState(paintTxtIn.getText(),
						sim.nw.features, sim.nw.traits);
				if (state != null) {
					canvas.setStateStroke(state);
					AppletFrame.this.updateStroke();
				}
			}
		};
	}

	class PaintSample extends JPanel {
		private static final long serialVersionUID = 4009724729060328197L;

		public PaintSample() {
			Border blackline = BorderFactory.createLineBorder(Color.black);
			setBorder(blackline);
		}

		public void paintComponent(Graphics g) {
			int color = canvas.rp.color(canvas.stateStroke);
			g.setColor(new Color(color));
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}

	public void updateStroke() {
		paintSample.repaint();
	}

	private void resetSimulation() {
		Container pane = this.getContentPane();
		if (canvas != null) {
			pane.remove(canvas);
		}
		if(!colorRepresentation.isSelected() && !bordersRepresentation.isSelected()){
			colorRepresentation.setSelected(true);
		}
		
		simThr = new Thread(sim);
		toggle_simulation_button.setText("Start");
		step_simulation_button.setEnabled(true);
		nw_size_in.setValue(sim.nw.size);
		f_in.setValue(sim.nw.features);
		q_in.setValue(sim.nw.traits);
		
		this.pack();
		this.repaint();
	}

	private ActionListener reset_simulation = new ActionListener() {
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			System.out.println("resetting simulation thread...");
			if (sim != null){
				sim.finish();
				System.out.println("Simulation interrupted.");
				System.out.println(sim.execution_statistics_string());
			}
				sim = CultureDisseminationSimulation.factory(
						(Class<? extends CultureDisseminationSimulation>) simulation_type_in
								.getSelectedItem(), new CulturalNetwork((Integer)nw_size_in.getValue(), 
																		(Integer) f_in.getValue(), 
																		(Integer) q_in.getValue(), 
																		periodicBoundarySelect.isSelected(), 
																		networkRefreshRateSlider.getValue()));
				
				sim.addListener(new SimulationEventAdapter(){
					public void started(){
						System.out.println("Simulation started.");
						can_be_stopped();
					}
					public void toggled_pause(){
						if(sim.state == SimulationState.RUNNING){
							can_be_stopped();
						} else if(sim.state == SimulationState.PAUSED){
							can_be_started();
						}
					}
					public void finished(){
						System.out.println("Simulation finished.");
						System.out.println(sim.execution_statistics_string());
						can_be_started();
					}
					private void can_be_started(){
						toggle_simulation_button.setText("Start");
						step_simulation_button.setEnabled(true);
					}
					private void can_be_stopped(){
						toggle_simulation_button.setText("Stop");
						step_simulation_button.setEnabled(false);
					}
				});
				
			sim.setDefer_update(false);
			resetSimulation();
		}

	};

	private ActionListener toggle_simulation = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (!simThr.isAlive() && !(sim.state == SimulationState.FINISHED)) {
				simThr.start();
				return;
			}
			sim.toggle_pause();
		}
	};

	private ActionListener step_simulation = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if(sim.state == SimulationState.PAUSED){
				sim.simulation_step();
			}
		}
	};

	protected void switchCanvas(final CultureCanvas cnv){
		final Container pane = AppletFrame.this.getContentPane();
		if (canvas != null) {
			pane.remove(canvas);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() { 
					AppletFrame.this.pack();
					AppletFrame.this.repaint();
					pane.repaint();
				}
			});
		}
		boolean toggled = false;
		if(AppletFrame.this.sim.state.equals(CultureDisseminationSimulation.SimulationState.RUNNING)){
			AppletFrame.this.toggle_simulation.actionPerformed(null);
			toggled = true;
		}
		canvas = cnv;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				pane.add(canvas);
				AppletFrame.this.pack();
				AppletFrame.this.repaint();
				pane.repaint();
			}
		});
		if(toggled){
			AppletFrame.this.toggle_simulation.actionPerformed(null);
		}
	}

	public static void main(String[] args) {
		MainApplicationFrame axelrod = new MainApplicationFrame();
		axelrod.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		axelrod.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		axelrod.pack();
		axelrod.setVisible(true);
		axelrod.setExtendedState(axelrod.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}
}
