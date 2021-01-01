package com.lingpipe.cookbook.chapter6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;






import com.aliasi.spell.JaccardDistance;
import com.aliasi.tokenizer.CharacterTokenizerFactory;
import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class JaccardDistanceSample {

	
	public static void main(String[] args) throws IOException {
		
		TokenizerFactory indoEuropeanTf = IndoEuropeanTokenizerFactory.INSTANCE;
		TokenizerFactory characterTf = CharacterTokenizerFactory.INSTANCE;
		TokenizerFactory englishStopWordTf = new EnglishStopTokenizerFactory(indoEuropeanTf);
		
		JaccardDistance jaccardIndoEuropean = new JaccardDistance(indoEuropeanTf);
		JaccardDistance jaccardCharacter = new JaccardDistance(characterTf);
		JaccardDistance jaccardEnglishStopWord = new JaccardDistance(englishStopWordTf);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println("Enter the first string:");
			String text1 = reader.readLine();
			System.out.println("Enter the second string:");
			String text2 = reader.readLine();
			
			
			double jaccardIndoEuropeanDistance = jaccardIndoEuropean.distance(text1,text2);
			double jaccardCharacterDistance = jaccardCharacter.distance(text1,text2);
			double jaccardEnglishStopWordDistance = jaccardEnglishStopWord.distance(text1, text2);
			
			String indoEuropeanTokensT1 = join(indoEuropeanTf.tokenizer(text1.toCharArray(), 0, text1.length()).tokenize(),",");
			String indoEuropeanTokensT2 = join(indoEuropeanTf.tokenizer(text2.toCharArray(), 0, text2.length()).tokenize(),",");
			
			String characterTokensT1 = join(characterTf.tokenizer(text1.toCharArray(), 0, text1.length()).tokenize(),",");
			String characterTokensT2 = join(characterTf.tokenizer(text2.toCharArray(), 0, text2.length()).tokenize(),",");
			
			String englishStopWordTokensT1 = join(englishStopWordTf.tokenizer(text1.toCharArray(), 0, text1.length()).tokenize(),",");
			String englishStopWordTokensT2 = join(englishStopWordTf.tokenizer(text2.toCharArray(), 0, text2.length()).tokenize(),",");
			
			
			System.out.println("\nIndoEuropean Tokenizer");
			System.out.println("Text1 Tokens: {" + indoEuropeanTokensT1+"}");
			System.out.println("Text2 Tokens: {" + indoEuropeanTokensT2+"}");
			System.out.println("IndoEuropean Jaccard Distance is " + jaccardIndoEuropeanDistance);
			
			System.out.println("\nCharacter Tokenizer");
			System.out.println("Text1 Tokens: {" + characterTokensT1+"}");
			System.out.println("Text2 Tokens: {" + characterTokensT2+"}");
			System.out.println("Character Jaccard Distance between is " + jaccardCharacterDistance);
			
			System.out.println("\nEnglishStopWord Tokenizer");
			System.out.println("Text1 Tokens: {" + englishStopWordTokensT1+"}");
			System.out.println("Text2 Tokens: {" + englishStopWordTokensT2+"}");
			System.out.println("English Stopword Jaccard Distance between is " + jaccardEnglishStopWordDistance);
			System.out.println();
		}

	}

	private static String join(String[] tokens, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tokens.length -1; ++ i) {
			sb.append("'" + tokens[i] +  "'");
		}
		return sb.toString();	
	}

}
