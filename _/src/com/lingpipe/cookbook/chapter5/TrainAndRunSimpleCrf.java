package com.lingpipe.cookbook.chapter5;

import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.TagChunkCodec;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.crf.ChainCrf;
import com.aliasi.crf.ChainCrfChunker;
import com.aliasi.crf.ChainCrfFeatureExtractor;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ScoredObject;
import com.lingpipe.cookbook.chapter4.SimpleCrfFeatureExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

public class TrainAndRunSimpleCrf {

	public static void main(String[] args) throws IOException {
		Corpus<ObjectHandler<Chunking>> corpus
		= new TinyEntityCorpus();

		TokenizerFactory tokenizerFactory
		= IndoEuropeanTokenizerFactory.INSTANCE;
		boolean enforceConsistency = true;
		TagChunkCodec tagChunkCodec
		= new BioTagChunkCodec(tokenizerFactory,
				enforceConsistency);

		ChainCrfFeatureExtractor<String> featureExtractor
		= new SimpleCrfFeatureExtractor();

		int minFeatureCount = 1;

		boolean cacheFeatures = true;

		boolean addIntercept = true;

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
		int minEpochs = 10;
		int maxEpochs = 5000;

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

		System.out.println("Done Training");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("Enter text followed by new line\n>");
			String evalText = reader.readLine();
			char[] evalTextChars = evalText.toCharArray();
			System.out.println("\nFIRST BEST");
			Chunking chunking = crfChunker.chunk(evalText);
			System.out.println(chunking);

			int maxNBest = 10;
			System.out.println("\n" + maxNBest + " BEST CONDITIONAL");
			System.out.println("Rank log p(tags|tokens)  Tagging");
			Iterator<ScoredObject<Chunking>> it
			= crfChunker.nBestConditional(evalTextChars,0,evalTextChars.length,maxNBest);
			for (int rank = 0; rank < maxNBest && it.hasNext(); ++rank) {
				ScoredObject<Chunking> scoredChunking = it.next();
				System.out.println(rank + "    " + scoredChunking.score() + " " + scoredChunking.getObject().chunkSet());
			}

			System.out.println("\nMARGINAL CHUNK PROBABILITIES");
			System.out.println("Rank Chunk Phrase");
			int maxNBestChunks = 10;
			Iterator<Chunk> nBestChunkIt = crfChunker.nBestChunks(evalTextChars,0,evalTextChars.length,maxNBestChunks);
			for (int n = 0; n < maxNBestChunks && nBestChunkIt.hasNext(); ++n) {
				Chunk chunk = nBestChunkIt.next();
				System.out.println(n + " " + chunk + " " + evalText.substring(chunk.start(),chunk.end()));
			}
		}
	}

}