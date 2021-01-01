package com.lingpipe.cookbook.chapter4;
import com.aliasi.classify.BaseClassifierEvaluator;
import com.aliasi.classify.ConfusionMatrix;
import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.Parser;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmCharLmEstimator;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.MarginalTaggerEvaluator;
import com.aliasi.tag.NBestTaggerEvaluator;
import com.aliasi.tag.TaggerEvaluator;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.xml.sax.InputSource;

public class TagEvaluator {
	public static void main(String[] args) 
			throws ClassNotFoundException, IOException {
		HmmDecoder decoder = null;
		boolean storeTokens = true;
		TaggerEvaluator<String> evaluator
			= new TaggerEvaluator<String>(decoder,storeTokens);
		Corpus<ObjectHandler<Tagging<String>>> smallCorpus = new TinyPosCorpus();
		int numFolds = 10;
		XValidatingObjectCorpus<Tagging<String>> xValCorpus 
			= new XValidatingObjectCorpus<Tagging<String>>(numFolds);
		smallCorpus.visitCorpus(xValCorpus);
		xValCorpus.permuteCorpus(new Random(123234235));
		for (int i = 0; i < numFolds; ++i) {
			xValCorpus.setFold(i);
			HmmCharLmEstimator estimator = new HmmCharLmEstimator();
			xValCorpus.visitTrain(estimator);
			System.out.println("done training " + estimator.numTrainingTokens());
			decoder = new HmmDecoder(estimator);
			evaluator.setTagger(decoder);
			xValCorpus.visitTest(evaluator);
		}
		BaseClassifierEvaluator<String> classifierEval 
			= evaluator.tokenEval();
		System.out.println(classifierEval);
	}
}
