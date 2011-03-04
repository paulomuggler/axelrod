/**
 * 
 */
package br.edu.axelrod;


/**
 * @author muggler
 * 
 */
public class Node {
	
	public static final int MAX_COORD_REP_BASE = 15; // 28-bit node representation, 32-bit edge representation (30 + 2)
	public static final int NODE_REP_MASK = (1 << (2 * MAX_COORD_REP_BASE)) - 1;
	public static final int COORD_REP_MASK = (1 << MAX_COORD_REP_BASE) - 1;
	public static final int COORD_I_MASK = ((1 << MAX_COORD_REP_BASE) - 1) << MAX_COORD_REP_BASE;
	public static final int COORD_J_MASK = COORD_REP_MASK;
	
	public static enum Neighbors{
		UP(0), RIGHT(1), DOWN(2), LEFT(3);
		public final int dir;
		private Neighbors(int dir){
		this.dir = dir;
		};
	}
	
	public static int node_neighbor_encoded(int node_encoded, int dir, int coord_rep_mask){
		int[] n = to_int_array_tuple(node_encoded);
		return node_neighbor_encoded(n[0], n[1], dir, coord_rep_mask);
	}
	
	public static int node_neighbor_encoded(int coord_i, int coord_j, int dir, int coord_rep_mask){
		int[] nbr = Node.node_neighbor_as_tuple(coord_i, coord_j, dir, coord_rep_mask);
		return Node.encode_from_coords(nbr[0], nbr[1]);
	}
	
	public static int[] node_neighbor_as_tuple(int encoded_node, int dir, int coord_rep_mask){
		int[] n = to_int_array_tuple(encoded_node);
		return node_neighbor_as_tuple(n[0], n[1], dir, coord_rep_mask);
	}
	
	public static int[] node_neighbor_as_tuple(int coord_i, int coord_j, int dir, int coord_rep_mask){
		int edge_dir = index_to_edge_dir(dir); // get transform coefficient (4 bits)
		/*	2 bits are used for transforming each node coordinate i and j, yielding 
		 * the coordinates i and j of the node's neighbor at the direction encoded in dir. */
		int dir_i = (edge_dir >>> 2) - 1; // 2 MSBs of edge_dir
		int dir_j = (edge_dir & 3) - 1; // 2 LSBs of edge_dir
		int[] nbr = {((coord_i + dir_i) & coord_rep_mask), ((coord_j + dir_j) & coord_rep_mask)};
		return nbr;
	}
	
	/** A 4-value discrete function mapping 2-bit indices (0, 1, 2, 3) to 
	 *  numerical 3-bit transform coefficients (1, 6, 9, 4) used to transform 
	 *  the representation of a node into the representation of one of its neighbors */
	private static final int index_to_edge_dir(int idx){
		return index_to_edge_dir[idx];
	}
	static final int[] index_to_edge_dir = new int[4];
	static{
		index_to_edge_dir[0] = 1;
		index_to_edge_dir[1] = 6;
		index_to_edge_dir[2] = 9;
		index_to_edge_dir[3] = 4;
	}

	public static int encode_from_coords(int i, int j) {
		return (i << Node.MAX_COORD_REP_BASE)+j;
	}
	
	public static int encode_from_index(int index, int nwSize){
		int j = index % nwSize;
		int i = index / nwSize;
		return Node.encode_from_coords(i, j);
	}

	/**
	 * Converts an encoded node to an int[2] array tuple.
	 * @param encoded_node
	 * @return
	 */
	public static int[] to_int_array_tuple(int encoded_node) {
		int[] tuple = {(encoded_node >>> Node.MAX_COORD_REP_BASE), (encoded_node & Node.COORD_J_MASK)};
		return tuple;
	}

	public static int to_index(int encoded_node, int coord_rep_base) {
		return ((encoded_node & Node.COORD_I_MASK) >>> (Node.MAX_COORD_REP_BASE - coord_rep_base))
		+(encoded_node & Node.COORD_J_MASK);
	}

	public static String print_as_tuple(int node_encoded) {
		int[] n = Node.to_int_array_tuple(node_encoded);
		return Node.print_as_tuple(n[0], n[1]);
	}
	
	public static String print_as_tuple(int coord_i, int coord_j){
		return String.format("(%d,%d)", coord_i, coord_j);
	}
	
	public static int get_coord_base(int nwSize){
		return (int)(Math.log(nwSize)/Math.log(2));
	}
	
	public static int get_coord_bit_mask(int nwSize){
		return (1 << get_coord_base(nwSize)) - 1;
	}

	public final int coord_i;
	public final int coord_j;

	public Node(int coord_i, int coord_j) {
		super();

		this.coord_i = coord_i;
		this.coord_j = coord_j;
	}

	/**
	 * @param i
	 */
	public Node(int node_encoded) {
		this.coord_i = node_encoded>>>Node.MAX_COORD_REP_BASE;
		this.coord_j = node_encoded & Node.COORD_J_MASK;
	}

	public int encode() {
		return Node.encode_from_coords(coord_i, coord_j);
	}
	
	public int[] neighbor_as_tuple(int direction, int coord_rep_mask){
		return node_neighbor_as_tuple(coord_i, coord_j, direction, coord_rep_mask);
	}
	
	public int neighbor_encoded(int direction, int coord_rep_mask){
		return node_neighbor_encoded(this.encode(), direction, coord_rep_mask);
	}

	public String toString() {
		return Node.print_as_tuple(coord_i, coord_j);
	}

}
