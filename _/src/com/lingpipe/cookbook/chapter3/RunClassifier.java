package com.lingpipe.cookbook.chapter3;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.aliasi.classify.Classified;
import com.aliasi.classify.LogisticRegressionClassifier;
import com.aliasi.classify.ScoredClassification;
import com.aliasi.classify.ScoredClassifier;
import com.aliasi.classify.ScoredClassifierEvaluator;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.util.AbstractExternalizable;
import com.lingpipe.cookbook.Util;

public class RunClassifier {
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		String filePath = args.length > 0 ? args[0] : "data/freshDisney.csv";
		String modelPath = args.length > 1 ? args[1] : "models/ClassifierBuilder.LogisticRegression";
		System.out.println("Data is: " + filePath + " model is: " + modelPath);
		
		@SuppressWarnings("unchecked")
		LogisticRegressionClassifier<CharSequence> classifier
			= (LogisticRegressionClassifier<CharSequence>) AbstractExternalizable.readObject(new File(modelPath));
	
		
		File annotsFile = new File(filePath);
		List<String[]> rows = Util.readCsvRemoveHeader(annotsFile);
		int numFolds = 0;
		XValidatingObjectCorpus<Classified<CharSequence>> corpus 
			= Util.loadXValCorpus(rows, numFolds);
		boolean storeInputs = false;
		String[] categories = Util.getCategories(rows);
		if (categories.length < 2) {
			System.out.println("No annotations found, not evaluating");
		}
		else {
			ScoredClassifierEvaluator<CharSequence> evaluator 
				= new ScoredClassifierEvaluator<CharSequence>(classifier, categories, storeInputs);
			corpus.visitCorpus(evaluator);
			Util.printConfusionMatrix(evaluator.confusionMatrix());
			Util.printPrecRecall(evaluator);
			Util.printPRcurve(evaluator);
		}
		for (String[] row : rows) {
			ScoredClassification classification = classifier.classify(row[Util.TEXT_OFFSET]);
			row[Util.GUESSED_CLASS] = classification.bestCategory();
			row[Util.SCORE] = String.valueOf(classification.score(0));
		}
		System.out.println("writing scored output to " + annotsFile);
		Util.writeCsvAddHeader(rows, annotsFile);
	}
}
