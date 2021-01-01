package com.lingpipe.cookbook.chapter3;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;

import com.aliasi.classify.BaseClassifier;
import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassifierEvaluator;
import com.aliasi.classify.ConfusionMatrix;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassification;
import com.aliasi.classify.JointClassifier;
import com.aliasi.classify.JointClassifierEvaluator;
import com.aliasi.classify.LMClassifier;
import com.aliasi.classify.LogisticRegressionClassifier;
import com.aliasi.classify.PrecisionRecallEvaluation;
import com.aliasi.classify.ScoredClassification;
import com.aliasi.classify.ScoredClassifier;
import com.aliasi.classify.ScoredClassifierEvaluator;
import com.aliasi.classify.ScoredPrecisionRecallEvaluation;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;
import com.aliasi.lm.CompiledNGramBoundaryLM;
import com.aliasi.lm.NGramBoundaryLM;
import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.MultivariateDistribution;
import com.aliasi.stats.RegressionPrior;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;
import com.lingpipe.cookbook.Util;

public class LinguisticTuning {

	

	static void featPrint(LogisticRegressionClassifier<CharSequence> lr) {
		for (String cat : lr.categorySymbols()) {
			ObjectToDoubleMap<String> feats = lr.featureValues(cat);
			String ZeroCategory = feats.size() > 0 ? "NON_ZERO " : "ZERO ";
			System.out.println("######################" + 
					"Printing features for category " + cat 
					+ " " + ZeroCategory);
			for (String feat : feats.keysOrderedByValueList()) {
				System.out.print(feat);
				System.out.printf(": %.2f\n",feats.getValue(feat));
			}
		}
	}

	public static void main(String[] args) throws IOException {
		String trainingFile = args.length > 0 ? args[0] 
					: "data/activeLearningCompleted/disneySentimentDedupe.2.csv";
		List<String[]> rows = Util.readAnnotatedCsvRemoveHeader(new File(trainingFile));
		int numFolds = 10;
		XValidatingObjectCorpus<Classified<CharSequence>> corpus =
			Util.loadXValCorpus(rows, numFolds);
		TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
			//= new NGramTokenizerFactory(2,4);
			//tokenizerFactory = new LowerCaseTokenizerFactory(tokenizerFactory);
		
		FeatureExtractor<CharSequence> featureExtractor
		= new TokenFeatureExtractor(tokenizerFactory);
		int minFeatureCount = 1;
		boolean addInterceptFeature = true;
		boolean noninformativeIntercept = true;
		RegressionPrior prior = RegressionPrior.gaussian(1.0,noninformativeIntercept);
		AnnealingSchedule annealingSchedule
		= AnnealingSchedule.exponential(0.00025,0.999);
		double minImprovement = 0.000000001;
		int minEpochs = 100;
		int maxEpochs = 2000;
		PrintWriter progressWriter = new PrintWriter(System.out,true);
		progressWriter.println("Reading data.");
		Reporter reporter = Reporters.writer(progressWriter);
		reporter.setLevel(LogLevel.WARN);
		boolean storeInputs = true;
		String[] categories = Util.getCategories(rows);
		ScoredClassifierEvaluator<CharSequence> evaluator 
			= new ScoredClassifierEvaluator<CharSequence>(null, categories, storeInputs);
		for (int i = 0; i < numFolds; ++i) {
			corpus.setFold(i);
			System.out.println("Training on fold " + i);
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
					
			featPrint(classifier);
			evaluator.setClassifier(classifier);		
			System.out.println("Testing on fold " + i);
			corpus.visitTest(evaluator);
		}
		
		Util.printConfusionMatrix(evaluator.confusionMatrix());
		Util.printPrecRecall(evaluator);
		Util.printFalsePositives("p", evaluator, corpus);
		
	}
}
