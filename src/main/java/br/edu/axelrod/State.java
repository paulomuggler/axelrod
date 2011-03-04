/**
 * 
 */
package br.edu.axelrod;

import java.util.Random;

/**
 * @author muggler
 * 
 */
public class State {

	static final Random rand = new Random();

	public static boolean is_state_valid(int[] state, int features, int traits) {
		for (int f = 0; f < features; f++) {
			if (state[f] < 0 || state[f] >= traits)
				return false;
		}
		return true;
	}

	public static void incr(int[] state, int f, int q) {
		if (f == state.length)
			return;
		if (state[f] == q - 1) {
			state[f] = 0;
			f++;
			incr(state, f, q);
			return;
		}
		state[f]++;
		return;
	}

	public static void decr(int[] state, int f, int q) {
		if (f == state.length)
			return;
		if (state[f] == 0) {
			state[f] = q - 1;
			f++;
			decr(state, f, q);
			return;
		}
		state[f]--;
		return;
	}

	public static int[] max_state(int features, int traits) {
		int[] max_state = new int[features];
		for (int f = 0; f < max_state.length; f++) {
			max_state[f] = traits - 1;
		}
		return max_state;
	}

	public static int[] min_state(int features, int traits) {
		int[] min_state = new int[features];
		for (int f = 0; f < min_state.length; f++) {
			min_state[f] = 0;
		}
		return min_state;
	}

	public static boolean hasNext(int[] state, int features, int traits) {
		for (int f = 0; f < features; f++) {
			if (state[f] < (traits - 1)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasPrevious(int[] state, int features) {
		for (int f = 0; f < features; f++) {
			if (state[f] > 0) {
				return true;
			}
		}
		return false;
	}

	public static String toString(int[] state) {

		Object[] args = new Object[state.length];
		for (int i = 0; i < state.length; i++) {
			args[i] = state[i];
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < state.length; i++) {
			sb.append('%');
			sb.append('d');
			sb.append('.');
		}
		sb.deleteCharAt(sb.length() - 1);
		return String.format(sb.toString(), args);
	}

	public static int[] random_node_state(int features, int traits) {
		int[] state = new int[features];
		for (int f = 0; f < features; f++) {
			state[f] = rand.nextInt(traits);
		}
		return state;
	}

}
