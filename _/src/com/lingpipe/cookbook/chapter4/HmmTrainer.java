package com.lingpipe.cookbook.chapter4;

import com.aliasi.io.FileExtensionFilter;
import com.aliasi.corpus.Corpus;
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

public class HmmTrainer {

	static void addTagging(TokenizerFactory tokenizerFactory, List<Tagging<String>> taggingList, char[] text) {
		
		Tokenizer tokenizer = tokenizerFactory.tokenizer(text, 0, text.length);
		List<String> tokens = new ArrayList<String>();
		List<String> tags = new ArrayList<String>();
		boolean bosFound = false;
		for (String token : tokenizer.tokenize()) {
			if (token.equals("[")) {
				bosFound = true;
			}
			else if (token.equals("]")) {
				tags.set(tags.size() - 1,"EOS");
			}
			else {
				tokens.add(token);
				if (bosFound) {
					tags.add("BOS");
					bosFound = false;
				}
				else {
					tags.add("WORD");
				}
			}
		}
		if (tokens.size() > 0) {
			taggingList.add(new Tagging<String>(tokens,tags));
		}
	}
	
	public static void main(String[] args) throws IOException {
		String inputFile = args.length > 0 ? args[0] : "data/connecticut_yankee_EOS.txt";
		char[] text = Files.readCharsFromFile(new File(inputFile), Strings.UTF8);
		TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		
		/*List<Tagging<String>> taggingList = new ArrayList<Tagging<String>>();
		addTagging(tokenizerFactory,taggingList,text);
		ListCorpus<Tagging<String>> corpus
			= new ListCorpus<Tagging<String>> ();
		for (Tagging<String> tagging : taggingList) {
			System.out.println("Training " + tagging);
			corpus.addTrain(tagging);
		}
		*/
		
		  Corpus<ObjectHandler<Tagging<String>>> corpus = new TinyPosCorpus();
		 
		HmmCharLmEstimator estimator
			= new HmmCharLmEstimator();
		corpus.visitTrain(estimator);
		System.out.println("done training, token count: " + estimator.numTrainingTokens());
		HmmDecoder decoder = new HmmDecoder(estimator);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("Enter text followed by new line\n>");
			String evalText = reader.readLine();
			Tokenizer tokenizer = tokenizerFactory.tokenizer(evalText.toCharArray(),0,evalText.length());
			List<String> evalTokens = Arrays.asList(tokenizer.tokenize());
			Tagging<String> evalTagging = decoder.tag(evalTokens);
			System.out.println(evalTagging);
		}
	}
}
