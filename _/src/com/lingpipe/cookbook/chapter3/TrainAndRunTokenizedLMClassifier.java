package com.lingpipe.cookbook.chapter3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.lm.NGramBoundaryLM;
import com.aliasi.lm.TokenizedLM;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.lingpipe.cookbook.Util;

import au.com.bytecode.opencsv.CSVReader;


public class TrainAndRunTokenizedLMClassifier {
	
	public static void main(String[] args) throws IOException {
		String dataPath = args.length > 0 ? args[0] : "data/disney_e_n.csv";
		List<String[]> annotatedData = Util.readAnnotatedCsvRemoveHeader(new File(dataPath));
		String[] categories = Util.getCategories(annotatedData);
		int maxTokenNGram = 2;
		TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		DynamicLMClassifier<TokenizedLM> classifier 
			= DynamicLMClassifier.createTokenized(categories,tokenizerFactory,maxTokenNGram);

		for (String[] row: annotatedData) {
			String truth = row[Util.ANNOTATION_OFFSET];
			String text = row[Util.TEXT_OFFSET];
			Classification classification = new Classification(truth);
			Classified<CharSequence> classified = new Classified<CharSequence>(text,classification);
			classifier.handle(classified);
		}
		Util.consoleInputPrintClassification(classifier);
	}
}



