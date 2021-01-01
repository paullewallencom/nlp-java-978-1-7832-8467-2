package com.lingpipe.cookbook.chapter3;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassifierEvaluator;
import com.aliasi.classify.LogisticRegressionClassifier;
import com.aliasi.classify.ScoredPrecisionRecallEvaluation;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;
import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;
import com.lingpipe.cookbook.Util;



public class BuildLogRegSys {
	
	public static void main(String[] args) throws IOException {
		String trainingFile = args.length > 0 ? args[0] : "data2/allDisneyDeduped.4.csv";
		int numFolds = 10;
		List<String[]> training 
			= Util.readAnnotatedCsvRemoveHeader(new File(trainingFile));
		String[] categories = Util.getCategories(training);
		XValidatingObjectCorpus<Classified<CharSequence>> corpus 
			= Util.loadXValCorpus(training,numFolds);
		int min = 2;
		int max = 4;
		TokenizerFactory tokenizerFactory 
			//= new NGramTokenizerFactory(min,max);
			= IndoEuropeanTokenizerFactory.INSTANCE;
		FeatureExtractor<CharSequence> featureExtractor
			= new TokenFeatureExtractor(tokenizerFactory);
		int minFeatureCount = 2;
		boolean addInterceptFeature = true;
		boolean noninformativeIntercept = false;
		double priorVariance = 1;
		RegressionPrior prior 
			= RegressionPrior.laplace(priorVariance,noninformativeIntercept);
		//= RegressionPrior.gaussian(priorVariance,noninformativeIntercept);
		AnnealingSchedule annealingSchedule
			= AnnealingSchedule.exponential(0.00025,0.999);
		double minImprovement = 0.000000001;
		int minEpochs = 2;
		int maxEpochs = 10000;
		PrintWriter progressWriter = new PrintWriter(System.out,true);
		Reporter reporter = Reporters.writer(progressWriter);
		reporter.setLevel(LogLevel.INFO);
		int numThreads = 3;
		ConditionalClassifierEvaluator<CharSequence> evaluator = Util.xvalLogRegMultiThread(corpus,
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
		
		reporter.setLevel(LogLevel.INFO);
		Util.printConfusionMatrix(evaluator.confusionMatrix());
		for (String category : categories) {
			Util.printFalsePositives(category, evaluator,corpus);
			ScoredPrecisionRecallEvaluation prEval = evaluator.scoredOneVersusAll(category);
			boolean interpolation = false;
			double[][] prScoreCurve = prEval.prScoreCurve(interpolation);
			reporter.report(LogLevel.INFO,"PR curve for: " + category );
			reporter.report(LogLevel.INFO,"Recall, Prec, Score");
			for (double[] row : prScoreCurve) {
				reporter.report(LogLevel.INFO,String.format("%.3f,%.3f,%.3f\n",row[0],row[1],row[2]));
			}
			//System.out.println(prEval.toString());	
		}
	/*	reporter.setLevel(LogLevel.WARN);
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
		*/
		
	}
}

