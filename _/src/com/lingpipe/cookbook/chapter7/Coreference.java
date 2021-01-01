package com.lingpipe.cookbook.chapter7;

import com.aliasi.chunk.*;
import com.aliasi.coref.*;
import com.aliasi.sentences.*;
import com.aliasi.tokenizer.*;
import com.aliasi.util.*;


import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Coreference {

	static Pattern MALE_EN_PRONOUNS = Pattern.compile("\\b(He|he|Him|him)\\b");
	static Pattern FEMALE_EN_PRONOUNS = Pattern.compile("\\b(She|she|Her|her)\\b");

	static File MODEL_FILE
	= new File("models/ne-en-news-muc6.AbstractCharLmRescoringChunker");

	static TokenizerFactory TOK_FACTORY
	= new IndoEuropeanTokenizerFactory();

	static SentenceModel SENT_MODEL
	   = new IndoEuropeanSentenceModel();
	
	public static void main(String[] args) 
			throws ClassNotFoundException, IOException {
		Chunker neChunker 
		= (Chunker) AbstractExternalizable.readObject(MODEL_FILE);
		Chunker sentenceChunker 
		= new SentenceChunker(TOK_FACTORY,SENT_MODEL);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("Enter text followed by new line\n>");
			String text = reader.readLine();
			MentionFactory mf = new EnglishMentionFactory();  
			WithinDocCoref coref = new WithinDocCoref(mf);
			Chunking sentenceChunking
			= sentenceChunker.chunk(text);
			Iterator<Chunk> sentenceIt 
			= sentenceChunking.chunkSet().iterator();
			for (int sentenceNum = 0; sentenceIt.hasNext(); ++sentenceNum) {
				Chunk sentenceChunk = sentenceIt.next();
				String sentenceText 
				= text.substring(sentenceChunk.start(),
						sentenceChunk.end());
				System.out.println("Sentence Text=" + sentenceText);
				Chunking mentionChunking
				= neChunker.chunk(sentenceText);
				Set<Chunk> chunkSet = new TreeSet<Chunk>(Chunk.TEXT_ORDER_COMPARATOR);
				chunkSet.addAll(mentionChunking.chunkSet());
				addRegexMatchingChunks(MALE_EN_PRONOUNS,"MALE_PRONOUN",sentenceText,chunkSet);
				addRegexMatchingChunks(FEMALE_EN_PRONOUNS,"FEMALE_PRONOUN",sentenceText,chunkSet);
				Iterator<Chunk> mentionIt = chunkSet.iterator();
				while (mentionIt.hasNext()) {
					Chunk mentionChunk = (Chunk) mentionIt.next();
					String mentionText
					= sentenceText.substring(mentionChunk.start(),
							mentionChunk.end());
					String mentionType = mentionChunk.type();
					Mention mention = mf.create(mentionText,mentionType);
					int mentionId = coref.resolveMention(mention,sentenceNum);
					System.out.println("     mention text=" + mentionText
							+ " type=" + mentionType
							+ " id=" + mentionId);
				}	
			}
		}
	}

	static void addRegexMatchingChunks(Pattern pattern, String type, String text, Set<Chunk> chunkSet) {
		java.util.regex.Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			Chunk regexChunk = ChunkFactory.createChunk(matcher.start(),
					matcher.end(),
					type);
			for (Chunk chunk : chunkSet) {
				if (ChunkingImpl.overlap(chunk,regexChunk)) {
					chunkSet.remove(chunk);
				}
			}
			chunkSet.add(regexChunk);
		}
	}
	
	
}
