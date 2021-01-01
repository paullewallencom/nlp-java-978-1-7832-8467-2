package com.lingpipe.cookbook.chapter6;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.aliasi.lm.NGramProcessLM;
import com.aliasi.spell.CompiledSpellChecker;
import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;

public class CaseRestore {

	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		int NGRAM_LENGTH = 5;
		NGramProcessLM lm = new NGramProcessLM(NGRAM_LENGTH);
		
	    TrainSpellChecker sc = new TrainSpellChecker(lm,CompiledSpellChecker.CASE_RESTORING);
	    
	    String bigEnglish = Files.readFromFile(new File("data/project_gutenberg_books.txt"), Strings.UTF8);
	    sc.handle(bigEnglish);
	    CompiledSpellChecker csc = (CompiledSpellChecker) AbstractExternalizable.compile(sc);
	    
	    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	    String query = "";
	    while (true) {
			System.out.println("Enter input, . to quit:");
			query = reader.readLine();
			if(query.equals(".")){
				break;
			}
			
            String bestAlternative = csc.didYouMean(query);
            System.out.println("Query Text: " + query);
            System.out.println("Best Alternative: " + bestAlternative);
	    }
	}

}
