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

import au.com.bytecode.opencsv.CSVReader;


public class StoopidBreck {
	TokenizerFactory mTokFact;
	String[] mCategories;
	
	public StoopidBreck(String[] categories, TokenizerFactory tokFact) {
		mTokFact = tokFact;
		mCategories = categories;
	}

	public void train(String text, Classification classification ) {
		String[] tokens = mTokFact.tokenizer(text.toCharArray(), 0, text.length()).tokenize();
		String bestCategory = classification.bestCategory();
		
		
	}

	public ConditionalClassification classify(String data) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
