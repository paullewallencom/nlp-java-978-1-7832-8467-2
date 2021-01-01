package com.lingpipe.cookbook.chapter2;

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

public class TestTokenizerFactory {

	static void checkTokens(TokenizerFactory tokFactory, String input, String[] correctTokens) {
		 Tokenizer tokenizer = tokFactory.tokenizer(input.toCharArray(),0,input.length());
			String[] tokens =	tokenizer.tokenize();
		if (tokens.length != correctTokens.length) {
			System.out.println("Token list lengths do not match");
			System.exit(-1);
		}
		for (int i = 0; i < tokens.length; ++i) {
			if (!correctTokens[i].equals(tokens[i])) {
				System.out.println("Token mismatch: got |" + tokens[i] + "|");
				System.out.println(" expected |" + correctTokens[i] + "|" );
				System.exit(-1);
			}
		}
	}
	
	static void checkTokensAndWhiteSpaces(TokenizerFactory tokFactory, String string, String[] correctTokens, String[] correctWhiteSpaces) {
		Tokenizer tokenizer = tokFactory.tokenizer(string.toCharArray(),0,string.length());
		String token = null;
		int index = -1;
		while ((token = tokenizer.nextToken()) != null) {
			String whiteSpace = tokenizer.nextWhitespace();
			++index;
			System.out.println(index + " " + token + "|" + whiteSpace + "|");
			if (index >= correctTokens.length ) {
				System.out.println("Token list lengths do not match");
				System.exit(-1);
			}
			if (!correctTokens[index].equals(token)) {
				System.out.println("Token mismatch: got |" + token + "|");
				System.out.println(" expected |" + correctTokens[index] + "|" );
				System.exit(-1);
			}
			if (index > correctWhiteSpaces.length ) {
				System.out.println("White space list lengths do not match");
				System.exit(-1);
			}
			if (!correctWhiteSpaces[index].equals(whiteSpace)) {
				System.out.println("White space mismatch: got |" + whiteSpace + "|");
				System.out.println(" expected |" + correctWhiteSpaces[index] + "|" );
				System.out.println("at index " + index);
				System.exit(-1);
			}	
		}
		
	}
	
	public static void main(String[] args) {
		String pattern = "[a-zA-Z]+|[0-9]+|\\S";
		TokenizerFactory tokFactory = new RegExTokenizerFactory(pattern);
		String[] tokens = {"Tokenizers","need","unit","tests","."};
		String text = "Tokenizers need unit tests.";
		checkTokens(tokFactory,text,tokens);
		String[] whiteSpaces = {" "," "," ","",""};
		checkTokensAndWhiteSpaces(tokFactory,text,tokens,whiteSpaces);
		System.out.println("All tests passed!");
	}
}
