package com.lingpipe.cookbook.chapter3;

import java.util.Map;

import com.aliasi.features.AddFeatureExtractor;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Counter;
import com.aliasi.util.FeatureExtractor;

public class CustomFeatureExtractor {
	
	public static void main(String[] args) {
		int min = 2;
		int max = 4;
		TokenizerFactory tokenizerFactory = new NGramTokenizerFactory(min,max);
		FeatureExtractor<CharSequence> tokenFeatures = new TokenFeatureExtractor(tokenizerFactory);
		FeatureExtractor<CharSequence> numberFeatures = new ContainsNumberFeatureExtractor();
		FeatureExtractor<CharSequence> joinedFeatureExtractors 
			= new AddFeatureExtractor<CharSequence>(tokenFeatures,numberFeatures);
		String input = "show me 1!";
		Map<String,? extends Number> features = joinedFeatureExtractors.features(input);
		System.out.println(features);
	}

}
