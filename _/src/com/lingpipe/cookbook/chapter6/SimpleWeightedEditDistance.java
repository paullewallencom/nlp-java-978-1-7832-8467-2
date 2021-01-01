package com.lingpipe.cookbook.chapter6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.aliasi.spell.EditDistance;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.WeightedEditDistance;

public class SimpleWeightedEditDistance {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String text1 = "";
		String text2 = "";
		EditDistance dmAllowTrans = new EditDistance(true);
		EditDistance dmNoTrans = new EditDistance(false);
		
		double matchWeight = 0;
		double deleteWeight = -2;
		double insertWeight = -2;
		double substituteWeight = -2;
		double transposeWeight = Double.NEGATIVE_INFINITY;
		WeightedEditDistance wed = new FixedWeightEditDistance(matchWeight,deleteWeight,insertWeight,substituteWeight,transposeWeight);
		System.out.println("Fixed Weight Edit Distance: "+ wed.toString());
		CustomWeightedEditDistance cwed = new CustomWeightedEditDistance();
		System.out.println("Custom Weight Edit Distance: "+ cwed.toString());
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		//reader.markSupported();
		//reader.mark(0);
		while (true) {
			System.out.println("Enter the first string:");
			text1 = reader.readLine();
			//reader.reset();
			System.out.println("Enter the second string:");
			text2 = reader.readLine();
			//reader.reset();
			double allowTransDistance = dmAllowTrans.distance(text1, text2);
			double noTransDistance = dmNoTrans.distance(text1, text2);
			double wedDistance = wed.distance(text1, text2);
			double cwedDistance = cwed.distance(text1, text2);
			
			
			System.out.println("Allowing Transposition Distance between: " + text1 + " and " + text2 + " is " + allowTransDistance);
			System.out.println("No Transposition Distance between: " + text1 + " and " + text2 + " is " + noTransDistance);
			System.out.println("Fixed Weight Edit Distance between: " + text1 + " and " + text2 + " is " + wedDistance);
			System.out.println("Custom Weight Edit Distance between: " + text1 + " and " + text2 + " is " + cwedDistance);
		}
	}
	
	public static class CustomWeightedEditDistance extends WeightedEditDistance{

		@Override
		public double deleteWeight(char arg0) {
			return (Character.isDigit(arg0)||Character.isLetter(arg0)) ? -1 : 0;
			
		}

		@Override
		public double insertWeight(char arg0) {
			return deleteWeight(arg0);
		}

		@Override
		public double matchWeight(char arg0) {
			return 0;
		}

		@Override
		public double substituteWeight(char cDeleted, char cInserted) {
			return Character.toLowerCase(cDeleted) == Character.toLowerCase(cInserted) ? 0 :-1;
			
		}

		@Override
		public double transposeWeight(char arg0, char arg1) {
			return Double.NEGATIVE_INFINITY;
		}
		
		
	}

}
