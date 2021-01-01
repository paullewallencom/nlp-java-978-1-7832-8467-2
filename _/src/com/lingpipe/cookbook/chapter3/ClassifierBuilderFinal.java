package com.lingpipe.cookbook.chapter3;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassifierEvaluator;
import com.aliasi.classify.LogisticRegressionClassifier;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.lingpipe.cookbook.Util;



public class ClassifierBuilderFinal {

	public static void main(String args[]) throws IOException {
		String trainingFile = args.length > 0 ? args[0] : "data/activeLearningCompleted/disneySentimentDedupe.2.csv";
		int numFolds = 10;
		List<String[]> training 
		= Util.readAnnotatedCsvRemoveHeader(new File(trainingFile));
		String[] categories = Util.getCategories(training);
		XValidatingObjectCorpus<Classified<CharSequence>> corpus 
		= Util.loadXValCorpus(training,numFolds);
		TokenizerFactory tokenizerFactory 
		= IndoEuropeanTokenizerFactory.INSTANCE;

		PrintWriter progressWriter = new PrintWriter(System.out,true);
		Reporter reporter = Reporters.writer(progressWriter);
		reporter.setLevel(LogLevel.WARN);
		boolean storeInputs = true;	
		ConditionalClassifierEvaluator<CharSequence> evaluator 
		= new ConditionalClassifierEvaluator<CharSequence>(null, categories, storeInputs);
		corpus.setNumFolds(numFolds);
		for (int i = 0; i < numFolds; ++i) {
			corpus.setFold(i);
			LogisticRegressionClassifier<CharSequence> classifier 
			= Util.trainLogReg(corpus, tokenizerFactory, progressWriter);
			evaluator.setClassifier(classifier);
			corpus.visitTest(evaluator);
		}
		
		
		/*corpus.setNumFolds(0);
		LogisticRegressionClassifier<CharSequence> classifier = Util.trainLogReg(corpus, tokenizerFactory, progressWriter);
		evaluator.setClassifier(classifier);
		corpus.visitTrain(evaluator);
		System.out.println("!!!Testing on training!!!");
		*/
		Util.printConfusionMatrix(evaluator.confusionMatrix());

		
		//AbstractExternalizable.compileTo(classifier, 
			//new File("models/ClassifierBuilder.LogisticRegression"));

		
	}
}


