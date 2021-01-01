package com.lingpipe.cookbook.chapter4;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.aliasi.classify.ConditionalClassification;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.ScoredTagging;
import com.aliasi.tag.TagLattice;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;


public class PosTagger {

	public static void main(String[] args) 
			throws ClassNotFoundException, IOException {
		TokenizerFactory tokFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		String hmmModelPath = args.length > 0 ? args[0] : "models/pos-en-general-brown.HiddenMarkovModel";
		HiddenMarkovModel hmm = (HiddenMarkovModel) AbstractExternalizable.readObject(new File(hmmModelPath));
		HmmDecoder decoder = new HmmDecoder(hmm);
		BufferedReader bufReader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("\n\nINPUT> ");
			System.out.flush();
			String input = bufReader.readLine();
			Tokenizer tokenizer = tokFactory.tokenizer(input.toCharArray(),0,input.length());
			String[] tokens = tokenizer.tokenize();
			List<String> tokenList = Arrays.asList(tokens);
			firstBest(tokenList,decoder);
		}
	}

	static void firstBest(List<String> tokenList, HmmDecoder decoder) {
		Tagging<String> tagging = decoder.tag(tokenList);
		System.out.println("\nFIRST BEST");
		for (int i = 0; i < tagging.size(); ++i)
			System.out.print(tagging.token(i) + "_" + tagging.tag(i) + " ");
		System.out.println();
	}

	/*static void nBest(List<String> tokenList, HmmDecoder decoder, int maxNBest) {
		System.out.println("\nN BEST");
		System.out.println("#   JointLogProb         Analysis");
		Iterator<ScoredTagging<String>> nBestIt = decoder.tagNBest(tokenList,maxNBest);
		for (int n = 0; n < maxNBest && nBestIt.hasNext(); ++n) {
			ScoredTagging<String> scoredTagging = nBestIt.next();
			double score = scoredTagging.score();
			System.out.print(n + "   " + format(score) + "  ");
			for (int i = 0; i < tokenList.size(); ++i)
				System.out.print(scoredTagging.token(i) + "_" + pad(scoredTagging.tag(i),5));
			System.out.println();
		}        
	}

	static void confidence(List<String> tokenList, HmmDecoder decoder) {
		System.out.println("\nCONFIDENCE");
		System.out.println("#   Token          (Prob:Tag)*");
		TagLattice<String> lattice = decoder.tagMarginal(tokenList);
		for (int tokenIndex = 0; tokenIndex < tokenList.size(); ++tokenIndex) {
			ConditionalClassification tagScores = lattice.tokenClassification(tokenIndex);
			System.out.print(pad(Integer.toString(tokenIndex),4));
			System.out.print(pad(tokenList.get(tokenIndex),15));
			for (int i = 0; i < 5; ++i) {
				double conditionalProb = tagScores.score(i);
				String tag = tagScores.category(i);
				System.out.print(" " + format(conditionalProb) 
						+ ":" + pad(tag,4));
			}
			System.out.println();
		}
	}

	static String format(double x) {
		return String.format("%9.3f",x);
	}

	static String pad(String in, int length) {
		if (in.length() > length) return in.substring(0,length-3) + "...";
		if (in.length() == length) return in;
		StringBuilder sb = new StringBuilder(length);
		sb.append(in);
		while (sb.length() < length) sb.append(' ');
		return sb.toString();

	}
*/
}
