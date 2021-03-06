/**
 * 
 */
package br.edu.cultural.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
public class CulturalNetwork {
	final Random rand = new Random();

	// Network characteristics
	public static final int MAX_DEGREE = 8;

	public final int size;
	public final int n_nodes;
	public final int features;
	public final int traits;
	public final boolean periodicBoundary;

	public int refresh_rate;

	/** The network state representation */
	public final int[][] states; // size^2 (~100kB)

	/** Stores connectivity information */
	public final int[][] adj_matrix; // 8*size^2 (~100kB)

	/** Gives the degree of each node */
	public final int[] degree;

	/** Stores all nodes that may interact */
	public final List<Integer> interactiveNodes = new ArrayList<Integer>();

	/**
	 * This list is used by some plots which are only interested in certain
	 * nodes in the network.
	 */
	// TODO: Refactor. Should be in the Plot class structure somehow.
	// Refactoring the save to file solution to have a broader scope would
	// probably help with this.
	public final List<Integer> nodes_to_listen = new ArrayList<Integer>();

	/** Stores the activity state of each node */
	public final boolean[] is_node_active;

	public CulturalNetwork(int size, int features, int traits,
			boolean periodicBoundary, int refreshAdjust) {
		this.size = size;
		this.n_nodes = size * size;
		this.features = features;
		this.traits = traits;
		this.periodicBoundary = periodicBoundary;
		this.refresh_rate = calc_update_rate(refreshAdjust);

		this.states = new int[this.n_nodes][this.features];
		this.adj_matrix = new int[this.n_nodes][MAX_DEGREE];
		this.is_node_active = new boolean[this.n_nodes];
		this.degree = new int[this.n_nodes];
		this.init_adj_matrix(periodicBoundary);
		 this.random_starting_distribution();
		int[] state1 = { 0, 1 };
		int[] state2 = { 0, 0 };

//		this.bubble(this.size / 4, state1, state2);
	}

	private int calc_update_rate(int refreshAdjust) {
		return (int) Math.floor((refreshAdjust
				* this.features
				* (1 + Math.sqrt(1 + 4 * 0.2 * this.interactiveNodes.size()
						/ 25.0)) / 2.0));
	}

	public CulturalNetwork(File f) throws IOException {
		if (f.exists() && f.canRead()) {
			FileReader fr = new FileReader(f);
			BufferedReader r = new BufferedReader(fr);
			String[] params = r.readLine().split(":");

			this.size = Integer.parseInt(params[0]);
			this.n_nodes = size * size;
			this.features = Integer.parseInt(params[1]);
			this.traits = Integer.parseInt(params[2]);
			this.periodicBoundary = Boolean.parseBoolean(params[3]);
			this.refresh_rate = calc_update_rate(100);

			this.states = new int[this.n_nodes][this.features];
			this.adj_matrix = new int[this.n_nodes][MAX_DEGREE];
			this.is_node_active = new boolean[this.n_nodes];
			this.degree = new int[this.n_nodes];

			for (int i = 0; i < this.n_nodes; i++) {
				String[] stateStr = r.readLine().toString().split(":");
				int[] state = new int[stateStr.length];
				for (int j = 0; j < stateStr.length; j++) {
					state[j] = Integer.parseInt(stateStr[j]);
				}
				System.arraycopy(state, 0, this.states[i], 0, this.features);
			}
			String line = r.readLine();
			if (line != null && line.startsWith("monitor:")) {
				String[] monitor = line.substring(line.indexOf("[") + 1,
						line.indexOf("]")).split(",");
				for (String s : monitor) {
					this.nodes_to_listen.add(Integer.parseInt(s));
				}
			}
		} else {
			throw new IOException("File does not exist or read not allowed");
		}
		this.init_adj_matrix(periodicBoundary);
		this.reset_interaction_list(true);
	}

	public void init_adj_matrix(boolean periodicBoundary) {

		for (int nd = 0; nd < this.n_nodes; nd++) {

			degree[nd] = 0;
			int i = nd / size;
			int j = nd % size;

			if (periodicBoundary) {
				adj_matrix[nd][degree[nd]++] = j
						+ (i == 0 ? this.size - 1 : i - 1) * size; // up
				adj_matrix[nd][degree[nd]++] = (j == 0 ? this.size - 1 : j - 1)
						+ (i * size); // left
				adj_matrix[nd][degree[nd]++] = j + (i == size - 1 ? 0 : i + 1)
						* size; // down
				adj_matrix[nd][degree[nd]++] = (j == size - 1 ? 0 : j + 1)
						+ (i * size); // right
			} else {
				if (i > 0) // not on line 0
					adj_matrix[nd][degree[nd]++] = j + (i - 1) * size; // up

				if (j > 0) // not on column 0
					adj_matrix[nd][degree[nd]++] = (j - 1) + (i * size); // left

				if (i < size - 1) // not on last line
					adj_matrix[nd][degree[nd]++] = j + (i + 1) * size; // down

				if (j < size - 1) // not on last column
					adj_matrix[nd][degree[nd]++] = (j + 1) + (i * size); // right
			}
		}
	}

	public void reset_interaction_list(boolean complete) {
		this.interactiveNodes.clear();
		for (int nd = 0; nd < this.n_nodes; nd++) {
			// if (complete || interactiveNodes.contains(nd)) {
			if (complete || (is_node_active[nd] == true)) {
				is_node_active[nd] = false;
				for (int nbr_idx = 0; nbr_idx < degree[nd]; nbr_idx++) {
					// int similarity = 0;
					int nbr = adj_matrix[nd][nbr_idx];

					// for (int f = 0; f < features; f++){
					// if (states[nd][f] == states[nbr][f]){
					// similarity++;
					// }
					// }

					// if ((similarity > 0) && (similarity < features)) {
					if (is_interaction_possible(states[nd], states[nbr]) == true) {
						is_node_active[nd] = true;
						interactiveNodes.add(nd);
						break;
					}
				}
			}
		}
	}

	public Map<Integer, Integer> count_cultures() {
		Map<Integer, Integer> cultureSizes = new HashMap<Integer, Integer>();
		for (int nd = 0; nd < n_nodes; nd++) {
			cultureSizes.put(Arrays.hashCode(states[nd]), (cultureSizes
					.get(Arrays.hashCode(states[nd])) == null ? 0
					: cultureSizes.get(Arrays.hashCode(states[nd]))) + 1);
		}
		return cultureSizes;
	}

	public Integer count_interactive_edges() {
		Integer interactive_edges = 0;
		for (int nd = 0; nd < this.n_nodes; nd++) {
			for (int nbr_idx = 0; nbr_idx < degree[nd]; nbr_idx++) {
				// int similarity = 0;
				int nbr = adj_matrix[nd][nbr_idx];
				// for (int f = 0; f < features; f++)
				// if (states[nd][f] == states[nbr][f])
				// similarity++;
				// if ((similarity > 0) && (similarity < features)) {
				if (is_interaction_possible(this.states[nd], this.states[nbr])) {
					interactive_edges++;
				}
			}
		}
		return interactive_edges;
	}

	public void update_representations(int nd) {

		int[] nd_state = this.states[nd];

		is_node_active[nd] = false;
		interactiveNodes.remove(new Integer(nd));

		for (int k0 = 0; k0 < degree[nd]; k0++) {

			int nbr = this.adj_matrix[nd][k0];
			int[] nbr_state = this.states[nbr];

			is_node_active[nbr] = false;
			interactiveNodes.remove(new Integer(nbr));

			if (!interactiveNodes.contains(nd)) {
				if (is_interaction_possible(nd_state, nbr_state)) {
					is_node_active[nd] = true;
					interactiveNodes.add(nd);
					is_node_active[nbr] = true;
					interactiveNodes.add(nbr);
					continue;
				}
			}

			for (int k1 = 0; k1 < degree[nbr] && is_node_active[nbr] == false; k1++) {
				// for (int k1 = 0; k1 < degree[nbr] &&
				// !interactiveNodes.contains(nbr); k1++) {
				int nbr_nbr = this.adj_matrix[nbr][k1];
				int[] nbr_nbr_state = this.states[nbr_nbr];

				if (is_interaction_possible(nbr_state, nbr_nbr_state)) {
					is_node_active[nbr] = true;
					interactiveNodes.add(nbr);
					break;
				}
			}
		}
	}

	public static boolean is_interaction_possible(int[] ndState, int[] nbrState) {
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

	public String interaction_list_to_string() {
		StringBuilder sb = new StringBuilder("Interactive node count: "
				+ interactiveNodes.size() + "\n");
		sb.append("Interactive nodes:\n");
		Object[] values = this.interactiveNodes.toArray();
		Arrays.sort(values);
		for (Object o : values) {
			Integer nd = (Integer) o;
			sb.append(String.format("Node %d/(%d, %d)\n", nd, nd / size, nd
					% size));
		}
		return sb.toString();
	}

	public String network_state_to_string() {
		StringBuilder sb = new StringBuilder("Network state:\n");
		for (int nd = 0; nd < this.n_nodes; nd++) {
			sb.append(String.format("Node %d/(%d, %d): ", nd, nd / size, nd
					% size));
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
			sb.append(String.format("node %d/(%d,%d):\t", nd, nd / size, nd
					% size));
			for (int j = 0; j < degree[nd]; j++) {
				int nbr = adj_matrix[nd][j];
				sb.append(String.format("%d/(%d,%d)\t", nbr, nbr / size, nbr
						% size));
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public void random_starting_distribution() {
		for (int nd = 0; nd < this.n_nodes; nd++) {
			states[nd] = State.random_node_state(features, traits);
		}
		this.reset_interaction_list(true);
	}

	public void voter_starting_distribution() {
		for (int nd = 0; nd < this.n_nodes; nd++) {
			states[nd] = State.random_node_state_voter(traits);
		}
		this.reset_interaction_list(true);
	}

	public void bubble(int radius, int[] state1, int[] state2) {

		int r2 = radius * radius;

		int lin_centro = this.size / 2;
		int col_centro = this.size / 2;

		for (int i = 0; i < this.size; i++) {
			int magy = (i - lin_centro) * (i - lin_centro);
			for (int j = 0; j < this.size; j++) {
				int magx = (j - col_centro) * (j - col_centro);
				if (magx + magy <= r2) {
					System.arraycopy(state1, 0, this.states[j + i * this.size],
							0, this.features);
				} else {
					System.arraycopy(state2, 0, this.states[j + i * this.size],
							0, this.features);
				}
			}
		}

		this.reset_interaction_list(true);
	}

	public void striped_starting_distribution() {
		int[] state1 = new int[features];
		int[] state2 = new int[features];
		state1[0] = state2[0] = traits - 1;
		for (int i = 1; i < state1.length; i++) {
			state1[i] = 0;
			state2[i] = traits - 1;
		}
		for (int nd = 0; nd < n_nodes; nd++) {
			if (nd % 2 == 0) {
				System.arraycopy(state1, 0, states[nd], 0, state1.length);
			} else {
				System.arraycopy(state2, 0, states[nd], 0, state2.length);
			}
		}
		this.reset_interaction_list(true);
	}

	public void homogeneous_distribution() {
		int[] randst = State.random_node_state(features, traits);
		for (int nd = 0; nd < n_nodes; nd++) {
			System.arraycopy(randst, 0, states[nd], 0, randst.length);
		}
		this.reset_interaction_list(true);
	}

	public boolean is_state_valid(int[] state) {
		for (int i = 0; i < features; i++) {
			if (state[i] < 0 || state[i] >= traits)
				return false;
		}
		return true;
	}

	public Integer random_interactive_node() {
		Integer idx = rand.nextInt(this.interactiveNodes.size());
		return this.interactiveNodes.get(idx);
	}

	public List<Integer> interactive_nodes() {
		return Collections.unmodifiableList(interactiveNodes);
	}

	public int degree(int node) {
		return degree[node];
	}

	public int node_neighbor(int node, int nbrIdx) {
		if (nbrIdx > degree[node])
			return -1;
		return adj_matrix[node][nbrIdx];
	}

	public void save_to_file(File f) throws IOException {
		if (!f.exists())
			f.createNewFile();
		if (f.canWrite()) {
			FileWriter fw = new FileWriter(f);
			fw.write(String.valueOf(this.size));
			fw.write(':');
			fw.write(String.valueOf(this.features));
			fw.write(':');
			fw.write(String.valueOf(this.traits));
			fw.write(':');
			fw.write(String.valueOf(this.periodicBoundary));
			fw.write('\n');
			for (int i = 0; i < states.length; i++) {
				int[] state = states[i];
				for (int j = 0; j < state.length; j++) {
					fw.write(String.valueOf(state[j]));
					if (j != state.length - 1)
						fw.write(':');
				}
				fw.write('\n');
			}
			if (!nodes_to_listen.isEmpty())
				fw.write("monitor:[");
			for (Integer node : nodes_to_listen) {
				fw.write(Integer.toString(node));
				fw.write(nodes_to_listen.indexOf(node) == nodes_to_listen
						.size() - 1 ? ']' : ',');
			}
			fw.close();
		}
	}

	public void setRefreshAdjust(int refreshAdjust) {
		this.refresh_rate = calc_update_rate(refreshAdjust);
	}

	public Integer overlap(int node, int nbr) {
		Integer overlap = 0;
		for (int f = 0; f < features; f++) {
			if (states[node][f] == states[nbr][f]) {
				overlap++;
			}
		}
		return overlap;
	}

	public int n_edges() {
		if (periodicBoundary) {
			return n_nodes * 2;
		} else
			return n_nodes * 2 - size * 2;
	}

	public int LyapunovPotential() {
		int potential = 0;
		for (int i = 0; i < this.n_nodes; i++) {
			for (int nbr_idx = 0; nbr_idx < this.degree(i); nbr_idx++) {
				if (i < this.adj_matrix[i][nbr_idx]) {
					potential -= this.overlap(i, this.adj_matrix[i][nbr_idx]);
				}
			}
		}
		return potential;
	}

	public int RemainingTraits() {
		int rt = 0;
		int q;
		int F;
		boolean[][] R = new boolean[this.traits][this.features];
		for (q = 0; q < this.traits; q++) {
			for (F = 0; F < this.features; F++) {
				R[q][F] = false;
			}
		}
		for (int i = 0; i < this.n_nodes; i++) {
			for (F = 0; F < this.features; F++) {
				R[this.states[i][F]][F] = true;
			}
		}
		for (q = 0; q < this.traits; q++) {
			for (F = 0; F < this.features; F++) {
				if (R[q][F] == true) {
					rt++;
				}
			}
		}
		return rt;
	}

	public double Entropy() {
		double entropy = 0;
		int[] domains = new int[this.n_nodes];
		List<Integer> FirstList = new ArrayList<Integer>();
		List<Integer> SecondList = new ArrayList<Integer>();
		List<Integer> ClusterSizes = new ArrayList<Integer>();
		Integer node;
		Integer count = 0;
		int nbr;
		int nm;
		double pm;

		for (node = 0; node < this.n_nodes; node++) {
			FirstList.add(node);
		}
		for (nm = 0; nm < this.n_nodes; nm++) {
			domains[nm] = 0;
		}

		while (!FirstList.isEmpty()) {
			node = FirstList.remove(0);
			SecondList.add(node);
			while (!SecondList.isEmpty()) {
				node = SecondList.remove(0);
				for (int nbr_idx = 0; nbr_idx < this.degree(node.intValue()); nbr_idx++) {
					nbr = this.adj_matrix[node.intValue()][nbr_idx];
					if (this.overlap(node.intValue(), nbr) == this.features) {
						if (FirstList.remove(new Integer(nbr)) == true) {
							SecondList.add(new Integer(nbr));
						}
					}
				}
				count++;
			}
			ClusterSizes.add(count);
			count = 0;
		}
		while (!ClusterSizes.isEmpty()) {
			nm = ClusterSizes.remove(0).intValue();
			domains[nm - 1] += nm;
		}
		for (nbr = 0; nbr < this.n_nodes; nbr++) {
			pm = (double) domains[nbr] / this.n_nodes;
			if (pm != 0) {
				entropy -= pm * Math.log(pm);
			}
		}

		return entropy;
	}

}
