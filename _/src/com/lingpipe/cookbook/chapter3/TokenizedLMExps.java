package com.lingpipe.cookbook.chapter3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import com.aliasi.lm.UniformBoundaryLM;
import com.aliasi.stats.MultivariateDistribution;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ScoredObject;

public class TokenizedLMExps {

	static int TEXT_INDEX = 3;

	static void dumpProbs(String[] tokens, TokenizedLM lm) {
        System.out.println("TOKENS: " + java.util.Arrays.asList(tokens));
        System.out.println("lm.tokenProbability(): "
                           + lm.tokenProbability(tokens,0,tokens.length));
       /* System.out.println("lm.tokenProbCharSmooth(): "
                           + lm.tokenProbCharSmooth(tokens,0,tokens.length));
        System.out.println("lm.tokenProbCharSmoothNoBound(): "
                           + lm.tokenProbCharSmoothNoBounds(tokens,0,tokens.length));
                           */
        System.out.println("lm.processLog2Probability(): "
                + lm.processLog2Probability(tokens));
        System.out.println();
    }
	
	public static void testBackoff() {
        TokenizerFactory tokenizerFactory 
        = IndoEuropeanTokenizerFactory.INSTANCE;
    int nGramOrder = 3;
    NGramBoundaryLM unknownTokenModel = new NGramBoundaryLM(3);
    NGramBoundaryLM  whitespaceModel = new NGramBoundaryLM(3);
    double lambdaFactor = 1.0;
    TokenizedLM lm 
        = new TokenizedLM(tokenizerFactory, 
                          nGramOrder,
                          unknownTokenModel, whitespaceModel, 
                          lambdaFactor);
    TokenizedLM lm2 
    	= new TokenizedLM(tokenizerFactory, nGramOrder);
    String training = "ab abc ab bd";
    lm.train(training);
    lm2.train(training);
    dumpProbs(new String[] {"b"},lm);
    dumpProbs(new String[] {"b"},lm2);
    }
	
	public static void main(String[] args) throws IOException {
		CSVReader csvReader = null;
		List<String[]> lines = null;
		csvReader = new CSVReader(new FileReader(args[0]));		
		lines = csvReader.readAll();
		int nGramOrder = 1;
		UniformBoundaryLM uniLM = new UniformBoundaryLM(0);
		
		TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		
		NGramBoundaryLM unknownWhiteSpaceModel = new NGramBoundaryLM(3);
		NGramBoundaryLM unknownTokenModel = new NGramBoundaryLM(3);
		TokenizedLM bgs 
			= new TokenizedLM(tokenizerFactory,nGramOrder);
		TokenizedLM backgroundLanguageModel 
		    = new TokenizedLM(tokenizerFactory, nGramOrder, 
		    		unknownTokenModel, unknownWhiteSpaceModel, 1.0);
		
		 NGramBoundaryLM unknownTokenModel2 = new NGramBoundaryLM(3);
		    NGramBoundaryLM  whitespaceModel = new NGramBoundaryLM(3);
		    double lambdaFactor = 1.0;
		    TokenizedLM lm 
		        = new TokenizedLM(tokenizerFactory, 
		                          nGramOrder,
		                          unknownTokenModel2, whitespaceModel, 
		                          lambdaFactor);
		
		String training = "ab abc ab bd";
	    lm.train(training);
		for (String [] line: lines) {
			//backgroundLanguageModel.train(line[TEXT_INDEX]);
			//bgs.train(line[TEXT_INDEX]);
		}
		backgroundLanguageModel.train("ab abc ab bd");
		bgs.train("ab abc ab bd");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			StringBuilder sb = new StringBuilder();
			System.out.print("Enter Phrase to score:");
			String string = reader.readLine();
			String[] tokens = tokenizerFactory.tokenizer(string.toCharArray(), 0, string.length()).tokenize();
			double bgToks = backgroundLanguageModel.tokenProbability(tokens, 0, tokens.length);
			double bgScore = backgroundLanguageModel.log2Estimate(string);
			double bgSimpleToks = bgs.tokenProbability(tokens,0,tokens.length);
			double bgSimpleScore = bgs.log2Estimate(string);
			sb.append("Complex:" + bgScore + " " + bgToks);
			sb.append("Simple: " + bgSimpleScore + " " + bgSimpleToks);
			//sb.append("Uniform:" + uniLM.log2Estimate(string) + " ");
			System.out.println(sb);
			//dumpProbs(new String[] {"b"},bgs);
			//dumpProbs(new String[] {"b"},backgroundLanguageModel);
			testBackoff();
		}
		
		
		
	}
	
}


