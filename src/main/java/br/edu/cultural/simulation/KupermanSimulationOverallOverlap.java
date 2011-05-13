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
public class KupermanSimulationOverallOverlap extends CultureDisseminationSimulation {

	public KupermanSimulationOverallOverlap(CulturalNetwork nw) {
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
				
				if(will_increase_similarity(nbr, f, nw.states[node][f])){
					int[] oldState = Utils.copyArray(nw.states[nbr]);
					nw.states[nbr][f] = nw.states[node][f];
					int[] newState = Utils.copyArray(nw.states[nbr]);
					interacted(nbr, oldState, newState);
				}
			}
		}
	}

	private boolean will_increase_similarity(int node, int f, int new_trait) {
		int sim_old_state = 0;
		int sim_new_state = 0;
		int old_trait = nw.states[node][f];
		for(int nbr_idx = 0; nbr_idx < nw.degree[node]; nbr_idx++){
			int neigh = nw.node_neighbor(node, nbr_idx);
			if(nw.states[node][f]==nw.states[neigh][f]){
				sim_old_state += nw.overlap(node, neigh);
			}
			nw.states[node][f] = new_trait;
			if(nw.states[node][f] == nw.states[neigh][f]){
				sim_new_state += nw.overlap(node, neigh);
			}
			nw.states[node][f] = old_trait;
		}
		return (sim_new_state == sim_old_state? rand.nextBoolean() : (sim_new_state > sim_old_state));
	}
}
