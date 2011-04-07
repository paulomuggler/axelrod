package br.edu.axelrod.simulation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import br.edu.axelrod.network.CulturalNetwork;
import br.edu.axelrod.plot.Plot;

public abstract class CultureDisseminationSimulation implements Runnable {
	public enum SimulationState { RUNNING, STOPPED, FINISHED }

	public CulturalNetwork nw;
	protected final List<Plot<?, ?>> plots = new ArrayList<Plot<?, ?>>();
	
	protected final Random rand = new Random();
	protected int speed = 100;
	public SimulationState state = SimulationState.STOPPED; 
	
	// Statistics
	protected long sim_start_ms;
	protected long sim_finish_ms = -1;
	protected int iterations = 0;
	
	protected List<SimulationEventListener> listeners = Collections.synchronizedList(new ArrayList<SimulationEventListener>());
	
	public void run(){
		this.iterations = 0;
		this.sim_start_ms = System.currentTimeMillis();
		
		while (!(this.state == SimulationState.FINISHED)) {
			pause();
			throttle();
			defer_run();
		}
		this.sim_finish_ms = System.currentTimeMillis();
	}
	
	protected abstract void defer_run();

	/**
	 * Performs a single step of the simulation
	 */
	public abstract void simulation_step();
	
	public void stop() {
		this.state = SimulationState.STOPPED;
	}

	public void start() {
		this.state = SimulationState.RUNNING;
	}

	public void quit() {
		this.state = SimulationState.FINISHED;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	public void addPlot(Plot<?, ?> p){
		if (!plots.contains(p)) plots.add(p);
	}
	
	public void removePlot(Plot<?, ?> p){
		plots.remove(p);
	}

	private void pause() {
		while(this.state == SimulationState.STOPPED){
			try { Thread.sleep(33);
			} catch (InterruptedException e) {}
		}
	}

	private void throttle() {
		if(this.speed < 100){
			try { Thread.sleep((long)((100 - speed)*0.1));
			} catch (InterruptedException e) {}
		}
	}
	
	public boolean toggleMonitor(int nodeClicked) {
		if(this.nw.monitorNodes.contains(nodeClicked)){
			this.nw.monitorNodes.remove(new Integer(nodeClicked));
			return false;
		}else{
			this.nw.monitorNodes.add(nodeClicked);
			return true;
		}
	}

	public long sim_start_ms() {
		return sim_start_ms;
	}

	public long sim_finish_ms() {
		return sim_finish_ms;
	}
	
	public long elapsed_time(){
		return (sim_finish_ms == -1? System.currentTimeMillis(): sim_finish_ms) - sim_start_ms;
	}
	
	public double elapsed_time_in_seconds(){
		return elapsed_time() / 1000.0;
	}

	public int iterations() {
		return iterations;
	}

	public void addListener(SimulationEventListener lis) {
		if(!listeners.contains(lis)) listeners.add(lis);
	}
	
	public void removeListener(SimulationEventListener lis) {
		listeners.remove(lis);
	}

	public static interface SimulationEventListener {
		public void iteration();
		public void interaction(int i, int j);
		public void epoch();
	}

	public static class SimulationEventAdapter implements SimulationEventListener {
		public void iteration() {}
		public void interaction(int i, int j) {}
		public void epoch() {}
	}

	public int epoch() {
		return this.iterations/this.nw.n_nodes;
	}

	public void print_execution_statistics() 
	{
		double iterations_per_second = iterations() / elapsed_time_in_seconds();
		System.out.println(String.format("Simulation: %d iterations elapsed in %f seconds.", iterations(), elapsed_time_in_seconds()));
		System.out.println(String.format("Average execution speed: %f iterations/second", iterations_per_second));
		System.out.println(String.format("Time: %f iters/area", epoch()));
		System.out.println();
	}
	
	public CultureDisseminationSimulation(CulturalNetwork nw){
		this.nw = nw;
	}
	
	public static CultureDisseminationSimulation factory(Class<? extends CultureDisseminationSimulation> clazz, CulturalNetwork nw){
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

}
