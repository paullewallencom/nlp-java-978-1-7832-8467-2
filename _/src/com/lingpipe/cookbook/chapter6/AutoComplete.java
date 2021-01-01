package com.lingpipe.cookbook.chapter6;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.SortedSet;

import com.aliasi.io.FileLineReader;
import com.aliasi.spell.AutoCompleter;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ScoredObject;

public class AutoComplete {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws UnsupportedEncodingException, IOException {
		File wordsFile = new File("data/city_populations_2012.csv");
	    String[] lines = FileLineReader.readLineArray(wordsFile,"ISO-8859-1");
	    ObjectToCounterMap<String> cityPopMap = new ObjectToCounterMap<String>();
	    int lineCount = 0;
	    for (String line : lines) {
	    	if(lineCount++ <1) continue;
	        int i = line.lastIndexOf(',');
	        if (i < 0) continue;
	        String phrase = line.substring(0,i);
	        String countString = line.substring(i+1);
	        Integer count = Integer.valueOf(countString);
	        System.out.println("Phrase: " + phrase + " Count: " + count);
	        cityPopMap.set(phrase,count);
	    }
	    double matchWeight = 0.0;
	    double insertWeight = -10.0;
	    double substituteWeight = -10.0;
	    double deleteWeight = -10.0;
	    double transposeWeight = Double.NEGATIVE_INFINITY;
	    FixedWeightEditDistance editDistance
	        = new FixedWeightEditDistance(matchWeight,
	                                      deleteWeight,
	                                      insertWeight,
	                                      substituteWeight,
	                                      transposeWeight);
	    int maxResults = 5;
	    int maxQueueSize = 10000;
	    double minScore = -25.0;
	    AutoCompleter completer
	        = new AutoCompleter(cityPopMap, editDistance,
	                            maxResults, maxQueueSize, minScore);
	    
	    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	    String query = "";
	    while (true) {
			System.out.println("Enter word, . to quit:");
			query = reader.readLine();
			if(query.equals(".")){
				break;
			}
			SortedSet<ScoredObject<String>> completions = completer.complete(query);
			System.out.println("\n|" + query + "|");
	        for (ScoredObject<String> so : completions)
	            System.out.printf("%6.2f %s\n", so.score(), so.getObject());
	    }
	}

}
