package com.lingpipe.cookbook.chapter4;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aliasi.classify.LMClassifier;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.lm.CompiledNGramBoundaryLM;
import com.aliasi.stats.MultivariateDistribution;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Strings;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class PosTagTweetsHMM {

	//language ID
	//postag with '_~_' 
	
	static int TEXT_OFFSET = 3;
	static String ENGLISH = "english";
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		@SuppressWarnings("unchecked")
		LMClassifier<CompiledNGramBoundaryLM, MultivariateDistribution> langClassifier 
			= (LMClassifier<CompiledNGramBoundaryLM, MultivariateDistribution>) 
				AbstractExternalizable.readObject(new File("models/3LangId.LMClassifier"));
		TokenizerFactory tokFactory = new RegExTokenizerFactory("[\\w@#$]+");
		HiddenMarkovModel hmm = (HiddenMarkovModel) AbstractExternalizable.readObject(new File("models/pos-en-general-brown.HiddenMarkovModel"));
		HmmDecoder decoder = new HmmDecoder(hmm);
		String inFile = args[0];
		CSVReader csvReader = new CSVReader(new FileReader(inFile));
		List<String[]> annotatedData = csvReader.readAll();
		Set<String> categories = new HashSet<String>();
		String outFile = args[1];
		FileOutputStream fileOut =  new FileOutputStream(outFile);
		OutputStreamWriter streamWriter = new OutputStreamWriter(fileOut,Strings.UTF8);
		//CsvListWriter writer = new CsvListWriter(streamWriter,CsvPreference.EXCEL_PREFERENCE); 
		for (String[] tweetData : annotatedData) {
			String tweet = tweetData[TEXT_OFFSET];
			if (!langClassifier.classify(tweet).bestCategory().equals(ENGLISH)) {
				continue;
			}
			List<String> tokens = Arrays.asList(tokFactory.tokenizer(tweet.toCharArray(), 0, tweet.length()).tokenize());
			Tagging<String> tagging = decoder.tag(tokens);
			for (int i = 0; i < tagging.size(); ++i) {
				//writer.write(new String[] {tagging.token(i),tagging.tag(i)});
			}
			//tweetData[TEXT_OFFSET] = tagging.toString();
			//writer.write(new String[] {"","EOT"});
			//System.out.println(tagging);
		}
		//writer.close();
	}
}
