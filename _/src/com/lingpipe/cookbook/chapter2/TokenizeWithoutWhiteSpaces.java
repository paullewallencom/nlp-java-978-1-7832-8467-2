package com.lingpipe.cookbook.chapter2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.aliasi.lm.NGramProcessLM;
import com.aliasi.spell.CompiledSpellChecker;
import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.spell.WeightedEditDistance;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;

public class TokenizeWithoutWhiteSpaces {
	
	public static void main (String[] args) throws IOException, ClassNotFoundException {
		String dataPath = args.length > 0 ? args[0] : "data/connecticut_yankee_king_arthur.txt";
		int nGram = 5;
		NGramProcessLM lm = new NGramProcessLM(nGram);
		WeightedEditDistance spaceInsertingEditDistance 
			= CompiledSpellChecker.TOKENIZING;
		TrainSpellChecker trainer = new TrainSpellChecker(lm, spaceInsertingEditDistance);
		File trainingFile = new File(dataPath);
		String training = Files.readFromFile(trainingFile, Strings.UTF8);
		trainer.handle(training);
		System.out.println("Compiling Spell Checker");
		CompiledSpellChecker spellChecker
			= (CompiledSpellChecker) AbstractExternalizable.compile(trainer);
		spellChecker.setAllowInsert(true);
		spellChecker.setAllowMatch(true);
		spellChecker.setAllowDelete(false);
		spellChecker.setAllowSubstitute(false);
		spellChecker.setAllowTranspose(false);
		spellChecker.setNumConsecutiveInsertionsAllowed(1);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println("type an Englese sentence (English without spaces like Chinese)");
			String input = reader.readLine();
			String result = spellChecker.didYouMean(input);
			System.out.println(result);
		}
	}
}
