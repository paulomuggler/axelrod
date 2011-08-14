/**
 * 
 */
package br.edu.cultural.simulation;

import java.util.HashSet;
import java.util.Set;

import br.edu.cultural.network.CulturalNetwork;
import br.edu.cultural.network.Utils;

/**
 * @author muggler
 * 
 */
public class FacilitatedDisseminationWithSurfaceTension extends CultureDisseminationSimulation {

	public FacilitatedDisseminationWithSurfaceTension(CulturalNetwork nw) {
		super(nw);
	}

	protected void simulation_dynamic() {
		int node = nw.random_interactive_node();

		int nbr = -1;
		int nbr_idx = rand.nextInt(nw.degree(node));
		
		Set<Integer> selectedNodes = new HashSet<Integer>();
		for (int i = 0; i < nw.degree[node]; i++) {
			selectedNodes.add(i);
		}
//		int count = 0;
		
		boolean interactive;
		do {
			nbr = nw.node_neighbor(node, nbr_idx);
			interactive = CulturalNetwork.is_interaction_possible(nw.states[node],
					nw.states[nbr]);
			nbr_idx = (Integer) selectedNodes.toArray()[rand.nextInt(selectedNodes.size())];
			 selectedNodes.remove(nbr_idx);
		} while (!interactive && selectedNodes.size() > 0);
//			nbr_idx = rand.nextInt(nw.degree(node));
//			count++;
//		} while (!interactive && count <= nw.degree(nbr));
//		if (count > nw.degree(nbr)){
//			return;
//		}
		if (selectedNodes.size() == 0){
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
//			}
		}
	}
}
