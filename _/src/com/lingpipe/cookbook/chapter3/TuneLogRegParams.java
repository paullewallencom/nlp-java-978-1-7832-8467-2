package com.lingpipe.cookbook.chapter3;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassifierEvaluator;
import com.aliasi.classify.LogisticRegressionClassifier;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;
import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;
import com.lingpipe.cookbook.Util;



public class TuneLogRegParams {
	
	public static void main(String[] args) throws IOException {
		String trainingFile = args.length > 0 ? args[0] : "data/disney_e_n.csv";
		int numFolds = 10;
		List<String[]> training 
			= Util.readAnnotatedCsvRemoveHeader(new File(trainingFile));
		String[] categories = Util.getCategories(training);
		XValidatingObjectCorpus<Classified<CharSequence>> corpus 
			= Util.loadXValCorpus(training,numFolds);
		int min = 2;
		int max = 4;
		TokenizerFactory tokenizerFactory //= new NGramTokenizerFactory(min,max);
			= IndoEuropeanTokenizerFactory.INSTANCE;
		FeatureExtractor<CharSequence> featureExtractor
			= new TokenFeatureExtractor(tokenizerFactory);
		int minFeatureCount = 1;
		boolean addInterceptFeature = true;
		boolean noninformativeIntercept = false;
		double priorVariance = 2;
		RegressionPrior prior 
			//= RegressionPrior.laplace(priorVariance,noninformativeIntercept);
		= RegressionPrior.gaussian(priorVariance,noninformativeIntercept);
		AnnealingSchedule annealingSchedule
			= AnnealingSchedule.exponential(0.00025,0.999);
		double minImprovement = 0.000000001;
		int minEpochs = 10;
		int maxEpochs = 20;
		PrintWriter progressWriter = new PrintWriter(System.out,true);
		Reporter reporter = Reporters.writer(progressWriter);
		reporter.setLevel(LogLevel.WARN);
		int numThreads = 2;
		ConditionalClassifierEvaluator<CharSequence> eval = Util.xvalLogRegMultiThread(corpus,
				featureExtractor,
				minFeatureCount,
				addInterceptFeature,
				prior,
				annealingSchedule,
				minImprovement,
				minEpochs,
				maxEpochs,
				reporter,
				numFolds,
				numThreads,
				categories);
		Util.printPRcurve(eval);
		Util.printConfusionMatrix(eval.confusionMatrix());
		corpus.setNumFolds(0);
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
		int featureCount = 0;
		for (String category : categories) {
			ObjectToDoubleMap<String> featureCoeff = classifier.featureValues(category);
			System.out.println("Feature coefficients for category " + category);
			for (String feature : featureCoeff.keysOrderedByValueList()) {
				++featureCount;
				System.out.print(feature);
				System.out.printf(" : %.8f\n",featureCoeff.getValue(feature));
			}
		}
		System.out.println("Got feature count: " + featureCount);
		Util.consoleInputPrintClassification(classifier);
	}
}

