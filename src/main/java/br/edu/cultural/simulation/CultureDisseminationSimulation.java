package br.edu.cultural.simulation;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import sun.misc.Launcher;
import br.edu.cultural.network.CulturalNetwork;
import br.edu.cultural.plot.Plot;

public abstract class CultureDisseminationSimulation implements Runnable {
	public enum SimulationState {
		RUNNING, STOPPED, FINISHED
	}

	public CulturalNetwork nw;
	protected final List<Plot<?, ?>> plots = new ArrayList<Plot<?, ?>>();
	protected final List<Integer> defer_node_update = new ArrayList<Integer>();

	protected final Random rand = new Random();
	protected int speed = 100;
	public SimulationState state = SimulationState.STOPPED;

	// Statistics
	protected long sim_start_ms;
	protected long sim_finish_ms = -1;
	protected int iterations = 0;
	protected int interactions = 0;
	
	protected long stop_after_iterations = Long.MAX_VALUE;

	protected List<SimulationEventListener> listeners = Collections
			.synchronizedList(new ArrayList<SimulationEventListener>());
	private boolean defer_update;

	public void run() {
		this.iterations = 0;
		this.sim_start_ms = System.currentTimeMillis();

		for (SimulationEventListener lis : listeners) {
			lis.started();
		}

		while (!(this.state == SimulationState.FINISHED)) {
			pause();
			throttle();
			defer_run();
			if (iterations % 100000 == 0) {
				nw.reset_interaction_list(false);
			}
			if(iterations >= stop_after_iterations){
				this.quit();
			}
		}
		this.sim_finish_ms = System.currentTimeMillis();

		for (SimulationEventListener lis : listeners) {
			lis.finished();
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
	
	/** pretty prints information on the running simulation */
	public String execution_statistics_string() {
		float iterations_per_second = iterations() / elapsed_time_in_seconds();
		float interactions_per_second = interactions()
				/ elapsed_time_in_seconds();
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append(String.format(
				"Simulation: %d iterations elapsed in %f seconds.",
				iterations(), elapsed_time_in_seconds()));
		sb.append("\n");
		sb.append(String.format(
				"Average execution speed: %f iterations/second",
				iterations_per_second));
		sb.append("\n");
		sb.append(String.format("Time: %d epochs", epoch()));
		sb.append("\n");
		sb.append(String.format("%d node interactions.", interactions()));
		sb.append("\n");
		sb.append(String.format(
				"Average node interaction speed: %f interactions/second",
				interactions_per_second));
		sb.append("\n");
		return sb.toString();
	}

	/** Performs a single step of the simulation */
	public abstract void simulation_step();

	public void stop() {
		this.state = SimulationState.STOPPED;
	}

	/** set simulation state to RUNNING */
	public void start() {
		this.state = SimulationState.RUNNING;
	}

	/** set simulation state to FINISHED */
	public void quit() {
		this.state = SimulationState.FINISHED;
	}
	

	/** Idle loop used to emulate the STOPPED state; */
	private void pause() {
		while (this.state == SimulationState.STOPPED) {
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

	public void setDefer_update(boolean deferUpdate) {
		defer_update = deferUpdate;
	}
	
	/** Toggle this simulation's optimization on/off */
	public void toggle_defer_update_optimize() {
		defer_update = !defer_update;
	}

	/** adds a plot to the list of running plots */
	public void addPlot(Plot<?, ?> p) {
		if (!plots.contains(p))
			plots.add(p);
	}

	/** removes a plot from the list of running plots */
	public void removePlot(Plot<?, ?> p) {
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
		if (this.nw.nodes_listened.contains(node)) {
			this.nw.nodes_listened.remove(new Integer(node));
			return false;
		} else {
			this.nw.nodes_listened.add(node);
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

	public static interface SimulationEventListener {
		/**	Triggered when this simulation starts running. */
		public void started();

		/**	Triggered on each iteration of this simulation. */
		public void iteration();

		/**	Triggered when two nodes in this simulation interact. */
		public void interaction(int i, int j, int[] oldState, int[] newState);

		/**	Triggered at each n iterations, n being the 
		 * number of nodes in the network. */
		public void epoch();

		/**	Triggered when this simulation quits or reaches an 
		 * absorbent state, if such a state exists for the current 
		 * simulation. */
		public void finished();
	}

	public static class SimulationEventAdapter implements
			SimulationEventListener {
		public void started() {
		}

		public void iteration() {
		}

		public void interaction(int i, int j, int[] oldState, int[] newState) {
		}

		public void epoch() {
		}

		public void finished() {
		}
	}

	public int iterations() {
		return iterations;
	}
	
	public Integer interactions() {
		return interactions;
	}
	
	public int epoch() {
		return this.iterations / this.nw.n_nodes;
	}

	public CultureDisseminationSimulation(CulturalNetwork nw) {
		this.nw = nw;
	}

	public static CultureDisseminationSimulation factory(
			Class<? extends CultureDisseminationSimulation> clazz,
			CulturalNetwork nw) {
		try {
			return clazz.getConstructor(CulturalNetwork.class).newInstance(nw);
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
	
	@SuppressWarnings("unchecked")
	public static List<Class<? extends CultureDisseminationSimulation>> subclasses(){
		List<Class<? extends CultureDisseminationSimulation>>  subclasses = new ArrayList<Class<? extends CultureDisseminationSimulation>>();
		String pckgname = CultureDisseminationSimulation.class.getPackage().getName();
        String name = new String(pckgname);
        if (!name.startsWith("/")) {
            name = "/" + name;
        }        
        name = name.replace('.','/');
        URL url = Launcher.class.getResource(name);
        File directory = new File(url.getFile());
        if (directory.exists()) {
            String [] files = directory.list();
            for (int i=0;i<files.length;i++) {
                if (files[i].endsWith(".class")) {
                    String classname = files[i].substring(0,files[i].length()-6);
                    try {
                    	Class<?> cl = Class.forName(pckgname+"."+classname);
                        if (CultureDisseminationSimulation.class.isAssignableFrom(
                        					Class.forName(pckgname+"."+classname)) 
                        		&& !cl.equals(CultureDisseminationSimulation.class)) {
                        	subclasses.add((Class<? extends CultureDisseminationSimulation>) cl);
                        }
                    } catch (ClassNotFoundException cnfex) {
                        System.err.println(cnfex);
                    }
                }
            }
        }
		return subclasses;
	}
	

}
