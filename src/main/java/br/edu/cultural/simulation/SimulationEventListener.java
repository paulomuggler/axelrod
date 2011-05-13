package br.edu.cultural.simulation;

public interface SimulationEventListener {
	/**	Triggered when this simulation starts running. */
	public void started();
	
	/**	Triggered when this simulation is manually stopped. */
	public void toggled_pause();

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

	public static class SimulationEventAdapter implements SimulationEventListener {
		public void started() {}
		public void toggled_pause() {}
		public void finished() {}
		public void iteration() {}
		public void interaction(int i, int j, int[] oldState, int[] newState) {}
		public void epoch() {}
	}
}

