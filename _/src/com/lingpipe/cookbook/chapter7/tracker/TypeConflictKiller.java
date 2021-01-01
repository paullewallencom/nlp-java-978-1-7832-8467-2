package com.lingpipe.cookbook.chapter7.tracker;

import com.aliasi.coref.Killer;
import com.aliasi.coref.Mention;
import com.aliasi.coref.MentionChain;

/**
 * The <code>TypeConflictKiller</code> implements a killing function
 * that defeats the matching of a mention with chain of incompatible
 * type.  
 */
public class TypeConflictKiller implements Killer {

    /**
     * Returns <code>true</code> if the specified mention and mention
     * chain have types that cannot be unified.  This is determined
     * by whether the static method {@link TTMatchers#unifyEntityTypes(String,String)}
     * applied to the type of the mention and the type of the chain
     * returns <code>null</code>.
     *
     * @param mention Mention to test.
     * @param chain Mention chain to test.
     * @return <code>true</code> if the mention has a  type that
     * is incompatible with the type of the chain.
     */
    public boolean kill(Mention mention, MentionChain chain) {
	String unifiedType
	    = TTMatchers.unifyEntityTypes(mention.entityType(),
					  chain.entityType());
	return unifiedType == null;
    }

}
