package com.lingpipe.cookbook.chapter1;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.lm.NGramBoundaryLM;
import com.aliasi.util.AbstractExternalizable;
import com.lingpipe.cookbook.Util;


public class TrainAndWriteClassifierToDisk {
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		String inputPath = args.length > 0 ? args[0] : "data/disney_e_n.csv";	
		String outputPath = args.length > 1 ? args[1] : "models/my_disney_e_n.LMClassifier";
		System.out.println("Training on " + inputPath);
		List<String[]> annotatedData = Util.readAnnotatedCsvRemoveHeader(new File(inputPath));
		File outFile = new File(outputPath);
		String[] categoriesList = Util.getCategories(annotatedData);
		int maxCharNGram = 3;
		DynamicLMClassifier<NGramBoundaryLM> classifier 
			= DynamicLMClassifier.createNGramBoundary(categoriesList, maxCharNGram);
		int count = 1;
		for (String[] row : annotatedData) {
				classifier.train(row[Util.ANNOTATION_OFFSET], row[Util.TEXT_OFFSET], count);
		}
		AbstractExternalizable.compileTo(classifier,outFile);
		System.out.println("Wrote model to " + outFile);
	}

}

