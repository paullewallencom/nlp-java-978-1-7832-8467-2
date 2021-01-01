package com.lingpipe.cookbook.chapter4;

import java.util.List;

import com.aliasi.classify.ConditionalClassification;
import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.Handler;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.crf.ChainCrfFeatureExtractor;
import com.aliasi.crf.ChainCrfFeatures;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.TagLattice;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.IndoEuropeanTokenCategorizer;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToDoubleMap;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ModifiedCrfFeatureExtractor
implements ChainCrfFeatureExtractor<String> {

	HmmDecoder mDecoder;
	
	public ModifiedCrfFeatureExtractor() throws IOException, ClassNotFoundException {	
		File hmmFile = new File("models/pos-en-general-brown.HiddenMarkovModel");
		HiddenMarkovModel hmm = (HiddenMarkovModel) AbstractExternalizable.readObject(hmmFile);
		mDecoder = new HmmDecoder(hmm);
	}
	
	public ChainCrfFeatures<String> extract(List<String> tokens,
			List<String> tags) {
		return new ModChainCrfFeatures(tokens,tags);
	}

	class ModChainCrfFeatures extends ChainCrfFeatures<String> {
		
		TagLattice<String> mBrownTaggingLattice;
		
		public ModChainCrfFeatures(List<String> tokens,
				List<String> tags) {
			super(tokens,tags);
			mBrownTaggingLattice = mDecoder.tagMarginal(tokens);	
		}

		public Map<String,? extends Number> edgeFeatures(int n, int k) {
			ObjectToDoubleMap<String> features = new ObjectToDoubleMap<String>();
			features.set("TAG_" + tag(k),
					1.0d);
			String category = IndoEuropeanTokenCategorizer
					.CATEGORIZER
					.categorize(token(n));
			features.set("TOKEN_SHAPE_" + category,1.0d);
			return features;
		}
		
		public Map<String,? extends Number> nodeFeatures(int n) {
			ObjectToDoubleMap<String> features = new ObjectToDoubleMap<String>();
			features.set("TOK_" + token(n), 1);
			ConditionalClassification tagScores 
				= mBrownTaggingLattice.tokenClassification(n);
			for (int i = 0; i < 3; ++ i) {
				double conditionalProb = tagScores.score(i);
				String tag = tagScores.category(i);
				features.increment(tag, conditionalProb);
			}
			return features;
		}

	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Corpus <ObjectHandler<Tagging<String>>> corpus = new TinyPosCorpus();
		final ChainCrfFeatureExtractor<String> featureExtractor 
			= new ModifiedCrfFeatureExtractor();
		corpus.visitCorpus(new ObjectHandler<Tagging<String>>() {
			@Override
			public void handle(Tagging<String> tagging) {
				ChainCrfFeatures<String> features = featureExtractor.extract(tagging.tokens(),tagging.tags());
				for (int i = 0; i < tagging.size(); ++i) {
					System.out.println("-------------------");
					System.out.println("Tagging:  " + tagging.token(i) + "/" + tagging.tag(i));
					System.out.println("Node Feats:" + features.nodeFeatures(i));
					for (int j = 0; j < tagging.size(); ++j) {
						System.out.println("Edge Feats:" + features.edgeFeatures(i, j));
					}
				}
			}
		});
	}
}
