package com.lingpipe.cookbook.chapter5;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;

public class ParagraphDetection {

	public static void main(String[] args) throws IOException {
		String document = Files.readFromFile(new File(args[0]), Strings.UTF8);
		ChunkingImpl chunking = new ChunkingImpl(document.toCharArray(),0,document.length());
		String[] paragraphs = document.split("\n\n");
		int start = 0;
		int end = 0;
		for (String paragraph : paragraphs) {
			end = start + paragraph.length();
			chunking.add(ChunkFactory.createChunk(start,end,"p"));
			start = end + 2;
		}
		String underlyingString = chunking.charSequence().toString();
		Set<Chunk> chunkSet = chunking.chunkSet();
		Chunk[] chunkArray = chunkSet.toArray(new Chunk[0]);
		Arrays.sort(chunkArray,Chunk.TEXT_ORDER_COMPARATOR);
		StringBuilder output = new StringBuilder(underlyingString);
		for (int i = chunkArray.length -1; i > 0; --i) {
			Chunk chunk = chunkArray[i];
			output.insert(chunk.end(),"}");
			output.insert(chunk.start(),"{");
		}
		System.out.println(output.toString());
	}
}
