/**
 * 
 */
package br.edu.axelrod;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author muggler
 *
 */
public class AxelrodNetworkTest {
	
	static final int size = 4;
	static final int features = 3;
	static final int traits = 3;
	
	@Test
	public void test_random_starting_distribution() {
		System.out.println("Testing network random starting distribution...");
		System.out.println(String.format("Network size = %d, features = %d, traits = %d.", size, features, traits));
		AxelrodNetwork nw = new AxelrodNetwork(size, features, traits);
		for (int nd  = 0; nd < size; nd ++) {
				Assert.assertTrue(nw.is_state_valid(nw.state(nd)));
		}
		System.out.println(nw.network_state_to_string());
		System.out.println();
	}
	
	@Test
	public void test_init_adjacency_matrix(){
		System.out.println("Testing network adjacency matrix initialization...");
		System.out.println(String.format("Network size = %d, features = %d, traits = %d.", size, features, traits));
		AxelrodNetwork nw = new AxelrodNetwork(size, features, traits);
		nw.random_starting_distribution();
		System.out.println(nw.adjacency_matrix_to_string());
	}
	
	@Test 
	public void test_interactive_nodes_list(){
		System.out.println("Testing network interactive nodes initialization...");
		System.out.println(String.format("Network size = %d, features = %d, traits = %d.", size, features, traits));
		AxelrodNetwork nw = new AxelrodNetwork(size, features, traits);
		nw.homogeneous_distribution();
		System.out.println(nw.network_state_to_string());
		System.out.println();
		System.out.println(nw.interaction_list_to_string());
	}
	
	@Test
	public void test_update_interaction_list(){
		System.out.println("Testing network interactive nodes initialization...");
		System.out.println(String.format("Network size = %d, features = %d, traits = %d.", size, features, traits));
		AxelrodNetwork nw = new AxelrodNetwork(size, features, traits);
		nw.homogeneous_distribution();

		// Assert initial expectations
		System.out.println(nw.network_state_to_string());
		System.out.println();
		System.out.println(nw.interaction_list_to_string());
		
		// Update node state
		int[] state = nw.states[1];
		state[0] = (state[0] == 0? state[0]+1:state[0]-1);
//		nw.setState(0, state);
		nw.states[1] = state;
		nw.update_representations(1);
		
		// Assert new network state is consistent
		System.out.println(nw.network_state_to_string());
		System.out.println(nw.interaction_list_to_string());
		
	}
	
	@Test
	public void test_homogeneous_dist(){
		AxelrodNetwork nw = new AxelrodNetwork(size, features, traits);
		nw.homogeneous_distribution();
		System.out.println(nw.network_state_to_string());
		nw.states[0][0] = 0;
		System.out.println(nw.network_state_to_string());
	}

}
