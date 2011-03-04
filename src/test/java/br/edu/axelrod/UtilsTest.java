/**
 * 
 */
package br.edu.axelrod;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

import br.edu.axelrod.Utils;
import static br.edu.axelrod.Utils.*;

/**
 * @author muggler
 * 
 */
public class UtilsTest {
	
	static final String[] xnors = {};

	@Test
	public void testXnor() {
		System.out.println("Testing xnor...");
		System.out.println();
		int bit_width = 40;
		System.out.println("bit width: "+bit_width);
		for (int i = 0; i < 16; i++) {
			BigInteger o1 = BigInteger.valueOf(i);
			BigInteger o2 = BigInteger.valueOf(i+1);
			System.out.println(String.format("XNOR for %s, %s: %s;\n", Utils.to_binary_string(o1, bit_width), 
			Utils.to_binary_string(o2, bit_width), 
			Utils.to_binary_string(Utils.xnor(o1, o2, bit_width),bit_width)));
		}
	}

	@Test
	public void testNextPowerOfTwo() {
		Assert.assertEquals(1, nextPowerOfTwo(0));
		Assert.assertEquals(1, nextPowerOfTwo(1));
		int exp = 1;
		for (int i = 2; i < Math.pow(2, 12); i++) {
			double x = Math.log(i)/Math.log(2);
			exp = (x%1 == 0) ? (int) x : (int) Math.ceil(x);
			Assert.assertEquals((int)Math.pow(2, exp), nextPowerOfTwo(i));
		}
	}
	
	static final String[] binary_reps = {
		"00000", "00001", "00010", "00011",
		"00100","00101","00110","00111",
		"01000", "01001", "01010", "01011",
		"01100","01101","01110","01111",
		"10000", "10001", "10010", "10011",
		"10100","10101","10110","10111",
		"11000", "11001", "11010", "11011",
		"11100","11101","11110","11111"
		};
	
	@Test
	public void test_to_binary_string(){
		System.out.println("Testing conversion from integer to binary string...");
		StringBuilder msbs_0_fill = new StringBuilder();
		for (int i  = 0; i < 32-binary_reps[0].length(); i ++) {
			msbs_0_fill.append(0);
		}
		for (int i  = 0; i < 16; i++) {
			String bin_rep = Utils.to_binary_string(i);
			System.out.println("\n"+i+":");
			System.out.println("Assumed:\t"+msbs_0_fill.toString()+binary_reps[i]);
			System.out.println("Calculated:\t"+bin_rep);
			Assert.assertEquals(msbs_0_fill.toString()+binary_reps[i], bin_rep);
		}
	}

}
