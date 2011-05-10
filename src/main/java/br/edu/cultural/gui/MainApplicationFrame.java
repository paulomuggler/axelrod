package br.edu.cultural.gui;

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
import java.io.OutputStream;
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
import br.edu.cultural.plot.CultureDistributionScatterPlot;
import br.edu.cultural.plot.OrderParametersScatterPlot;
import br.edu.cultural.plot.Plot;
import br.edu.cultural.plot.QCritPlot;
import br.edu.cultural.simulation.CultureDisseminationSimulation;
import br.edu.cultural.simulation.CultureDisseminationSimulation.SimulationEventAdapter;
import br.edu.cultural.simulation.CultureDisseminationSimulation.SimulationEventListener;
import br.edu.cultural.simulation.CultureDisseminationSimulation.SimulationState;

public class MainApplicationFrame extends JFrame {

	/** */
	private static final long serialVersionUID = -2360890643572400835L;

	@SuppressWarnings("unused")
	private static final int FRAME_WIDTH = 1000;
	@SuppressWarnings("unused")
	private static final int FRAME_HEIGHT = (int) (Toolkit.getDefaultToolkit().getScreenSize().height*0.96);
	private static final int CANVAS_WIDTH = (int) (Toolkit.getDefaultToolkit().getScreenSize().height*0.9);

	private static final String APP_TITLE = "Axelrod Simulation";

	private static JFileChooser fc;

	JComboBox simulation_type_in;
	JCheckBox periodicBoundarySelect;
	JCheckBox deferredUpdateSelect;

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
	
	SpinnerNumberModel stop_spinner_model = new SpinnerNumberModel(10, 0, 100, 1);
	JSpinner stop_after_iterations = new JSpinner(stop_spinner_model);

	JLabel lLbl = new JLabel("Size:");
	JTextField lTxtIn = new JTextField("100");

	JLabel fLbl = new JLabel("F:");
	JTextField fTxtIn = new JTextField("2");

	JLabel qLbl = new JLabel("q:");
	JTextField qTxtIn = new JTextField("2");

	JLabel paintLbl = new JLabel("Paint with state: ");
	JTextField paintTxtIn = new JTextField();
	PaintSample paintSample = new PaintSample();

	JButton reset_simulation_button = new JButton("Reset");
	JButton toggle_simulation_button = new JButton("Start");
	JButton step_simulation_button = new JButton("Step");
	JCheckBox enable_visual_representation = new JCheckBox("Enable visual representation");
	
	ButtonGroup representations = new ButtonGroup();
	JRadioButton colorRepresentation = new JRadioButton(new AbstractAction() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8478401213428301372L;

		@Override
		public void actionPerformed(ActionEvent e) {
			MainApplicationFrame.this.switchCanvas(new CultureColorsCanvas(CANVAS_WIDTH, sim.nw));
		}
	});
	JRadioButton bordersRepresentation = new JRadioButton(new AbstractAction() {
		private static final long serialVersionUID = 2178464349352290560L;

		@Override
		public void actionPerformed(ActionEvent e) {
			MainApplicationFrame.this.switchCanvas(new CultureBordersCanvas(CANVAS_WIDTH, sim.nw));
		}
	});
	
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
			MainApplicationFrame.this.toggleSim.actionPerformed(null);
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
			MainApplicationFrame.this.toggleSim.actionPerformed(null);
		}
	}
	
	JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
	JSlider networkRefreshRateSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, 10);

	JTextArea out;

	// JLabel tLbl = new JLabel("Time: ");
	JLabel iterationsLbl = new JLabel("Iterations: ");
	JLabel iterationsLblOut = new JLabel("0");
	JLabel interactionsLbl = new JLabel("Interactions: ");
	JLabel interactionsLblOut = new JLabel("0");
	JLabel epochsLbl = new JLabel("Epochs: ");
	JLabel epochsLblOut = new JLabel("0");

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

	public MainApplicationFrame() {

		setTitle(APP_TITLE);
		
		simulationPropertiesFrame.add(simulationProperties);

		simulation_type_in = new JComboBox();
		simulation_type_in.setRenderer(new ClassNameComboBoxRenderer());
		for (Class<? extends CultureDisseminationSimulation> cl : CultureDisseminationSimulation.subclasses()) {
			simulation_type_in.addItem(cl);
		}
		
		periodicBoundarySelect = new JCheckBox("Periodic boundary condition");
		deferredUpdateSelect = new JCheckBox("Defer representation updates (optimization)");

		reset_simulation_button.addActionListener(resetSim);
		toggle_simulation_button.addActionListener(toggleSim);
		step_simulation_button.addActionListener(stepSim);
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

		resetSim.actionPerformed(null);

		menuBar.add(fileMenu);
		menuBar.add(plotMenu);

		lTxtIn.setPreferredSize(new Dimension(60, 18));
		fTxtIn.setPreferredSize(new Dimension(30, 18));
		qTxtIn.setPreferredSize(new Dimension(30, 18));

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
		// Turn on labels at major tick marks.
		speedSlider.setMajorTickSpacing(10);
		speedSlider.setMinorTickSpacing(1);
		speedSlider.setPaintTicks(true);
		speedSlider.setPaintLabels(true);
		
		networkRefreshRateSlider.addChangeListener(networkRefreshSliderHandler());
		// Turn on labels at major tick marks.
		networkRefreshRateSlider.setMajorTickSpacing(10);
		networkRefreshRateSlider.setMinorTickSpacing(1);
		networkRefreshRateSlider.setPaintTicks(true);
		networkRefreshRateSlider.setPaintLabels(true);
		
		simulationProperties.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");
		simulationProperties.add(new JLabel("Stop simulation after 10^"),"");
		simulationProperties.add(stop_after_iterations, "al left");
		simulationProperties.add(new JLabel("iterations."), "grow, wrap");
		simulationProperties.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");
		
		simulationProperties.add(deferredUpdateSelect, "span3, grow, wrap");
		simulationProperties.add(new JLabel("Network refresh adjust:"), "wrap");
		simulationProperties.add(networkRefreshRateSlider, "span 3, grow, wrap, gapbottom 18");
		simulationProperties.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");

		controls.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");
		controls.add(simulation_type_in, "span 3, grow, wrap");
		
		controls.add(lLbl, "split 6");
		controls.add(lTxtIn, "");
		controls.add(fLbl, "");
		controls.add(fTxtIn, "");
		controls.add(qLbl, "");
		controls.add(qTxtIn, "wrap");
		
		controls.add(periodicBoundarySelect, "span 3, grow, wrap");
		controls.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");

		controls.add(paintLbl, "wrap, gapbottom 3");
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
//		controls.add(new JSeparator(SwingConstants.HORIZONTAL), "span 3, grow, wrap, gaptop 9, gapbottom 9");

		controls.setPreferredSize(new Dimension(400, 800));

		Container pane = this.getContentPane();
		pane.add(menuBar, BorderLayout.NORTH);
		pane.add(controls, BorderLayout.WEST);

		final JPopupMenu outputMenu = new JPopupMenu("Output");
		outputMenu.add(new AbstractAction("Clear") {
			private static final long serialVersionUID = 5606674484787535063L;
			@Override
			public void actionPerformed(ActionEvent e) {
				out.setText("");
			}
		});
		out = new JTextArea(5, 30);
		out.setEditable(false);
		out.addMouseListener( new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON3){
					outputMenu.show(out, e.getX(), e.getY());
				}
			}
		});
		
		JScrollPane scrollOut = new JScrollPane(out);
		System.setOut(new PrintStream(textAreaOutputStream(out)));
		System.setErr(new PrintStream(textAreaOutputStream(out)));
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
					activePlotsListModel.removeElement(plot);
					plotsListModel.addElement(plot);
					plot.unlink();
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
					plotsListModel.removeElement(plot);
					activePlotsListModel.addElement(plot);
					plot.link();
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

	private OutputStream textAreaOutputStream(final JTextArea t) {
		return new OutputStream() {
			JTextArea ta = t;

			public void write(int b) {
				byte[] bs = new byte[1];
				bs[0] = (byte) b;
				ta.append(new String(bs));
				ta.setCaretPosition(ta.getDocument().getLength());
			}
		};
	}

	public void updateStroke() {
		paintSample.repaint();
	}

	private void resetGui() {
		Container pane = this.getContentPane();
		if (canvas != null) {
			pane.remove(canvas);
		}
		deferredUpdateSelect.setSelected(true);
		if(!colorRepresentation.isSelected() && !bordersRepresentation.isSelected()){
			colorRepresentation.setSelected(true);
		}
		
		if (enable_visual_representation.isSelected()) {
			if(colorRepresentation.isSelected()){
				canvas = new CultureColorsCanvas(CANVAS_WIDTH, sim.nw);
			}else{
				canvas = new CultureBordersCanvas(CANVAS_WIDTH, sim.nw);
			}
			pane.add(canvas, BorderLayout.CENTER);
			SimulationEventListener canvasRepaintListener = new SimulationEventAdapter() {
				@Override
				public void interaction(int i, int j, int[] oldState, int[] newState) {
					canvas.repaint();
				}
			};
			sim.addListener(canvasRepaintListener);
			sim.setDefer_update(deferredUpdateSelect.isSelected());
		}
		simThr = new Thread(sim);
		toggle_simulation_button.setText("Start");
		step_simulation_button.setEnabled(true);
		lTxtIn.setText(String.valueOf(sim.nw.size));
		fTxtIn.setText(String.valueOf(sim.nw.features));
		qTxtIn.setText(String.valueOf(sim.nw.traits));
		
		activeNodesPlotAction = new PlotAction("Active Nodes",
				"Active Nodes - Scatter Plot", new ActiveNodesScatterPlot()) {
			private static final long serialVersionUID = -2489956318996611551L;

			public void defer_init() {
				plotter.addChartScaleSelectorX("time");
				plotter.validate();
				plotter.repaint();
			}
		};
		
		activeEdgesPlotAction = new PlotAction("Active Edges",
				"Active Edges - Scatter Plot", new ActiveEdgesScatterPlot()) {
			private static final long serialVersionUID = -2489956318996611551L;

			public void defer_init() {
				plotter.addChartScaleSelectorX("time");
				plotter.validate();
				plotter.repaint();
			}
		};

		cultureDistributionPlotAction = new PlotAction("Culture Distribution",
				"Cultures sizes distribution plot",
				new CultureDistributionScatterPlot());
		activeRoomsPlotAction = new PlotAction("Rooms plot",
				"Active Rooms - Scatter Plot", new ActiveRoomScatterPlot());
		activeRoomsHistogramAction = new PlotAction("Rooms histogram",
				"Active Rooms - Histogram", new ActiveRoomsHistogram());
		commonFeaturesPlotAction = new PlotAction("Common features plot",
				"Common Features - Scatter Plot", new CommonFeaturesScatterPlot()){
			private static final long serialVersionUID = -2489956318996611551L;
			public void defer_init() {
				plotter.addChartScaleSelectorX("time");
				plotter.validate();
				plotter.repaint();
			}
		};
		
		orderParametersPlotAction = new PlotAction("Order parameters plot",
				"Order parameters - Scatter Plot", new OrderParametersScatterPlot()){
			private static final long serialVersionUID = -2489956318996611551L;
			public void defer_init() {
				plotter.addChartScaleSelectorX("time");
				plotter.validate();
				plotter.repaint();
			}
		};


		clearPlots();
		addPlots(activeNodesPlotAction, activeEdgesPlotAction, cultureDistributionPlotAction,
				activeRoomsPlotAction, activeRoomsHistogramAction, commonFeaturesPlotAction, orderParametersPlotAction);

		this.pack();
		this.repaint();
	}

	private void addPlots(PlotAction... plotActions) {
		for (PlotAction plot : plotActions) {
			plotMenu.add(plot);
			plotsListModel.addElement(plot);
		}
		plotMenu.add(new AbstractAction("critical Q plot...") {
			private static final long serialVersionUID = -1945523231629447680L;
			public void actionPerformed(ActionEvent e) {
				QCritPlot.start_from_dialog(MainApplicationFrame.this);
			}
		});
	}

	private void clearPlots() {
		plotMenu.removeAll();
		plotsListModel.removeAllElements();
		activePlotsListModel.removeAllElements();
	}

	private ActionListener resetSim = new ActionListener() {
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			System.out.println("resetting simulation thread...");
			if (sim != null){
				System.out.println("Simulation interrupted.");
				System.out.println(sim.execution_statistics_string());
				sim.quit();
			}
			try {
				sim = CultureDisseminationSimulation.factory(
						(Class<? extends CultureDisseminationSimulation>) simulation_type_in
								.getSelectedItem(), new CulturalNetwork(Integer
								.parseInt(lTxtIn.getText()), Integer
								.parseInt(fTxtIn.getText()), Integer
								.parseInt(qTxtIn.getText()), periodicBoundarySelect.isSelected(), networkRefreshRateSlider.getValue()));
				
				sim.stop_after_iterations((long) Math.pow(10, ((SpinnerNumberModel)stop_after_iterations.getModel()).getNumber().longValue()));
				sim.addListener(new SimulationEventAdapter(){
					public void finished(){
						System.out.println("Simulation finished.");
						System.out.println(sim.execution_statistics_string());
					}
				});
				
				deferredUpdateSelect.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						sim.setDefer_update(deferredUpdateSelect.isSelected());
					}
				});
				sim.setDefer_update(deferredUpdateSelect.isSelected());
				
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(MainApplicationFrame.this,
						"Numbers only please!", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			resetGui();
		}

	};

	private ActionListener toggleSim = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (!simThr.isAlive()) {
				simThr.start();
			}
			if (sim.state == SimulationState.RUNNING) {
				sim.stop();
				toggle_simulation_button.setText("Start");
				step_simulation_button.setEnabled(true);
			} else {
				System.out.println("starting simulation thread...");
				sim.start();
				toggle_simulation_button.setText("Stop");
				step_simulation_button.setEnabled(false);
			}
		}
	};

	private ActionListener stepSim = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			sim.simulation_step();
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
						sim.quit();
					sim = CultureDisseminationSimulation.factory((Class<? extends CultureDisseminationSimulation>) simulation_type_in.getSelectedItem(), nw);
					resetGui();
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

	class PlotAction extends AbstractAction {
		private static final long serialVersionUID = 8874503720547085785L;
		protected Plotter plotter;
		protected Plot<CultureDisseminationSimulation, ?> plot;
		private String pTitle;

		@SuppressWarnings("unchecked")
		public PlotAction(String actionCaption, String plotTitle, Plot<?, ?> p) {
			super(actionCaption);
			plot = (Plot<CultureDisseminationSimulation, ?>) p;
			pTitle = plotTitle;
		}

		public void actionPerformed(ActionEvent e) {
			if (plotter == null) {
				plotter = new Plotter(pTitle, plot);
				defer_init();
			}
			plotter.mostra();
		}

		public void defer_init() {
		};

		public void link() {
			plot.link(sim);
		}

		public void unlink() {
			plot.unlink();
		}

		public String toString() {
			return pTitle;
		}
	}

	public static void main(String[] args) {
		MainApplicationFrame axelrod = new MainApplicationFrame();
		axelrod.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		axelrod.pack();
		axelrod.setVisible(true);
		axelrod.setExtendedState(axelrod.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}

	public static JFileChooser getFileChooser() {
		return fc == null? fc = new JFileChooser() : fc;
	}
}
