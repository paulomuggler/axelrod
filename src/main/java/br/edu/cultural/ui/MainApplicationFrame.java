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
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import br.edu.cultural.network.CulturalNetwork;
import br.edu.cultural.network.State;
import br.edu.cultural.plot.ActiveEdgesScatterPlot;
import br.edu.cultural.plot.ActiveNodesScatterPlot;
import br.edu.cultural.plot.ActiveRoomScatterPlot;
import br.edu.cultural.plot.ActiveRoomsHistogram;
import br.edu.cultural.plot.CommonFeaturesScatterPlot;
import br.edu.cultural.plot.CriticalityPlot;
import br.edu.cultural.plot.CultureDistributionScatterPlot;
import br.edu.cultural.plot.EnergyPlot;
import br.edu.cultural.plot.EntropyPlot;
import br.edu.cultural.plot.EntropyTimePlot;
import br.edu.cultural.plot.EntropyVsEnergyPlot;
import br.edu.cultural.plot.LyapunovPlot;
import br.edu.cultural.plot.OrbitPlot;
import br.edu.cultural.plot.OrderParametersScatterPlot;
import br.edu.cultural.plot.RemainingTraitsPlot;
import br.edu.cultural.plot.ChiPlot;
import br.edu.cultural.plot.CultureSizesDistributed;
import br.edu.cultural.plot.HistogramTransition;
import br.edu.cultural.plot.StandAlonePlot;
import br.edu.cultural.plot.TimeToAbsortion;
import br.edu.cultural.plot.TruncatedCriticalityPlot;
import br.edu.cultural.simulation.CultureDisseminationSimulation;
import br.edu.cultural.simulation.CultureDisseminationSimulation.SimulationState;
import br.edu.cultural.simulation.SimulationEventListener;
import br.edu.cultural.simulation.SimulationEventListener.SimulationEventAdapter;

public class MainApplicationFrame extends JFrame {

	/** */
	private static final long serialVersionUID = -2360890643572400835L;

	private static final int FRAME_WIDTH = (int) (Toolkit.getDefaultToolkit().getScreenSize().width*0.96);;
	private static final int FRAME_HEIGHT = (int) (Toolkit.getDefaultToolkit().getScreenSize().height*0.96);
	private static final int CANVAS_HEIGHT = (int) (Toolkit.getDefaultToolkit().getScreenSize().height*0.9);

	private static final String APP_TITLE = "Axelrod Simulation";

	private static JFileChooser fc;

	JComboBox simulation_type_in;
	JCheckBox periodicBoundarySelect;
	JCheckBox deferredUpdateSelect;
	JCheckBox simulationTimeAdjustSelect;

	JMenuBar menuBar = new JMenuBar();
	JMenu fileMenu = new JMenu("File");
	JMenu plotMenu = new JMenu("Plot");

	private JList plotsJList;
	private DefaultListModel plotsListModel;
	private JList activePlotsJList;
	private DefaultListModel activePlotsListModel;

	JPanel controls = new JPanel(new MigLayout("fillx"));
	
	JFrame simulationPropertiesFrame = new JFrame("Simulation Properties");
	JPanel simulationProperties = new JPanel(new MigLayout("fillx"));
	
	SpinnerNumberModel stop_spinner_model = new SpinnerNumberModel(10, 0, (int)Math.log10(Long.MAX_VALUE), 1);
	JSpinner stop_after_epochs = new JSpinner(stop_spinner_model);

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
	JCheckBox enable_visual_representation = new JCheckBox("Enable visual representation");
	
	ButtonGroup representations = new ButtonGroup();
	@SuppressWarnings("serial")
	JRadioButton colorRepresentation = new JRadioButton(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			MainApplicationFrame.this.switchCanvas(new CultureColorsCanvas(CANVAS_HEIGHT, sim.nw));
		}
	});
	@SuppressWarnings("serial")
	JRadioButton bordersRepresentation = new JRadioButton(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			MainApplicationFrame.this.switchCanvas(new CultureBordersCanvas(CANVAS_HEIGHT, sim.nw));
		}
	});
	
	JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
	JSlider networkRefreshRateSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, 10);

	JTextArea console_out;

	public CultureCanvas canvas;
	public CultureDisseminationSimulation sim;
	Thread simThr = new Thread(sim);

	private PlotAction activeNodesPlotAction;
	private PlotAction activeEdgesPlotAction;
	private PlotAction cultureDistributionPlotAction;
	private PlotAction activeRoomsPlotAction;
	private PlotAction activeRoomsHistogramAction;
	private PlotAction commonFeaturesPlotAction;
	private PlotAction orderParametersPlotAction;
	private PlotAction lyapunovPlotAction;
	private PlotAction remainingTraitsPlotAction;
	private PlotAction entropyTimePlotAction;
	private PlotAction orbitPlotAction;
	
	public MainApplicationFrame() {

		setTitle(APP_TITLE);
		
		simulationPropertiesFrame.add(simulationProperties);

		simulation_type_in = new JComboBox();
		simulation_type_in.setRenderer(new ClassNameComboBoxRenderer());
		
		for (Class<?> cl : CultureDisseminationSimulation.simulationClasses) {
			simulation_type_in.addItem(cl);
		}
		
		periodicBoundarySelect = new JCheckBox("Periodic boundary condition");
		periodicBoundarySelect.setSelected(true);
		
		deferredUpdateSelect = new JCheckBox("Defer representation updates (optimization)");
		
		simulationTimeAdjustSelect = new JCheckBox("Adjust simulation time with L^2");


		reset_simulation_button.addActionListener(reset_simulation);
		toggle_simulation_button.addActionListener(toggle_simulation);
		step_simulation_button.addActionListener(step_simulation);
		enable_visual_representation.setSelected(true);

		fileMenu.add(simulation_properties);
		fileMenu.add(load_from_file);
		fileMenu.add(save_to_file);
		fileMenu.add(quit);

		Dimension listSize = new Dimension(200, 100);
		plotsListModel = new DefaultListModel();
		plotsJList = new JList(plotsListModel);
		plotsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		plotsJList.setSelectedIndex(0);
		plotsJList.setVisibleRowCount(5);
		plotsJList.addMouseListener(plotsListMouseHandler());
		JScrollPane plotListScrollPane = new JScrollPane(plotsJList);
		plotListScrollPane.setPreferredSize(listSize);

		activePlotsListModel = new DefaultListModel();
		activePlotsJList = new JList(activePlotsListModel);
		activePlotsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		activePlotsJList.setSelectedIndex(0);
		activePlotsJList.setVisibleRowCount(5);
		activePlotsJList.addMouseListener(activePlotsListMouseHandler());
		JScrollPane activePlotListScrollPane = new JScrollPane(activePlotsJList);
		activePlotListScrollPane.setPreferredSize(listSize);

		reset_simulation.actionPerformed(null);

		menuBar.add(fileMenu);
		menuBar.add(plotMenu);

		reset_simulation_button.setPreferredSize(new Dimension(80, 20));
		toggle_simulation_button.setPreferredSize(new Dimension(80, 20));
		step_simulation_button.setPreferredSize(new Dimension(80, 20));

		speedSlider.setPreferredSize(new Dimension(240, 18));
		networkRefreshRateSlider.setPreferredSize(new Dimension(240, 18));
		networkRefreshRateSlider.setValue(100);
		
		enable_visual_representation.setPreferredSize(new Dimension(240, 36));
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
		
		simulationProperties.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");
		simulationProperties.add(new JLabel("Stop simulation after 10^"),"");
		simulationProperties.add(stop_after_epochs, "al left");
		simulationProperties.add(new JLabel("epochs."), "grow, wrap");
		simulationProperties.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");
		
		simulationProperties.add(deferredUpdateSelect, "span3, grow, wrap");
		simulationProperties.add(new JLabel("Network refresh adjust:"), "wrap");
		simulationProperties.add(networkRefreshRateSlider, "span 3, grow, wrap, gapbottom 18");
		simulationProperties.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");
		
		simulationProperties.add(simulationTimeAdjustSelect, "span3, grow, wrap");
		simulationProperties.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");

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

		controls.add(new JLabel("State pencil color: "), "wrap, gapbottom 3");
		controls.add(paintTxtIn, "span 3, split 2, growx");
		controls.add(paintSample, "wrap");
		controls.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");

		controls.add(new JLabel("Plots:"), "wrap");
		controls.add(plotListScrollPane, "span 3, split 2, grow");
		controls.add(activePlotListScrollPane, "wrap, grow");
		controls.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");
		
		controls.add(new JLabel("Representations:"), "wrap");
		controls.add(representations_panel, "wrap");
		controls.add(enable_visual_representation, "span 3, wrap");
		controls.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");

		controls.add(new JLabel("Speed:"), "wrap");
		controls.add(speedSlider, "span 3, grow, wrap, gapbottom 18");
		
		controls.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");

		controls.add(toggle_simulation_button, "span 3, split 3, growx");
		controls.add(step_simulation_button, "growx");
		controls.add(reset_simulation_button, "growx, wrap");

		Container pane = this.getContentPane();
		pane.add(menuBar, BorderLayout.NORTH);
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
				MainApplicationFrame.this.sim
						.setSpeed(((JSlider) e.getSource()).getValue());
			}
		};
	}
	
	private ChangeListener networkRefreshSliderHandler() {
		return new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				MainApplicationFrame.this.sim.nw
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
					MainApplicationFrame.this.updateStroke();
				}
			}
		};
	}

	private MouseAdapter activePlotsListMouseHandler() {
		return new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = activePlotsJList.locationToIndex(e.getPoint());
					PlotAction plot = (PlotAction) activePlotsListModel
							.getElementAt(index);
					activePlotsJList.ensureIndexIsVisible(index);
					plot.unlink(sim);
					activePlotsListModel.removeElement(plot);
					plotsListModel.addElement(plot);
					plotMenu.remove(plot.menuItemForThisAction());
				}
			}
		};
	}

	private MouseAdapter plotsListMouseHandler() {
		return new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = plotsJList.locationToIndex(e.getPoint());
					PlotAction plot = (PlotAction) plotsListModel
							.getElementAt(index);
					plotsJList.ensureIndexIsVisible(index);
					plot.link(sim);
					plotsListModel.removeElement(plot);
					activePlotsListModel.addElement(plot);
					plotMenu.add(plot.menuItemForThisAction());
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

	@SuppressWarnings("serial")
	private void resetSimulation() {
		Container pane = this.getContentPane();
		if (canvas != null) {
			pane.remove(canvas);
		}
		deferredUpdateSelect.setSelected(true);
		simulationTimeAdjustSelect.setSelected(true);
		if(!colorRepresentation.isSelected() && !bordersRepresentation.isSelected()){
			colorRepresentation.setSelected(true);
		}
		
		if (enable_visual_representation.isSelected()) {
			if(colorRepresentation.isSelected()){
				canvas = new CultureColorsCanvas(CANVAS_HEIGHT, sim.nw);
			}else{
				canvas = new CultureBordersCanvas(CANVAS_HEIGHT, sim.nw);
			}
			pane.add(canvas, BorderLayout.CENTER);
			SimulationEventListener canvasRepaintListener = new SimulationEventAdapter() {
				public void interaction(int i, int j, int[] oldState, int[] newState) {
					canvas.repaint();
				}
			};
			sim.addListener(canvasRepaintListener);
			sim.setDefer_update(deferredUpdateSelect.isSelected());
			sim.set_adjust_simulation_time(simulationTimeAdjustSelect.isSelected());
		}
		simThr = new Thread(sim);
		toggle_simulation_button.setText("Start");
		step_simulation_button.setEnabled(true);
		nw_size_in.setValue(sim.nw.size);
		f_in.setValue(sim.nw.features);
		q_in.setValue(sim.nw.traits);
		
		activeNodesPlotAction = new PlotAction("Active Nodes",
				"Active Nodes - Scatter Plot", new ActiveNodesScatterPlot());
		
		activeEdgesPlotAction = new PlotAction("Active Edges",
				"Active Edges - Scatter Plot", new ActiveEdgesScatterPlot());

		cultureDistributionPlotAction = new PlotAction("Culture Distribution",
				"Cultures sizes distribution plot",
				new CultureDistributionScatterPlot());

		// PLOT ACTIONS
		activeRoomsPlotAction = new PlotAction("Rooms plot",
				"Active Rooms - Scatter Plot", new ActiveRoomScatterPlot());
		
		activeRoomsHistogramAction = new PlotAction("Rooms histogram",
				"Active Rooms - Histogram", new ActiveRoomsHistogram());
		
		commonFeaturesPlotAction = new PlotAction("Common features plot",
				"Common Features - Scatter Plot", new CommonFeaturesScatterPlot());
		
		orderParametersPlotAction = new PlotAction("Order parameters plot",
				"Order parameters - Scatter Plot", new OrderParametersScatterPlot());
		
		lyapunovPlotAction = new PlotAction("Lyapunov plot",
				"Lyapunov Plot", new LyapunovPlot());

		remainingTraitsPlotAction = new PlotAction("Remaining Traits plot",
				"Remaining Traits Plot", new RemainingTraitsPlot());
		
		entropyTimePlotAction = new PlotAction("Entropy Time Series plot",
				"Entropy Time Series Plot", new EntropyTimePlot());
		
		orbitPlotAction = new PlotAction("Orbit plot",
				"Orbit Plot", new OrbitPlot());
		
		
		// STANDALONE PLOTS
		JMenu standalonePlots = new JMenu("Standalone Plots");
		standalonePlots.add(new AbstractAction("Criticality Plot") {
			public void actionPerformed(ActionEvent e) {
				StandAlonePlot.start_from_dialog(MainApplicationFrame.this, CriticalityPlot.class);
			}
		});
		standalonePlots.add(new AbstractAction("Time to Absortion Plot") {
			public void actionPerformed(ActionEvent e) {
				StandAlonePlot.start_from_dialog(MainApplicationFrame.this, TimeToAbsortion.class);
			}
		});
		standalonePlots.add(new AbstractAction("Energy Plot") {
			public void actionPerformed(ActionEvent e) {
				StandAlonePlot.start_from_dialog(MainApplicationFrame.this, EnergyPlot.class);
			}
		});
		standalonePlots.add(new AbstractAction("Truncated Criticality Plot") {
			public void actionPerformed(ActionEvent e) {
				StandAlonePlot.start_from_dialog(MainApplicationFrame.this, TruncatedCriticalityPlot.class);
			}
		});
		standalonePlots.add(new AbstractAction("Entropy Plot") {
			public void actionPerformed(ActionEvent e) {
				StandAlonePlot.start_from_dialog(MainApplicationFrame.this, EntropyPlot.class);
			}
		});
		standalonePlots.add(new AbstractAction("Entropy vs Energy Plot") {
			public void actionPerformed(ActionEvent e) {
				StandAlonePlot.start_from_dialog(MainApplicationFrame.this, EntropyVsEnergyPlot.class);
			}
		});
		standalonePlots.add(new AbstractAction("Histogram Transition") {
			public void actionPerformed(ActionEvent e) {
				StandAlonePlot.start_from_dialog(MainApplicationFrame.this, HistogramTransition.class);
			}
		});
		standalonePlots.add(new AbstractAction("Chi Plot") {
			public void actionPerformed(ActionEvent e) {
				StandAlonePlot.start_from_dialog(MainApplicationFrame.this, ChiPlot.class);
			}
		});
		standalonePlots.add(new AbstractAction("Culture Distribution Plot") {
			public void actionPerformed(ActionEvent e) {
				StandAlonePlot.start_from_dialog(MainApplicationFrame.this, CultureSizesDistributed.class);
			}
		});
		plotMenu.removeAll();
		plotMenu.add(standalonePlots);
		plotsListModel.removeAllElements();
		activePlotsListModel.removeAllElements();
		PlotAction[] plotActions = { activeNodesPlotAction, activeEdgesPlotAction, cultureDistributionPlotAction,
				activeRoomsPlotAction, activeRoomsHistogramAction, commonFeaturesPlotAction, orderParametersPlotAction, lyapunovPlotAction, remainingTraitsPlotAction, entropyTimePlotAction, orbitPlotAction};
		for (PlotAction plot : plotActions) {
			plotsListModel.addElement(plot);
		}
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
				
				sim.stop_after_epochs((long) Math.pow(10, ((SpinnerNumberModel)stop_after_epochs.getModel()).getNumber().longValue()));
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
				
				deferredUpdateSelect.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						sim.setDefer_update(deferredUpdateSelect.isSelected());
					}
				});
				sim.setDefer_update(deferredUpdateSelect.isSelected());
				
				simulationTimeAdjustSelect.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						sim.set_adjust_simulation_time(simulationTimeAdjustSelect.isSelected());
					}
				});
				
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

	Action save_to_file = new AbstractAction("Save...") {
		private static final long serialVersionUID = -4675654942388512094L;

		public void actionPerformed(ActionEvent e) {
			int select = getFileChooser().showSaveDialog(MainApplicationFrame.this);
			if (select == JFileChooser.APPROVE_OPTION) {
				File f = getFileChooser().getSelectedFile();
				try {
					sim.nw.save_to_file(f);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(MainApplicationFrame.this,
							"Error saving to file: " + e1.getMessage(),
							"Error!", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	};

	Action load_from_file = new AbstractAction("Load...") {
		private static final long serialVersionUID = -4675654942388512094L;

		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			System.out.println("load simulation from file...");
			int select = getFileChooser().showOpenDialog(MainApplicationFrame.this);
			if (select == JFileChooser.APPROVE_OPTION) {
				File f = getFileChooser().getSelectedFile();
				try {
					CulturalNetwork nw = new CulturalNetwork(f);
					if (sim != null)
						sim.finish();
					sim = CultureDisseminationSimulation.factory((Class<? extends CultureDisseminationSimulation>) simulation_type_in.getSelectedItem(), nw);
					resetSimulation();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(MainApplicationFrame.this,
							"Error saving to file: " + e1.getMessage(),
							"Error!", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	};
	
	Action simulation_properties = new AbstractAction("Simulation Properties...") {
		private static final long serialVersionUID = -4675654942388512094L;

		public void actionPerformed(ActionEvent e) {
			simulationPropertiesFrame.pack();
			simulationPropertiesFrame.setVisible(true);
		}
	};

	Action quit = new AbstractAction("Exit") {
		private static final long serialVersionUID = -4675654942388512094L;

		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	};

	public static JFileChooser getFileChooser() {
		return fc == null? fc = new JFileChooser() : fc;
	}
	
	protected void switchCanvas(final CultureCanvas cnv){
		final Container pane = MainApplicationFrame.this.getContentPane();
		if (canvas != null) {
			pane.remove(canvas);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() { 
					MainApplicationFrame.this.pack();
					MainApplicationFrame.this.repaint();
					pane.repaint();
				}
			});
		}
		boolean toggled = false;
		if(MainApplicationFrame.this.sim.state.equals(CultureDisseminationSimulation.SimulationState.RUNNING)){
			MainApplicationFrame.this.toggle_simulation.actionPerformed(null);
			toggled = true;
		}
		canvas = cnv;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				pane.add(canvas);
				MainApplicationFrame.this.pack();
				MainApplicationFrame.this.repaint();
				pane.repaint();
			}
		});
		if(toggled){
			MainApplicationFrame.this.toggle_simulation.actionPerformed(null);
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
