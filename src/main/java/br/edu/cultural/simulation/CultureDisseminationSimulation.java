package br.edu.cultural.simulation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import br.edu.cultural.gui.ClassNameComboBoxRenderer;
import br.edu.cultural.network.CulturalNetwork;
import br.edu.cultural.plot.Plot;

public abstract class CultureDisseminationSimulation implements Runnable {
	public enum SimulationState {
		RUNNING, PAUSED, FINISHED
	}
	
	
  public static final Class<?>[] simulationClasses = {
                                FacilitatedDisseminationWithSurfaceTension.class,
                                FacilitatedDisseminationWithoutSurfaceTension.class,
                                AxelrodSimulation.class,
                                BelousovZhabotinskySimulation.class,
                                KupermanSimulationOneFeatureOverlap.class,
                                KupermanSimulationOverallOverlap.class
                                };

	public CulturalNetwork nw;
	protected final List<Plot<?, ?>> plots = new ArrayList<Plot<?, ?>>();
	protected final List<Integer> defer_node_update = new ArrayList<Integer>();

	protected final Random rand = new Random();
	protected int speed = 100;
	public SimulationState state = SimulationState.PAUSED;

	// Statistics
	protected long sim_start_ms;
	protected long sim_finish_ms = -1;
	protected long iterations = 0;
	protected long interactions = 0;
	
	protected long stop_after_iterations = Long.MAX_VALUE;

	protected List<SimulationEventListener> listeners = Collections
			.synchronizedList(new ArrayList<SimulationEventListener>());
	private boolean defer_update;

	public void run() {
		this.start();
		
		while (!(this.state == SimulationState.FINISHED)) {
			pause();
			throttle();
			defer_run();
			if (iterations % 100000 == 0) {
				nw.reset_interaction_list(false);
			}
		}
	}

	protected abstract void defer_run();

	public void print_execution_statistics() {
		System.out.print(execution_statistics_string());
	}

	protected void interacted(int node, int[] oldState, int[] newState) {
		if (defer_update) {
			deferred_representation_update(node);
		} else {
			nw.update_representations(node);
		}
		for (SimulationEventListener lis : listeners) {
			lis.interaction(node / nw.size, node % nw.size, oldState, newState);
		}
		interactions++;
	}

	/* defers an update to the list of interactive nodes until
	 * refresh_rate interactions have occurred	 */
	protected void deferred_representation_update(Integer node) {
		if(nw.is_node_active[node] == false){
			nw.interactiveNodes.add(node);
			nw.is_node_active[node] = true;
		}
		for (int nbrIdx = 0; nbrIdx < nw.degree[node]; nbrIdx++) {
			int nbr = nw.node_neighbor(node, nbrIdx);
			if(nw.is_node_active[nbr] == false){
				nw.interactiveNodes.add(nbr);
				nw.is_node_active[nbr] = true;
			}
		}
		if (interactions % nw.refresh_rate == 0) {
				nw.reset_interaction_list(true);
		}
	}

	/** Performs a single step of the simulation */
	public void simulation_step() {
		if ((this.nw.interactive_nodes().size() == 0) || (iterations >= stop_after_iterations)) {
			this.finish();
			return;
		}
		this.simulation_dynamic();
		this.iterations++;
	}
	
	protected abstract void simulation_dynamic();

	private void start() {
		this.iterations = 0;
		this.sim_start_ms = System.currentTimeMillis();
		this.state = SimulationState.RUNNING;
		for (SimulationEventListener lis : listeners) {
			lis.started();
		}
	}
	
	public void toggle_pause() {
		if (this.state == SimulationState.RUNNING){
			this.state = SimulationState.PAUSED;
			notify_listeners_toggled();
		}else if (this.state ==  SimulationState.PAUSED){
			this.state = SimulationState.RUNNING;
			notify_listeners_toggled();
		}
	}
	
	private void notify_listeners_toggled(){
		for (SimulationEventListener lis : listeners) {
			lis.toggled_pause();
		}
	}

	public void finish() {
		this.state = SimulationState.FINISHED;
		this.sim_finish_ms = System.currentTimeMillis();
		for (SimulationEventListener lis : listeners) {
			lis.finished();
		}
	}
	

	/** Idle loop used to emulate the PAUSED state; */
	private void pause() {
		while (this.state == SimulationState.PAUSED) {
			try {
				Thread.sleep(33);
			} catch (InterruptedException e) {
			}
		}
	}

	/** Sleeps for an amount of time proportional to the set speed */
	private void throttle() {
		if (this.speed < 100) {
			try {
				Thread.sleep((long) ((100 - speed) * 0.1));
			} catch (InterruptedException e) {
			}
		}
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	public void stop_after_iterations(long n){
		this.stop_after_iterations = n;
	}

	public void setDefer_update(boolean defer_update) {
		this.defer_update = defer_update;
	}
	
	/** Toggle this simulation's optimization on/off */
	public void toggle_defer_update_optimize() {
		defer_update = !defer_update;
	}

	/** adds a plot to the list of running plots */
	public void add_plot(Plot<?, ?> p) {
		if (!plots.contains(p))
			plots.add(p);
	}

	/** removes a plot from the list of running plots */
	public void remove_plot(Plot<?, ?> p) {
		plots.remove(p);
	}

	/**
	 * Adds a node to the list of monitored nodes.
	 * A monitored node will trigger an event whenever 
	 * its state changes.
	 * @param node
	 * @return
	 */
	public boolean toggle_listening(int node) {
		if (this.nw.nodes_to_listen.contains(node)) {
			this.nw.nodes_to_listen.remove(new Integer(node));
			return false;
		} else {
			this.nw.nodes_to_listen.add(node);
			return true;
		}
	}

	public long sim_start_ms() {
		return sim_start_ms;
	}

	public long sim_finish_ms() {
		return sim_finish_ms;
	}

	public long elapsed_time() {
		return (sim_finish_ms == -1 ? 
					System.currentTimeMillis()
					: sim_finish_ms)
				- sim_start_ms;
	}

	public float elapsed_time_in_seconds() {
		return (elapsed_time() / 1000f);
	}


	/**
	 * Registers a listener with this simulation. Meaningful events along 
	 * the simulation's lifecycle will trigger specific callbacks on the
	 * registered listeners. 
	 * {@link SimulationEventAdapter} is a utility class with empty implementations
	 * for {@link SimulationEventListener}'s callback methods.
	 * @param lis
	 * @return
	 */
	public boolean addListener(SimulationEventListener lis) {
		if (!listeners.contains(lis))
			return listeners.add(lis);
		return false;
	}

	/**
	 * De-registers a listener.
	 * @param lis
	 * @return
	 */
	public boolean removeListener(SimulationEventListener lis) {
		return listeners.remove(lis);
	}

	public long iterations() {
		return iterations;
	}
	
	public long interactions() {
		return interactions;
	}
	
	public long current_epoch() {
		return this.iterations / this.nw.n_nodes;
	}

	public CultureDisseminationSimulation(CulturalNetwork nw) {
		this.nw = nw;
	}

	/**
	 * Builds you a pristine {@link CultureDisseminationSimulation}.
	 * @param clazz which type of simulation do you want to build?
	 * @param nw the network on which this simulation will be run.
	 * @return the simulation, duh!
	 */
	public static CultureDisseminationSimulation factory( 
								Class<? extends CultureDisseminationSimulation> clazz,
								CulturalNetwork nw)
	{
		try {
			return clazz.getConstructor(CulturalNetwork.class).newInstance(nw);
		
		// gotta love checked exceptions...
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/** pretty prints information about the running simulation */
	public String execution_statistics_string() {
		float iterations_per_second = iterations() / elapsed_time_in_seconds();
		float interactions_per_second = interactions()
				/ elapsed_time_in_seconds();
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append(String.format(
				"%s: %d iterations elapsed in %f seconds.",
				ClassNameComboBoxRenderer.humanize(this.getClass().getSimpleName()),
				iterations(), elapsed_time_in_seconds()));
		sb.append("\n");
		sb.append(String.format(
				"Average execution speed: %f iterations/second",
				iterations_per_second));
		sb.append("\n");
		sb.append(String.format("Time: %d epochs", current_epoch()));
		sb.append("\n");
		sb.append(String.format("%d node interactions.", interactions()));
		sb.append("\n");
		sb.append(String.format(
				"Average node interaction speed: %f interactions/second",
				interactions_per_second));
		sb.append("\n");
		return sb.toString();
	}
}
