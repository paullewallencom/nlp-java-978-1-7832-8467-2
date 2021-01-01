package com.lingpipe.cookbook.chapter7;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.classify.PrecisionRecallEvaluation;

import com.aliasi.cluster.HierarchicalClusterer;
import com.aliasi.cluster.ClusterScore;
import com.aliasi.cluster.CompleteLinkClusterer;
import com.aliasi.cluster.SingleLinkClusterer;
import com.aliasi.cluster.Dendrogram;
import com.aliasi.coref.EnglishMentionFactory;
import com.aliasi.coref.Mention;
import com.aliasi.coref.MentionFactory;
import com.aliasi.coref.WithinDocCoref;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Counter;
import com.aliasi.util.Distance;
import com.aliasi.util.Files;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;

import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.spell.TfIdfDistance;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;


import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class JohnSmith {


	static Chunker SENTENCE_CHUNKER;
	static Chunker NAMED_ENTITY_CHUNKER;

	public static void main(String[] args) 
			throws ClassNotFoundException, IOException {
		TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		SentenceModel sentenceModel
		= new IndoEuropeanSentenceModel();
		SENTENCE_CHUNKER 
		= new SentenceChunker(tokenizerFactory,sentenceModel);
		File modelFile
		= new File("models/ne-en-news-muc6.AbstractCharLmRescoringChunker");
		NAMED_ENTITY_CHUNKER 
		= (Chunker) AbstractExternalizable.readObject(modelFile);
		TfIdfDocumentDistance tfIdfDist = new TfIdfDocumentDistance(tokenizerFactory);
		File dir = new File("data/johnSmith");
		Set<Set<Document>> referencePartition
		= new HashSet<Set<Document>>();
		for (File catDir : dir.listFiles()) {
			System.out.println("Category from file=" + catDir);
			Set<Document> docsForCat = new HashSet<Document>();
			referencePartition.add(docsForCat);
			for (File file : catDir.listFiles()) {
				Document doc = new Document(file);
				tfIdfDist.train(doc.mText);
				docsForCat.add(doc);
			}
		}
		Set<Document> docSet = new HashSet<Document>();
		for (Set<Document> cluster : referencePartition) {
			docSet.addAll(cluster);
		}
		// eval clusterers
		HierarchicalClusterer<Document> clClusterer
		= new CompleteLinkClusterer<Document>(tfIdfDist);
		Dendrogram<Document> completeLinkDendrogram
		= clClusterer.hierarchicalCluster(docSet);

		HierarchicalClusterer<Document> slClusterer
		= new SingleLinkClusterer<Document>(tfIdfDist);
		Dendrogram<Document> singleLinkDendrogram
		= slClusterer.hierarchicalCluster(docSet);
		System.out.println();
		System.out.println(" --------------------------------------------------------");
		System.out.println("|  K  |  Complete      |  Single        |  Cross         |");
		System.out.println("|     |  P    R    F   |  P    R    F   |  P    R    F   |");
		System.out.println(" --------------------------------------------------------");
		for (int k = 1; k <= docSet.size(); ++k) {
			Set<Set<Document>> clResponsePartition
			= completeLinkDendrogram.partitionK(k);
			Set<Set<Document>> slResponsePartition
			= singleLinkDendrogram.partitionK(k);

			ClusterScore<Document> scoreCL
			= new ClusterScore<Document>(referencePartition,
					clResponsePartition);
			PrecisionRecallEvaluation clPrEval = scoreCL.equivalenceEvaluation();


			ClusterScore<Document> scoreSL
			= new ClusterScore<Document>(referencePartition,
					slResponsePartition);
			PrecisionRecallEvaluation slPrEval = scoreSL.equivalenceEvaluation();

			System.out.printf("| %3d | %3.2f %3.2f %3.2f | %3.2f %3.2f %3.2f \n",
					k,
					clPrEval.precision(),
					clPrEval.recall(),
					clPrEval.fMeasure(),
					slPrEval.precision(),
					slPrEval.recall(),
					slPrEval.fMeasure()
					);
		}
		System.out.println(" --------------------------------------------------------");
		System.out.println("B-cubed eval");
		for (double maxDist = 0.0; maxDist < 1.01; maxDist += .05) {
			HierarchicalClusterer<Document> slClustererThresholded
			= new SingleLinkClusterer<Document>(maxDist,tfIdfDist);
			Set<Set<Document>> thresholdedCluster
			= slClustererThresholded.cluster(docSet);
			ClusterScore<Document> score 
			= new ClusterScore<Document>(referencePartition,thresholdedCluster);
			System.out.printf("Dist: %.2f P: %.2f R: %.2f size:%3d\n", maxDist, score.b3ClusterPrecision(),score.b3ClusterRecall(),thresholdedCluster.size());
		}
	}

	static final Set<String> getCoreferentSents(String targetPhrase, String text) {
		Chunking sentenceChunking
		= SENTENCE_CHUNKER.chunk(text);
		Iterator<Chunk> sentenceIt 
		= sentenceChunking.chunkSet().iterator();
		int targetId = -2;
		MentionFactory mentionFactory = new EnglishMentionFactory();
		WithinDocCoref coref = new WithinDocCoref(mentionFactory);
		Set<String> matchingSentenceAccumulator = new HashSet<String>();
		for (int sentenceNum = 0; sentenceIt.hasNext(); ++sentenceNum) {
			Chunk sentenceChunk = sentenceIt.next();
			String sentenceText 
			= text.substring(sentenceChunk.start(),
					sentenceChunk.end());
			Chunking neChunking
			= NAMED_ENTITY_CHUNKER.chunk(sentenceText);
			Set<Chunk> chunkSet = new TreeSet<Chunk>(Chunk.TEXT_ORDER_COMPARATOR);
			chunkSet.addAll(neChunking.chunkSet());
			Coreference.addRegexMatchingChunks(Pattern.compile("\\bJohn Smith\\b"),"PERSON",sentenceText,chunkSet);
			Coreference.addRegexMatchingChunks(Pattern.compile("\\b(He|he|him|his|His)\\b"),"MALE_PRONOUN",sentenceText,chunkSet);
			Iterator<Chunk> neChunkIt = chunkSet.iterator();
			while (neChunkIt.hasNext()) {
				Chunk neChunk = neChunkIt.next();
				String mentionText
				= sentenceText.substring(neChunk.start(),
						neChunk.end());
				String mentionType = neChunk.type();
				Mention mention = mentionFactory.create(mentionText,mentionType);
				int mentionId = coref.resolveMention(mention,sentenceNum);
				if (targetId == -2 && mentionText.matches(targetPhrase)) {
					targetId = mentionId;
				}
				if (mentionId == targetId) {
					matchingSentenceAccumulator.add(sentenceText);
					System.out.println("Adding " + sentenceText);

					System.out.println("     mention text=" + mentionText
							+ " type=" + mentionType
							+ " id=" + mentionId);
				}
			}
		}
		if (targetId == -2) {
			System.out.println("!!!Missed target doc " + text);
		}
		return matchingSentenceAccumulator;
	}

	static class Document {
		final File mFile;
		final CharSequence mText; 
		final CharSequence mCoreferentText;
		Document(File file) throws IOException {
			mFile = file; // includes name
			mText = Files.readFromFile(file,Strings.UTF8);
			Set<String> coreferentSents = getCoreferentSents(".*John Smith.*",mText.toString());
			StringBuilder sb = new StringBuilder();
			for (String sentence : coreferentSents) {
				sb.append(sentence);
			}
			mCoreferentText = sb.toString();
		}

		public String toString() {
			return mFile.getParentFile().getName() + "/"  + mFile.getName();
		}
	}
}
