package com.lingpipe.cookbook.chapter5;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.RegExChunker;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.util.AbstractExternalizable;


public class MultipleNer {

	/**
	 * @param args
	 */
	static final double CHUNK_SCORE = 1.0;
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Chunker pronounChunker = new RegExChunker(" He | he | Him | him ","MALE_PRONOUN",1.0);
		File MODEL_FILE
		= new File("models/ne-en-news-muc6.AbstractCharLmRescoringChunker");
		Chunker neChunker 
		= (Chunker) AbstractExternalizable.readObject(MODEL_FILE);
		
		MapDictionary<String> dictionary = new MapDictionary<String>();
		dictionary.addEntry(new DictionaryEntry<String>("Obama","PRESIDENT",CHUNK_SCORE));
		dictionary.addEntry(new DictionaryEntry<String>("Bush","PRESIDENT",CHUNK_SCORE));
		dictionary.addEntry(new DictionaryEntry<String>("Clinton","PRESIDENT",CHUNK_SCORE));
		dictionary.addEntry(new DictionaryEntry<String>("Reagan","PRESIDENT",CHUNK_SCORE));

		
		ExactDictionaryChunker dictionaryChunker
		= new ExactDictionaryChunker(dictionary,
				IndoEuropeanTokenizerFactory.INSTANCE,
				false,true);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
	    	System.out.println("Enter text, . to quit:");
	    	String text = reader.readLine();
	    	if(text.equals(".")){
	    		break;
	    	}
	    	Set<Chunk> neChunking = neChunker.chunk(text).chunkSet();
			System.out.println("neChunking: " + neChunking);
			Set<Chunk> pChunking = pronounChunker.chunk(text).chunkSet();
			System.out.println("pChunking: " + pChunking);
			Set<Chunk> dChunking = dictionaryChunker.chunk(text).chunkSet();
			System.out.println("dChunking: " + dChunking);
			
			Set<Chunk> allChunks = new HashSet<Chunk>();
			allChunks.addAll(neChunking);
			allChunks.addAll(pChunking);
			allChunks.addAll(dChunking);
			System.out.println("----Overlaps Allowed");
			getCombinedChunks(allChunks,true);
			System.out.println("\n----Overlaps Not Allowed");
			getCombinedChunks(allChunks,false);
	    	
		}
		
	}

	static void getCombinedChunks(Set<Chunk> chunkSet, boolean allowOverlap){
		Set<Chunk> combinedChunks = new HashSet<Chunk>();
		Set<Chunk>overLappedChunks = new HashSet<Chunk>();
		for(Chunk c : chunkSet){
			combinedChunks.add(c);
			for(Chunk x : chunkSet){
				if(c.equals(x)){
					continue;
				}
				boolean debug = false;
				if(debug){
					System.out.println("C: " + c);
					System.out.println("X: " + x);
					System.out.println("Overlap: " + overlap(c,x));
				}
				if (overlap(c,x)) {
					if (allowOverlap){
						combinedChunks.add(x);
					} else {
						overLappedChunks.add(x);
						combinedChunks.remove(c);
					}
				} 
			}
		}
		if(allowOverlap){
			System.out.println("\n Combined Chunks:");
			System.out.println(combinedChunks);
		} else {
			System.out.println("\n Unique Chunks:");
			System.out.println(combinedChunks);
			System.out.println("\n OverLapped Chunks:");
			System.out.println(overLappedChunks);
		}
		
		
	}
		
		

	
	static boolean overlap(Chunk c1, Chunk c2){
		return overlap(c1.start(),c1.end(),c2.start(),c2.end());
	}
	
	static boolean overlap(int start1, int end1,
			int start2, int end2) {
		return java.lang.Math.max(start1,start2)
				< java.lang.Math.min(end1,end2);
	}
	
}
