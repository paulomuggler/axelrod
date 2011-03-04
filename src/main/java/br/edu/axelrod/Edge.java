/**
 * 
 */
package br.edu.axelrod;


/**
 * @author muggler
 *
 */
public class Edge {
	
	public static final String print_from_node_tuple(int node0, int node1){
		return String.format("%s-->%s", Node.print_as_tuple(node0), Node.print_as_tuple(node1));
	}
	
	public static final String print_from_encoded_edge(int edge_encoded, int coord_rep_mask){
		int[] e = Edge.to_node_tuple(edge_encoded, coord_rep_mask);
		return print_from_node_tuple(e[0], e[1]);
	}
	
	/**
	 * Encodes an edge given a node representation and a direction index. 
	 * The direction index is as follows: 0 => up, 1 => right, 2 => down, 3 => left.
	 * An edge's direction is encoded in the 2 most significant bits of its
	 * 32-bit integer representation.
	 * 
	 * @param node_encoded
	 * @param direction
	 * @return
	 */
	public static int encode(int node_encoded, int direction) {
		return (direction << 2 * Node.MAX_COORD_REP_BASE) + node_encoded;
	}
	
	/** Decodes an integer representation of an edge.
	 * @param edge_encoded integer representation of one edge
	 * @return An int[][] array containing the integer representation of the nodes connected by this edge: int[0] = n0, int[1] = n1.
	 */
	public static int[] to_node_tuple(int edge_encoded, int coord_rep_mask) {
		int[] nodes = new int[2];
		nodes[0] = edge_encoded & Node.NODE_REP_MASK;
		int dir_idx = edge_encoded >>> (2*Node.MAX_COORD_REP_BASE);
		nodes[1] = Node.node_neighbor_encoded(nodes[0], dir_idx, coord_rep_mask);
		return nodes;
	}
	
	public static int[] to_node_and_direction(int edge_encoded){
		int[] edge = {edge_encoded >>> (2*Node.MAX_COORD_REP_BASE), edge_encoded & Node.NODE_REP_MASK};
		return edge;
	}
	
	final int edge_encoded;
	
	public Edge(Node node, int direction){
		this.edge_encoded = Edge.encode(node.encode(), direction);
	}
	
	public Edge(int edge_encoded) {
		this.edge_encoded = edge_encoded;
	}

	public int encode(){
		return this.edge_encoded;
	}
	
}
