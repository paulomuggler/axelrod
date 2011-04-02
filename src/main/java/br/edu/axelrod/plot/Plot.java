package br.edu.axelrod.plot;

import org.jfree.chart.JFreeChart;

import br.edu.axelrod.simulation.CultureDisseminationSimulation;
import br.edu.axelrod.simulation.CultureDisseminationSimulation.SimulationEventListener;

public abstract class Plot<S extends CultureDisseminationSimulation> implements SimulationEventListener {
	
	protected JFreeChart chart;
	protected S simulation;
	
	public abstract JFreeChart createPlot(S sim);
	public abstract void plot();
	
	public void link(S sim) {
		createPlot(sim);
		simulation.addListener(this);
	}
	public void unlink() {
		simulation.removeListener(this);
	}
	
	public JFreeChart chart(){ return chart; }
	
	public String simInfo(){
		return String.format("L = %d, F = %d, q = %d", simulation.nw.size, 
							 simulation.nw.features, simulation.nw.traits);
	}
	
	public void iteration() {}
	public void interaction(int i, int j) {}
	public void epoch() {}
}
