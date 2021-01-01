package com.lingpipe.cookbook.chapter5;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.TagChunkCodec;
import com.aliasi.chunk.TagChunkCodecAdapters;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.StringParser;
import com.aliasi.tag.LineTaggingParser;
import com.aliasi.tag.Tagging;

public class Conll2002ChunkTagParser extends StringParser<ObjectHandler<Chunking>> {

	static final String TOKEN_TAG_LINE_REGEX
	= "(\\S+)\\s(\\S+\\s)?(O|[B|I]-\\S+)"; // token ?posTag entityTag
	static final int TOKEN_GROUP = 1; // token
	static final int TAG_GROUP = 3;   // entityTag
	static final String IGNORE_LINE_REGEX
	= "-DOCSTART(.*)";  // lines that start with "-DOCSTART"
	static final String EOS_REGEX
	= "\\A\\Z";         // empty lines

	static final String BEGIN_TAG_PREFIX = "B-";
	static final String IN_TAG_PREFIX = "I-";
	static final String OUT_TAG = "O";

	private final LineTaggingParser mParser
	= new LineTaggingParser(TOKEN_TAG_LINE_REGEX, TOKEN_GROUP, TAG_GROUP,
			IGNORE_LINE_REGEX, EOS_REGEX);

	private final TagChunkCodec mCodec
	= new BioTagChunkCodec(null, // no tokenizer
			false,  // don't enforce consistency
			BEGIN_TAG_PREFIX, // custom BIO tag coding matches regex
			IN_TAG_PREFIX,
			OUT_TAG);

	public void parseString(char[] cs, int start, int end) {
		mParser.parseString(cs,start,end);
	}

	public void setHandler(ObjectHandler<Chunking> handler) {
		ObjectHandler<Tagging<String>> taggingHandler
		= TagChunkCodecAdapters.chunkingToTagging(mCodec,handler);
		mParser.setHandler(taggingHandler);
	}

	public TagChunkCodec getTagChunkCodec(){
		return mCodec;
	}

}
