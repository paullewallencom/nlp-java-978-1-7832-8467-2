package com.lingpipe.cookbook.chapter7.tracker;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.coref.Mention;
import com.aliasi.coref.MentionChain;
import com.aliasi.coref.WithinDocCoref;

import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;

import com.aliasi.sentences.SentenceChunker;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.lingpipe.cookbook.chapter7.tracker.TTMention;
import com.lingpipe.cookbook.chapter7.tracker.TTMentionFactory;
import com.lingpipe.cookbook.chapter7.tracker.TTSynonymMatch;

import com.aliasi.util.Strings;

import com.aliasi.xml.SAXWriter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;



import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Tracker {

	static boolean DEBUG = false;

	TTSynonymMatch mSynonymMatch;

	private final Chunker mSentenceChunker;

	private final TTMentionFactory mMentionFactory;

	protected final XDocCoref mXDocCoref;

	private EntityPhraseChunker mEntityPhraseChunker;

	public Tracker(TokenizerFactory tokenizerFactory,
			Chunker sentenceChunker,
			EntityPhraseChunker entityPhraseChunker,
			Dictionary dictionary) {
		this(tokenizerFactory,
				sentenceChunker,
				entityPhraseChunker,
				dictionary,
				true);
	}
	

	public Tracker(TokenizerFactory tokenizerFactory,
			Chunker sentenceChunker,
			EntityPhraseChunker entityPhraseChunker,
			Dictionary dictionary,
			boolean addSpeculativeEntitiesToEntityUniverse) {
		this(tokenizerFactory,
				sentenceChunker,
				entityPhraseChunker,
				dictionary,
				new XDocCoref(new EntityUniverse(tokenizerFactory),addSpeculativeEntitiesToEntityUniverse));
	}
	
	public Tracker(TokenizerFactory tokenizerFactory,
			Chunker sentenceChunker,
			EntityPhraseChunker entityPhraseChunker,
			Dictionary dictionary,
			XDocCoref xDocCoref) {

		mSentenceChunker = sentenceChunker;
		mMentionFactory
		= new TTMentionFactory(tokenizerFactory);
		mXDocCoref = xDocCoref;
		mEntityPhraseChunker = entityPhraseChunker;
		setDictionaryInEntityUniverse(dictionary);
	}

	// READERS

	

	public Chunker sentenceChunker() {
		return mSentenceChunker;
	}

	public TTMentionFactory mentionFactory() {
		return mMentionFactory;
	}

	public EntityPhraseChunker getMentionChunker() {
		return mEntityPhraseChunker;
	}


	public XDocCoref xDocCoref() {
		return mXDocCoref;
	}


	// WRITERS


	public synchronized OutputDocument[] processDocuments(InputSource in)
			throws SAXException, IOException {
		InputDocument[] docsIn = InputDocument.parse(in);
		OutputDocument[] docsOut = new OutputDocument[docsIn.length];
		for (int i = 0; i < docsIn.length; ++i)
			docsOut[i] = processDocument(docsIn[i]);
		return docsOut;
	}

	public synchronized OutputDocument processDocument(InputDocument document) {
		WithinDocCoref coref
		= new WithinDocCoref(mMentionFactory);

		String title = document.title();
		String content = document.content();

		List<String> sentenceTextList = new ArrayList<String>();
		List<Mention[]> sentenceMentionList = new ArrayList<Mention[]>();
		List<int[]> mentionStartList = new ArrayList<int[]>();
		List<int[]> mentionEndList = new ArrayList<int[]>();

		int firstContentSentenceIndex
		= processBlock(title,0,
				sentenceTextList,
				sentenceMentionList,
				mentionStartList,mentionEndList,
				coref);

		processBlock(content,firstContentSentenceIndex,
				sentenceTextList,
				sentenceMentionList,
				mentionStartList,mentionEndList,
				coref);        

		MentionChain[] chains = coref.mentionChains();

		Entity[] entities  = mXDocCoref.xdocCoref(chains);

		Map<Mention,Entity> mentionToEntityMap
		= new HashMap<Mention,Entity>();
		for (int i = 0; i < chains.length; ++i) {
			for (Mention mention : chains[i].mentions()) {
				mentionToEntityMap.put(mention,entities[i]);
			}
		}

		String[] sentenceTexts
		= sentenceTextList
		.<String>toArray(new String[sentenceTextList.size()]);

		Mention[][] sentenceMentions
		= sentenceMentionList
		.<Mention[]>toArray(new Mention[sentenceMentionList.size()][]);

		int[][] mentionStarts
		= mentionStartList
		.<int[]>toArray(new int[mentionStartList.size()][]);

		int[][] mentionEnds
		= mentionEndList
		.<int[]>toArray(new int[mentionEndList.size()][]);

		Chunking[] chunkings = new Chunking[sentenceTexts.length];
		for (int i = 0; i < chunkings.length; ++i) {
			ChunkingImpl chunking = new ChunkingImpl(sentenceTexts[i]);
			chunkings[i] = chunking;
			for (int j = 0; j < sentenceMentions[i].length; ++j) {
				Mention mention = sentenceMentions[i][j];
				Entity entity = mentionToEntityMap.get(mention);
				if (entity == null) {
					Chunk chunk = ChunkFactory.createChunk(mentionStarts[i][j],
							mentionEnds[i][j],
							mention.entityType()
							+ ":-1");
					//chunking.add(chunk); //uncomment to get unresolved ents as -1 indexed.
				} else {
					Chunk chunk = ChunkFactory.createChunk(mentionStarts[i][j],
							mentionEnds[i][j],
							entity.type()
							+ ":" + entity.id());
					chunking.add(chunk);
				}
			}
		}

		// needless allocation here and last, but simple
		Chunking[] titleChunkings = new Chunking[firstContentSentenceIndex];
		for (int i = 0; i < titleChunkings.length; ++i)
			titleChunkings[i] = chunkings[i];

		Chunking[] bodyChunkings = new Chunking[chunkings.length - firstContentSentenceIndex];
		for (int i = 0; i < bodyChunkings.length; ++i)
			bodyChunkings[i] = chunkings[firstContentSentenceIndex+i];

		String id = document.id();

		OutputDocument result = new OutputDocument(id,titleChunkings,bodyChunkings);
		return result;
	}


	int processBlock(String text,
			int sentenceCount,
			List<String> sentenceTextList,
			List<Mention[]> sentenceMentionList,
			List<int[]> mentionStartList,
			List<int[]> mentionEndList,
			WithinDocCoref coref) {

		Chunking sentenceChunking = mSentenceChunker.chunk(text);
		for (Chunk sentenceChunk : sentenceChunking.chunkSet()) {

			String sentenceText
			= text.substring(sentenceChunk.start(),
					sentenceChunk.end());

			Set<Chunk> entityChunkSet = mEntityPhraseChunker.chunk(sentenceText).chunkSet();

			//mLog.trace("\nChunk set=" + entityChunkSet + "\n");
			Chunk[] chunks
			= entityChunkSet.<Chunk>toArray(new Chunk[entityChunkSet.size()]);
			Arrays.sort(chunks,Chunk.TEXT_ORDER_COMPARATOR);
			Mention[] mentions = new Mention[chunks.length];
			int[] sentMentStarts = new int[chunks.length];
			int[] sentMentEnds = new int[chunks.length];
			int mentionIndex = 0;
			for (Chunk entityChunk : chunks) {
				String chunkText
				= sentenceText.substring(entityChunk.start(),
						entityChunk.end());
				Mention mention
				= mMentionFactory.create(chunkText,
						entityChunk.type());
				sentMentStarts[mentionIndex] = entityChunk.start();
				sentMentEnds[mentionIndex] = entityChunk.end();
				mentions[mentionIndex++] = mention;
				// int withinDocId =  // don't need it -- use chains later
						coref.resolveMention(mention,sentenceCount);
			}
			sentenceTextList.add(sentenceText);
			sentenceMentionList.add(mentions);
			mentionStartList.add(sentMentStarts);
			mentionEndList.add(sentMentEnds);
			++sentenceCount;
		}
		return sentenceCount;
	}

	/*
    //Needed by DictionaryServlet for runtime dictionary updates
    public synchronized Dictionary resetDictionary(InputSource in)
        throws SAXException, IOException {
        Dictionary dict = Dictionary.read(in);
        setDictionary(dict);
        mEntityPhraseChunker.resetDictionary(dict);
        return dict;
    }
	 */

	public synchronized void setDictionaryInEntityUniverse(Dictionary dictionary) {
		EntityUniverse entityUniverse = mXDocCoref.entityUniverse();

		// - in new dict, +user currently
		// entity is dangling if was user defined and not in new dict
		Set<Entity> danglingEntitySet
		= new HashSet<Entity>(entityUniverse.userDefinedEntitySet());
		for (DictionaryEntitySpec entitySpec : dictionary.entitySpecs()) {
			Entity e = entityUniverse.getEntity(entitySpec.id());
			danglingEntitySet.remove(e);
		}
		for (Entity entity : danglingEntitySet)
			entityUniverse.remove(entity);

		// +in new dict
		for (DictionaryEntitySpec entitySpec : dictionary.entitySpecs())
			entityUniverse.updateEntitySpec(entitySpec);

		//System.out.println("Got Dict" + dictionary.stopPhrases());
		//mEntityPhraseChunker.setStopPhrases(dictionary.stopPhrases());
		//        TokenizerFactory casePreservingTokenizer = IndoEuropeanTokenizerFactory.FACTORY;
		//mEntityPhraseChunker.setDictionaryChunker(dictionary,casePreservingTokenizer);
		//mEntityPhraseChunker.setStopSubstringList(STOP_SUBSTRING_LIST);

		setSynonymMatcher(dictionary);



		//        mEntityDictionary = dictionary;
	}



	private void setSynonymMatcher(Dictionary dictionary) {
		TTSynonymMatch ttSynonymMatcher
		= mMentionFactory.mMatchers.mSynonymMatch;
		ttSynonymMatcher.clearSynonyms();
		for (DictionaryEntitySpec entitySpec : dictionary.entitySpecs()) {
			String[] aliases = entitySpec.aliases();
			for (int i = 0; i < aliases.length; ++i)
				for (int j = i + 1; j < aliases.length; ++j)
					ttSynonymMatcher.addSynonym(aliases[i],aliases[j]);
		}
	}




}
