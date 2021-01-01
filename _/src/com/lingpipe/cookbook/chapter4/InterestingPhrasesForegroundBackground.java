package com.lingpipe.cookbook.chapter4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.SortedSet;

import au.com.bytecode.opencsv.CSVReader;

import com.aliasi.classify.LMClassifier;
import com.aliasi.lm.CompiledNGramBoundaryLM;
import com.aliasi.lm.LanguageModel;
import com.aliasi.lm.LanguageModel.Sequence;
import com.aliasi.lm.NGramBoundaryLM;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.lm.TokenizedLM;
import com.aliasi.stats.MultivariateDistribution;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.TokenLengthTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ScoredObject;
import com.lingpipe.cookbook.Util;

public class InterestingPhrasesForegroundBackground {
	
	public static void main(String[] args) throws IOException {
		String backgroundCsv = args.length > 0 ? args[0] : "data/disneyWorld.csv";	
		List<String[]> backgroundData = Util.readCsv(new File(backgroundCsv));
		String foregroundCsv = args.length > 1 ? args[1] : "data/disneyLand.csv";	
		List<String[]> foregroundData = Util.readCsv(new File(foregroundCsv));
		
		TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		tokenizerFactory = new LowerCaseTokenizerFactory(tokenizerFactory);		
		int minLength = 5;
		tokenizerFactory = new LengthFilterTokenizerFactoryPreserveToken(tokenizerFactory,minLength);
		int nGramOrder = 3;
		TokenizedLM backgroundLanguageModel 
		 	= new TokenizedLM(tokenizerFactory, nGramOrder);
		for (String [] line: backgroundData) {
			backgroundLanguageModel.train(line[Util.TEXT_OFFSET]);
		}		
		
		TokenizedLM foregroundLanguageModel = new TokenizedLM(tokenizerFactory,nGramOrder);
		for (String [] line: foregroundData) {
			foregroundLanguageModel.train(line[Util.TEXT_OFFSET]);
		}
		int phraseSize = 2;
		int minCount = 3;
		int maxReturned = 100;
		SortedSet<ScoredObject<String[]>> suprisinglyNewPhrases
			= foregroundLanguageModel.newTermSet(phraseSize, minCount, maxReturned,backgroundLanguageModel);
		for (ScoredObject<String[]> scoredTokens : suprisinglyNewPhrases) {
			double score = scoredTokens.score();
			String[] tokens = scoredTokens.getObject();
			System.out.printf("Score %f : ", score);
			System.out.println(java.util.Arrays.asList(tokens));
		}
	}
}


