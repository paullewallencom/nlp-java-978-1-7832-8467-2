package com.lingpipe.cookbook.chapter3;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;

public class EditDistance {

	public static void main(String[] args) throws IOException {
		//String text = Files.readFromFile(new File(args[0]), Strings.UTF8);
		String text = "Hello my name is Foobar";
		String[] tokens = IndoEuropeanTokenizerFactory.INSTANCE.tokenizer(text.toCharArray(), 0, text.length()).tokenize();
		Set<String> tokenSet  = new HashSet<String>();
		for (String token : tokens) {
			tokenSet.add(token);
		}
		double matchWeight = 0;
		double deleteWeight = -1;
		double insertWeight = -1;
		double substituteWeight = -1;
		double transposeWeight = -1;
		FixedWeightEditDistance editDistance 
			= new FixedWeightEditDistance(matchWeight,deleteWeight,insertWeight,substituteWeight,transposeWeight);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println("enter a token to compare:");
			String inputToken = reader.readLine();
			for (String token : tokenSet) {
				double proximity = editDistance.proximity(token, inputToken);
				//if (proximity >= -1.0d) {
					System.out.printf("Proximity is %.2f between " + token + " and " + inputToken + "\n",proximity);
				//}
			}
		}
	}
}
