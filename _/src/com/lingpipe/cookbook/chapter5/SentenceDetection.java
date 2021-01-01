package com.lingpipe.cookbook.chapter5;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;


public class SentenceDetection {

	public static void main(String[] args) throws IOException {
		boolean endSent = true;
		boolean parenS = true;
		SentenceModel sentenceModel 
			= new IndoEuropeanSentenceModel(endSent,parenS);
		TokenizerFactory tokFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		Chunker sentenceChunker 
			= new SentenceChunker(tokFactory,sentenceModel);	
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("Enter text followed by new line\n>");
			String text = reader.readLine();
			Chunking chunking 
			= sentenceChunker.chunk(text);
			Set<Chunk> sentences = chunking.chunkSet();
			if (sentences.size() < 1) {
				System.out.println("No sentence chunks found.");
				return;
			}
			String textStored = chunking.charSequence().toString();
			for (Chunk sentence : sentences) {
				int start = sentence.start();
				int end = sentence.end();
				System.out.println("SENTENCE :" + textStored.substring(start,end));
			}
		}
	}
}


