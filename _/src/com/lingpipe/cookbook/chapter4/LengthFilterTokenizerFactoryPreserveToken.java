package com.lingpipe.cookbook.chapter4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

public class LengthFilterTokenizerFactoryPreserveToken extends ModifyTokenTokenizerFactory{

	int mMinLength;
	int mCounter = 0;
	
	public LengthFilterTokenizerFactoryPreserveToken(TokenizerFactory factory,int minLength) {
		super(factory);
		mMinLength = minLength;
	}
	
	public String modifyToken(String token) {
		if (token.length() < 5) {
			++mCounter;
			return "_" + mCounter;
		}
		return token;
	}
	
	private static final long serialVersionUID = -8346896300824818296L;
	
	public static void main(String[] args) throws IOException {
		TokenizerFactory tf = new IndoEuropeanTokenizerFactory();
		tf = new LengthFilterTokenizerFactoryPreserveToken(tf, 5);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println("type a sentence below to see the tokens and white spaces:");
			String input = reader.readLine();
			Tokenizer tokenizer = tf.tokenizer(input.toCharArray(), 0, input.length());
			String token = null;
			StringBuilder sb = new StringBuilder();
			while ((token = tokenizer.nextToken()) != null) {
				String ws = tokenizer.nextWhitespace();
				System.out.println("Token:'" + token + "'");
				System.out.println("WhiteSpace:'" + ws + "'");
				sb.append(token);
				sb.append(ws);
			}
			System.out.println("Modified Output: " + sb.toString());
		}
	}
	}
	

