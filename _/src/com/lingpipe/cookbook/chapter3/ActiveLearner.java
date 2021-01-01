package com.lingpipe.cookbook.chapter3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.ConditionalClassifierEvaluator;
import com.aliasi.classify.LogisticRegressionClassifier;
import com.aliasi.classify.ScoredClassification;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToDoubleMap;
import com.lingpipe.cookbook.Util;


public class ActiveLearner {

	static String getLatestEpochFile(String fileName) throws FileNotFoundException {
		File file = new File(fileName);
		if (!file.exists()) {
			System.out.println("No file found, did you create your own directory and put in a <file name>.0.csv?");
			throw new FileNotFoundException();
		}
		File dir = file.getParentFile();
		Pattern rootAndEpochFinder = Pattern.compile("(.*)\\.(\\d+)\\.csv");
		Matcher matcher = rootAndEpochFinder.matcher(file.getAbsolutePath());
		matcher.find();
		String root = matcher.group(1);
		int maxAnnot = 0;
		for (File annotatedFile : dir.listFiles()) {
			matcher = rootAndEpochFinder.matcher(annotatedFile.getAbsolutePath());
			matcher.find();
			if (!matcher.group(1).equals(root)){
				continue;
			}
			int fileAnnotEpoch = Integer.valueOf(matcher.group(2));
			if (maxAnnot < fileAnnotEpoch) {
				maxAnnot = fileAnnotEpoch;
			}
		}	
		return root + "." + maxAnnot + ".csv";
	}

	static String incrementFileName(String fileName) {
		Pattern rootAndEpochFinder = Pattern.compile("(.*)\\.(\\d+)\\.csv");
		Matcher matcher = rootAndEpochFinder.matcher(fileName);
		matcher.find();
		String root = matcher.group(1);
		int nextIncrement = Integer.valueOf(matcher.group(2)) + 1;
		return root + "." + nextIncrement + ".csv";
	}

	public static void main(String[] args) throws IOException {
		String fileName = args.length > 0 ? args[0] : "data/activeLearningCompleted/disneySentimentDedupe.0.csv"; 
		System.out.println("First file:  " + fileName);
		String latestFile = getLatestEpochFile(fileName);
		System.out.println("Reading from file " + latestFile);
		List<String[]> data = Util.readCsvRemoveHeader(new File(latestFile));
		int numFolds = 10;
		XValidatingObjectCorpus<Classified<CharSequence>> corpus 
		= Util.loadXValCorpus(data,numFolds);
		String[] categories = Util.getCategoryArray(corpus);
		PrintWriter progressWriter = new PrintWriter(System.out,true);
		boolean storeInputs = true;
		ConditionalClassifierEvaluator<CharSequence> evaluator 
		= new ConditionalClassifierEvaluator<CharSequence>(null, categories, storeInputs);
		TokenizerFactory tokFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		for (int i = 0; i < numFolds; ++i) {
			corpus.setFold(i);
			final LogisticRegressionClassifier<CharSequence> classifier 
			= Util.trainLogReg(corpus,tokFactory, progressWriter);
			evaluator.setClassifier(classifier);
			corpus.visitTest(evaluator);
		}
		final ObjectToDoubleMap<String[]> accumulator = new ObjectToDoubleMap<String[]>();
		for (String category : categories) {
			List<Classified<CharSequence>> inCategory = evaluator.truePositives(category);
			inCategory.addAll(evaluator.falseNegatives(category));
			for (Classified<CharSequence> testCase : inCategory) {
				CharSequence text = testCase.getObject();
				ConditionalClassification classification = (ConditionalClassification) testCase.getClassification();
				double score = classification.conditionalProbability(0);
				String[] xFoldRow = new String[Util.TEXT_OFFSET + 1];
				xFoldRow[Util.SCORE] = String.valueOf(score);
				xFoldRow[Util.GUESSED_CLASS] = classification.bestCategory();
				xFoldRow[Util.ANNOTATION_OFFSET] = category;
				xFoldRow[Util.TEXT_OFFSET] = text.toString();
				accumulator.set(xFoldRow,score);
			}
		}
		//Util.printPRcurve(evaluator);
		Util.printConfusionMatrix(evaluator.confusionMatrix());
		Util.printPrecRecall(evaluator);	
		corpus.setNumFolds(0);
		LogisticRegressionClassifier<CharSequence> classifier = Util.trainLogReg(corpus,tokFactory,progressWriter);
		for (String[] csvData : data) {
			if (!csvData[Util.ANNOTATION_OFFSET].equals("")) {
				continue;
			}
			ScoredClassification classification = classifier.classify(csvData[Util.TEXT_OFFSET]);
			csvData[Util.GUESSED_CLASS] = classification.category(0);
			double estimate = classification.score(0);
			csvData[Util.SCORE] = String.valueOf(estimate);
			accumulator.set(csvData,estimate);
		}
		String outfile = incrementFileName(latestFile);
		Util.writeCsvAddHeader(accumulator.keysOrderedByValueList(), new File(outfile));
		System.out.println("Corpus size: " + corpus.size());
		System.out.println("Writing to file: " + outfile);
		System.out.println("Done, now go annotate and save with same file name");
	}
}
