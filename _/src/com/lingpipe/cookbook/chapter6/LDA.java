package com.lingpipe.cookbook.chapter6;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.cluster.LatentDirichletAllocation;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.symbol.SymbolTable;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.StopTokenizerFactory;
import com.aliasi.tokenizer.TokenLengthTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Strings;
import com.lingpipe.cookbook.Util;

public class LDA {
	
	static int ANNOTATION_OFFSET = 2;
	static int TEXT_OFFSET = 3;
	static int NUM_FOLDS = 4;

	static String[] CATEGORIES;

	static List<String> getCsvData(File file,String encoding) throws IOException {
		InputStreamReader fileReader = new InputStreamReader(new FileInputStream(file),encoding);
		CSVReader csvReader = new CSVReader(fileReader);
		List<String[]> inputData = csvReader.readAll();
		csvReader.close();
		List<String> lines = new ArrayList<String>();
		for (String[] row : inputData) {
			if (row[TEXT_OFFSET].equals("Text") ) {
				continue;
			}
			lines.add(row[TEXT_OFFSET]);
		}
		return lines;
	}

	public static void main(String[] args) throws Exception {
		String inFile = args.length > 0 ? args[0] : "data/gravity_tweets.csv";
        File corpusFile = new File(inFile);
        List<String[]> tweets = Util.readCsvRemoveHeader(corpusFile);
        int minTokenCount = 5;
        short numTopics = 25;
        double documentTopicPrior = .1;
        double wordPrior = 0.01;
        int burninEpochs = 0;
        int sampleLag = 1;
        int numSamples = 2000;
        long randomSeed = 6474835;
        SymbolTable symbolTable = new MapSymbolTable();
        TokenizerFactory tokFactory = new RegExTokenizerFactory("[^\\s]+");
        	//= IndoEuropeanTokenizerFactory.INSTANCE;
        
        tweets = Util.filterJaccard(tweets, tokFactory, .5);
        System.out.println("Input file=" + corpusFile);
        System.out.println("Minimum token count=" + minTokenCount);
        System.out.println("Number of topics=" + numTopics);
        System.out.println("Topic prior in docs=" + documentTopicPrior);
        System.out.println("Word prior in topics=" + wordPrior);
        System.out.println("Burnin epochs=" + burninEpochs);
        System.out.println("Sample lag=" + sampleLag);
        System.out.println("Number of samples=" + numSamples);
        // reportCorpus(articleTexts);
        String[] ldaTexts = new String[tweets.size()];
        for (int i = 0; i < tweets.size(); ++i) {
        	ldaTexts[i] = tweets.get(i)[Util.TEXT_OFFSET];
        }
        System.out.println("##########Got " + ldaTexts.length);
        int[][] docTokens
            = LatentDirichletAllocation
            .tokenizeDocuments(ldaTexts,tokFactory,symbolTable,minTokenCount);
        System.out.println("Number of unique words above count threshold=" + symbolTable.numSymbols());

        int numTokens = 0;
        for (int[] tokens : docTokens) {
            numTokens += tokens.length;
        }
        System.out.println("Tokenized.  #Tokens After Pruning=" + numTokens);

        LdaReportingHandler handler
            = new LdaReportingHandler(symbolTable);

        LatentDirichletAllocation.GibbsSample sample
            = LatentDirichletAllocation
            .gibbsSampler(docTokens,
                          numTopics,
                          documentTopicPrior,
                          wordPrior,
                          burninEpochs,
                          sampleLag,
                          numSamples,
                          new Random(randomSeed),
                          handler);

        int maxWordsPerTopic = 20;
        int maxTopicsPerDoc = 10;
        boolean reportTokens = true;
        handler.reportTopics(sample,maxWordsPerTopic,maxTopicsPerDoc,reportTokens);
        handler.reportDocuments(sample, maxWordsPerTopic, maxTopicsPerDoc, reportTokens);
    }
}
