package com.lingpipe.cookbook.chapter2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

public class Rot13TokenizerFactory extends ModifyTokenTokenizerFactory{

	
	
	private static final long serialVersionUID = -3200042384201663330L;

	
	public Rot13TokenizerFactory(TokenizerFactory f) {
        super(f);
    }
	
	@Override
    public String modifyToken(String tok) {
        return rot13(tok);
    }

    public static String rot13(String input) {
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < input.length(); i++) {
    		char c = input.charAt(i);
    		if       (c >= 'a' && c <= 'm') c += 13;
    		else if  (c >= 'A' && c <= 'M') c += 13;
    		else if  (c >= 'n' && c <= 'z') c -= 13;
    		else if  (c >= 'N' && c <= 'Z') c -= 13;
    		sb.append(c);
    	}
    	return sb.toString();
    }


	public static void main(String[] args) throws IOException {
		
		TokenizerFactory tokFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		tokFactory = new LowerCaseTokenizerFactory(tokFactory);
		tokFactory = new Rot13TokenizerFactory(tokFactory);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println("type a sentence below to see the tokens and white spaces:");
			String input = reader.readLine();
			Tokenizer tokenizer = tokFactory.tokenizer(input.toCharArray(), 0, input.length());
			String token = null;
			StringBuilder sb = new StringBuilder();
			while ((token = tokenizer.nextToken()) != null) {
				String ws = tokenizer.nextWhitespace();
				System.out.println("Token:'" + token + "'");
				sb.append(token);
				sb.append(ws);
			}
			System.out.println("Modified Output: " + sb.toString());
		}

	}

}
