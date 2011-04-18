/**
 * 
 */
package br.edu.cultural.network;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;


/**
 * @author muggler
 * 
 */
public class Utils {
	static final Random rand = new Random();

	public static int nextPowerOfTwo(int n) {
		if (n<=1) return 1;
		double y = Math.floor(Math.log((double) n-1)/Math.log(2));

		return (int) Math.pow(2, y+1);
	}

	public static BigInteger xnor(BigInteger state1, BigInteger state2, int bit_width) {
		return (state1.xor(state2)).not().and(BigInteger.valueOf((((1<<bit_width)-1))));
	}
	
	public static <T> T random_list_element(List<T> aList){
		int idx = rand.nextInt(aList.size());
		return aList.get(idx);
	}

	/**
	 * @param i
	 * @return
	 */
	public static String to_binary_string(int n) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 32; i++) {
			sb.append((n >>> (31 - i)) & 1);
		}
		return sb.toString();
	}
	
	public static String to_binary_string(BigInteger n, int bit_width) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bit_width; i++) {
			sb.append((n.shiftRight(((bit_width-1) - i))).and(BigInteger.ONE));
//			sb.append((n >>> ((bit_width-1) - i)) & 1);
		}
		return sb.toString();
	}
	
	public static int[] copyArray(int[] ary){
		int[] copy = new int[ary.length];
		System.arraycopy(ary, 0, copy, 0, ary.length);
		return copy;
	}

}
