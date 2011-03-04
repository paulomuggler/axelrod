/**
 * 
 */
package br.edu.axelrod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * @author muggler
 *
 */
public class RgbPartitioner {
	
	private final int features;
	private final int traits;
	private final int partitions;
	private final int[] partitioning;
	
	private final List<Integer> colors; // O(q^F) int entries, max 256^3 ~67.1 MB
//	private final BidiMap colorMap = new TreeBidiMap(); // O(q^F) int x int entries, max 256^3 ~268.4 MB
	private final Map<Integer, Integer> colorMap = new HashMap<Integer,Integer>(); // O(q^F) int x int entries, max 256^3 ~268.4 MB
	
	public RgbPartitioner(int features, int traits){
		this.features = features;
		this.traits = traits;
		this.partitions = (int) Math.pow(this.traits, this.features);
		if(this.partitions > 0x1000000){
			throw new IllegalArgumentException("Not enough colors in RGB space!");
		}
		partitioning = this.partitionRgbColorSpace(this.partitions);
		this.colors = this.calcRgbColorValues(this.partitioning);
		this.removeUnusedColors(this.partitioning, this.partitions, this.colors);
		this.createColorMap(this.features, this.traits, this.colors, this.colorMap);
	}
	
	public int color(int[] state){
		return (Integer) colorMap.get(Arrays.hashCode(state));
	}
	
	public Map<Integer, Integer> getColorMap(){
		return Collections.unmodifiableMap(colorMap);
	}

	/**
	 * @param features
	 * @param traits
	 * @param colors
	 * @param colorMap
	 */
	private void createColorMap(int features, int traits, List<Integer> colors,	Map<Integer, Integer> colorMap) {
		int[] state = State.min_state(features, traits);
		int c = 0;
		colorMap.put(Arrays.hashCode(state), colors.get(c++));
		while(State.hasNext(state, features, traits)){
			State.incr(state, 0, traits);
			colorMap.put(Arrays.hashCode(state), colors.get(c++));
		}
	}

	/**
	 * @param partitioning
	 * @param partitions
	 * @param colors
	 */
	private void removeUnusedColors(int[] partitioning, int partitions, List<Integer> colors) {
		
		int pR = partitioning[0];
		int pG = partitioning[1];
		int pB = partitioning[2];
		
		int slots = pR*pG*pB;
		
		int holes = slots - partitions;
		
		if(holes > 0){
			
			int skip = (int) Math.floor((float)slots/holes);
			
			int i = 1;
			for (int h  = 0; h < holes; h ++) {
				int rem = (int)(((h+1) * skip) - i);
				i += 1;
				colors.remove(rem);
			}
		}
	}

	/**
	 * @param partitioning
	 * @return
	 */
	private List<Integer> calcRgbColorValues(int[] partitioning) {
		
		int pR = partitioning[0];
		int pG = partitioning[1];
		int pB = partitioning[2];

		List<Integer> colors = new ArrayList<Integer>();
		
		int stepR = 0xFF / Math.max(1, (pR-1));
		int stepG = 0xFF / Math.max(1, (pG-1));
		int stepB = 0xFF / Math.max(1, (pB-1));
		
		for (int r  = 0; r < pR; r ++) {
			for (int g  = 0; g < pG; g ++) {
				for (int b  = 0; b < pB; b ++) {
					int color = r * stepR * 0x10000 + g * stepG * 0x100 + b * stepB;
					colors.add(color);
				}
			}
		}
		return colors;
	}

	/**
	 * @param partitions
	 * @return
	 */
	private int[] partitionRgbColorSpace(int partitions) {
		int[] partitioning = new int[3];
		
		int pR = (int) Math.ceil(Math.cbrt(partitions));
		int pG = (int) Math.ceil(Math.sqrt(partitions/(float) pR));
		int pB = (int) Math.ceil((float)partitions/(pR * pG));
		
		partitioning[0] = pR;
		partitioning[1] = pG;
		partitioning[2] = pB;
		
		return partitioning;
	}

}
