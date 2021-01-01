package com.lingpipe.cookbook.chapter3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.LogisticRegressionClassifier;
import com.aliasi.classify.NaiveBayesClassifier;
import com.aliasi.classify.TradNaiveBayesClassifier;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;
import com.aliasi.lm.NGramBoundaryLM;
import com.aliasi.lm.TokenizedLM;
import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;
import com.lingpipe.cookbook.Util;

import au.com.bytecode.opencsv.CSVReader;


public class TrainAndRunLogReg {
	
	public static void main(String[] args) throws IOException {
		String trainingFile = args.length > 0 ? args[0] : "data/disney_e_n.csv";
		String modelFile = args.length > 1 ? args[1] : "models/disney_e_n.LogisticRegression";
		int numFolds = 0;
		List<String[]> training 
			= Util.readAnnotatedCsvRemoveHeader(new File(trainingFile));
		XValidatingObjectCorpus<Classified<CharSequence>> corpus 
			= Util.loadXValCorpus(training,numFolds);
		
		TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		FeatureExtractor<CharSequence> featureExtractor
			= new TokenFeatureExtractor(tokenizerFactory);
		int minFeatureCount = 1;
		boolean addInterceptFeature = false;
		boolean noninformativeIntercept = true;
		double priorVariance = 2 ;
		RegressionPrior prior = RegressionPrior.laplace(priorVariance,noninformativeIntercept);
		AnnealingSchedule annealingSchedule
			= AnnealingSchedule.exponential(0.00025,0.999);
		double minImprovement = 0.000000001;
		int minEpochs = 100;
		int maxEpochs = 2000;
		PrintWriter progressWriter = new PrintWriter(System.out,true);
		progressWriter.println("Reading data.");
		Reporter reporter = Reporters.writer(progressWriter);
		reporter.setLevel(LogLevel.INFO);

		LogisticRegressionClassifier<CharSequence> classifier
		= LogisticRegressionClassifier.<CharSequence>train(corpus,
				featureExtractor,
				minFeatureCount,
				addInterceptFeature,
				prior,
				annealingSchedule,
				minImprovement,
				minEpochs,
				maxEpochs,
				reporter);
	
		AbstractExternalizable.compileTo(classifier, new File("models/myModel.LogisticRegression"));	
		Util.consoleInputPrintClassification(classifier);
	}
	
}

