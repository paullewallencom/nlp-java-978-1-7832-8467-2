package com.lingpipe.cookbook.chapter4;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.SortedSet;

import au.com.bytecode.opencsv.CSVReader;

import com.aliasi.lm.TokenizedLM;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.util.ScoredObject;
import com.lingpipe.cookbook.Util;

public class InterestingPhrases {
	static int TEXT_INDEX = 3;
	public static void main(String[] args) throws IOException {
		String inputCsv = args.length > 0 ? args[0] : "data/disney.csv";	
		List<String[]> lines = Util.readCsv(new File(inputCsv));
		int ngramSize = 3;
		TokenizedLM languageModel 
			= new TokenizedLM(IndoEuropeanTokenizerFactory.INSTANCE,ngramSize);
		for (String [] line: lines) {
			languageModel.train(line[TEXT_INDEX]);
		}
		int phraseLength = 3;
		int minCount = 2;
		int maxReturned = 100;
		SortedSet<ScoredObject<String[]>> collocations 
			= languageModel.collocationSet(phraseLength, minCount, maxReturned);
		for (ScoredObject<String[]> scoredTokens : collocations) {
			double score = scoredTokens.score();
			StringBuilder sb = new StringBuilder();
			for (String token : scoredTokens.getObject()) {
				sb.append(token + " ");
			}
			System.out.printf("Score %.1f : ", score);
			System.out.println(sb);
		}
		
	}
}
