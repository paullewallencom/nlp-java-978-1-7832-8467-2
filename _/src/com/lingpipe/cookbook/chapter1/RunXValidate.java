package com.lingpipe.cookbook.chapter1;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import com.aliasi.classify.BaseClassifierEvaluator;
import com.aliasi.classify.Classified;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.lm.NGramBoundaryLM;
import com.lingpipe.cookbook.Util;

public class RunXValidate {

	public static void main(String[] args) throws IOException {
		String inputPath = args.length > 0 ? args[0] : "data/disney_e_n.csv";	
		System.out.println("Training data is: " + inputPath);
		List<String[]> truthData = Util.readAnnotatedCsvRemoveHeader(new File(inputPath));
		int numFolds = 4;
		XValidatingObjectCorpus<Classified<CharSequence>> corpus =
			Util.loadXValCorpus(truthData, numFolds);
		corpus.permuteCorpus(new Random(123413));
		String[] categories = Util.getCategories(truthData);
		boolean storeInputs = false;
		BaseClassifierEvaluator<CharSequence> evaluator 
			= new BaseClassifierEvaluator<CharSequence>(null, categories, storeInputs);
		int maxCharNGram = 3;
		for (int i = 0; i < numFolds; ++i) {
			corpus.setFold(i);
			DynamicLMClassifier<NGramBoundaryLM> classifier 
				= DynamicLMClassifier.createNGramBoundary(categories, maxCharNGram);
			System.out.println("Training on fold " + i);
			//corpus.visitTrain(Util.corpusPrinter());
			corpus.visitTrain(classifier);
			evaluator.setClassifier(classifier);
			System.out.println("Testing on fold " + i);
			//corpus.visitTest(Util.corpusPrinter());
			corpus.visitTest(evaluator);
		}
		System.out.println(Util.confusionMatrixToString(evaluator.confusionMatrix()));
	}
}
