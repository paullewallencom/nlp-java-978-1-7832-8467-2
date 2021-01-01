package com.lingpipe.cookbook.chapter7.tracker;

import com.aliasi.coref.Mention;
import com.aliasi.coref.MentionChain;
import com.aliasi.coref.MentionFactory;

import com.aliasi.tokenizer.TokenizerFactory;

import java.util.Collections;

/**
 * An instance of <code>TTMentionFactory</code> is used to create
 * mentions and mention chains for the coreference engine.
 */

public class TTMentionFactory implements MentionFactory {

    private static int mNextId = 0;

    public TTMatchers mMatchers = new TTMatchers();

    private final TokenizerFactory mTokenizerFactory;

    /**
     * Construct an instance of a mention factory.
     *
     * @param tokenizerFactory The tokenizer factory
     * for this mention factory.
     */
    public TTMentionFactory(TokenizerFactory tokenizerFactory) {
        mTokenizerFactory = tokenizerFactory;
    }

    /**
     * Returns the matching function mapping for this mention factory.
     *
     * @return The matching function mapping for this mention factory.
     */
    public TTMatchers matchers() {
        return mMatchers;
    }

    /**
     * Returns the TokenizerFactory for this MentionFactory
     *
     * @return TokenizerFactory
     */

    public TokenizerFactory tokenizerFactory() {
        return mTokenizerFactory;
    }

    /**
     * Returns a newly created mention of the specified phrase and
     * entity type.
     *
     * @param phrase Raw phrase underlying this mention.
     * @param entityType Type of this entity.
     * @return The created mention, an instance of {@link TTMention}.
     */
    public Mention create(String phrase, String entityType) {
        String gender = computeGender(entityType);
        boolean isPronominal = isPronominal(entityType);
        char[] cs = phrase.toCharArray();
        String[] tokens = mTokenizerFactory.tokenizer(cs,0,cs.length).tokenize();
        return new TTMention(phrase,entityType,
                             Collections.EMPTY_SET,
                             tokens,gender,isPronominal);
    }

    /**
     * Returns a newly created mention chain constructed from the
     * specified mention occurring at the specified sentence offset in
     * the document to a mention chain.
     *
     * @param mention Mention to promote to a mention chain.
     * @param sentenceOffset Sentence offset of mention in the document from
     * which it was drawn.
     * @return The created mention chain, an instance of {@link
     * TTMentionChain}.
     */
    public MentionChain promote(Mention mention, int sentenceOffset) {
        return new TTMentionChain((TTMention) mention,sentenceOffset,
                                  mNextId++,
                                  mMatchers);
    }


    /**
     * Returns the gender of the specified entity type.
     *
     * @param type Entity type whose gender is returned.
     * @return Gender associated with the specified entity type.
     */
    public static String computeGender(String type) {
        if (type.equals(MALE_TAG)) return MALE_GENDER;
        if (type.equals(MALE_PRONOUN_TAG)) return MALE_GENDER;
        if (type.equals(FEMALE_TAG)) return FEMALE_GENDER;
        if (type.equals(FEMALE_PRONOUN_TAG)) return FEMALE_GENDER;
        if (type.equals(PERSON_TAG)) return null;
        return NEUTER_GENDER;
    }

    /**
     * Returns <code>true</code> if the specified entity type
     * picks out an entity with male gender.
     *
     * @param type Entity type to check.
     * @return <code>true</code> if the specified entity type
     * picks out an entity with male gender.
     */
    public static boolean isMale(String type) {
        String gender = computeGender(type);
        return gender != null && gender.equals(MALE_GENDER);
    }


    /**
     * Returns <code>true</code> if the specified entity type
     * picks out an entity with female gender.
     *
     * @param type Entity type to check.
     * @return <code>true</code> if the specified entity type
     * picks out an entity with female gender.
     */
    public static boolean isFemale(String type) {
        String gender = computeGender(type);
        return gender != null && gender.equals(FEMALE_GENDER);
    }


    /**
     * Returns <code>true</code> if the specified entity type is
     * pronominal.
     *
     * @param type Entity type to check.
     * @return <code>true</code> if the specified entity type is
     * pronominal.
     */
    public static boolean isPronominal(String type) {
        return type.equals(MALE_PRONOUN_TAG)
            || type.equals(FEMALE_PRONOUN_TAG);
    }

    /**
     * Returns <code>true</code> if the genders implied by the
     * specified entity types are compatible.
     *
     * @param entityType1 First type to test.
     * @param entityType2 Second type to test.
     * @return <code>true</code> if the genders of the specified types
     * are compatible.
     */
    public static boolean genderMatch(String entityType1, String entityType2) {
        String gender1 = computeGender(entityType1);
        String gender2 = computeGender(entityType2);
        if (gender1 == null)
            return gender2 == null
                || gender2.equals(MALE_GENDER)
                || gender2.equals(FEMALE_GENDER);
        if (gender1.equals(MALE_GENDER))
            return gender2 == null || gender2.equals(MALE_GENDER);
        if (gender1.equals(FEMALE_GENDER))
            return gender2 == null || gender2.equals(FEMALE_GENDER);
        return gender1.equals(gender2);
    }

    /**
     * Male gender value.
     */
    public static final String MALE_GENDER = "m";

    /**
     * Female gender value.
     */
    public static final String FEMALE_GENDER = "f";

    /**
     * Neuter gender value.
     */
    public static final String NEUTER_GENDER = "n";


    /** The named-entity tag assigned to people of unknown gender. */
    public static final String PERSON_TAG = "PERSON";

    /** The named-entity tag assigned to female people. */
    public static final String FEMALE_TAG = "FEMALE";

    /** The named-entity tag assigned to female pronouns. */
    public static final String FEMALE_PRONOUN_TAG = "FEMALE_PRONOUN";

    /** The named-entity tag assigned to male people. */
    public static final String MALE_TAG = "MALE";

    /** The named-entity tag assigned to male pronouns. */
    public static final String MALE_PRONOUN_TAG = "MALE_PRONOUN";

    /** The named-entity tag assigned to organizations. */
    public static final String ORGANIZATION_TAG = "ORGANIZATION";

    /** The named-entity tag assigned to locations. */
    public static final String LOCATION_TAG = "LOCATION";

    /** The named-entity tag assigned to entities that does not belong to another class. */
    public static final String OTHER_TAG = "OTHER";



}
