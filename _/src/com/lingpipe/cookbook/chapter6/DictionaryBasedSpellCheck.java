package com.lingpipe.cookbook.chapter6;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aliasi.io.FileLineReader;
import com.aliasi.spell.EditDistance;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Files;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.Strings;

public class DictionaryBasedSpellCheck {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		double MATCH_WEIGHT = -0.0;
	    double DELETE_WEIGHT = -3.0;
	    double INSERT_WEIGHT = -3.0;
	    double SUBSTITUTE_WEIGHT = -2.0;
	    double TRANSPOSE_WEIGHT = -1.0;
	    
	    FixedWeightEditDistance fixedEdit =
	            new FixedWeightEditDistance(MATCH_WEIGHT,
	                                        DELETE_WEIGHT,
	                                        INSERT_WEIGHT,
	                                        SUBSTITUTE_WEIGHT,
	                                        TRANSPOSE_WEIGHT);
	    EditDistance simpleEdit = new EditDistance(true);
	    TokenizerFactory tokenizerFactory = new com.aliasi.tokenizer.LowerCaseTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE);
	    List<String> dictLines = FileLineReader.readLines(new File("data/websters_words.txt"), Strings.UTF8);
	    Set<String> dictSet = new HashSet<String>(dictLines);
	    
	    //ObjectToDoubleMap<String> closestDictionaryWord = new ObjectToDoubleMap<String>();
	    
	    String input = "i want to acheive good skillz in lingpipe";
	    
	    String [] tokens = tokenizerFactory.tokenizer(input.toCharArray(), 0, input.length()).tokenize();
	    
	    for(String tok: tokens){
	    	ObjectToDoubleMap<String> closestDictionaryWord = new ObjectToDoubleMap<String>();
		    for(String word: dictSet){
		    	double proximity = fixedEdit.proximity(tok, word);
		    	closestDictionaryWord.put(word, proximity);
		    }
		    
		    int count = 0;
		    for(String word: closestDictionaryWord.keysOrderedByValueList()){
		    	if(count++ >= 1) break;
		    	System.out.println("Tok: " + tok + " Replacement: " + word + " Proximity: " + closestDictionaryWord.getValue(word) );
		    	//System.out.println("Distance: " + fixedEdit.distance(input, word));
		    }
	    }
	    

	}

}
