package com.lingpipe.cookbook.chapter5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.RegExChunker;

public class RegexNer {

	public static void main(String[] args) throws IOException {
		String emailRegex
    		= "[A-Za-z0-9](([_\\.\\-]?[a-zA-Z0-9]+)*)@([A-Za-z0-9]+)"
    				+ "(([\\.\\-]?[a-zA-Z0-9]+)*)\\.([A-Za-z]{2,})";
		String chunkType = "email";
		double score = 1.0;
		Chunker chunker = new RegExChunker(emailRegex,chunkType,score);   	  
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	    String input = "";
	    while (true) {
			System.out.println("Enter text, . to quit:");
			input = reader.readLine();
			if(input.equals(".")){
				break;
			}
			Chunking chunking = chunker.chunk(input);
	        System.out.println("input=" + input);
	        System.out.println("chunking=" + chunking);
	        Set<Chunk> chunkSet = chunking.chunkSet();
	        Iterator<Chunk> it = chunkSet.iterator();
	        while (it.hasNext()) {
	            Chunk chunk = it.next();
	            int start = chunk.start();
	            int end = chunk.end();
	            String text = input.substring(start,end);
	            System.out.println("     chunk=" + chunk + "  text=" + text);
	        }
	    }
	}
}
