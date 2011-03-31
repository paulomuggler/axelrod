/**
 * 
 */
package br.edu.axelrod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;


/**
 * @author muggler
 * 
 */
public class AxelrodSimulation implements Runnable{
	final Random rand = new Random();

	// Configurations
	int speed = 100;

	// Statistics
	long sim_start_ms;
	long sim_finish_ms;
	int iterations = 0;
	int interactions = 0;
	double[][] series;
	ScatterPlotter plot;
	private final List<Integer> activeNodesSeries = new ArrayList<Integer>();

	//
	public final AxelrodNetwork nw;
	SimulationObserver obs;
	public int state = 2; // 2 => stopped; 1 => running; 0 => finished


	public AxelrodSimulation(int size, int features, int traits) {
		this(new AxelrodNetwork(size, features, traits));
	}
	
	public AxelrodSimulation(AxelrodNetwork nw) {
		this.nw = nw;
		createActiveNodesPlot(nw);
		this.obs = new BaseSimulationObserver();
	}

	private void createActiveNodesPlot(AxelrodNetwork nw) {
		series = new double[2][1];
		for (int i = 0; i < 1; i++) {
			series[0][i] = 1;
		}
		plot = new ScatterPlotter("Simulation Plot - Interactive nodes",
				String.format("L = %d, F = %d, q = %d", nw.size, nw.features, nw.traits), "time", "active nodes", series);
	}

	/**
	 * Performs a single step of the axelrod simulation
	 * @throws InterruptedException
	 */
	public void sim_step() {
		if (this.nw.interactive_nodes().size() == 0) {
			this.state = 0; // absorbent state
			return;
		}
		int node = nw.random_interactive_node();
		this.networkDynamic(node);
		this.iterations++;
	}

	public void run() {
		this.iterations = 0;
		this.sim_start_ms = System.currentTimeMillis();
		while (!(this.state == 0)) {
			pause();
			throttle();
			this.sim_step();
			if(this.iterations%(nw.n_nodes) == 0){
				activeNodesSeries.add(nw.interactiveNodes.size());
				if(plot.isActive()){
					updateActiveNodesPlot();
				}
			}
		}
		this.sim_finish_ms = System.currentTimeMillis();
		print_execution_statistics();
	}

	private void updateActiveNodesPlot() {
		series = new double[2][activeNodesSeries.size()];
		for (int i = 0; i < activeNodesSeries.size(); i++) {
			series[0][i] = i+1;
			series[1][i] = activeNodesSeries.get(i)/(double)nw.n_nodes;
		}
		plot.setSeries(series);
	}

	private void pause() {
		while(this.state == 2){
			try {
				Thread.sleep(33);
			} catch (InterruptedException e) {}
		}
	}

	private void throttle() {
		if(this.speed < 100){
			try {
				Thread.sleep((long)((100 - speed)*0.1));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void networkDynamic(int node) {

		int nbr = -1;
		int nbr_idx = rand.nextInt(nw.degree(node));
		
		boolean interactive;
		do {
			nbr = nw.node_neighbor(node, nbr_idx);
			interactive = nw.is_interaction_possible(nw.states[node], nw.states[nbr]);
			nbr_idx = rand.nextInt(nw.degree(node));
		} while (!interactive);

		int rand_f = rand.nextInt(nw.features);

		if (nw.states[node][rand_f] == nw.states[nbr][rand_f]) {
			int[] diff_features = new int[nw.features];
			int diff_count = 0;
			for (int f = 0; f < nw.features; f++) {
				if (nw.states[node][f] != nw.states[nbr][f]) {
					diff_features[diff_count++] = f;
				}
			}

			if (diff_count > 0) {
				int i = rand.nextInt(diff_count);
				int f = diff_features[i];
				
//				nw.states[node][f] = nw.states[nbr][f];
//				nw.update_representations(node);
//				obs.nodeInteraction(node / nw.size, node % nw.size, nw
//						.states[node]);
				
				nw.states[nbr][f] = nw.states[node][f];
				nw.update_representations(nbr);
				obs.nodeInteraction(nbr / nw.size, nbr % nw.size, nw
						.states[nbr]);
				
				interactions++;
			}
		}
	}

	public void print_execution_statistics() {
		double seconds = (this.sim_finish_ms - this.sim_start_ms) / 1000.0;
		double iterations_per_second = this.iterations / seconds;
		double interactions_per_second = this.interactions / seconds;
		double time_epochs = this.interactions / (this.nw.size * this.nw.size);
		System.out.println(String.format(
				"Simulation: %d iterations elapsed in %f seconds.",
				this.iterations, seconds));
		System.out.println(String.format("%d node interactions.",
				this.interactions));
		System.out.println(String.format(
				"Average execution speed: %f iterations/second",
				iterations_per_second));
		System.out.println(String.format(
				"Average node interaction speed: %f interactions/second",
				interactions_per_second));
		System.out.println(String.format("Time: %f iters/area", time_epochs));
		System.out.println();
	}

	public final void setObserver(SimulationObserver obs) {
		this.obs = obs;
	}

	public static interface SimulationObserver {
		public void simulationStep(AxelrodNetwork nw);

		public void nodeInteraction(int i, int j, int[] newState);
	}

	public static class BaseSimulationObserver implements SimulationObserver {
		public void simulationStep(AxelrodNetwork nw) {
		}

		public void nodeInteraction(int i, int j, int[] newState) {
		}
	}
	
	public ScatterPlotter iListPlot(boolean logarithmic){
		if(plot == null){
			createActiveNodesPlot(nw);
		}
		ValueAxis xAxis = new NumberAxis("time");;
		if (logarithmic){
			xAxis = new LogarithmicAxis("time");
		}
		XYPlot pl = (XYPlot) plot.chart.getPlot();
		pl.setDomainAxis(xAxis);
		updateActiveNodesPlot();
		return plot;
	}

//	public static void main(String[] args) throws IOException,InterruptedException {
//		 q_crit_plot();
//		 culture_distribution_plot();
//		visual_representation();
//	}

	public static void visual_representation() throws InterruptedException {
		int size = 100;
		int f = 2;
		int q = 2;
		int bolha = 20;
		AxelrodSimulation sim = new AxelrodSimulation(size, f, q);
//		int[] bubble_state = {0,0,0};
		int[] bubble_state = State.random_node_state(f, q);
		sim.nw.bubble_random_starting_distribution(bolha, bubble_state);
		final AxelrodCanvas opc = new AxelrodCanvas(640, sim.nw, false);
		SimulationObserver obs = new SimulationObserver() {
			public void simulationStep(AxelrodNetwork nw) {
//				opc.initCanvas(nw);
//				opc.repaint();
			}

			public void nodeInteraction(final int i, final int j, final  int[] newState) {
				opc.repaint();
			}
		};
		
		sim.setObserver(obs);
		sim.run();
	}

	public static void culture_distribution_plot() throws InterruptedException {

		int size = 64;
		int f = 3;
		int q = 12;
		int bolha = 20;
		int[] bubble_state = State.random_node_state(f, q);

		AxelrodSimulation sim = new AxelrodSimulation(size, f, q);
		sim.nw.bubble_random_starting_distribution(bolha, bubble_state);
		sim.run();
		sim.print_execution_statistics();

		List<Integer> cSizes = new ArrayList<Integer>(sim.nw.count_cultures()
				.values());
		while (cSizes.contains(new Integer(0))) {
			cSizes.remove(new Integer(0));
		}
		Collections.sort(cSizes);

		int[] cumulativeSizes = new int[cSizes.size() + 1];
		cumulativeSizes[0] = 0;

		for (int i = 0; i < cSizes.size(); i++) {
			cumulativeSizes[i + 1] = cumulativeSizes[i] + cSizes.get(i);
		}

		int diff = 0;
		int y = size * size;
		int x = 2;

		int count = 1;

		double result[][] = new double[2][cumulativeSizes.length];
		result[0][0] = 1;
		result[1][0] = 1;

		for (int i = 0; i < cumulativeSizes.length - 1; i++) {
			diff = cumulativeSizes[i + 1] - cumulativeSizes[i];
			if (diff < x) {
				y = y - diff;
			} else {
				result[0][count] = x;
				result[1][count++] = ((double) y) / (size * size);
				x = diff + 1;
				y = y - diff;
			}
		}

		double[][] series = new double[2][1];
		series[0][0] = 1;
		series[1][0] = 1;
		series = new double[2][count];
		for (int i = 0; i < count; i++) {
			series[0][i] = result[0][i];
			series[1][i] = result[1][i];
		}

		ScatterPlotter plotter = new ScatterPlotter("Axelrod Simulation Plot",
				String.format("L = %d, F = %d, q = %d", size, f, q), "", "", series);
		LogarithmicAxis xAxis = new LogarithmicAxis("S");
		LogarithmicAxis yAxis = new LogarithmicAxis("% culturas >= S");
		XYPlot pl = (XYPlot) plotter.chart.getPlot();
		pl.setDomainAxis(xAxis);
		pl.setRangeAxis(yAxis);
		plotter.setSeries(series);
		plotter.mostra();
	}

	public static void q_crit_plot() throws InterruptedException {
		int size = 64;
		int f = 2;
		int numSims = 1;
		int qMin = 2;
		int qMax = 100;
		
		double[][] series = new double[2][(qMax - qMin + 1) * numSims];
		int si = 0;
		ScatterPlotter plotter = null;
		for (int q = qMin; q <= qMax; q++) {
			System.out.println("q: " + q);
			double[][] cSizes = new double[2][numSims];
			for (int i = 0; i < numSims; i++) {
				int bolha = 20;
				int[] bubble_state = State.random_node_state(f, q);
				AxelrodSimulation sim = new AxelrodSimulation(size, f, q);
				sim.nw.bubble_random_starting_distribution(bolha, bubble_state);
				sim.run();
				sim.print_execution_statistics();
				Integer[] culture_sizes = new ArrayList<Integer>(sim.nw.count_cultures().values()).toArray(new Integer[0]);
				Arrays.sort(culture_sizes);
				Integer largest_culture = (Integer) culture_sizes[culture_sizes.length - 1];
				cSizes[0][i] = q; // current q
				cSizes[1][i] = ((double) largest_culture)
						/ (sim.nw.size * sim.nw.size); // largest culture,
														// normalized
			}
			for (int i = 0; i < numSims; i++) {
				series[0][si] = cSizes[0][i];
				series[1][si] = cSizes[1][i];
				si++;
			}
			if (plotter == null) {
				plotter = new ScatterPlotter("Axelrod Simulation Plot", String
						.format("L = %d, F = %d", size, f), "q", "Smax/N", series);
				plotter.mostra();
			}
			plotter.setSeries(series);
		}
	}

	public void stop() {
		this.state = 2;
	}

	public void start() {
		this.state = 1;
	}

	public void quit() {
		this.state = 0;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

}
