/**
 * 
 */
package br.edu.cultural.simulation;

import br.edu.cultural.network.CulturalNetwork;

/**
 * @author muggler
 * 
 */
public class AxelrodSimulation extends CultureDisseminationSimulation {

	// Statistics
	protected int interactions = 0;

	public AxelrodSimulation(CulturalNetwork nw) {
		super(nw);
	}

	/**
	 * Performs a single step of the axelrod simulation
	 * 
	 * @throws InterruptedException
	 */
	public void simulation_step() {
		if (this.nw.interactive_nodes().size() == 0) {
			this.state = SimulationState.FINISHED;
			return;
		}
		this.networkDynamic();
		this.iterations++;
	}

	protected void defer_run() {
		this.simulation_step();
		if (this.iterations % (nw.n_nodes) == 0) {
			for (SimulationEventListener lis : listeners) {
				lis.epoch();
			}
		}
	}

	private void networkDynamic() {
		int node = nw.random_interactive_node();

		int nbr = -1;
		int nbr_idx = rand.nextInt(nw.degree(node));

		nbr = nw.node_neighbor(node, nbr_idx);

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

				nw.states[nbr][f] = nw.states[node][f];
				interacted(nbr);
			}
		}
	}
}
