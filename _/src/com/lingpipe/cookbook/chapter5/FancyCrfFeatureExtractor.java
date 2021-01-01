package com.lingpipe.cookbook.chapter5;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.crf.ChainCrfFeatureExtractor;
import com.aliasi.crf.ChainCrfFeatures;

import com.aliasi.tag.Tagger;
import com.aliasi.tag.Tagging;

import com.aliasi.tokenizer.IndoEuropeanTokenCategorizer;

import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FastCache;
import com.aliasi.util.ObjectToDoubleMap;
import com.lingpipe.cookbook.chapter4.ModifiedCrfFeatureExtractor;
import com.lingpipe.cookbook.chapter4.TinyPosCorpus;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FancyCrfFeatureExtractor
implements ChainCrfFeatureExtractor<String> {

	private final Tagger<String> mPosTagger;

	public FancyCrfFeatureExtractor()
			throws ClassNotFoundException, IOException {
		File posHmmFile = new File("models/pos-en-general-brown.HiddenMarkovModel");
		@SuppressWarnings("unchecked") 
		HiddenMarkovModel posHmm
		= (HiddenMarkovModel)
		AbstractExternalizable
		.readObject(posHmmFile);
		FastCache<String,double[]> emissionCache
			= new FastCache<String,double[]>(100000);
		mPosTagger = new HmmDecoder(posHmm,null,emissionCache);
	}

	public ChainCrfFeatures<String> extract(List<String> tokens,
			List<String> tags) {
		return new ChunkerFeatures(tokens,tags);
	}


	class ChunkerFeatures extends ChainCrfFeatures<String> {
		private final Tagging<String> mPosTagging;
		public ChunkerFeatures(List<String> tokens,
				List<String> tags) {
			super(tokens,tags);
			mPosTagging = mPosTagger.tag(tokens);
		}
		public Map<String,? extends Number> nodeFeatures(int n) {
			ObjectToDoubleMap<String> feats
			= new ObjectToDoubleMap<String>();

			boolean bos = n == 0;
			boolean eos = (n + 1) >= numTokens();

			String tokenCat = tokenCat(n);
			String prevTokenCat = bos ? null : tokenCat(n-1);
			String nextTokenCat = eos ? null : tokenCat(n+1);

			String token = normedToken(n);
			String prevToken = bos ? null : normedToken(n-1);
			String nextToken = eos ? null : normedToken(n+1);

			String posTag = mPosTagging.tag(n);
			String prevPosTag = bos ? null : mPosTagging.tag(n-1);
			String nextPosTag = eos ? null : mPosTagging.tag(n+1);

			if (bos)
				feats.set("BOS",1.0);
			if (eos)
				feats.set("EOS",1.0);
			if (!bos && !eos)
				feats.set("!BOS!EOS",1.0);

			feats.set("TOK_" + token, 1.0);
			if (!bos)
				feats.set("TOK_PREV_" + prevToken,1.0);
			if (!eos)
				feats.set("TOK_NEXT_" + nextToken,1.0);

			feats.set("TOK_CAT_" + tokenCat, 1.0);
			if (!bos)
				feats.set("TOK_CAT_PREV_" + prevTokenCat, 1.0);
			if (!eos)
				feats.set("TOK_CAT_NEXT_" + nextToken, 1.0);

			feats.set("POS_" + posTag,1.0);
			if (!bos)
				feats.set("POS_PREV_" + prevPosTag,1.0);
			if (!eos)
				feats.set("POS_NEXT_" + nextPosTag,1.0);

			for (String suffix : suffixes(token))
				feats.set("SUFF_" + suffix,1.0);
			if (!bos)
				for (String suffix : suffixes(prevToken))
					feats.set("SUFF_PREV_" + suffix,1.0);
			if (!eos)
				for (String suffix : suffixes(nextToken))
					feats.set("SUFF_NEXT_" + suffix,1.0);

			for (String prefix : prefixes(token))
				feats.set("PREF_" + prefix,1.0);
			if (!bos)
				for (String prefix : prefixes(prevToken))
					feats.set("PREF_PREV_" + prefix,1.0);
			if (!eos)
				for (String prefix : prefixes(nextToken))
					feats.set("PREF_NEXT_" + prefix,1.0);

			return feats;
		}

		public Map<String,? extends Number> edgeFeatures(int n, int k) {
			ObjectToDoubleMap<String> feats
			= new ObjectToDoubleMap<String>();
			feats.set("PREV_TAG_" + tag(k),
					1.0);
			feats.set("PREV_TAG_TOKEN_CAT_"  + tag(k)
					+ "_" + tokenCat(n-1),
					1.0);
			return feats;
		}

		// e.g. 12/3/08 to *DD*/*D*/*DD*
		public String normedToken(int n) {
			return token(n).replaceAll("\\d+","*$0*").replaceAll("\\d","D");
		}

		public String tokenCat(int n) {
			return IndoEuropeanTokenCategorizer.CATEGORIZER.categorize(token(n));
		}

	}

	// unfolding this would go faster with less GC
	static int MAX_PREFIX_LENGTH = 4;
	static List<String> prefixes(String s) {
		int numPrefixes = Math.min(MAX_PREFIX_LENGTH,s.length());
		if (numPrefixes == 0)
			return Collections.emptyList();
		if (numPrefixes == 1)
			return Collections.singletonList(s);
		List<String> result = new ArrayList<String>(numPrefixes);
		for (int i = 1; i <= Math.min(MAX_PREFIX_LENGTH,s.length()); ++i)
			result.add(s.substring(0,i));
		return result;
	}

	// unfolding this would go faster with less GC
	static int MAX_SUFFIX_LENGTH = 4;
	static List<String> suffixes(String s) {
		int numSuffixes = Math.min(s.length(), MAX_SUFFIX_LENGTH);
		if (numSuffixes <= 0)
			return Collections.emptyList();
		if (numSuffixes == 1)
			return Collections.singletonList(s);
		List<String> result = new ArrayList<String>(numSuffixes);
		for (int i = s.length() - numSuffixes; i < s.length(); ++i)
			result.add(s.substring(i));
		return result;
	}

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		Corpus<ObjectHandler<Tagging<String>>> corpus = new TinyPosCorpus();
		final ChainCrfFeatureExtractor<String> featureExtractor = new FancyCrfFeatureExtractor();
		corpus.visitTrain(new ObjectHandler<Tagging<String>> () {
			@Override
			public void handle(Tagging<String> tagging) {
				ChainCrfFeatures<String> features = featureExtractor.extract(tagging.tokens(),tagging.tags());
				for (int i = 0; i < tagging.size(); ++i) {
					System.out.println("-------------------");
					System.out.println("Tagging:  " + tagging.token(i) + "/" + tagging.tag(i));
					System.out.print("Node Feats:" + features.nodeFeatures(i));
					if (i > 0) {
						System.out.println("Edge Feats:" + features.edgeFeatures(i, i -1));
					}
				}
			}

		});
	}
}


