package com.lingpipe.cookbook.chapter6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.aliasi.spell.EditDistance;

public class SimpleEditDistance {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
	
		EditDistance dmAllowTrans = new EditDistance(true);
		EditDistance dmNoTrans = new EditDistance(false);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println("Enter the first string:");
			String text1 = reader.readLine();
			System.out.println("Enter the second string:");
			String text2 = reader.readLine();
			double allowTransDist = dmAllowTrans.distance(text1, text2);
			double noTransDist = dmNoTrans.distance(text1, text2);
			System.out.println("Allowing Transposition Distance between: " + text1 + " and " + text2 + " is " + allowTransDist);
			System.out.println("No Transposition Distance between: " + text1 + " and " + text2 + " is " + noTransDist);
		}
	}

}
