package com.lingpipe.cookbook.chapter7;

import com.aliasi.chunk.*;
import com.aliasi.coref.*;
import com.aliasi.sentences.*;
import com.aliasi.tokenizer.*;
import com.aliasi.util.*;


import java.io.*;
import java.util.*;
import java.util.regex.*;

public class NamedEntityCoreference {
	static TokenizerFactory mTokenizerFactory;
	static Chunker mSentenceChunker;
	static Chunker mNamedEntChunker;
	
	public static void main(String[] args) 
			throws ClassNotFoundException, IOException {
	
		String inputDoc = args.length > 0 ? args[0] : "data/simpleCoref.txt";
		System.out.println("Reading in file :" + inputDoc);
		mTokenizerFactory
		= IndoEuropeanTokenizerFactory.INSTANCE;
		SentenceModel sentenceModel
		= new IndoEuropeanSentenceModel();
		Chunker sentenceChunker 
		= new SentenceChunker(mTokenizerFactory,sentenceModel);
		File modelFile
		= new File("models/ne-en-news-muc6.AbstractCharLmRescoringChunker");
		Chunker namedEntChunker 
		= (Chunker) AbstractExternalizable.readObject(modelFile);
		MentionFactory mf = new EnglishMentionFactory();  
		WithinDocCoref coref = new WithinDocCoref(mf);
		File doc = new File(inputDoc);
		String text = Files.readFromFile(doc,Strings.UTF8);
		Chunking sentenceChunking
		= sentenceChunker.chunk(text);
		Iterator sentenceIt 
		= sentenceChunking.chunkSet().iterator();
		for (int sentenceNum = 0; sentenceIt.hasNext(); ++sentenceNum) {
			Chunk sentenceChunk = (Chunk) sentenceIt.next();
			String sentenceText 
			= text.substring(sentenceChunk.start(),
					sentenceChunk.end());
			System.out.println("Sentence Text=" + sentenceText);
			Chunking neChunking
			= namedEntChunker.chunk(sentenceText);
			for (Chunk neChunk : neChunking.chunkSet()) {
				String mentionText
					= sentenceText.substring(neChunk.start(),
						neChunk.end());
				String mentionType = neChunk.type();
				Mention mention = mf.create(mentionText,mentionType);
				int mentionId = coref.resolveMention(mention,sentenceNum);
				System.out.println("     mention text=" + mentionText
						+ " type=" + mentionType
						+ " id=" + mentionId);
			}
		}
	}
}
