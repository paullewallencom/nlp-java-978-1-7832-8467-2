package com.lingpipe.cookbook.chapter1;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.lingpipe.cookbook.Util;

public class DeduplicateCsvData {
	
		public static void main(String[] args) throws IOException {
			String inputPath = args.length > 0 ? args[0] : "data/disney.csv";	
			String outputPath = args.length > 1 ? args[1] : "data/disneyDeduped.csv";	
			List<String[]> data = Util.readCsvRemoveHeader(new File(inputPath));
			System.out.println(data.size());
			TokenizerFactory tokenizerFactory = new RegExTokenizerFactory("\\w+");
			double cutoff = .5d;
			List<String[]> dedupedData = Util.filterJaccard(data, tokenizerFactory, cutoff);
			System.out.println(dedupedData.size());
			Util.writeCsvAddHeader(dedupedData, new File(outputPath));
		}
}
