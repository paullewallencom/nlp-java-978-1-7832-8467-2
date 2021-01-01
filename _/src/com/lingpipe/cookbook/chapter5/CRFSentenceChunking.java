package com.lingpipe.cookbook.chapter5;

import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.chunk.TagChunkCodec;
import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ListCorpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XValidatingObjectCorpus;

import com.aliasi.crf.ChainCrf;
import com.aliasi.crf.ChainCrfChunker;
import com.aliasi.crf.ChainCrfFeatureExtractor;
import com.aliasi.crf.ForwardBackwardTagLattice;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.sentences.SentenceChunker;
import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;

import com.aliasi.tag.ScoredTagging;
import com.aliasi.tag.TagLattice;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.aliasi.crf.ChainCrfFeatureExtractor;
import com.aliasi.crf.ChainCrfFeatures;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CRFSentenceChunking {
	
	
	static Chunking getChunking(TokenizerFactory tokenizerFactory, char[] text) {
		List<Chunk> chunks = new ArrayList<Chunk>();
		StringBuilder sb = new StringBuilder();
		Tokenizer tokenizer = tokenizerFactory.tokenizer(text, 0, text.length);
		String token = null;
		int sentStart = -1;
		int sentBody = 0;
		while ((token = tokenizer.nextToken()) != null) {
			String whiteSpace = tokenizer.nextWhitespace();
			if (token.equals("[")) {
				sentStart = sb.length();
				sentBody = 0;
			}
			else if (token.equals("]")) {
				chunks.add(ChunkFactory.createChunk(sentStart,sentStart + sentBody,"S"));
				sentStart = -1;
			}
			else {
				sb.append(token);
				sentBody += token.length();
				sentBody += whiteSpace.length();
			}
			sb.append(whiteSpace);
		}
		ChunkingImpl chunking = new ChunkingImpl(sb.toString());
		chunking.addAll(chunks);
		return chunking;
	}
	
    public static void main(String[] args) throws IOException {
    	TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
    	char[] text = Files.readCharsFromFile(new File(args[0]), Strings.UTF8);
		Chunking chunking = getChunking(tokenizerFactory,text);
		
		System.out.println("Training Chunking \n" + chunking);
		String t = (String) chunking.charSequence();
		for (Chunk chunk : chunking.chunkSet()) {
			System.out.println(t.substring(chunk.start(), chunk.end()) + "|");
		}
		ListCorpus<Chunking> corpus
			= new ListCorpus<Chunking> ();
		corpus.addTrain(chunking);
		boolean enforceConsistency = false;
        TagChunkCodec tagChunkCodec
            = new BioTagChunkCodec(tokenizerFactory,
                                   enforceConsistency);
      
       
        ChainCrfFeatureExtractor<String> featureExtractor
            = null;//new SimpleChainCrfFeatureExtractor();
        boolean addIntercept = true;
        int minFeatureCount = 1;
        boolean cacheFeatures = false;
        boolean allowUnseenTransitions = true;
        double priorVariance = 4.0;
        boolean uninformativeIntercept = true;
        RegressionPrior prior
            = RegressionPrior.gaussian(priorVariance,
                                       uninformativeIntercept);
        int priorBlockSize = 3;
        double initialLearningRate = 0.05;
        double learningRateDecay = 0.995;
        AnnealingSchedule annealingSchedule
            = AnnealingSchedule.exponential(initialLearningRate,
                                            learningRateDecay);
        double minImprovement = 0.00001;
        int minEpochs = 2;
        int maxEpochs = 2000;
        Reporter reporter
            = Reporters.stdOut().setLevel(LogLevel.DEBUG);
        System.out.println("\nEstimating");
        ChainCrfChunker crfChunker
        	= ChainCrfChunker.estimate(corpus,
                                   tagChunkCodec,
                                   tokenizerFactory,
                                   featureExtractor,
                                   addIntercept,
                                   minFeatureCount,
                                   cacheFeatures,
                                   prior,
                                   priorBlockSize,
                                   annealingSchedule,
                                   minImprovement,
                                   minEpochs,
                                   maxEpochs,
                                   reporter);

        
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("Enter text followed by new line\n>");
			System.out.flush();
			String evalText = reader.readLine();
			List<String> evalTokens = Arrays.asList(tokenizerFactory.tokenizer(evalText.toCharArray(),0,evalText.length()).tokenize());
			Chunking evalChunking = crfChunker.chunk(evalText);
			Tagging<String> tagging2 = crfChunker.crf().tag(evalTokens);
			System.out.println(tagging2);
			System.out.println(evalChunking);
			
            
		}
    }
}


