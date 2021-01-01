package com.lingpipe.cookbook.chapter3;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.LogisticRegressionClassifier;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;


public class RunLogisticRegression {

	public static void main (String[] args) throws IOException, ClassNotFoundException {
		String modelFile = args.length > 0 ? args[0] : "models/disney_e_n.LogisticRegression";
		@SuppressWarnings("unchecked")
		LogisticRegressionClassifier<CharSequence> classifier 
			= (LogisticRegressionClassifier<CharSequence>) AbstractExternalizable.readObject(new File(modelFile));

		FeatureExtractor<CharSequence> featureExtractor = classifier.featureExtractor();
		List<String> categories = classifier.categorySymbols();
		for (String category : categories) {
			ObjectToDoubleMap<String> featureCoeff = classifier.featureValues(category);
			System.out.println("Feature coefficients for category " + category);
			for (String feature : featureCoeff.keysOrderedByValueList()) {
				System.out.print(feature);
				System.out.printf(" : %.2f\n",featureCoeff.getValue(feature));
			}
		}
		
		BufferedReader reader = new BufferedReader(new 	InputStreamReader(System.in));
		while (true) {
			System.out.println("\nType a string to be classified");
			String data = reader.readLine();
			ConditionalClassification classification 
				= classifier.classify(data);
			System.out.println(classification);
		}
		
		
		
	}
	
}
