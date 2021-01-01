package com.lingpipe.cookbook.chapter7.tracker;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.util.Strings;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;

import com.aliasi.tag.Tagging;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.dict.MapDictionary;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

public class EntityPhraseChunker implements Chunker {

    protected Chunker mNeSpeculativeChunker;
    protected Chunker mNePronounChunker = new PronounChunker();

    protected ExactDictionaryChunker mNeDictionaryChunker;
    //    private Dictionary mEntityDictionary;

    protected HmmDecoder mPosTagger;

    protected Set<String> mStopPhraseSet;

    protected String[]  mStopSubstringList = {};

    protected Map<String,Integer> mPhraseCounts;

    public EntityPhraseChunker (Chunker neSpeculativeChunker,
                                HmmDecoder posTagger,
                                ExactDictionaryChunker dictionary,
                                String[] stopPhrases,
                                String[] stopSubstrings,
                                Map<String,Integer> phraseCounts) {
        mNeSpeculativeChunker = neSpeculativeChunker;
        mPosTagger = posTagger;
        mNeDictionaryChunker = dictionary;
        setStopPhrases(stopPhrases);
        mStopSubstringList = stopSubstrings;
        mPhraseCounts = phraseCounts;
    }

    public Chunking chunk (CharSequence cSeq) {
        char[] cs = Strings.toCharArray(cSeq);
        return chunk(cs, 0, cs.length);
    }

    public Chunking chunk( char[] characters, int start, int end) {
        String text = String.valueOf(characters);
        ChunkingImpl chunking = new ChunkingImpl(characters, 0, characters.length);
        Set<Chunk> entityChunkSet = new HashSet<Chunk>();

        add(entityChunkSet,mNePronounChunker,text);
        //addDictionaryWithPosFilter(entityChunkSet,text);
        add(entityChunkSet,mNeDictionaryChunker,text);
        //addDictionaryWithPosPhraseCountFilter(entityChunkSet,text);

        addSpeculativeWithPosPhraseCountFilter(entityChunkSet,mNeSpeculativeChunker,text);
        for (Chunk chunk : entityChunkSet)
            chunking.add(chunk);
        return chunking;
    }


    public void setDictionaryChunker(ExactDictionaryChunker chunker) {
        mNeDictionaryChunker = chunker;
    }

    public synchronized void setStopPhrases(String[] stopPhrases) {
        Set<String> stopPhraseSet = new HashSet<String>();
        for (String phrase : stopPhrases)
            stopPhraseSet.add(phrase);
        mStopPhraseSet = stopPhraseSet;
    }

    public synchronized void setStopSubstringList(String[] stopSubstrings) {
        mStopSubstringList = stopSubstrings;
    }


    //boy this is getting ugly

    protected void addSpeculative(Set<Chunk> chunkSet, Chunker chunker, String input) {
        if (chunker == null) return;
        Set<Chunk> nextChunkSet = chunker.chunk(input).chunkSet();
        if (nextChunkSet == null) return; // probably not nec.
        for (Chunk chunk : nextChunkSet) {
            if (chunk.end() - chunk.start() < 2)
                continue;
            String text = input.substring(chunk.start(),chunk.end());
            if (mStopPhraseSet.contains(text)) continue;
            if (overlap(chunk,chunkSet)) continue;
            boolean pass = true;
            for (int i = 0; i < mStopSubstringList.length; ++i) {
                if (text.contains(mStopSubstringList[i]))
                    pass = false;
            }
            if (pass) {
                Chunk normChunk = ChunkFactory.createChunk(chunk.start(),chunk.end(),"OTHER");
                chunkSet.add(normChunk);
            }
        }
    }

    protected void addSpeculativeWithPosPhraseCountFilter (Set<Chunk> chunkSet, Chunker chunker, String input) {
        if (chunker == null) return;
        Set<Chunk> nextChunkSet = chunker.chunk(input).chunkSet();
        if (nextChunkSet == null) return; // probably not nec.
        PosTagFilter posTagFilter = new PosTagFilter(input,
                                                     mPosTagger,
                                                     mNeDictionaryChunker.tokenizerFactory());
        for (Chunk chunk : nextChunkSet) {
            if (chunk.end() - chunk.start() < 2)
                continue;
            String text = input.substring(chunk.start(),chunk.end());
            if (failPhraseCountFilter(text) && posTagFilter.fail(text)) {
                //              System.out.println("Skipping: " + text);
                continue;
            }
            if (mStopPhraseSet.contains(text)) continue;
            if (overlap(chunk,chunkSet)) continue;
            boolean pass = true;
            for (int i = 0; i < mStopSubstringList.length; ++i) {
                if (text.contains(mStopSubstringList[i]))
                    pass = false;
            }
            if (pass) {
                Chunk normChunk = ChunkFactory.createChunk(chunk.start(),chunk.end(),"OTHER");
                chunkSet.add(normChunk);
            }
        }
    }

    protected void addSpeculativeWithPosFilter(Set<Chunk> chunkSet, Chunker chunker, String input) {
        if (chunker == null) return;
        Set<Chunk> nextChunkSet = chunker.chunk(input).chunkSet();
        if (nextChunkSet == null) return; // probably not nec.
        char[] inputChar = input.toCharArray();
        Tokenizer tokenizer = mNeDictionaryChunker.tokenizerFactory().tokenizer(inputChar,0,inputChar.length);
        String[] tokens = tokenizer.tokenize();
        Tagging<String> taggedStrings = mPosTagger.tag(Arrays.asList(tokens));//this mess is due to version change from 3 to 4
	String[] tags = taggedStrings.tags().toArray(new String[0]);//continued messyness. 
        //        for (int i = 0; i < tokens.length; ++i)
            //          System.out.println(tokens[i] + " " + tags[i]);

        Chunk[] chunkArray
            = nextChunkSet.<Chunk>toArray(new Chunk[nextChunkSet.size()]);
        Arrays.sort(chunkArray,Chunk.TEXT_ORDER_COMPARATOR);
        int posIndex = 0;
        for (int i = 0; i < chunkArray.length; ++i) {
            boolean rejectEntity = false;
            Chunk chunk = chunkArray[i];

            if (chunk.end() - chunk.start() < 2)
                continue;
            String text = input.substring(chunk.start(),chunk.end());
            if (mStopPhraseSet.contains(text)) continue;
            if (overlap(chunk,chunkSet)) continue;
            boolean pass = true;
            for (int j = 0; j < mStopSubstringList.length; ++j) {
                if (text.contains(mStopSubstringList[j]))
                    pass = false;
            }
            if (pass) {
                Chunk normChunk = ChunkFactory.createChunk(chunk.start(),chunk.end(),"OTHER");
                chunkSet.add(normChunk);
            }
        }
    }

    protected void addDictionaryWithPosFilter(Set<Chunk> chunkSet, String input) {
        if (mNeDictionaryChunker == null) return;
        Set<Chunk> nextChunkSet = mNeDictionaryChunker.chunk(input).chunkSet();
        if (nextChunkSet == null) return; // probably not nec.
        Chunk[] chunkArray
            = nextChunkSet.<Chunk>toArray(new Chunk[nextChunkSet.size()]);
        Arrays.sort(chunkArray,Chunk.TEXT_ORDER_COMPARATOR);

        PosTagFilter posTagFilter = new PosTagFilter(input,
                                                     mPosTagger,
                                                     mNeDictionaryChunker.tokenizerFactory());

        for (int i = 0; i < chunkArray.length; ++i) {
            Chunk chunk = chunkArray[i];
            String text = input.substring(chunk.start(),chunk.end());
            if (posTagFilter.fail(text)) continue;
            if (mStopPhraseSet.contains(text)) continue;
            if (overlap(chunk,chunkSet)) continue;
            chunkSet.add(chunk);
            //System.out.println("accepting " + text);
        }
    }

    protected void addDictionaryWithPosPhraseCountFilter(Set<Chunk> chunkSet, String input) {
        if (mNeDictionaryChunker == null) return;
        Set<Chunk> nextChunkSet = mNeDictionaryChunker.chunk(input).chunkSet();
        if (nextChunkSet == null) return; // probably not nec.
        Chunk[] chunkArray
            = nextChunkSet.<Chunk>toArray(new Chunk[nextChunkSet.size()]);
        Arrays.sort(chunkArray,Chunk.TEXT_ORDER_COMPARATOR);

        PosTagFilter posTagFilter = new PosTagFilter(input,
                                                     mPosTagger,
                                                     mNeDictionaryChunker.tokenizerFactory());

        for (int i = 0; i < chunkArray.length; ++i) {
            Chunk chunk = chunkArray[i];
            String text = input.substring(chunk.start(),chunk.end());
            if (failPhraseCountFilter(text) && posTagFilter.fail(text)) {
                //              System.out.println("Skipping: " + text);
                continue;
            }
            if (mStopPhraseSet.contains(text)) continue;
            if (overlap(chunk,chunkSet)) continue;
            chunkSet.add(chunk);
            //System.out.println("accepting " + text);
        }
    }

    protected boolean failPhraseCountFilter (String phrase) {
        Integer count = mPhraseCounts.get(phrase);
        if (count == null)
            return false;
        if (count > 5)
            return true;
        return false;
    }

    class PosTagFilter {

        int mPosIndex;
        String[] mTokens;
        String[] mTags;
        HmmDecoder mHmm;
        TokenizerFactory mTokenizerFactory;

        public PosTagFilter(String input,
                            HmmDecoder hmm,
                            TokenizerFactory entityTokenizer) {
            mPosIndex = 0;
            mHmm = hmm;
            mTokenizerFactory = entityTokenizer;

            char[] inputChar
                = input.toCharArray();
            Tokenizer tokenizer
                = mTokenizerFactory.tokenizer(inputChar,0,inputChar.length);

            mTokens = tokenizer.tokenize();
	    Tagging<String> tagging = mPosTagger.tag(Arrays.asList(mTokens));
	    
            mTags = tagging.tags().toArray(new String[0]);

            //         for (int i = 0; i < mTokens.length; ++i)
            //     System.out.println(mTokens[i] + " " + mTags[i]);
        }

        protected boolean fail (String entityPhrase) {

            char[] textChar = entityPhrase.toCharArray();
            Tokenizer entityTokenizer = mTokenizerFactory.tokenizer(textChar,0,textChar.length);
            boolean rejectEntity = false;
            String[] entityTokens = entityTokenizer.tokenize();
            //            for (int i = 0; i < entityTokens.length; ++i)
                //                System.out.println(entityTokens[i]);
            int entityMatchIndex = 0;
            while (entityMatchIndex < entityTokens.length) {

                if (entityTokens[entityMatchIndex].equals(mTokens[mPosIndex]) ) {
                    if (! mTags[mPosIndex].equals("np")
                        && ! mTokens[mPosIndex].equals("-")) {
                        rejectEntity = true;
                        //System.out.println("rejecting: ");
                    }
                    ++entityMatchIndex;
                }
                ++mPosIndex;
            }
            return rejectEntity;
        }
    }

    // incredibly inefficient quadratic and growing
    // only add new chunks if don't overlap
    protected void add(Set<Chunk> chunkSet, Chunker chunker, String input) {
        if (chunker == null) return;
        Set<Chunk> nextChunkSet = chunker.chunk(input).chunkSet();
        if (nextChunkSet == null) return; // probably not nec.
        for (Chunk chunk : nextChunkSet) {
            String text = input.substring(chunk.start(),chunk.end());
            System.out.println("Got " + text);
            if (mStopPhraseSet.contains(text)) continue;
            if (overlap(chunk,chunkSet)) continue;
            chunkSet.add(chunk);
        }
    }


    protected static boolean overlap(Chunk chunk, Set<Chunk> chunkSet) {
        for (Chunk chunk2 : chunkSet)
            if (overlap(chunk,chunk2))
                return true;
        return false;
    }

    protected static boolean overlap(Chunk c1, Chunk c2) {
        return overlapLeft(c1,c2) || overlapLeft(c2,c1);
    }

    protected static boolean overlapLeft(Chunk c1, Chunk c2) {
        return c2.start() < c1.end()
            && c1.end() <= c2.end();
    }

}
