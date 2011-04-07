/**
 * 
 */
package br.edu.cultural;

import junit.framework.Assert;

import org.junit.Test;

import br.edu.cultural.network.State;

/**
 * @author muggler
 * 
 */
public class StateTest {

	final static int features = 3;
	final static int traits = 12;
	private static final int TEST_REPEAT = 10;

	@Test
	public void test_toString() {
		int[] state = { 0, 0, 0, 0 };
		String statestr = State.toString(state);
		System.out.println(statestr);
		Assert.assertEquals("States are not equal.", "0.0.0.0", statestr);

		int[] state1 = { 100, 45, 1010, 3 };
		statestr = State.toString(state1);
		System.out.println(statestr);
		Assert.assertEquals("States are not equal.", "3.1010.45.100", statestr);

		int[] state2 = { 20, 48, 7 };
		statestr = State.toString(state2);
		System.out.println(statestr);
		Assert.assertEquals("States are not equal.", "7.48.20", statestr);
	}

	@Test
	public void test_iteration_up() {
		System.out
				.println("Testing state incrementor function. Please validate the following output:");
		int[] min_state = State.min_state(features, traits);
		System.out.println("Iterating UP from state "
				+ State.toString(min_state) + "...");
		for (int st = 0; st < Math.pow(traits, features); st++) {
			State.incr(min_state, 0, traits);
			System.out.println(State.toString(min_state));
		}
		System.out.println("Done.");
	}

	@Test
	public void test_iteration_down() {
		int[] max_state = State.max_state(features, traits);
		System.out
				.println("Testing state decrementor function. Please validate the following output:");
		System.out.println("Iterating DOWN from state "
				+ State.toString(max_state) + "...");
		for (int st = 0; st < Math.pow(traits, features); st++) {
			State.decr(max_state, 0, traits);
			System.out.println(State.toString(max_state));
		}
		System.out.println("Done.");
	}

	@Test
	public void test_is_state_valid() {

		int[] valid_state = State.max_state(features, traits);
		Assert.assertTrue("State should be valid.", State.is_state_valid(
				valid_state, features, traits));

		valid_state = State.min_state(features, traits);
		Assert.assertTrue("State should be valid.", State.is_state_valid(
				valid_state, features, traits));

		for (int test = 0; test < TEST_REPEAT; test++) {
			valid_state = new int[features];
			for (int f = 0; f < valid_state.length; f++) {
				valid_state[f] = (int) (traits * Math.random());
			}
			Assert.assertTrue("State should be valid.", State.is_state_valid(
					valid_state, features, traits));
		}

		for (int test = 0; test < TEST_REPEAT; test++) {
			valid_state = new int[features];
			for (int f = 0; f < valid_state.length; f++) {
				valid_state[f] = (int) (traits / Math.random());
			}
			Assert.assertFalse("State should be invalid.", State
					.is_state_valid(valid_state, features, traits));
		}

	}

	@Test
	public void test_random_node_state() {
		for (int test = 0; test < TEST_REPEAT; test++) {
			Assert.assertTrue("State should be valid.", State
					.is_state_valid(State.random_node_state(features, traits),
							features, traits));
		}
	}
	
	@Test
	public void test_has_next(){
		Assert.assertTrue("Should have a next state.", State.hasNext(State.min_state(features, traits), features, traits));
		Assert.assertFalse("Should not have a next state.", State.hasNext(State.max_state(features, traits), features, traits));
	}
	
	@Test
	public void test_has_previous(){
		Assert.assertTrue("Should have a previous state.", State.hasPrevious(State.max_state(features, traits), features));
		Assert.assertFalse("Should not have a previous state.", State.hasPrevious(State.min_state(features, traits), features));
	}
	
}
