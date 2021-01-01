package com.lingpipe.cookbook.chapter7;

import com.aliasi.spell.TfIdfDistance;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Distance;
import com.lingpipe.cookbook.chapter7.JohnSmith.Document;

public class TfIdfDocumentDistance implements Distance<Document> {

	TfIdfDistance mTfIdfDistance;

	public TfIdfDocumentDistance (TokenizerFactory tokenizerFactory) {
		mTfIdfDistance = new TfIdfDistance(tokenizerFactory);
	}
        
    public void train(CharSequence text) {
    	mTfIdfDistance.handle(text);
    }

	@Override
	public double distance(Document doc1, Document doc2) {
		// TODO Auto-generated method stub
		//return mTfIdfDistance.distance(doc1.mCoreferentText,doc2.mCoreferentText);
		return mTfIdfDistance.distance(doc1.mText,doc2.mText);
	}
	
}
