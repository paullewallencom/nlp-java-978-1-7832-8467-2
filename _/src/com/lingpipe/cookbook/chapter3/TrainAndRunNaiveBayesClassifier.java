package com.lingpipe.cookbook.chapter3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.aliasi.classify.Classification;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.NaiveBayesClassifier;
import com.aliasi.classify.TradNaiveBayesClassifier;
import com.aliasi.lm.NGramBoundaryLM;
import com.aliasi.lm.TokenizedLM;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.lingpipe.cookbook.Util;

import au.com.bytecode.opencsv.CSVReader;


public class TrainAndRunNaiveBayesClassifier {
	static int ANNOTATION_OFFSET = 2;
	static int TEXT_OFFSET = 3;

	public static void main(String[] args) throws IOException {
		String inFile = args.length > 0 ? args[0] : "data/hotcold.csv";
		CSVReader csvReader = new CSVReader(new FileReader(inFile));
		List<String[]> annotatedData = csvReader.readAll();
		Set<String> categories = new TreeSet<String>();
		for (String[] tweetData : annotatedData) {
			if (!tweetData[ANNOTATION_OFFSET].equals("")) {
				categories.add(tweetData[ANNOTATION_OFFSET]);
			}
		}
		TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		String[] categoriesList = categories.toArray(new String[0]);
		double categoryPrior = 1.0;
		double tokenInCategoryPrior = 1.0;
		double lengthNorm = Double.NaN;
		TradNaiveBayesClassifier classifier 
			= new TradNaiveBayesClassifier(categories,tokenizerFactory,categoryPrior,tokenInCategoryPrior,lengthNorm);
		for (String[] tweetData : annotatedData) {
			if (!tweetData[ANNOTATION_OFFSET].equals("")) {
				int count = 1;
				classifier.train(tweetData[TEXT_OFFSET],
						new Classification(tweetData[ANNOTATION_OFFSET]),  			
						count);
			}
		}

		System.out.println(classifier);
		BufferedReader reader = new BufferedReader(new 	InputStreamReader(System.in));
		while (true) {
			System.out.println("\nType a string to be classified");
			String data = reader.readLine();
			ConditionalClassification classification 
			= classifier.classify(data);
			for (int i = 0; i < categoriesList.length; ++i) {
				System.out.format(classification.category(i) 
						+ " %.2f 	%n",classification.conditionalProbability(i));
			}
		}
	}

}

