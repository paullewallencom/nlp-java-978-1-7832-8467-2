package com.lingpipe.cookbook.chapter3;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aliasi.classify.BaseClassifier;
import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.lm.NGramBoundaryLM;
import com.lingpipe.cookbook.Util;

public class OverfittingClassifier implements BaseClassifier<CharSequence> {

	Map<String,Classification> mMap = new HashMap<String,Classification>();	

	@Override
	public Classification classify(CharSequence text) {
		if (mMap.containsKey(text)) {
			return mMap.get(text);
		}
		return new Classification("n");
	}
	
	public void handle(String text, Classification classification) {
		mMap.put(text, classification);
	}
	
	public static void main(String[] args) throws IOException {
		String dataPath = args.length > 0 ? args[0] : "data/disney_e_n.csv";
		List<String[]> annotatedData = Util.readAnnotatedCsvRemoveHeader(new File(dataPath));
		OverfittingClassifier classifier = new OverfittingClassifier();
		System.out.println("Training");
		for (String[] row: annotatedData) {
			String truth = row[Util.ANNOTATION_OFFSET];
			String text = row[Util.TEXT_OFFSET];
			classifier.handle(text,new Classification(truth));
		}
		Util.consoleInputBestCategory(classifier);
	}
}
