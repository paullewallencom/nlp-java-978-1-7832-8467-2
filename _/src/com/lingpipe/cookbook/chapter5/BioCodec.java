package com.lingpipe.cookbook.chapter5;

import java.util.ArrayList;
import java.util.List;

import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.tag.StringTagging;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

public class BioCodec {
	public static void main(String[] args) {
		
		List<String> tokens = new ArrayList<String>();
		tokens.add("The");
		tokens.add("rain");
		tokens.add("in");
		tokens.add("Spain");
		tokens.add(".");
		List<String> tags = new ArrayList<String>();
		tags.add("B_Weather");
		tags.add("I_Weather");
		tags.add("O");
		tags.add("B_Place");
		tags.add("O");
		CharSequence cs = "The rain in Spain.";
						 //012345678901234567
		int[] tokenStarts = {0,4,9,12,17};
		int[] tokenEnds = {3,8,11,17,17};
		StringTagging tagging = new StringTagging(tokens, tags, cs, tokenStarts, tokenEnds);
		System.out.println("Tagging for :" + cs);
		for (int i = 0; i < tagging.size(); ++i) {
			System.out.println(tagging.token(i) + "/" + tagging.tag(i));
		}
		BioTagChunkCodec codec = new BioTagChunkCodec();
		Chunking chunking = codec.toChunking(tagging);
		System.out.println("Chunking from StringTagging");
		for (Chunk chunk : chunking.chunkSet()) {
			System.out.println(chunk);
		}
		boolean enforceConsistency = true;
		BioTagChunkCodec codec2 = new BioTagChunkCodec(IndoEuropeanTokenizerFactory.INSTANCE, enforceConsistency);
		StringTagging tagging2 = codec2.toStringTagging(chunking);
		System.out.println("StringTagging from Chunking");
		for (int i = 0; i < tagging2.size(); ++i) {
			System.out.println(tagging2.token(i) + "/" + tagging2.tag(i));
		}
	}
}
