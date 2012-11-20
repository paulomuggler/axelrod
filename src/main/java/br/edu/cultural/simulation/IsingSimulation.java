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
public class IsingSimulation extends CultureDisseminationSimulation {

	// Statistics
	protected int interactions = 0;

	public IsingSimulation(CulturalNetwork nw) {
		super(nw);
		nw.voter_starting_distribution();
	}

	protected void simulation_dynamic() {
		int node = nw.random_interactive_node();

		int f = 1;
		int actvcount = 0;
		
		final List<Integer> actvNbrhd = new ArrayList<Integer>();
		for(int d=0; d < nw.degree(node); d++){
			if(nw.states[node][f] != nw.states[nw.node_neighbor(node, d)][f]){	
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
		
		if (actvcount >= nw.degree(node)/2){
			int[] oldState = Utils.copyArray(nw.states[node]);
			nw.states[node][f] = Math.abs(nw.states[node][f] - 1);
			int[] newState = Utils.copyArray(nw.states[node]);
			interacted(node, oldState, newState);
		}
	}
}
