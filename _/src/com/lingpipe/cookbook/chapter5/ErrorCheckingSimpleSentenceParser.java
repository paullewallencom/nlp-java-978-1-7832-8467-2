package com.lingpipe.cookbook.chapter5;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;

public class ErrorCheckingSimpleSentenceParser {

	public static void main(String[] args) throws IOException {
		char[] chars = Files.readCharsFromFile(new File(args[0]), Strings.UTF8);
		StringBuilder rawChars = new StringBuilder();
		int sentStart = -1;
		int sentEnd = -1;
		Set<Chunk> sentChunks = new HashSet<Chunk>();
		for (int i=0; i < chars.length; ++i) {
			if (chars[i] == '[') {
				if (sentStart != -1 || sentEnd != -1) {
					throw new RuntimeException("sentence start wrong. Got " + sentStart + " sentStart " + sentEnd + " sentEnd");
				}
				sentStart = rawChars.length();
			}
			else if (chars[i] == ']') {
				if (sentStart == -1 || sentEnd != -1) {
					throw new RuntimeException("sentence end wrong. Got " + sentStart + " sentStart " + sentEnd + " sentEnd");
				}
				sentEnd = rawChars.length();
				sentChunks.add(ChunkFactory.createChunk(sentStart,sentEnd,SentenceChunker.SENTENCE_CHUNK_TYPE));
				sentStart = -1;
				sentEnd = -1;
			}
			else {
				rawChars.append(chars[i]);
			}
		}
		String unannotatedText = rawChars.toString();
		for (Chunk sent : sentChunks) {
			System.out.println(sent);
		}
	}

}
