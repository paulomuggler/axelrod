/**
 * 
 */
package br.edu.axelrod;

import org.junit.Assert;
import org.junit.Test;

import br.edu.axelrod.Node;
import br.edu.axelrod.Utils;

/**
 * @author muggler
 * 
 */
public class NodeTest {

	static final int size = 4;
	static final int coord_rep_base = (int) Math.ceil(Math.log((double) size)/Math.log(2));

	@Test
	public void test_node_to_string(){
		System.out.println("Testing node string representation...");
		System.out.println("Network size: " + size);
		System.out.println();
		for (int i  = 0; i < size; i ++) {
			for (int j  = 0; j < size; j ++) {
				Node n = new Node(i, j);
				Assert.assertEquals(String.format("(%d,%d)", i, j), n.toString());
				Assert.assertEquals(String.format("(%d,%d)", i, j), Node.print_as_tuple(n.coord_i, n.coord_j));
				Assert.assertEquals(String.format("(%d,%d)", i, j), Node.print_as_tuple(n.encode()));
				System.out.print(String.format("%d,%d => %s; ", i, j, n.toString()));
			}
			System.out.println();
		}
		System.out.println();
	}
	
	@Test
	public void node_encode_test() {
		System.out.println("Testing node encoding...");
		System.out.println("Network size: " + size);
		int idx = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				Node n = new Node(i, j);
				int node_encoded = Node.encode_from_coords(i, j);
				Assert.assertEquals(n.toString(), Node.print_as_tuple(node_encoded));
				node_encoded = Node.encode_from_index(idx, size);
				Assert.assertEquals(n.toString(), Node.print_as_tuple(node_encoded));
				Node n_ = new Node(node_encoded);
				Assert.assertEquals(n.toString(), n_.toString());
				System.out.print(String.format("node " + n.toString()+": %s; ", Utils.to_binary_string(node_encoded)));
				idx++;
			}
			System.out.println();
		}
		System.out.println();
	}
	
	@Test
	public void node_to_int_array_tuple_test(){
		System.out.println("Testing encoded node to coordinate tuple decoding...");
		System.out.println("Network size: " + size);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				int node_encoded = Node.encode_from_coords(i, j);
				int[] t = Node.to_int_array_tuple(node_encoded); 
				Assert.assertEquals(i, t[0]);
				Assert.assertEquals(j, t[1]);
				Node n = new Node(node_encoded);
				System.out.print(String.format("node %s: %s; ", Utils.to_binary_string(node_encoded), n.toString()));
			}
			System.out.println();
		}
		System.out.println();
	}
	
	@Test
	public void encoded_node_to_index_test(){
		System.out.println("Testing encoded node to sequential index decoding...");
		System.out.println("Network size: " + size);
		for (int i  = 0; i < size * size; i ++) {
			int node_encoded = Node.encode_from_index(i, size);
			int idx = Node.to_index(node_encoded, coord_rep_base);
			Assert.assertEquals(i, idx);
			System.out.println(String.format("node %s:\t#%02d; ", Utils.to_binary_string(node_encoded), idx));
		}
		System.out.println();
	}
	
	@Test
	public void test_node_neighbor(){
		System.out.println("Testing node neighbors...");
		System.out.println("Network size: " + size);
		System.out.println();
		int cMask = Node.get_coord_bit_mask(size);
		for (int i  = 0; i < size; i ++) {
			for (int j  = 0; j < size; j ++) {
				Node n = new Node(i, j);
				
				System.out.print(String.format("Node %s:\n", n.toString()));
				
				Node n_up = new Node((i-1) & cMask, j);
				int nup_enc = Node.node_neighbor_encoded(n.encode(), Node.Neighbors.UP.dir, cMask);
				Assert.assertEquals(n_up.toString(), Node.print_as_tuple(nup_enc));
				System.out.print(String.format("UP:\texpected: %s, calculated: %s\n", n_up.toString(), Node.print_as_tuple(nup_enc)));
				
				Node n_right = new Node(i, (j+1)&cMask);
				int nr_enc = Node.node_neighbor_encoded(n.encode(), Node.Neighbors.RIGHT.dir, cMask);
				Assert.assertEquals(n_right.toString(), Node.print_as_tuple(nr_enc));
				System.out.print(String.format("RIGHT:\texpected: %s, calculated: %s\n", n_right.toString(), Node.print_as_tuple(nr_enc)));
				
				Node n_down = new Node((i+1)&cMask, j);
				int nd_enc = Node.node_neighbor_encoded(n.encode(), Node.Neighbors.DOWN.dir, cMask);
				Assert.assertEquals(n_down.toString(), Node.print_as_tuple(nd_enc));
				System.out.print(String.format("DOWN:\texpected: %s, calculated: %s\n", n_down.toString(), Node.print_as_tuple(nd_enc)));
				
				Node n_left = new Node(i, (j-1)&cMask);
				int nl_enc = Node.node_neighbor_encoded(n.encode(), Node.Neighbors.LEFT.dir, cMask);
				Assert.assertEquals(n_left.toString(), Node.print_as_tuple(nl_enc));
				System.out.print(String.format("LEFT:\texpected: %s, calculated: %s\n", n_left.toString(), Node.print_as_tuple(nl_enc)));
				
				System.out.println();
			}
		}
		System.out.println();
	}

}
