/**
 * 
 */
package br.edu.axelrod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author muggler
 * 
 */
public class AxelrodNetwork {
	final Random rand = new Random();

	// Network characteristics
	public static final int MAX_DEGREE = 8;

	public final int size;
	public final int n_nodes;
	public final int features;
	public final int traits;

	/** The network state representation */
	final int[][] states; // size^2 (~100kB)

	/** Stores connectivity information */
	final int[][] adj_matrix; // 8*size^2 (~100kB)

	/** Gives the degree of each node */
	final int[] degree;

	/** Stores all nodes that may interact */
	final List<Integer> interactiveNodes = new ArrayList<Integer>(); // O(4*size^2)
	// (~100kB)

	/** Stores the activity state of each node */
	final boolean[] is_node_active;

	/** Keep track of culture sizes on the network */
	final Map<Integer, Integer> cultureSizes = new HashMap<Integer, Integer>();

	public AxelrodNetwork(int size, int features, int traits) {
		super();
		this.size = size;
		this.n_nodes = size * size;
		this.features = features;
		this.traits = traits;

		this.states = new int[this.n_nodes][this.features];
		this.adj_matrix = new int[this.n_nodes][MAX_DEGREE];
		this.is_node_active = new boolean[this.n_nodes];
		this.degree = new int[this.n_nodes];
		this.init_adj_matrix();
		this.random_starting_distribution();
	}

	private void init_adj_matrix() {

		for (int nd = 0; nd < this.n_nodes; nd++) {

			degree[nd] = 0;
			int i = nd / size;
			int j = nd % size;

			if (j > 0) // not on column 0
				adj_matrix[nd][degree[nd]++] = (j - 1) + (i * size); // left

			if (j < size - 1) // not on last column
				adj_matrix[nd][degree[nd]++] = (j + 1) + (i * size); // right

			if (i > 0) // not on line 0
				adj_matrix[nd][degree[nd]++] = j + (i - 1) * size; // above

			if (i < size - 1) // not on last line
				adj_matrix[nd][degree[nd]++] = j + (i + 1) * size; // below

		}
	}

	private void initInteractionList() {
		this.interactiveNodes.clear();
		for (int nd = 0; nd < this.n_nodes; nd++) {
			is_node_active[nd] = false;
			for (int nbr_idx = 0; nbr_idx < degree[nd]; nbr_idx++) {
				int similarity = 0;
				int nbr = adj_matrix[nd][nbr_idx];
				for (int f = 0; f < features; f++)
					if (states[nd][f] == states[nbr][f])
						similarity++;
				if ((similarity > 0) && (similarity < features)) {
					is_node_active[nd] = true;
					interactiveNodes.add(nd);
					break;
				}
			}
		}
	}

	private void initCultureSizes() {
		this.cultureSizes.clear();
		for (int nd = 0; nd < n_nodes; nd++) {
			cultureSizes.put(Arrays.hashCode(states[nd]),
					(cultureSizes.get(Arrays.hashCode(states[nd])) == null ? 0 : cultureSizes
							.get(Arrays.hashCode(states[nd]))) + 1);
		}
	}

	public void update_representations(int nd) {

		int[] nd_state = this.state(nd);

		is_node_active[nd] = false;
		interactiveNodes.remove(new Integer(nd));
		
		for (int k0 = 0; k0 < degree[nd]; k0++) {

			int nbr = this.adj_matrix[nd][k0];
			int[] nbr_state = this.state(nbr);

			is_node_active[nbr] = false;
			interactiveNodes.remove(new Integer(nbr));
			
			if (is_node_active[nd] == false) {
				if (is_interaction_possible(nd_state, nbr_state)) {
					is_node_active[nd] = true;
					interactiveNodes.add(nd);
					
					is_node_active[nbr] = true;
					interactiveNodes.add(nbr);
					continue;
				}
			}

			for (int k1 = 0; k1 < degree[nbr] && is_node_active[nbr] == false; k1++) {

				int nbr_nbr = this.adj_matrix[nbr][k1];
				int[] nbr_nbr_state = this.state(nbr_nbr);

				if (is_interaction_possible(nbr_state, nbr_nbr_state)) {
					is_node_active[nbr] = true;
					interactiveNodes.add(nbr);
					break;
				}
			}

		}
	}

	public boolean is_interaction_possible(int[] ndState, int[] nbrState) {
		boolean has_different_element = false;
		boolean has_equal_element = false;

		for (int i = 0; i < ndState.length; i++) {
			if (!has_different_element && ndState[i] != nbrState[i]) {
				has_different_element = true;
			}
			if (!has_equal_element && ndState[i] == nbrState[i]) {
				has_equal_element = true;
			}
			if (has_different_element && has_equal_element)
				return true;
		}
		return false;
	}


	public Map<Integer, Integer> cultureSizes() {
		return Collections.unmodifiableMap(this.cultureSizes);
	}

	public String interaction_list_to_string() {
		StringBuilder sb = new StringBuilder("Interactive node count: "+interactiveNodes.size()+"\n");
		sb.append("Interactive nodes:\n");
		Object[] values = this.interactiveNodes.toArray();
		Arrays.sort(values);
		for (Object o : values) {
			Integer nd = (Integer) o;
			sb.append(String.format("Node %d/(%d, %d)\n",nd, nd/size, nd%size));
		}
		return sb.toString();
	}

	public String network_state_to_string() {
		StringBuilder sb = new StringBuilder("Network state:\n");
		for (int nd = 0; nd < this.n_nodes; nd++) {
				sb.append(String.format("Node %d/(%d, %d): ", nd, nd/size, nd%size));
				for (int f = 0; f < features; f++) {
					sb.append(states[nd][f]);
				}
				sb.append("\n");
		}
		return sb.toString();
	}

	public String adjacency_matrix_to_string() {
		StringBuilder sb = new StringBuilder();
		sb.append("Adjacency matrix:\n");
		for (int nd = 0; nd < this.n_nodes; nd++) {
			sb.append(String.format("node %d/(%d,%d):\t", nd, nd/size, nd%size));
			for (int j = 0; j < degree[nd]; j++) {
				int nbr = adj_matrix[nd][j];
				sb.append(String.format("%d/(%d,%d)\t", nbr, nbr/size, nbr%size));
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public void random_starting_distribution() {
		for (int nd = 0; nd < this.n_nodes; nd++) {
				states[nd] = State.random_node_state(features, traits);
		}
		this.initInteractionList();
		this.initCultureSizes();
	}
	
	public void bubble_random_starting_distribution(int bubble_radius, int[] state){
		bubble_radius -= bubble_radius % 2;
		for (int nd = 0; nd < n_nodes; nd++) {
			
		}
	}
	
	public void striped_starting_distribution() {
		int[] state1 = new int [features];
		int[] state2 = new int [features];
		state1[0] = state2[0] = traits-1;
		for (int i = 1; i < state1.length; i++) {
			state1[i] = 0;
			state2[i] = traits - 1;
		}
		for (int nd = 0; nd < n_nodes; nd++) {
			if(nd%2 == 0){
				System.arraycopy(state1, 0, states[nd], 0, state1.length);
			}else{
				System.arraycopy(state2, 0, states[nd], 0, state2.length);
			}
		}
		this.initInteractionList();
		this.initCultureSizes();
	}
	
	public void homogeneous_distribution(){
		int [] randst = State.random_node_state(features, traits);
		for (int nd = 0; nd < n_nodes; nd++) {
			System.arraycopy(randst, 0, states[nd], 0, randst.length);
		}
		this.initInteractionList();
		this.initCultureSizes();
	}

	public boolean is_state_valid(int[] state) {
		for (int i = 0; i < features; i++) {
			if (state[i] < 0 || state[i] >= traits) return false;
		}
		return true;
	}

	public int[] state(int node) {
		return this.states[node];
	}

//	public void setState(int node, int[] state) {
//		int[] old_state = this.states[node];
//		if (!state.equals(old_state)) {
//			this.states[node] = state;
//			this.update_representations(node);
//			Integer old_state_cSize = cultureSizes.get(Arrays.hashCode(old_state));
//			Integer state_cSize = cultureSizes.get(Arrays.hashCode(state));
//			cultureSizes.put(Arrays.hashCode(old_state), (old_state_cSize == null ? 0
//					: old_state_cSize) - 1);
//			cultureSizes
//					.put(Arrays.hashCode(state), (state_cSize == null ? 0 : state_cSize) + 1);
//		}
//	}
	
	public void setFeature(int node, int f_pos, int f_val){
		states[node][f_pos] = f_val;
	}

	public Integer random_interactive_node() {
		Integer idx = rand.nextInt(this.interactiveNodes.size());
		return (Integer) this.interactiveNodes.get(idx);
	}

	public List<Integer> interactive_nodes() {
		return Collections.unmodifiableList(interactiveNodes);
	}
	
	public int degree(int node){
		return degree[node];
	}

	public int node_neighbor(int node, int nbrIdx) {
		if(nbrIdx > degree[node]) return -1;
		return adj_matrix[node][nbrIdx];
	}

}
