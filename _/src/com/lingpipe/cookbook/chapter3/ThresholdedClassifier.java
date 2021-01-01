package com.lingpipe.cookbook.chapter3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.ConditionalClassifier;
import com.aliasi.classify.LogisticRegressionClassifier;
import com.aliasi.classify.ScoredClassification;
import com.aliasi.classify.ScoredClassifier;
import com.aliasi.classify.ScoredClassifierEvaluator;
import com.aliasi.classify.ScoredPrecisionRecallEvaluation;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.ScoredObject;
import com.lingpipe.cookbook.Util;

public class ThresholdedClassifier<E> implements ScoredClassifier<E> {
	ConditionalClassifier<E> mNonThresholdedClassifier;

	public ThresholdedClassifier (ConditionalClassifier<E> classifier) {
		mNonThresholdedClassifier = classifier;
	}

	@Override
	public ScoredClassification classify(E input) {
		ConditionalClassification classification 
		= mNonThresholdedClassifier.classify(input);
		List<ScoredObject<String>> scores = new ArrayList<ScoredObject<String>>();
		for (int i = 0; i < classification.size(); ++i) {
			String category = classification.category(i);
			Double score = classification.score(i);
			if (category.equals("p") && score < .76d) {
				score = 0.0;
			}
			if (category.equals("n") && score < .549d) {
				score = 0.0;
			}
			ScoredObject<String> scored 
			= new ScoredObject<String>(category,score);
			scores.add(scored);
		}
		ScoredClassification thresholded = ScoredClassification.create(scores);
		return thresholded;
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		String filePath = args.length > 0 ? args[0] : "data/freshDisneyAnnotated.csv";
		String modelPath = args.length > 1 ? args[1] : "models/ClassifierBuilder.LogisticRegression";
		System.out.println("Data is: " + filePath + " model is: " + modelPath);

		@SuppressWarnings("unchecked")
		LogisticRegressionClassifier<CharSequence> baseClassifier
		= (LogisticRegressionClassifier<CharSequence>) AbstractExternalizable.readObject(new File(modelPath));

		ScoredClassifier<CharSequence> classifier = new ThresholdedClassifier<CharSequence>(baseClassifier);
		File annotsFile = new File(filePath);
		List<String[]> rows = Util.readCsvRemoveHeader(annotsFile);
		int numFolds = 0;
		XValidatingObjectCorpus<Classified<CharSequence>> corpus 
		= Util.loadXValCorpus(rows, numFolds);
		boolean storeInputs = false;
		String[] categories = Util.getCategories(rows);

		ScoredClassifierEvaluator<CharSequence> evaluator 
		= new ScoredClassifierEvaluator<CharSequence>(classifier, categories, storeInputs);
		corpus.visitCorpus(evaluator);
		Util.printConfusionMatrix(evaluator.confusionMatrix());
		Util.printPrecRecall(evaluator);
		Util.printPRcurve(evaluator);
	}


}