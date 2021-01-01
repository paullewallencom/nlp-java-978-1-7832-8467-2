package com.lingpipe.cookbook.chapter1;

import java.io.File;
import java.io.IOException;

import com.aliasi.classify.JointClassifier;
import com.aliasi.util.AbstractExternalizable;
import com.lingpipe.cookbook.Util;

public class RunClassifierJoint {
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		String classifierPath = args.length > 0 ? args[0] : "models/3LangId.LMClassifier";
		@SuppressWarnings("unchecked")
		JointClassifier<CharSequence> classifier 
			= (JointClassifier<CharSequence>) AbstractExternalizable.readObject(new File(classifierPath));
		Util.consoleInputPrintClassification(classifier);
	}
}
