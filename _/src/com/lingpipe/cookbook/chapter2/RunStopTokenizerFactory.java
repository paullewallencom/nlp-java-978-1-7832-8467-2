package com.lingpipe.cookbook.chapter2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.StopTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

public class RunStopTokenizerFactory {

	
	public static void main(String[] args) throws IOException {
		TokenizerFactory tokFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		tokFactory = new LowerCaseTokenizerFactory(tokFactory);
		Set<String> stopWords = new HashSet<String>();
		stopWords.add("the");
		stopWords.add("of");
		stopWords.add("to");
		stopWords.add("is");
		
		tokFactory = new StopTokenizerFactory(tokFactory, stopWords);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println("type a sentence below to see the tokens and white spaces:");
			String input = reader.readLine();
			Tokenizer tokenizer = tokFactory.tokenizer(input.toCharArray(), 0, input.length());
			String token = null;
			while ((token = tokenizer.nextToken()) != null) {
				System.out.println("Token:'" + token + "'");
				System.out.println("WhiteSpace:'" + tokenizer.nextWhitespace() + "'");
			}
		}
	}
}
