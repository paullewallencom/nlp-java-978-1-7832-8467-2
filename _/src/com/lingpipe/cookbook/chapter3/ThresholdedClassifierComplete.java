package com.lingpipe.cookbook.chapter3;

import java.util.ArrayList;
import java.util.List;

import com.aliasi.classify.ScoredClassification;
import com.aliasi.classify.ScoredClassifier;
import com.aliasi.util.ScoredObject;

public class ThresholdedClassifierComplete<E> implements ScoredClassifier<E> {
	ScoredClassifier<CharSequence> mNonThresholdedClassifier;

	public ThresholdedClassifierComplete (ScoredClassifier<CharSequence> classifier) {
		mNonThresholdedClassifier = classifier;
	}

	@Override
	public ScoredClassification classify(E input) {
		ScoredClassification classification 
			= mNonThresholdedClassifier.classify((CharSequence) input);
		List<ScoredObject<String>> scores = new ArrayList<ScoredObject<String>>();
		for (int i = 0; i < classification.size(); ++i) {
			String category = classification.category(i);
			Double score = classification.score(i);
			if (category.equals("p") && score < .94d) {
			//if (category.equals("p") && score < .69d) {
				score = 0.0;
			}
			if (category.equals("n") && score < .4d) {
				score = 0.0;
			}
			ScoredObject<String> scored 
				= new ScoredObject<String>(category,score);
			scores.add(scored);
		}
		classification = ScoredClassification.create(scores);
		return classification;
	}
}