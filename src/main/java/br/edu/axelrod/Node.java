/**
 * 
 */
package br.edu.axelrod;


/**
 * @author muggler
 * 
 */
public class Node {
	
	public static enum Neighbors{
		UP(0), RIGHT(1), DOWN(2), LEFT(3);
		public final int dir;
		private Neighbors(int dir){
		this.dir = dir;
		};
	}
	

	/**
	 * Converts an encoded node to an int[2] array tuple.
	 * @param encoded_node
	 * @return
	 */
	public static int[] pos_to_indices(int node_pos, int gridSize) {
		int[] tuple = {(gridSize / node_pos), (gridSize % node_pos)};
		return tuple;
	}

	public static int indices_to_pos(int[] node_indices, int gridSize) {
		return (node_indices[0]*gridSize) + (node_indices[1]);
	}

	public static String print_as_tuple(int node_pos, int gridSize) {
		int[] n = Node.pos_to_indices(node_pos, gridSize);
		return Node.print_as_tuple(n[0], n[1]);
	}
	
	public static String print_as_tuple(int[] node_indices){
		return String.format("(%d,%d)", node_indices[0], node_indices[1]);
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

	public String toString() {
		return Node.print_as_tuple(coord_i, coord_j);
	}

}
