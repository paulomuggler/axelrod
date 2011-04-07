/**
 * 
 */
package br.edu.cultural;

import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.junit.Test;

import br.edu.cultural.gui.RgbPartitioner;
import br.edu.cultural.network.State;

/**
 * @author muggler
 * 
 */
public class RgbPartitionerTest {

	static final int features = 3;
	static final int traits = 3;

	@Test
	public void testRgbPartitioning() {
		RgbPartitioner rp = new RgbPartitioner(features, traits);
		Map<Integer, Integer> cm = rp.getColorMap();
		for (Entry<Integer, Integer> e : cm.entrySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append("State: " + e.getKey());
			sb.append(String.format("; Color: %06x", e.getValue()));
			System.out.println(sb.toString());
		}
	}

	@Test
	public void test_partition_contains_all_states() {
		RgbPartitioner rp = new RgbPartitioner(features, traits);
		int[] state = new int[features];
		for (int i = 0; i < state.length; i++) {
			state[i] = 0;
		}
		Assert.assertNotNull(rp.color(state));
		while (State.hasNext(state, features, traits)) {
			State.incr(state, 0, traits);
			Assert.assertNotNull(rp.color(state));
		}
	}

}
