package com.lingpipe.cookbook.chapter7.tracker;

import com.aliasi.dict.MapDictionary;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import java.util.Set;
import java.util.HashSet;

public class PronounChunker extends ExactDictionaryChunker {

    public PronounChunker() {
        super(createDictionary(),
              IndoEuropeanTokenizerFactory.INSTANCE,
              false,true);
    }

    static MapDictionary createDictionary() {
        MapDictionary dictionary = new MapDictionary();
        for (String phrase : MALE_PRONOUN_SET)
            dictionary.addEntry(new DictionaryEntry(phrase,"MALE_PRONOUN",1.0));
        for (String phrase : FEMALE_PRONOUN_SET)
            dictionary.addEntry(new DictionaryEntry(phrase,"FEMALE_PRONOUN",1.0));
        return dictionary;
    }

    static Set<String> MALE_PRONOUN_SET = new HashSet<String>();
    static Set<String> FEMALE_PRONOUN_SET = new HashSet<String>();
    static Set<String> PRONOUN_SET = new HashSet<String>();
    static {
        MALE_PRONOUN_SET.add("he");
        MALE_PRONOUN_SET.add("him");
        MALE_PRONOUN_SET.add("his");
        MALE_PRONOUN_SET.add("He");
        MALE_PRONOUN_SET.add("Him");
        MALE_PRONOUN_SET.add("His");
        MALE_PRONOUN_SET.add("HE");
        MALE_PRONOUN_SET.add("HIM");
        MALE_PRONOUN_SET.add("HIS");

        FEMALE_PRONOUN_SET.add("she");
        FEMALE_PRONOUN_SET.add("her");
        FEMALE_PRONOUN_SET.add("hers");
        FEMALE_PRONOUN_SET.add("She");
        FEMALE_PRONOUN_SET.add("Her");
        FEMALE_PRONOUN_SET.add("Hers");
        FEMALE_PRONOUN_SET.add("SHE");
        FEMALE_PRONOUN_SET.add("HER");
        FEMALE_PRONOUN_SET.add("HERS");

        PRONOUN_SET.addAll(MALE_PRONOUN_SET);
        PRONOUN_SET.addAll(FEMALE_PRONOUN_SET);
    }

    public static boolean isPronominal(String phrase) {
        return PRONOUN_SET.contains(phrase);
    }

    public static String MALE_PRONOUN = "MALE_PRONOUN";
    public static String FEMALE_PRONOUN = "FEMALE_PRONOUN";


}
