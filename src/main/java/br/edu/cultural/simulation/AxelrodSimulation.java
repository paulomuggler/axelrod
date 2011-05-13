/**
 * 
 */
package br.edu.cultural.simulation;

import br.edu.cultural.network.CulturalNetwork;
import br.edu.cultural.network.Utils;

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

	protected void defer_run() {
		this.simulation_step();
		if (this.iterations % (nw.n_nodes) == 0) {
			for (SimulationEventListener lis : listeners) {
				lis.epoch();
			}
		}
	}

	protected void simulation_dynamic() {
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

				int[] oldState = Utils.copyArray(nw.states[nbr]);
				nw.states[nbr][f] = nw.states[node][f];
				int[] newState = Utils.copyArray(nw.states[nbr]);
				interacted(nbr, oldState, newState);
			}
		}
	}
}
