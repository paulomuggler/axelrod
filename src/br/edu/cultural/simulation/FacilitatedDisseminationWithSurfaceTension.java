/**
 * 
 */
package br.edu.cultural.simulation;

import java.util.ArrayList;
import java.util.List;

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
		

		//boolean interactive;
		//int count = 0;
		int actvcount = 0;
		
		final List<Integer> actvNbrhd = new ArrayList<Integer>();
		for(int d=0; d < nw.degree(node); d++){
			if(CulturalNetwork.is_interaction_possible(nw.states[node], nw.states[nw.node_neighbor(node, d)])){	
				actvNbrhd.add(new Integer(nw.node_neighbor(node, d)));
				actvcount++;
			}
		}
		if(actvcount == 0){
			if (this.adjust_simulation_time()){
				this.iterations -= (this.nw.n_nodes/this.nw.interactiveNodes.size());	
			}
			else{
				this.iterations--;
			}
			return;
		}
		int nbr_idx = rand.nextInt(actvcount);
		nbr = actvNbrhd.get(nbr_idx).intValue();
		


		int rand_f = rand.nextInt(nw.features);

		if (nw.states[node][rand_f] == nw.states[nbr][rand_f]) {
			int[] diff_features = new int[nw.features];
			int diff_count = 0;
			for (int f = 0; f < nw.features; f++) {
				if (nw.states[node][f] != nw.states[nbr][f]) {
					diff_features[diff_count++] = f;
				}
			}


			int i = rand.nextInt(diff_count);
			int f = diff_features[i];

			int[] oldState = Utils.copyArray(nw.states[nbr]);
			nw.states[nbr][f] = nw.states[node][f];
			int[] newState = Utils.copyArray(nw.states[nbr]);
			interacted(nbr, oldState, newState);

		}
	}
}
