package com.lingpipe.cookbook.chapter6;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.aliasi.spell.TfIdfDistance;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Files;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.Strings;
import com.lingpipe.cookbook.Util;

import au.com.bytecode.opencsv.CSVReader;


public class TfIdfSearch {

	public static void main(String[] args) throws IOException {
		String searchableDocs = args.length > 0 ? args[0] : "data/disneyWorld.csv";
		System.out.println("Reading search index from " + searchableDocs);
		String idfFile = args.length > 1 ? args[1] : "data/connecticut_yankee_king_arthur.txt";
		System.out.println("Getting IDF data from " + idfFile);

		TokenizerFactory tokFact = IndoEuropeanTokenizerFactory.INSTANCE;
		TfIdfDistance tfIdfDist = new TfIdfDistance(tokFact);
		
		String training = Files.readFromFile(new File(idfFile), Strings.UTF8);
		for (String line: training.split("\\.")) {
			tfIdfDist.handle(line);
		}

		List<String[]> docsToSearch = Util.readCsvRemoveHeader(new File(searchableDocs));
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println("enter a query: ");
			String query = reader.readLine();
			ObjectToDoubleMap<String> scoredMatches = new ObjectToDoubleMap<String>();
			for (String [] line : docsToSearch) {
				scoredMatches.put(line[Util.TEXT_OFFSET], tfIdfDist.proximity(line[Util.TEXT_OFFSET],query));
			}
			List<String> rankedDocs = scoredMatches.keysOrderedByValueList();
			for (int i = 0; i < 10; ++i) {
				System.out.printf("%.2f : ",scoredMatches.get(rankedDocs.get(i)));
				System.out.println(rankedDocs.get(i));
			}
		}
	}
}
