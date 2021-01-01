package com.lingpipe.cookbook.chapter7.tracker;

import com.aliasi.coref.AbstractMentionChain;
import com.aliasi.coref.Matcher;
import com.aliasi.coref.Mention;
import com.aliasi.coref.Killer;

import java.util.HashSet;
import java.util.Set;

/**
 * A <code>TTMentionChain</code> is the threat tracker implementaiton
 * of mention chains.  A threat-tracker mention chain manages the
 * matchers for its mentions through an instance of {@link TTMatchers}.
 * Mention chains may only be constructed from a mention, but then
 * further mentions may be added through {@link #add(Mention)}.
 */

public class TTMentionChain extends AbstractMentionChain {

    private final Set<String> mNormalPhrases = new HashSet<String>();

    private final Set<String> mNormalTokens = new HashSet<String>();

    private final TTMatchers mMatchers;

    /**
     * Construct a mention chain from the specified mention at
     * the specified offset and with the specified identifier.
     *
     * @param mention Mention underlying this singleton mention chain.
     * @param offset Offset of the mention added to this chain.
     */
    TTMentionChain(TTMention mention, int offset, int id,
                   TTMatchers matchers) {
        super(mention,offset,id);
        addMentionPhrases(mention);
        mMatchers = matchers;
    }

    /**
     * Adds the specified mention to this mention chain.
     *
     * @param mention Mention to add to th is mention chain.
     */
    public void add(Mention mention) {
        unifyWithType(mention.entityType());
        addMentionPhrases(mention);
    }

    /**
     * Returns the set of normal phrases derived from the mentions
     * making up this mention chain.  Phrases derived from pronominal
     * mentions are not included.
     *
     * @return The set of normal phrases derived from the mentions
     * making up this mention chain.
     */
    public Set<String> normalPhrases() {
        return mNormalPhrases;
    }

    /**
     * Returns the set of tokens derived from the normal phrases
     * making up this mention chain.  Tokens derived from pronominal
     * mentions are not included.
     *
     * @return The set of tokens derived from the normal phrases
     * making up this mention chain.
     */
    public Set normalTokens() {
        return mNormalTokens;
    }

    /**
     * Return the array of matching functions for this mention chain.
     *
     * @return Array of matching functions for this mention chain.
     */
    public Matcher[] matchers() {
        return mMatchers.getMatchers(entityType());
    }

    /**
     * Return the array of killing functions for this mention chain.
     *
     * @return Array of killing functions for this mention chain.
     */
    public Killer[] killers() {
        return mMatchers.getKillers(entityType());
    }

    private void unifyWithType(String type) {
        if (entityType().equals(type)) return;
        setEntityType(TTMatchers.unifyEntityTypes(entityType(),type));
    }

    private void addMentionPhrases(Mention mention) {
        if (mention.isPronominal()) return;
        if (!mNormalPhrases.add(mention.normalPhrase()))
            return; // already exists
        String[] tokens = mention.normalTokens();
        for (int i = 0; i < tokens.length; ++i)
            mNormalTokens.add(tokens[i]);
    }


}
