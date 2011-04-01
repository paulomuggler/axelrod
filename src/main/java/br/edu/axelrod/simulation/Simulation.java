package br.edu.axelrod.simulation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class Simulation implements Runnable {
	protected final Random rand = new Random();
	protected final Map<String, Object> statistics = new HashMap<String, Object>();

	public void run() {
//		this.iterations = 0;
//		this.sim_start_ms = System.currentTimeMillis();
//		while (!(this.state == 0)) {
//			pause();
//			throttle();
//			this.simulation_step();
//			if(this.iterations%(nw.n_nodes) == 0){
//				activeNodesSeries.add(nw.interactiveNodes.size());
//				if(plot.isActive()){
//					updateActiveNodesPlot();
//				}
//			}
//		}
//		this.sim_finish_ms = System.currentTimeMillis();
//		print_execution_statistics();
	}
	
	/**
	 * Performs a single step of the simulation
	 */
	public abstract void simulation_step();

}
