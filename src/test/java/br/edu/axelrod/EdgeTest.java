/**
 * 
 */
package br.edu.axelrod;

import static br.edu.axelrod.Utils.to_binary_string;

import org.junit.Assert;
import org.junit.Test;

import br.edu.axelrod.Edge;
import br.edu.axelrod.Node;

/**
 * @author muggler
 *
 */
public class EdgeTest {

	static final int size = 4;
	
	@Test
	public void edge_to_string_test(){
		System.out.println("Testing edge string representations...");
		System.out.println("Network size: " + size);
		System.out.println();
		int cMask = Node.get_coord_bit_mask(size);
		for (int i  = 0; i < size; i ++) {
			for (int j  = 0; j < size; j ++) {
				Node n = new Node(i,j);
				System.out.print(String.format("Node %s:\n", n.toString()));
				Node n_up = new Node(n.neighbor_encoded(Node.Neighbors.UP.dir, cMask));
				Assert.assertEquals(String.format("%s-->%s", n.toString(), n_up.toString()), Edge.print_from_node_tuple(n.encode(), n_up.encode()));
				System.out.print(String.format("UP:\texpected: %s-->%s, calculated: %s\n", n.toString(), n_up.toString(), Edge.print_from_node_tuple(n.encode(), n_up.encode())));
			}
		}
	}
	
	@Test
	public void edge_encode_test(){
		System.out.println("Testing edge encoding...");
		System.out.println("Network size: " + size);
		System.out.println();
		
		int cMask = Node.get_coord_bit_mask(size);
		int e = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {

				int node_encoded = Node.encode_from_coords(i, j);
				
				System.out.println("Edge encoding for node "+Node.print_as_tuple(node_encoded)+", neighbor UP:");
				int dir = Node.Neighbors.UP.dir;
				int nbr_encoded = Node.node_neighbor_encoded(node_encoded, dir, cMask);
				int edge_encoded = Edge.encode(node_encoded, dir);
				System.out.println("\t"+to_binary_string(edge_encoded));
				Assert.assertEquals(Edge.print_from_node_tuple(node_encoded, nbr_encoded), Edge.print_from_encoded_edge(edge_encoded, cMask));
				
				System.out.println("Edge encoding for node "+Node.print_as_tuple(node_encoded)+", neighbor RIGHT:");
				dir = Node.Neighbors.RIGHT.dir;
				nbr_encoded = Node.node_neighbor_encoded(node_encoded, dir, cMask);
				edge_encoded = Edge.encode(node_encoded, dir);
				System.out.println("\t"+to_binary_string(edge_encoded));
				Assert.assertEquals(Edge.print_from_node_tuple(node_encoded, nbr_encoded), Edge.print_from_encoded_edge(edge_encoded, cMask));
				
				System.out.println("Edge encoding for node "+Node.print_as_tuple(node_encoded)+", neighbor DOWN:");
				dir = Node.Neighbors.DOWN.dir;
				nbr_encoded = Node.node_neighbor_encoded(node_encoded, dir, cMask);
				edge_encoded = Edge.encode(node_encoded, dir);
				System.out.println("\t"+to_binary_string(edge_encoded));
				Assert.assertEquals(Edge.print_from_node_tuple(node_encoded, nbr_encoded), Edge.print_from_encoded_edge(edge_encoded, cMask));
				
				System.out.println("Edge encoding for node "+Node.print_as_tuple(node_encoded)+", neighbor LEFT:");
				dir = Node.Neighbors.LEFT.dir;
				nbr_encoded = Node.node_neighbor_encoded(node_encoded, dir, cMask);
				edge_encoded = Edge.encode(node_encoded, dir);
				System.out.println("\t"+to_binary_string(edge_encoded));
				Assert.assertEquals(Edge.print_from_node_tuple(node_encoded, nbr_encoded), Edge.print_from_encoded_edge(edge_encoded, cMask));
				
				e++;
			}
		}
		System.out.println();
	}
	
}
