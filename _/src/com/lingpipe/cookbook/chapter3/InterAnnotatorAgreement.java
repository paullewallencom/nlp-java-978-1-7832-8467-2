package com.lingpipe.cookbook.chapter3;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aliasi.classify.BaseClassifierEvaluator;
import com.aliasi.classify.Classification;
import com.aliasi.classify.PrecisionRecallEvaluation;
import com.lingpipe.cookbook.Util;

public class InterAnnotatorAgreement {

	public static void main(String[] args) throws IOException {
		String truthFile = args.length > 0 ? args[0] : "data/disney_e_n.csv";
		String responseFile = args.length > 1 ? args[1] : "data/disney1_e_n.csv";
		System.out.println(truthFile + " treated as truth \n" + responseFile + " treated as response");
		List<String[]> truth = Util.readAnnotatedCsvRemoveHeader(new File(truthFile));
		List<String[]> response = Util.readAnnotatedCsvRemoveHeader(new File(responseFile));
		Map<String,String[]> dataToTruth = new HashMap<String,String[]>();
		Set<String> categorySet = new HashSet<String>();
		for (String[] annot : truth) {
			dataToTruth.put(annot[Util.TEXT_OFFSET],annot);
			categorySet.add(annot[Util.ANNOTATION_OFFSET]);
		}
		for (String[] annot : response) {
			categorySet.add(annot[Util.ANNOTATION_OFFSET]);
		}
		String[] categories = categorySet.toArray(new String[0]);
		boolean storeInputs = false;
		BaseClassifierEvaluator<CharSequence> evaluator 
			= new BaseClassifierEvaluator<CharSequence>(null, categories, storeInputs);
		for (String[] responseRow : response) {
			String responseCategory = responseRow[Util.ANNOTATION_OFFSET];
			String text = responseRow[Util.TEXT_OFFSET];
			String[] truthRow = dataToTruth.get(text);
			if (truthRow == null) {
				System.out.println("no truth data for " + text);
				continue;
			}
			String truthCategory = truthRow[Util.ANNOTATION_OFFSET];
			Classification responseClassification = new Classification(responseCategory);
			if (!responseCategory.equals(truthCategory)) {
				System.out.print("Disagreement: " + truthCategory + " x " + responseCategory + " for: ");
				System.out.println(text);
			}
			evaluator.addClassification(truthCategory, responseClassification, text);
		}
		Util.printConfusionMatrix(evaluator.confusionMatrix());
		for (String category : categories) {
			PrecisionRecallEvaluation prEval = evaluator.oneVersusAll(category);
			System.out.printf("Category: " + category + " Precision: %.2f, Recall: %.2f \n",prEval.precision(),prEval.recall());
		}
	}
	
	
}
