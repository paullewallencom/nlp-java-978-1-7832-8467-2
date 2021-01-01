package com.lingpipe.cookbook.chapter3;

import java.util.Map;

import com.aliasi.util.Counter;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToCounterMap;


public class ContainsNumberFeatureExtractor implements FeatureExtractor<CharSequence> {

	@Override
	public Map<String,Counter> features(CharSequence text) {
		ObjectToCounterMap<String> featureMap = new ObjectToCounterMap<String>();
		if (text.toString().matches(".*\\d.*")) {
			featureMap.set("CONTAINS_NUMBER", 1);
		}
		return featureMap;
	}
	
	public static void main(String[] args) {
		FeatureExtractor<CharSequence> featureExtractor = new ContainsNumberFeatureExtractor();
		System.out.println(featureExtractor.features("I have a number 1"));
	}
}	

