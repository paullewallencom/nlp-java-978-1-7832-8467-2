package com.lingpipe.cookbook.chapter7.tracker;

import com.aliasi.io.FileExtensionFilter;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;

import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.sentences.IndoEuropeanSentenceModel;

import com.aliasi.dict.ExactDictionaryChunker;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import com.aliasi.xml.SAXWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import java.util.Arrays;
import java.util.Set;
import java.util.HashMap;

import org.xml.sax.InputSource;

// run tracker over a directory structure
public class RunTracker {

	// usage  RunTracker <dictFile> <processDocFile> <inputDocDir> <outputDocDir> <neModelName>
	public static void main(String[] args) throws Exception {
		File dictFile = args.length > 0 ? new File(args[0]) : new File("data/xDoc/entity-dictionary.xml");
		File inputDir = args.length > 1 ? new File(args[1]) : new File("data/xDoc/input");
		File outputDir = args.length > 2 ? new File(args[2]) : new File("data/xDoc/output");
		String neModelFileName = args.length > 3 ? args[3] : "models/ne-en-news-muc6.AbstractCharLmRescoringChunker";
		String posModelFileName = args.length > 4 ? args[4] : "models/pos-en-general-brown.HiddenMarkovModel";
		String phraseCountFileName = args.length > 5 ? args[5] : "models/nGrams.txt.gz";

		Dictionary dictionary
		= Dictionary.read(dictFile,com.aliasi.util.Strings.UTF8);
		//System.out.println(dictionary);

		TokenizerFactory tokenizerFactory
		= IndoEuropeanTokenizerFactory.INSTANCE;

		SentenceModel sentenceModel
		= new IndoEuropeanSentenceModel(true,false);

		SentenceChunker sentenceChunker
		= new SentenceChunker(tokenizerFactory,sentenceModel);
		Chunker neChunker = (Chunker) AbstractExternalizable.readObject(new File(neModelFileName));
		HiddenMarkovModel posHmm
		= (HiddenMarkovModel) AbstractExternalizable.readObject(new File(posModelFileName));
		HmmDecoder posTagger= new HmmDecoder(posHmm);

		String phraseCounts =Files.readFromFile(new File(phraseCountFileName),Strings.UTF8);

		String[] lines = phraseCounts.split("\n");

		ExactDictionaryChunker dictChunker = dictionary.chunker(tokenizerFactory,
				PersistentTracker.RETURN_ALL_MATCHES_FALSE,
				PersistentTracker.CASE_SENSITIVE_FALSE);
		EntityPhraseChunker entityPhraseChunker 
		= new EntityPhraseChunker(neChunker,
				posTagger,
				dictChunker,
				dictionary.stopPhrases(),
				PersistentTracker.STOP_SUBSTRING_LIST,
				new HashMap <String, Integer>() );


		boolean addSpeculativeEntities = true;
		EntityUniverse entityUniverse = new EntityUniverse(tokenizerFactory);
		XDocCoref xDocCoref = new XDocCoref(entityUniverse,addSpeculativeEntities);
		TokenizerFactory normalizingTokenizerFactory = new NormalizedTokenizerFactory(tokenizerFactory);
		Tracker tracker
		= new Tracker(normalizingTokenizerFactory,
				sentenceChunker,
				entityPhraseChunker,
				dictionary,
				new XDocCoref(new EntityUniverse(normalizingTokenizerFactory),addSpeculativeEntities));

		// read input docs & write output docs
		for (File file : inputDir.listFiles(new FileExtensionFilter(".xml",false))) {
			System.out.println("\n\nINPUT FILE: " + file);  
			InputSource inSource = new InputSource(file.toURI().toURL().toString());
			inSource.setEncoding(Strings.UTF8);
			OutputDocument[] outDocs = tracker.processDocuments(inSource);
			System.out.println("# outDocs=" + outDocs.length); 
			for (OutputDocument doc : outDocs)
				System.out.println(doc);  
			File outFile = new File(outputDir,file.getName());
			System.out.println("OUTPUT FILE: " + outFile); 
			OutputStream out = new FileOutputStream(outFile);
			SAXWriter writer = new SAXWriter(out,Strings.UTF8);
			writer.startDocument();
			writer.startSimpleElement("docs");
			writer.characters("\n");
			for (OutputDocument doc : outDocs) {
				doc.writeTo(writer);
				writer.characters("\n");
			}
			writer.endSimpleElement("docs");
			writer.endDocument();
			Streams.closeOutputStream(out);
		}
	}

}
