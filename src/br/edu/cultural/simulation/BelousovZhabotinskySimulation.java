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
public class BelousovZhabotinskySimulation extends CultureDisseminationSimulation {

	public BelousovZhabotinskySimulation(CulturalNetwork nw) {
		super(nw);
	}

	protected void simulation_dynamic() {
		int node = nw.random_interactive_node();

		int nbr = -1;
		int nbr_idx = rand.nextInt(nw.degree(node));

		boolean interactive;
		int count = 0;
		do {
			nbr = nw.node_neighbor(node, nbr_idx);
			interactive = CulturalNetwork.is_interaction_possible(nw.states[node],
					nw.states[nbr]);
			nbr_idx = rand.nextInt(nw.degree(node));
			count+=1;
		} while (!interactive && count <= nw.degree(nbr));
		if (count > nw.degree(nbr)){
			return;
		}

		int rand_f = rand.nextInt(nw.features);

		if (nw.states[node][rand_f] == nw.states[nbr][rand_f]) {
			int[] diff_features = new int[nw.features];
			int diff_count = 0;
			for (int f = 0; f < nw.features; f++) {
				if (nw.states[node][f] != nw.states[nbr][f]) {
					diff_features[diff_count++] = f;
				}
			}

//			if (diff_count > 0) {
			int i = rand.nextInt(diff_count);
			int f = diff_features[i];

			int[] oldState = Utils.copyArray(nw.states[nbr]);
			nw.states[nbr][f] = nw.states[node][f];
			int[] newState = Utils.copyArray(nw.states[nbr]);
			interacted(nbr, oldState, newState);
//			nw.states[node][f] = nw.states[nbr][f];
//			interacted(node);
//			}
		}
	}
	
	protected void deferred_representation_update(Integer node) {
		nw.is_node_active[node] = true;
		if(nw.is_node_active[node] = false){
			nw.interactiveNodes.add(node);
		}
		if (interactions % nw.refresh_rate == 0) {
				nw.reset_interaction_list(false);
		}
	}
}
