package com.lingpipe.cookbook.chapter1;


import java.io.File;
import java.io.IOException;
import java.util.List;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.lm.NGramBoundaryLM;
import com.lingpipe.cookbook.Util;

public class TrainAndRunLMClassifier {

	public static void main(String[] args) throws IOException {
		String dataPath = args.length > 0 ? args[0] : "data/disney_e_n.csv";
		List<String[]> annotatedData = Util.readAnnotatedCsvRemoveHeader(new File(dataPath));
		String[] categories = Util.getCategories(annotatedData);
		int maxCharNGram = 3;
		DynamicLMClassifier<NGramBoundaryLM> classifier 
			= DynamicLMClassifier.createNGramBoundary(categories,maxCharNGram);
		for (String[] row: annotatedData) {
			String truth = row[Util.ANNOTATION_OFFSET];
			String text = row[Util.TEXT_OFFSET];
			Classification classification = new Classification(truth);
			Classified<CharSequence> classified = new Classified<CharSequence>(text,classification);
			classifier.handle(classified);
			//int count = 1;
			//classifier.train(truth,text,count);
		}
		Util.consoleInputPrintClassification(classifier);
	}
}

