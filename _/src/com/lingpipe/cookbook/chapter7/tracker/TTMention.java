package com.lingpipe.cookbook.chapter7.tracker;

import com.aliasi.coref.CachedMention;
import com.aliasi.coref.Mention;

import java.util.Set;

/**
 * A <code>TTMention</code> is a cached mention that is identified
 * by reference, rather than by content.  This allows instances to
 * be discriminated so that a mapping from mention instances may be
 * created.
 */
public class TTMention extends CachedMention {

    /**
     * Construct a mention with the specified phrase, entity type,
     * set of honorifics, array of normal tokens, gender and
     * indication of pronominal-ness.
     *
     * @param phrase Underlying phrase for the mention.
     * @param entityType The type of the mention.
     * @param honorifics The honorifics for the mention.
     * @param normalTokens The sequence of normal tokens for the mention.
     * @param gender The gender of the mention constructed.
     * @param isPronominal <code>true</code> if this mention is a
     * pronoun.
     */
    public TTMention(String phrase, String entityType,
                     Set honorifics, String[] normalTokens,
                     String gender, boolean isPronominal) {
        super(phrase,entityType,
              honorifics,normalTokens,
              gender,isPronominal);
    }


}
