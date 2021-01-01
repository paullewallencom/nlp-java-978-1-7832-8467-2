package com.lingpipe.cookbook.chapter7.tracker;

import com.aliasi.coref.BooleanMatcherAdapter;
import com.aliasi.coref.Mention;
import com.aliasi.coref.MentionChain;

import com.aliasi.util.ObjectToSet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Implements a matching function that returns the score specified in
 * the constructor if the mention has a synonym in the mention set as
 * specified by the synonym dictionary.  Synonyms are defined over
 * normalized phrases of the mention and the phrases of the mentions
 * in the mention chains.  Pairs of synonymous phrases are added to
 * the matcher with the method {@link #addSynonym(String,String)}.
 */
/**
 * An extension of <code>SynonymMatch</code> that exposes
 * the object-to-set synonym mapping.
 */
public class TTSynonymMatch extends BooleanMatcherAdapter {

    /**
     * The underlying mapping from phrases to their set of synonyms.
     * It's symmetric in that if the value for
     * <code>mSynonymMap.getSet(x).contains(y)</code> should be
     * the same as that for then
     * <code/>mSynonymMap.getSet(y).contains(x)</code>.
     */
    private final ObjectToSet mSynonymMap = new ObjectToSet();

    /**
     * Construct an instance of the synonym matcher.
     *
     * @param score Score to assign to a successful synonym match.
     */
    public TTSynonymMatch(int score) {
        super(score);
    }

    /**
     * Returns the synonym dictionary for
     */
    private ObjectToSet synonymMap() {
        return mSynonymMap;
    }

    /**
     * Returns <code>true</code> if the mention's normal phrase has a
     * synonym that is the normal phrase of one of the chain's mentions.
     *
     * @param mention Mention to test.
     * @param chain Mention chain to test.
     * @return <code>true</code> if there is a sequence substring
     * match between the mention and chain.
     */
    public boolean matchBoolean(Mention mention, MentionChain chain) {
        String phrase = mention.normalPhrase();
        if (!mSynonymMap.containsKey(phrase)) return false;
        Set synonyms = mSynonymMap.getSet(phrase);
        Iterator synonymIterator = synonyms.iterator();
        while (synonymIterator.hasNext()) {
            String synonym = synonymIterator.next().toString();
            Iterator chainMentionIterator = chain.mentions().iterator();
            while (chainMentionIterator.hasNext()) {
                Mention chainMention = (Mention) chainMentionIterator.next();
                if (synonym.equals(chainMention.normalPhrase()))
                    return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the specified normal phrases are
     * synonyms of one another.
     * 
     * @param normalPhrase1 First phrase to test.
     * @param normalPhrase2 Second phrase to test.
     * @return <code>true</code> if the specified normal phrases are
     * synonyms of one another.
     */
    public boolean areSynonyms(String normalPhrase1, String normalPhrase2) {
	return mSynonymMap.getSet(normalPhrase1).contains(normalPhrase2);
    }

    /**
     * Adds the two normal phrases as synonyms for one another.  The
     * operation is symmetric, so that they do not need to be added in
     * the reverse order.  But it is not transitive, so it is possible to
     * have &quot;Bobby&quot; and &quot;Robert&quot; as synonyms,
     * and have &quot;Robby&quot; and &quot;Robert&quot; as synonyms,
     * without having &quot;Bobby&quot; and &quot;Robby&quot; as synonyms.
     *
     * @param normalPhrase1 First normal phrase in the synonym pair.
     * @param normalPhrase2 Second normal phrase in the synonym pair.
     */
    public void addSynonym(String normalPhrase1, String normalPhrase2) {
	if (normalPhrase1.equals(normalPhrase2)) return;
	addSynonymOneWay(normalPhrase1,normalPhrase2);
	addSynonymOneWay(normalPhrase2,normalPhrase1);
    }

    /**
     * Clears all of the synonyms from the matcher.
     */
    public void clearSynonyms() {
	mSynonymMap.clear();
    }

    private void addSynonymOneWay(String normalPhrase1, String normalPhrase2) {
	mSynonymMap.addMember(normalPhrase1,normalPhrase2);
	/* this commented out block ensures transitivity
	Iterator it = new HashSet(mSynonymMap.getSet(normalPhrase1)).iterator();
	while (it.hasNext()) {
	    Object next = it.next();
	    mSynonymMap.addMember(next,normalPhrase2);
	    Iterator it2 = new HashSet(mSynonymMap.getSet(normalPhrase2)).iterator();
	    while (it2.hasNext()) {
		Object next2 = it2.next();
		mSynonymMap.addMember(next,next2);
	    }
	}	
	*/
    }



}

