package com.lingpipe.cookbook.chapter7;

import com.aliasi.chunk.*;
import com.aliasi.classify.PrecisionRecallEvaluation;
import com.aliasi.coref.*;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.sentences.*;
import com.aliasi.tokenizer.*;
import com.aliasi.util.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.regex.Matcher;

public class ModifiedCoref {

	static Pattern MALE_EN_PRONOUNS = Pattern.compile("\\b(He|he|Him|him)\\b");
	static Pattern FEMALE_EN_PRONOUNS = Pattern.compile("\\b(She|she|Her|her)\\b");

	static File MODEL_FILE
	= new File("models/ne-en-news-muc6.AbstractCharLmRescoringChunker");

	static TokenizerFactory TOK_FACTORY
	= new IndoEuropeanTokenizerFactory();

	static SentenceModel SENT_MODEL
	= new IndoEuropeanSentenceModel();

	static Chunker mNeChunker;

	static Pattern COREF_ANNOT_PATTERN = Pattern.compile("(<(\\d+):(\\w+)>)([\\w\\s]+)(<>)");

	Chunker mBasalNpVpChunker; 

	MentionFactory mMentionFactory;
	WithinDocCoref mCoref;

	public ModifiedCoref() throws IOException, ClassNotFoundException {
		mNeChunker
		= (Chunker) AbstractExternalizable.readObject(MODEL_FILE);
		mMentionFactory = new EnglishMentionFactory();  
		mCoref = new WithinDocCoref(mMentionFactory);
		HiddenMarkovModel posHmm
		= (HiddenMarkovModel) AbstractExternalizable.readObject(new File("models/pos-en-general-brown.HiddenMarkovModel"));

		// construct chunker
		HmmDecoder posTagger  = new HmmDecoder(posHmm,null,null);
		TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		mBasalNpVpChunker = new BasalNPVPChunker(posTagger,TOK_FACTORY);
	}


	static void test1() throws IOException, ClassNotFoundException {
		ModifiedCoref coref = new ModifiedCoref();

		String truth1 = "<0:PERSON>John<> went to <1:THING>the farm<>.";
		Pair<String,Set<CorefChunk>> pair = createChunks(truth1);
		String rawSent1 = pair.a();
		Set<CorefChunk> truthChunks1 = pair.b();
		Set<CorefChunk> responseChunks1 = coref.resolve(rawSent1,0);
		judge(rawSent1,truthChunks1,responseChunks1);
		new HashSet<CorefChunk>();
		String sent2 = "He wanted to see some cows.";
		String sent3 = "They mooed alot.";
	}

	static boolean matches(CorefChunk chunk1, CorefChunk chunk2) {
		if (chunk1.start() == chunk2.start()
				&& chunk1.end() == chunk2.end()
				&& chunk1.type().equals(chunk2.type())) {
			return true;
		}
		return false;

	}
	static void judge(String text,Set<CorefChunk> truth, Set<CorefChunk> response) {
		for (CorefChunk truthChunk : truth) {
			boolean foundMatch = false;
			for (CorefChunk responseChunk : response) {
				if (matches(truthChunk,responseChunk)) {
					foundMatch = true;
				}
			}
			if (!foundMatch) {
				System.out.println("Failed to find truth chunk in response '" 
						+ text.substring(truthChunk.start(),truthChunk.end()) + "' " + truthChunk);
			}
			else {

			}
		}
		for (CorefChunk responseChunk : response) {
			boolean foundMatch = false;
			for (CorefChunk truthChunk : truth) {
				if (matches(truthChunk,responseChunk)) {
					foundMatch = true;
				}
			}
			if (!foundMatch) {
				System.out.println("Failed to find response chunk in truth '" 
						+ text.substring(responseChunk.start(),responseChunk.end()) + "' " + responseChunk);
			}
		}
	}

	static Pair<String,Set<CorefChunk>> createChunks(String annotatedSent) {
		Set<CorefChunk> retChunks = new HashSet<CorefChunk>();
		Matcher matcher = COREF_ANNOT_PATTERN.matcher(annotatedSent);
		int annotOffset = 0;
		while (matcher.find()) {
			int mentionId = Integer.valueOf(matcher.group(2));
			String type = matcher.group(3);
			int annotLength = matcher.end(1) - matcher.start(1);
			annotOffset += annotLength;
			int mentionStart = matcher.start(4) - annotOffset;
			int mentionEnd = matcher.end(4) - annotOffset;		
			CorefChunk corefChunk = new CorefChunk(mentionId,type,mentionStart,mentionEnd);
			annotOffset += 2;
			retChunks.add(corefChunk);
		}
		String returnString = annotatedSent.replaceAll("<[^<>]*>", "");
		return new Pair<String,Set<CorefChunk>>(returnString,retChunks);
	}

	Set<CorefChunk> resolve(String sentenceText,int sentenceNum){
		Set<CorefChunk> ents = new HashSet<CorefChunk>();
		System.out.println("Sentence Text=" + sentenceText);
		Chunking mentionChunking
		= mNeChunker.chunk(sentenceText);

		//basal NPs
		Chunking basalNpVpChunking = mBasalNpVpChunker.chunk(sentenceText);
		//plurals
		//first person

		Set<Chunk> chunkSet = new TreeSet<Chunk>(Chunk.TEXT_ORDER_COMPARATOR);
		chunkSet.addAll(mentionChunking.chunkSet());
		addRegexMatchingChunks(MALE_EN_PRONOUNS,"MALE_PRONOUN",sentenceText,chunkSet);
		addRegexMatchingChunks(FEMALE_EN_PRONOUNS,"FEMALE_PRONOUN",sentenceText,chunkSet);
		addNonConflictingNpChunks(basalNpVpChunking.chunkSet(),chunkSet);
		Iterator<Chunk> mentionIt = chunkSet.iterator();
		while (mentionIt.hasNext()) {
			Chunk mentionChunk = (Chunk) mentionIt.next();
			String mentionText
			= sentenceText.substring(mentionChunk.start(),
					mentionChunk.end());
			String mentionType = mentionChunk.type();
			Mention mention = mMentionFactory.create(mentionText,mentionType);
			int mentionId = mCoref.resolveMention(mention,sentenceNum);
			System.out.println("     mention text=" + mentionText
					+ " type=" + mentionType
					+ " id=" + mentionId);
			CorefChunk corefChunk = new CorefChunk(mentionId,mentionType,mentionChunk.start(),mentionChunk.end());
			ents.add(corefChunk);
		}
		return ents;
	}	

	public static void main(String[] args) 
			throws ClassNotFoundException, IOException {

		test1();
		// create NE chunker
	}


	static void addNonConflictingNpChunks(Set<Chunk> chunksToAdd,Set<Chunk> existingChunks) {

		Iterator<Chunk> it = chunksToAdd.iterator();
		while (it.hasNext()) {
			Iterator<Chunk> itExistingChunks = existingChunks.iterator();
			Chunk chunk = (Chunk) it.next();
			boolean conflictFound = false;
			while(itExistingChunks.hasNext()) {
				Chunk existingChunk = itExistingChunks.next();
				if (overlap(chunk.start(),chunk.end(),
						existingChunk.start(),existingChunk.end())) {
					conflictFound = true;
				}
			}
			if (!conflictFound) {
				existingChunks.add(chunk);
			}
		}
	}


	static void addRegexMatchingChunks(Pattern pattern, String type, String text, Set<Chunk> chunkSet) {
		java.util.regex.Matcher matcher = pattern.matcher(text);
		int pos = 0;
		while (matcher.find(pos)) {
			Chunk regexChunk = ChunkFactory.createChunk(matcher.start(),
					matcher.end(),
					type);
			Iterator it = chunkSet.iterator();
			while (it.hasNext()) {
				Chunk chunk = (Chunk) it.next();
				if (overlap(chunk.start(),chunk.end(),
						regexChunk.start(),regexChunk.end())) {
					it.remove();
				}
			}
			chunkSet.add(regexChunk);
			pos = matcher.end();
		}
	}

	static boolean overlap(int start1, int end1,
			int start2, int end2) {
		return java.lang.Math.max(start1,start2)
				< java.lang.Math.min(end1,end2);
	}
}
