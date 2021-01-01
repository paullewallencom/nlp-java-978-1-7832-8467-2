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

public class LinguisticTuningLogisticRegression {

	static int ANNOTATION_OFFSET = 2;
	static int TEXT_OFFSET = 3;
	//static int NUM_FOLDS = 2;
	static int NUM_FOLDS = 10;

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
		String fileName = args[0];
		CSVReader csvReader = new CSVReader(new FileReader(fileName));
		csvReader.readNext();//skip headers
		List<String[]> annotatedData = csvReader.readAll();
		XValidatingObjectCorpus<Classified<CharSequence>> corpus 
		= new XValidatingObjectCorpus<Classified<CharSequence>>(NUM_FOLDS);
		Set<String> categories = new HashSet<String>();
		for (String[] tweetData : annotatedData) {
			if (tweetData[ANNOTATION_OFFSET].equals("")) {
				continue;
			}
			Classification classification = new Classification(tweetData[ANNOTATION_OFFSET]);
			Classified<CharSequence> classified = new Classified<CharSequence>(tweetData[TEXT_OFFSET],classification);
			corpus.handle(classified);
			categories.add(tweetData[ANNOTATION_OFFSET]);
		}
		csvReader.close();
		TokenizerFactory tokenizerFactory 
			= new NGramTokenizerFactory(2,4);
			tokenizerFactory = new LowerCaseTokenizerFactory(tokenizerFactory);
			//= IndoEuropeanTokenizerFactory.INSTANCE;
		
			//= new NGramTokenizerFactory(2,5);
		
		FeatureExtractor<CharSequence> featureExtractor
		= new TokenFeatureExtractor(tokenizerFactory);
		int minFeatureCount = 2;
		boolean addInterceptFeature = true;
		boolean noninformativeIntercept = false;
		RegressionPrior prior = RegressionPrior.gaussian(1.0,noninformativeIntercept);
		AnnealingSchedule annealingSchedule
		= AnnealingSchedule.exponential(0.00025,0.999);
		double minImprovement = 0.000000001;
		int minEpochs = 100;
		int maxEpochs = 5000;

		int blockSize = corpus.size(); // reduces to conjugate gradient
		LogisticRegressionClassifier<CharSequence> hotStart = null;
		int rollingAvgSize = 10;
		ObjectHandler<LogisticRegressionClassifier<CharSequence>> classifierHandler
		= null;
		PrintWriter progressWriter = new PrintWriter(System.out,true);
		progressWriter.println("Reading data.");
		Reporter reporter = Reporters.writer(progressWriter);
		reporter.setLevel(LogLevel.WARN);
		boolean storeInputs = true;
		ScoredClassifierEvaluator<CharSequence> evaluator 
		= new ScoredClassifierEvaluator<CharSequence>(null, categories.toArray(new String[0]), storeInputs);
		for (int i = 0; i < NUM_FOLDS; ++i) {
			corpus.setFold(i);
			System.out.println("Training on fold " + i);
			LogisticRegressionClassifier<CharSequence> classifier
			= LogisticRegressionClassifier.<CharSequence>train(corpus,
					featureExtractor,
					minFeatureCount,
					addInterceptFeature,
					prior,
					blockSize,
					hotStart,
					annealingSchedule,
					minImprovement,
					rollingAvgSize,
					minEpochs,
					maxEpochs,
					classifierHandler,
					reporter);
			//featPrint(classifier);

			ScoredClassifier<CharSequence> thresholdedClassifier 
			= new ThresholdedClassifierComplete<CharSequence>(classifier);
			evaluator.setClassifier(thresholdedClassifier);		
			System.out.println("Testing on fold " + i);
			corpus.visitTest(evaluator);
			//System.out.println("!!!TESTING ON TRAINING!!!");
			//corpus.visitTrain(evaluator);
		}
		//System.out.println(evaluator);
		ConfusionMatrix confMatrix = evaluator.confusionMatrix();
		String[] labels = confMatrix.categories();
		int[][] outcomes = confMatrix.matrix();
		System.out.println("reference\\response");
		System.out.print("          \\");
		for (String category : labels) {
			System.out.print(category + ",");
		}
		for (int i = 0; i< outcomes.length; ++ i) {
			System.out.print("\n         " + labels[i] + " ");
			for (int j = 0; j < outcomes[i].length; ++j) {
				System.out.print(outcomes[i][j] + ",");
			}
		}
		
		System.out.println("");
		String category = "p";
		PrecisionRecallEvaluation prEval = evaluator.oneVersusAll(category);
		System.out.println("Category " + category);
		System.out.printf("Recall: %.2f\n", prEval.recall());
		System.out.printf("Prec  : %.2f\n", prEval.precision());

		category = "n";
		prEval = evaluator.oneVersusAll(category);
		System.out.println("Category " + category);
		System.out.printf("Recall: %.2f\n", prEval.recall());
		System.out.printf("Prec  : %.2f\n", prEval.precision());
		
		category = "p";
		ScoredPrecisionRecallEvaluation scoredPrEval = evaluator.scoredOneVersusAll(category);
		boolean interpolate = false;
		double[][] scoredCurve = scoredPrEval.prScoreCurve(interpolate);
		ScoredPrecisionRecallEvaluation.printScorePrecisionRecallCurve(scoredCurve,progressWriter);
		System.out.println(scoredPrEval);
		List<Classified<CharSequence>> falsePositives = evaluator.falsePositives(category);
		System.out.println("False Positives for " + category);
		for (Classified<CharSequence> classified: falsePositives) {
			System.out.println(classified.getClassification().bestCategory() + ": " + classified.getObject());
		}
		System.out.println("False Negatives for " + category);
		for (Classified<CharSequence> classified: evaluator.falseNegatives(category)) {
			System.out.println(classified.getClassification().bestCategory() + ": " + classified.getObject());
		}
	}
}
