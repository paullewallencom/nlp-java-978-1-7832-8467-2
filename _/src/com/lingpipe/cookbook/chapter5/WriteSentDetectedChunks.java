package com.lingpipe.cookbook.chapter5;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

/** Use SentenceModel to find sentence boundaries in text */
public class WriteSentDetectedChunks {

	public static void main(String[] args) throws IOException {
		boolean eosIsSentBoundary = true;
		boolean balanceParens = true;
		SentenceModel sentenceModel 
		= new IndoEuropeanSentenceModel(eosIsSentBoundary,balanceParens);
		Chunker sentenceChunker 
		= new SentenceChunker(IndoEuropeanTokenizerFactory.INSTANCE,sentenceModel);	
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("Enter text followed by new line\n>");
			System.out.flush();
			String text = reader.readLine();
			Chunking chunking 
			= sentenceChunker.chunk(text);
			Set<Chunk> sentences = chunking.chunkSet();
			if (sentences.size() < 1) {
				System.out.println("No sentence chunks found.");
				continue;
			}
			String textStored = chunking.charSequence().toString();
			Set<Chunk> chunkSet = chunking.chunkSet();
			System.out.println("size: " + chunkSet.size());
			Chunk[] chunkArray = chunkSet.toArray(new Chunk[0]);
			Arrays.sort(chunkArray,Chunk.LONGEST_MATCH_ORDER_COMPARATOR);
			StringBuilder output = new StringBuilder(textStored);
			int sentBoundOffset = 0;
			for (int i = chunkArray.length -1; i >= 0; --i) {
				Chunk chunk = chunkArray[i];
				String sentence = textStored.substring(chunk.start(), chunk.end());
				if (sentence.contains("like")) {
					output.insert(chunk.end() + sentBoundOffset,"}");
					output.insert(chunk.start(),"{");
				}
			}
			System.out.println(output.toString());
		}
	}
}


