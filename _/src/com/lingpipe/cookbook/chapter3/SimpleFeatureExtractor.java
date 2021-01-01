package com.lingpipe.cookbook.chapter3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.FeatureExtractor;

public class SimpleFeatureExtractor {

	public static void main(String[] args) throws IOException {
		TokenizerFactory tokFact = IndoEuropeanTokenizerFactory.INSTANCE;
		FeatureExtractor<CharSequence> tokenFeatureExtractor = new TokenFeatureExtractor(tokFact);
		BufferedReader reader = new BufferedReader(new 	InputStreamReader(System.in));
		while (true) {
			System.out.println("\nType a string to see its features");
			String text = reader.readLine();
			Map<String, ? extends Number > features = tokenFeatureExtractor.features(text);
			System.out.println(features);
		}
	}
	
}
