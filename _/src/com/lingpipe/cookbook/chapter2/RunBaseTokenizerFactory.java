package com.lingpipe.cookbook.chapter2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

public class RunBaseTokenizerFactory {
	public static void main(String[] args) throws IOException {
		TokenizerFactory tokFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println("type a sentence to see to see tokens and white spaces");
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
