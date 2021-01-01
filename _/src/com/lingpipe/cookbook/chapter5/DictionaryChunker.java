package com.lingpipe.cookbook.chapter5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

public class DictionaryChunker {

	/**
	 * @param args
	 */
	static final double CHUNK_SCORE = 1.0;
	public static void main(String[] args) throws IOException {
		MapDictionary<String> dictionary = new MapDictionary<String>();
		dictionary.addEntry(new DictionaryEntry<String>("Arthur","PERSON",CHUNK_SCORE));
		dictionary.addEntry(new DictionaryEntry<String>("Ford","PERSON",CHUNK_SCORE));
		dictionary.addEntry(new DictionaryEntry<String>("Trillian","PERSON",CHUNK_SCORE));
		dictionary.addEntry(new DictionaryEntry<String>("Zaphod","PERSON",CHUNK_SCORE));

		dictionary.addEntry(new DictionaryEntry<String>("Marvin","ROBOT",CHUNK_SCORE));
		dictionary.addEntry(new DictionaryEntry<String>("Heart of Gold","SPACECRAFT",CHUNK_SCORE));
		dictionary.addEntry(new DictionaryEntry<String>("Hitchhikers Guide","PRODUCT",CHUNK_SCORE));
		dictionary.addEntry(new DictionaryEntry<String>("Heart","ORGAN",CHUNK_SCORE));
		boolean returnAllMatches = true;
		boolean caseSensitive = true;
		ExactDictionaryChunker dictionaryChunker
		= new ExactDictionaryChunker(dictionary,
				IndoEuropeanTokenizerFactory.INSTANCE,
				returnAllMatches,caseSensitive);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	    String text = "";
	    while (true) {
	    	System.out.println("Enter text, . to quit:");
	    	text = reader.readLine();
	    	if(text.equals(".")){
	    		break;
	    	}
	    	System.out.println("\nCHUNKER overlapping, case sensitive");
	    	Chunking chunking = dictionaryChunker.chunk(text);
		    for (Chunk chunk : chunking.chunkSet()) {
		        int start = chunk.start();
		        int end = chunk.end();
		        String type = chunk.type();
		        double score = chunk.score();
		        String phrase = text.substring(start,end);
		        System.out.println("     phrase=|" + phrase + "|"
		                           + " start=" + start
		                           + " end=" + end
		                           + " type=" + type
		                           + " score=" + score);
		    }
	    }

	}
	
	static void chunk(ExactDictionaryChunker chunker, String text) {
	    
	}

}
