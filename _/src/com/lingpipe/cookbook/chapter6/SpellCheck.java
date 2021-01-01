package com.lingpipe.cookbook.chapter6;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import com.aliasi.lm.NGramProcessLM;
import com.aliasi.spell.CompiledSpellChecker;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.SpellChecker;
import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;

public class SpellCheck {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		double matchWeight = -0.0;
	    double deleteWeight = -4.0;
	    double insertWeight = -2.5;
	    double substituteWeight = -2.5;
	    double transposeWeight = -1.0;
	    
	    FixedWeightEditDistance fixedEdit =
	            new FixedWeightEditDistance(matchWeight,
	                                        deleteWeight,
	                                        insertWeight,
	                                        substituteWeight,
	                                        transposeWeight);
	    int NGRAM_LENGTH = 6;
	    NGramProcessLM lm = new NGramProcessLM(NGRAM_LENGTH);
	    
	    TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
	    tokenizerFactory
	    	= new com.aliasi.tokenizer.LowerCaseTokenizerFactory(tokenizerFactory);
	    
	    TrainSpellChecker sc = new TrainSpellChecker(lm,fixedEdit,tokenizerFactory);
	    File inFile = new File("data/project_gutenberg_books.txt");
	    String bigEnglish = Files.readFromFile(inFile, Strings.UTF8);
	    sc.handle(bigEnglish);
	    File dict = new File("data/websters_words.txt");
	    String webster = Files.readFromFile(dict, Strings.UTF8);
	    sc.handle(webster);
	    CompiledSpellChecker csc = (CompiledSpellChecker) AbstractExternalizable.compile(sc);
	    Set<String> dontEdit = new HashSet<String>();
	    dontEdit.add("lingpipe");
	    csc.setDoNotEditTokens(dontEdit);
	    
	    csc.setTokenizerFactory(tokenizerFactory);
	    
	    int nBest = 3;
	    csc.setNBest(64);
	    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	    String query = "";
	    while (true) {
			System.out.println("Enter word, . to quit:");
			query = reader.readLine();
			if(query.equals(".")){
				break;
			}
            String bestAlternative = csc.didYouMean(query);
            System.out.println("Best Alternative: " + bestAlternative);
            int i = 0;
            Iterator<ScoredObject<String>> iterator = csc.didYouMeanNBest(query);
            while (i < nBest) {
            		ScoredObject<String> so = iterator.next();
            		System.out.println("Nbest: " + i + ": " + so.getObject() + " Score:" + so.score());
            		i++;
            }
	    }
	}
}
