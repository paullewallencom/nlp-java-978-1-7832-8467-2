package com.lingpipe.cookbook.chapter1;

import java.io.File;
import java.io.IOException;

import com.aliasi.classify.BaseClassifier;
import com.aliasi.util.AbstractExternalizable;
import com.lingpipe.cookbook.Util;

public class RunClassifierFromDisk {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		String classifierPath = args.length > 0 ? args[0] : "models/3LangId.LMClassifier";
		System.out.println("Loading: " + classifierPath);
		File serializedClassifier = new File(classifierPath);
		@SuppressWarnings("unchecked")
		BaseClassifier<CharSequence> classifier
			= (BaseClassifier<CharSequence>) AbstractExternalizable.readObject(serializedClassifier);
		Util.consoleInputBestCategory(classifier);	
	}
	
}
