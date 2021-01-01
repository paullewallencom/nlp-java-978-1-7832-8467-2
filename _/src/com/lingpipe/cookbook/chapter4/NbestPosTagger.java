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


public class NbestPosTagger {

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
			nBest(tokenList,decoder,5);
		}
	}


	static void nBest(List<String> tokenList, HmmDecoder decoder, int maxNBest) {
		System.out.println("\nN BEST");
		System.out.println("#   JointLogProb         Analysis");
		Iterator<ScoredTagging<String>> nBestIt = decoder.tagNBest(tokenList,maxNBest);
		for (int n = 0; nBestIt.hasNext(); ++n) {
			ScoredTagging<String> scoredTagging = nBestIt.next();
			System.out.printf(n + "   %9.3f  ",scoredTagging.score());
			for (int i = 0; i < tokenList.size(); ++i) {
				System.out.print(scoredTagging.token(i) + "_" + pad(scoredTagging.tag(i),5));
			}
			System.out.println();
		}        
	}

	static String pad(String in, int length) {
		if (in.length() > length) return in.substring(0,length-3) + "...";
		if (in.length() == length) return in;
		StringBuilder sb = new StringBuilder(length);
		sb.append(in);
		while (sb.length() < length) sb.append(' ');
		return sb.toString();

	}

}
