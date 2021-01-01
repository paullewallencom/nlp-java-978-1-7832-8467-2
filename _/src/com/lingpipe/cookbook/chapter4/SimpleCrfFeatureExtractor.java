package com.lingpipe.cookbook.chapter4;

import com.aliasi.crf.ChainCrfFeatureExtractor;
import com.aliasi.crf.ChainCrfFeatures;
import com.aliasi.tag.Tagging;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToDoubleMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SimpleCrfFeatureExtractor
    implements ChainCrfFeatureExtractor<String> {

    public ChainCrfFeatures<String> extract(List<String> tokens,
                                            List<String> tags) {
        return new SimpleChainCrfFeatures(tokens,tags);
    }

    static class SimpleChainCrfFeatures
        extends ChainCrfFeatures<String> {

        public SimpleChainCrfFeatures(List<String> tokens,
                                      List<String> tags) {
            super(tokens,tags);
        }
        
        public Map<String,Double> nodeFeatures(int n) {
        	ObjectToDoubleMap<String> features = new ObjectToDoubleMap<String>();
			features.increment("TOK_" + token(n),1.0);
			return features;
        }
        
        
        
        public Map<String,Double> edgeFeatures(int n, int k) {
        	ObjectToDoubleMap<String> features = new ObjectToDoubleMap<String>();
        	features.increment("TAG_" + tag(k),1.0);
        	return features;
        }
    }
}