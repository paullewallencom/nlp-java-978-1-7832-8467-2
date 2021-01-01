package com.lingpipe.cookbook.chapter4;


import com.aliasi.corpus.Corpus;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.crf.ChainCrf;
import com.aliasi.crf.ChainCrfFeatureExtractor;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;


import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;


import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;




import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CRFTagger {

	public static void main(String[] args) throws IOException {
		Corpus<ObjectHandler<Tagging<String>>> corpus = new TinyPosCorpus();
		final ChainCrfFeatureExtractor<String> featureExtractor
			= new SimpleCrfFeatureExtractor();
			//= new ModifiedCrfFeatureExtractor();
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
			= Reporters.stdOut().setLevel(LogLevel.INFO);
		System.out.println("\nEstimating");
		ChainCrf<String> crf
			= ChainCrf.estimate(corpus,
				featureExtractor,
				addIntercept,
				minFeatureCount,
				cacheFeatures,
				allowUnseenTransitions,
				prior,
				priorBlockSize,
				annealingSchedule,
				minImprovement,
				minEpochs,
				maxEpochs,
				reporter);

		TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("Enter text followed by new line\n>");
			System.out.flush();
			String text = reader.readLine();
			Tokenizer tokenizer = tokenizerFactory.tokenizer(text.toCharArray(),0,text.length());
			List<String> evalTokens = Arrays.asList(tokenizer.tokenize());
			Tagging<String> evalTagging = crf.tag(evalTokens);
			System.out.println(evalTagging);
			/*int maxNBest = 5;
			Iterator<ScoredTagging<String>> it
			= crf.tagNBestConditional(evalTokens,maxNBest);
			for (int rank = 0; rank < maxNBest && it.hasNext(); ++rank) {
				ScoredTagging<String> scoredTagging = it.next();
				System.out.println(rank + "    " + scoredTagging);
			}
			TagLattice<String> fbLattice
			= crf.tagMarginal(evalTokens);
			for (int n = 0; n < evalTokens.size(); ++n) {
				System.out.println(evalTokens.get(n));
				for (int k = 0; k < fbLattice.numTags(); ++k) {
					String tag = fbLattice.tag(k);
					double prob = fbLattice.logProbability(n,k);
					System.out.println("     " + tag + " " + prob);
				}
			}
			*/

		}
	}
}


