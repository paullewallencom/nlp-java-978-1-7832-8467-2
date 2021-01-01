package com.lingpipe.cookbook.chapter7.tracker;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;

/**
 * This class provides a normalized tokenizer that only
 * iterates over normal tokens.  Normalization converts
 * tokens to lower case and eliminates stopped non-normal
 * tokens.
 */
public class NormalizedTokenizerFactory implements TokenizerFactory {

    private final TokenizerFactory mBaseTokenizerFactory;

    public NormalizedTokenizerFactory(TokenizerFactory factory) {
        mBaseTokenizerFactory = new LowerCaseTokenizerFactory(factory);
    }

    public Tokenizer tokenizer(char[] ch, int start, int length) {
        return mBaseTokenizerFactory.tokenizer(ch,start,length);
        
    }

}


