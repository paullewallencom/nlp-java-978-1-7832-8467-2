package com.lingpipe.cookbook.chapter5;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;

public class ParagraphSentenceDetection {

	public static void main(String[] args) throws IOException {
		String filePath = args.length > 0 ? args[0] : "data/paragraphExample.txt";
		String document = Files.readFromFile(new File(filePath), Strings.UTF8);
		String[] paragraphs = document.split("\n\n");
		int paraSeparatorLength = 2;
		ChunkingImpl paraChunking = new ChunkingImpl(document.toCharArray(),0,document.length());
		ChunkingImpl sentChunking = new ChunkingImpl(paraChunking.charSequence());
		boolean eosIsSentBoundary = true; 
		boolean balanceParens = false;
		SentenceModel sentenceModel 
			= new IndoEuropeanSentenceModel(eosIsSentBoundary,balanceParens);
		SentenceChunker sentenceChunker 
			= new SentenceChunker(IndoEuropeanTokenizerFactory.INSTANCE,sentenceModel);	
		int paraStart = 0;
		for (String paragraph : paragraphs) {
			for (Chunk sentChunk : sentenceChunker.chunk(paragraph).chunkSet()) {
				Chunk adjustedSentChunk 
					= ChunkFactory.createChunk(sentChunk.start() + paraStart,sentChunk.end() + paraStart, "S");
				sentChunking.add(adjustedSentChunk);
			}
			paraChunking.add(ChunkFactory.createChunk(paraStart, paraStart + paragraph.length(),"P"));
			paraStart += paragraph.length() + paraSeparatorLength;
		}
		String underlyingString = paraChunking.charSequence().toString();
		ChunkingImpl displayChunking = new ChunkingImpl(paraChunking.charSequence());
		displayChunking.addAll(sentChunking.chunkSet());
		displayChunking.addAll(paraChunking.chunkSet());
		Set<Chunk> chunkSet = displayChunking.chunkSet();
		Chunk[] chunkArray = chunkSet.toArray(new Chunk[0]);
		Arrays.sort(chunkArray,Chunk.LONGEST_MATCH_ORDER_COMPARATOR);
		StringBuilder output = new StringBuilder(underlyingString);
		int sentBoundOffset = 0;
		for (int i = chunkArray.length -1; i >= 0; --i) {
			Chunk chunk = chunkArray[i];
			System.out.println(chunk);
			if (chunk.type().equals("P")) {
				output.insert(chunk.end() + sentBoundOffset,"}");
				output.insert(chunk.start(),"{");
				sentBoundOffset = 0;
			}
			if (chunk.type().equals("S")) {
				output.insert(chunk.end(),"]");
				output.insert(chunk.start(),"[");
				sentBoundOffset += 2;
			}
		}
		System.out.println(output.toString());
	}
}

