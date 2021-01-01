package com.lingpipe.cookbook.chapter7.tracker;

import com.aliasi.coref.Killer;
import com.aliasi.coref.Matcher;
import com.aliasi.coref.Mention;
import com.aliasi.coref.MentionChain;

import com.aliasi.coref.matchers.ExactPhraseMatch;
import com.aliasi.coref.matchers.EntityTypeMatch;
import com.aliasi.coref.matchers.GenderKiller;
import com.aliasi.coref.matchers.HonorificConflictKiller;
import com.aliasi.coref.matchers.SequenceSubstringMatch;

//import com.aliasi.util.Collections;
import com.aliasi.util.ObjectToSet;
import com.aliasi.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A class providing matching functions and killing functions based on
 * entity type.  The static method {@link
 * #unifyEntityTypes(String,String)} is used to combiene entity types.
 * Because it returns <code>null</code> if the types cannot be
 * combiend, it may be used to test for consistency of types.  This is
 * used by an instance of {@link TypeConflictKiller} assgined to each
 * type.  This class also provides a pair of methods, {@link
 * #addSynonym(String,String)} and {@link #clearSynonyms()} to control
 * the synonym relationship used by the instance of {@link
 * TTSynonymMatch} assigned to each type.
 */
public final class TTMatchers {

    private final Map mTypeToMatchers = new HashMap();

    private final Map mTypeToKillers = new HashMap();

    private final Matcher mExactPhraseMatch
        = new ExactPhraseMatch(1);
    private final Matcher mPersonFemalePronounMatch
        = new EntityTypeMatch(3,TTMentionFactory.FEMALE_PRONOUN_TAG);
    private final Matcher mPersonMalePronounMatch
        = new EntityTypeMatch(3,TTMentionFactory.MALE_PRONOUN_TAG);
    private final Matcher mFemalePronounMatch
        = new EntityTypeMatch(1,TTMentionFactory.FEMALE_PRONOUN_TAG);
    private final Matcher mMalePronounMatch
        = new EntityTypeMatch(1,TTMentionFactory.MALE_PRONOUN_TAG);
    private final Matcher mSequenceSubstringMatch
        = new SequenceSubstringMatch(3);
    public final TTSynonymMatch mSynonymMatch
        = new TTSynonymMatch(3);


    /**
     * Adds the information that the two specified normal phrases
     * are synonyms.  This information is used by a matcher for
     * within-document coreference.
     *
     * @param normalPhrase1 First synonym.
     * @param normalPhrase2 Second synonym.
     */
    public void addSynonym(String normalPhrase1, String normalPhrase2) {
        mSynonymMatch.addSynonym(normalPhrase1,normalPhrase2);
    }


    /**
     * Resets the synonym map so that no phrases are considered
     * synonymous.
     */
    public void clearSynonyms() {
        mSynonymMatch.clearSynonyms();
    }

    /**
     * Returns the unification of the specified types, or
     * <code>null</code> if they cannot be unified.  Unification is
     * symmetric in that
     * <code>unifyEntityTypes(s1,s2).equals(unifyEntityTypes(s2,s1))</code>
     * is <code>true</code>.
     *
     * <P>The rules for unification are as follows:
     *
     * <UL>
     *
     *   <LI>Any type unifies with itself to produce itself.
     *
     *   <LI>Any type unifies with <code>null</code> to produce itself.
     *
     *   <LI><code>LOCATION</code> and <code>ORGANIZATION</code> unify
     *   to produce <code>LOCATION</code>.
     *
     *   <LI><code>PERSON</code> unifies with <code>(FE)MALE</code> or
     *   <code>(FE)MALE_PRONOUN</code> to produce
     *   <code>(FE)MALE</code>.
     *
     *   <LI><code>(FE)MALE</code> unifies with
     *   <code>(FE)MALE_PRONOUN</code> to produce
     *   <code>(FE)MALE</code>.
     *
     * </UL>
     *
     * All unspecified combinations return <code>null</code>.
     *
     * @param type1 First entity type.
     * @param type2 Second entity type.
     * @return Result of unifying first and second entity types.
     */
    static public String unifyEntityTypes(String type1, String type2) {
        if (type1 == null) return type2;
        if (type2 == null) return type1;
        if (type1.equals(type2)) return type1;
        HashMap map2 = (HashMap) UNIFICATION_MAP.get(type1);
        if (map2 == null) {
            return null;
        }
        Object result = map2.get(type2);
        if (result == null) {
            return null;
        }
        return result.toString();
    }

    static private void simpleType(String type) {
        subsumes(type,type);
    }

    static private void subsumes(String  type1, String type2) {
        unify(type1,type2,type2);
    }

    static private void unify(String type1, String type2, String type) {
        setMap(type1,type2,type);
        setMap(type2,type1,type);
    }

    static private void setMap(String type1, String type2, String type) {
        if (!UNIFICATION_MAP.containsKey(type1))
            UNIFICATION_MAP.put(type1,new HashMap());
        HashMap map = (HashMap) UNIFICATION_MAP.get(type1);
        map.put(type2,type);
    }

    static HashMap UNIFICATION_MAP = new HashMap();
    static {
        simpleType(TTMentionFactory.PERSON_TAG);
        simpleType(TTMentionFactory.MALE_TAG);
        simpleType(TTMentionFactory.MALE_PRONOUN_TAG);
        simpleType(TTMentionFactory.FEMALE_TAG);
        simpleType(TTMentionFactory.FEMALE_PRONOUN_TAG);
        simpleType(TTMentionFactory.LOCATION_TAG);
        simpleType(TTMentionFactory.ORGANIZATION_TAG);
        simpleType(TTMentionFactory.OTHER_TAG);

        // subsumes(TTMentionFactory.OTHER_TAG,TTMentionFactory.PERSON_TAG);
        // subsumes(TTMentionFactory.OTHER_TAG,TTMentionFactory.MALE_TAG);
        // subsumes(TTMentionFactory.OTHER_TAG,TTMentionFactory.MALE_PRONOUN_TAG);
        // subsumes(TTMentionFactory.OTHER_TAG,TTMentionFactory.FEMALE_TAG);
        // subsumes(TTMentionFactory.OTHER_TAG,TTMentionFactory.FEMALE_PRONOUN_TAG);
        // subsumes(TTMentionFactory.OTHER_TAG,TTMentionFactory.LOCATION_TAG);
        // subsumes(TTMentionFactory.OTHER_TAG,TTMentionFactory.ORGANIZATION_TAG);

        // subsumes(TTMentionFactory.ORGANIZATION_TAG,TTMentionFactory.PERSON_TAG);
        // subsumes(TTMentionFactory.ORGANIZATION_TAG,TTMentionFactory.MALE_TAG);
        // subsumes(TTMentionFactory.ORGANIZATION_TAG,TTMentionFactory.MALE_PRONOUN_TAG);
        // subsumes(TTMentionFactory.ORGANIZATION_TAG,TTMentionFactory.FEMALE_TAG);
        // subsumes(TTMentionFactory.ORGANIZATION_TAG,TTMentionFactory.FEMALE_PRONOUN_TAG);
        subsumes(TTMentionFactory.ORGANIZATION_TAG,TTMentionFactory.LOCATION_TAG);

        // subsumes(TTMentionFactory.LOCATION_TAG,TTMentionFactory.PERSON_TAG);
        // subsumes(TTMentionFactory.LOCATION_TAG,TTMentionFactory.MALE_TAG);
        // subsumes(TTMentionFactory.LOCATION_TAG,TTMentionFactory.MALE_PRONOUN_TAG);
        // subsumes(TTMentionFactory.LOCATION_TAG,TTMentionFactory.FEMALE_TAG);
        // subsumes(TTMentionFactory.LOCATION_TAG,TTMentionFactory.FEMALE_PRONOUN_TAG);

        subsumes(TTMentionFactory.PERSON_TAG,TTMentionFactory.MALE_TAG);
        subsumes(TTMentionFactory.PERSON_TAG,TTMentionFactory.FEMALE_TAG);

        subsumes(TTMentionFactory.MALE_PRONOUN_TAG,TTMentionFactory.MALE_TAG);

        subsumes(TTMentionFactory.FEMALE_PRONOUN_TAG,TTMentionFactory.FEMALE_TAG);

        unify(TTMentionFactory.PERSON_TAG,TTMentionFactory.FEMALE_PRONOUN_TAG,
              TTMentionFactory.FEMALE_TAG);

        unify(TTMentionFactory.PERSON_TAG,TTMentionFactory.MALE_PRONOUN_TAG,
              TTMentionFactory.MALE_TAG);

        unify(TTMentionFactory.MALE_TAG,TTMentionFactory.FEMALE_TAG,
              TTMentionFactory.PERSON_TAG);
    }

    private final Matcher[] mMaleAndFemaleMatchers = new Matcher[] {
        mExactPhraseMatch,
        mPersonFemalePronounMatch,
        mPersonMalePronounMatch,
        mSequenceSubstringMatch,
        mSynonymMatch
    };
    private final Matcher[] mFemaleMatchers = new Matcher[] {
        mExactPhraseMatch,
        mFemalePronounMatch,
        mSequenceSubstringMatch,
        mSynonymMatch
    };
    private final Matcher[] mFemalePronounMatchers = new Matcher[] {
        mFemalePronounMatch
    };
    private final Matcher[] mMaleMatchers = new Matcher[] {
        mExactPhraseMatch,
        mSequenceSubstringMatch,
        mMalePronounMatch,
        mSynonymMatch
    };
    private final Matcher[] mMalePronounMatchers = new Matcher[] {
        mMalePronounMatch
    };
    private final Matcher[] mThingMatchers = new Matcher[] {
        mExactPhraseMatch,
        mSequenceSubstringMatch,
        mSynonymMatch
    };
    private final Matcher[] mUnknownMatchers = new Matcher[] {
        mExactPhraseMatch,
        mSequenceSubstringMatch,
        mSynonymMatch
    };

    private final Killer mHonorificConflictKiller
        = new HonorificConflictKiller();

    private final Killer mGenderKiller
        = new GenderKiller();

    private final Killer mTypeConflictKiller
        = new TypeConflictKiller();

    private final Killer[] mMaleAndFemaleKillers = new Killer[] {
        mHonorificConflictKiller,
        mTypeConflictKiller
    };
    private final Killer[] mMaleKillers = new Killer[] {
        mHonorificConflictKiller,
        mGenderKiller,
        mTypeConflictKiller
    };
    private final Killer[] mMalePronounKillers = new Killer[] {
        mHonorificConflictKiller,
        mGenderKiller,
        mTypeConflictKiller
    };
    private final Killer[] mFemaleKillers = new Killer[] {
        mHonorificConflictKiller,
        mGenderKiller,
        mTypeConflictKiller
    };
    private final Killer[] mFemalePronounKillers = new Killer[] {
        mHonorificConflictKiller,
        mGenderKiller,
        mTypeConflictKiller
    };
    private final Killer[] mThingKillers = new Killer[] {
        mHonorificConflictKiller,
        mTypeConflictKiller
    };
    private final Killer[] mUnknownKillers = new Killer[] {
        mHonorificConflictKiller,
        mTypeConflictKiller
    };


    /**
     * Construct an instance of a matcher set using hard-coded
     * constraints.
     */
    public TTMatchers() {
        mTypeToMatchers.put(TTMentionFactory.PERSON_TAG,mMaleAndFemaleMatchers);
        mTypeToMatchers.put(TTMentionFactory.FEMALE_TAG,mFemaleMatchers);
        mTypeToMatchers.put(TTMentionFactory.FEMALE_PRONOUN_TAG,mFemalePronounMatchers);
        mTypeToMatchers.put(TTMentionFactory.MALE_TAG,mMaleMatchers);
        mTypeToMatchers.put(TTMentionFactory.MALE_PRONOUN_TAG,mMalePronounMatchers);
        mTypeToMatchers.put(TTMentionFactory.ORGANIZATION_TAG,mThingMatchers);
        mTypeToMatchers.put(TTMentionFactory.LOCATION_TAG,mThingMatchers);
        mTypeToMatchers.put(TTMentionFactory.OTHER_TAG,mThingMatchers);
        mTypeToKillers.put(TTMentionFactory.PERSON_TAG,mMaleAndFemaleKillers);
        mTypeToKillers.put(TTMentionFactory.FEMALE_TAG,mFemaleKillers);
        mTypeToKillers.put(TTMentionFactory.FEMALE_PRONOUN_TAG,mFemalePronounKillers);
        mTypeToKillers.put(TTMentionFactory.MALE_TAG,mMaleKillers);
        mTypeToKillers.put(TTMentionFactory.MALE_PRONOUN_TAG,mMalePronounKillers);
        mTypeToKillers.put(TTMentionFactory.ORGANIZATION_TAG,mThingKillers);
        mTypeToKillers.put(TTMentionFactory.LOCATION_TAG,mThingKillers);
        mTypeToKillers.put(TTMentionFactory.OTHER_TAG,mThingKillers);
    }

    /**
     * Return this matcher set's database-driven synonym matcher.
     *
     * @return This matcher set's database-driven synonym matcher.
     */
    private TTSynonymMatch synonymMatcher() {
        return mSynonymMatch;
    }

    /**
     * Return the array of matchers for the specified entity type.
     *
     * @param entityType Type of entity whose matchers are returned.
     * @return Array of matchers for the specified entity type.
     */
    public Matcher[] getMatchers(String entityType) {
        Object result = mTypeToMatchers.get(entityType);
        if (result == null) return mUnknownMatchers;
        return (Matcher[]) result;
    }

    /**
     * Return the array of killers for the specified entity type.
     *
     * @param entityType Type of entity whose killers are returned.
     * @return Array of killers for the specified entity type.
     */
    public Killer[] getKillers(String entityType) {
        Object result = mTypeToKillers.get(entityType);
        if (result == null) return mUnknownKillers;
        return (Killer[]) result;
    }


}
