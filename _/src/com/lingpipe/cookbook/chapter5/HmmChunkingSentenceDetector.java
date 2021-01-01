package com.lingpipe.cookbook.chapter5;


import com.aliasi.io.FileExtensionFilter;

import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.HmmChunker;
import com.aliasi.corpus.ListCorpus;
import com.aliasi.corpus.Parser;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XValidatingObjectCorpus;

import com.aliasi.hmm.HmmCharLmEstimator;
import com.aliasi.hmm.HmmDecoder;

import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HmmChunkingSentenceDetector {

	static Tagging<String> getTagging(TokenizerFactory tokenizerFactory, char[] text) {
		Tokenizer tokenizer = tokenizerFactory.tokenizer(text, 0, text.length);
		List<String> tokens = new ArrayList<String>();
		List<String> tags = new ArrayList<String>();
		String token = null;
		boolean bosFound = false;
		while ((token = tokenizer.nextToken()) != null) {
			if (token.equals("[")) {
				bosFound = true;
			}
			else if (token.equals("]")) {
				tags.set(tags.size() - 1,"E_S");
			}
			else {
				tokens.add(token);
				if (bosFound) {
					tags.add("B_S");
					bosFound = false;
				}
				else {
					tags.add("M_S");
				}
			}
		}
		return new Tagging<String>(tokens,tags);
	}
	
	public static void main(String[] args) throws IOException {
		char[] text = Files.readCharsFromFile(new File(args[0]), Strings.UTF8);
		TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		Tagging<String> tagging = getTagging(tokenizerFactory,text);
		System.out.println("Training Tagging " + tagging);		
		ListCorpus<Tagging<String>> corpus
			= new ListCorpus<Tagging<String>> ();
		corpus.addTrain(tagging);
		HmmCharLmEstimator estimator
			= new HmmCharLmEstimator();
		corpus.visitTrain(estimator);
		System.out.println("done training " + estimator.numTrainingTokens());
		HmmDecoder decoder = new HmmDecoder(estimator);
		HmmChunker chunker = new HmmChunker(tokenizerFactory, decoder);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("Enter text followed by new line\n>");
			String evalText = reader.readLine();
			List<String> evalTokens = Arrays.asList(tokenizerFactory.tokenizer(evalText.toCharArray(),0,evalText.length()).tokenize());
			Tagging<String> evalTagging = decoder.tag(evalTokens);
			System.out.println(evalTagging);
			
			Chunking chunking = chunker.chunk(evalText);
			System.out.println("Chunking: " + chunking);
		}
	}
}
