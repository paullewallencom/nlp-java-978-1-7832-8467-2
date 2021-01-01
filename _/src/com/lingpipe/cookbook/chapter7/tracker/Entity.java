package com.lingpipe.cookbook.chapter7.tracker;



import com.aliasi.tokenizer.TokenizerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.io.IOException;
import java.io.Writer;


public class Entity {

    private final long mId;

    private String mEntityType;
    private boolean mAllowSpeculativeAliases = true;
    private String[] mUserXdcPhrases;
    private String[] mUserNonXdcPhrases;
    private String[] mSpeculativeXdcPhrases;
    private String[] mSpeculativeNonXdcPhrases;

    public Entity(long id,
                  String entityType,
                  Set<String> userXdcPhraseSet,
                  Set<String> userNonXdcPhraseSet,
                  Set<String> speculativeXdcPhraseSet,
                  Set<String> speculativeNonXdcPhraseSet) {

        mId = id;
        mEntityType = entityType;
        mUserXdcPhrases = toArray(userXdcPhraseSet);
        mUserNonXdcPhrases = toArray(userNonXdcPhraseSet);
        mSpeculativeXdcPhrases = toArray(speculativeXdcPhraseSet);
        mSpeculativeNonXdcPhrases = toArray(speculativeNonXdcPhraseSet);

    }

    /*
    public Entity(long id,
                  String entityType,
                  String[] userXdcPhraseSet,
                  String[] userNonXdcPhraseSet,
                  String[] speculativeXdcPhraseSet,
                  String[] speculativeNonXdcPhraseSet) {
        mId = id;
        mEntityType = entityType;

        mUserXdcPhrases = userXdcPhraseSet;
        mUserNonXdcPhrases = userNonXdcPhraseSet;
        mSpeculativeXdcPhrases = speculativeXdcPhraseSet;
        mSpeculativeNonXdcPhrases = speculativeNonXdcPhraseSet;
    }
    */

    // READS

    public void writeTo(Writer writer) throws IOException {
        writer.write(Long.toString(mId));
        writer.write(' ');
        writer.write(mEntityType);
        writer.write(' ');
        writer.write(mAllowSpeculativeAliases ? " +spec" : " -spec");
        writer.write('\n');
        write(writer,mUserNonXdcPhrases," +user,-xdc");
        write(writer,mUserXdcPhrases," +user,+xdc");
        write(writer,mSpeculativeNonXdcPhrases," -user,-xdc");
        write(writer,mSpeculativeXdcPhrases," -user,+xdc");
        write(writer,mSpeculativeNonXdcPhrases," -user,-xdc");
    }

    void write(Writer writer, String[] aliases, String desc) throws IOException {
        for (String alias : aliases) {
            writer.write(alias);
            writer.write(desc);
            writer.write('\n');
        }
    }

    public boolean isUserDefined() {
        return mUserXdcPhrases.length > 0
            || mUserNonXdcPhrases.length > 0;
    }

    public long id() {
        return mId;
    }

    public String type() {
        return mEntityType;
    }

    public boolean allowSpeculativeAliases() {
        return mAllowSpeculativeAliases;
    }

    public Set<String> tokens(TokenizerFactory tokenizerFactory) {
        Set<String> tokenSet = new HashSet<String>();
        addTokens(mUserXdcPhrases,tokenSet,tokenizerFactory);
        addTokens(mUserNonXdcPhrases,tokenSet,tokenizerFactory);
        addTokens(mSpeculativeXdcPhrases,tokenSet,tokenizerFactory);
        addTokens(mSpeculativeNonXdcPhrases,tokenSet,tokenizerFactory);
        return tokenSet;
    }

    public Set<String> xdcPhrases() {
        Set<String> xdcPhraseSet = new HashSet<String>();
        for (String phrase : mUserXdcPhrases)
            xdcPhraseSet.add(phrase);
        for (String phrase : mSpeculativeXdcPhrases)
            xdcPhraseSet.add(phrase);
        return xdcPhraseSet;
    }

    public Set<String> nonXdcPhrases() {
        Set<String> nonXdcPhraseSet = new HashSet<String>();
        for (String phrase : mUserNonXdcPhrases)
            nonXdcPhraseSet.add(phrase);
        for (String phrase : mSpeculativeNonXdcPhrases)
            nonXdcPhraseSet.add(phrase);
        return nonXdcPhraseSet;
    }

    public boolean containsPhrase(String phrase) {
        Set xdcPhraseSet = xdcPhrases();
        Set nonXdcPhraseSet = nonXdcPhrases();
        return xdcPhraseSet.contains(phrase) ||
        nonXdcPhraseSet.contains(phrase);
    }


    public int hashCode() {
        return (int) mId;
    }

    public boolean equals(Object that) {
        return (that instanceof Entity)
            && ((Entity)that).mId == mId;
    }

  public String toString() {
        return "id=" + mId
            + " type=" + mEntityType
            + " userDefined=" + isUserDefined()
            + " allowSpec=" + mAllowSpeculativeAliases
            + " user XDC=[" + join("| ",mUserXdcPhrases) + "]"
            + " user non-XDC=[" + join("| ",mUserNonXdcPhrases) + "]"
            + " spec XDC=[" + join("| ",mSpeculativeXdcPhrases) + "]"
            + " spec non-XDC=[" + join("| ",mSpeculativeNonXdcPhrases) + "]";
    }

   private String join(String joiner, String[] list) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < list.length; ++i) {
            sb.append(list[i]);
            if (i + 1 != list.length) 
                sb.append(joiner);
        }
        return sb.toString();
    }

    // WRITES

    public void setType(String type) {
        mEntityType = type;
    }

    /*    public void unifyType(String entityType) {
        if (type().equals(entityType)) return;
        setType(TTMatchers.unifyEntityTypes(type(),entityType));
    }
    */


    public void merge(DictionaryEntitySpec entitySpec) {
        if (entitySpec.id() != id()) {
            String msg = "non-matching ids."
                + " this=" + id()
                + " spec=" + entitySpec.id();
            throw new IllegalArgumentException(msg);
        }

        mEntityType = entitySpec.type();
        mAllowSpeculativeAliases = entitySpec.allowSpeculativeAliases();

        String[] aliases = entitySpec.aliases();
        boolean[] xdcs = entitySpec.xdcs();


        Set<String> userXdcPhraseSet = new HashSet<String>();
        Set<String> userNonXdcPhraseSet = new HashSet<String>();

        for (int i = 0; i < aliases.length; ++i) {
            if (xdcs[i])
                userXdcPhraseSet.add(aliases[i]);
            else
                userNonXdcPhraseSet.add(aliases[i]);
        }
        mUserXdcPhrases = toArray(userXdcPhraseSet);
        mUserNonXdcPhrases = toArray(userNonXdcPhraseSet);

        if (entitySpec.allowSpeculativeAliases()) {
            mSpeculativeXdcPhrases = filter(mSpeculativeXdcPhrases,aliases);
            mSpeculativeNonXdcPhrases = filter(mSpeculativeNonXdcPhrases,aliases);
        } else {
            mSpeculativeXdcPhrases = EMPTY_STRING_ARRAY;
            mSpeculativeNonXdcPhrases = EMPTY_STRING_ARRAY;
        }
    }

    public void addUserXdcPhrase(String phrase) {
        mUserXdcPhrases = add(mUserXdcPhrases,phrase);
    }

    public void addSpeculativeXdcPhrase(String phrase) {
        if (!allowSpeculativeAliases()) return;
        for (String userPhrase : mUserXdcPhrases)
            if (phrase.equals(userPhrase))
                return;
        mSpeculativeXdcPhrases = add(mSpeculativeXdcPhrases,phrase);
    }

    public void addSpeculativeNonXdcPhrase(String phrase) {
        if (!allowSpeculativeAliases()) return;
        mSpeculativeNonXdcPhrases = add(mSpeculativeNonXdcPhrases,phrase);
    }


    private static final String[] EMPTY_STRING_ARRAY
        = new String[0];

    private static void addTokens(String[] phrases, Set<String> tokenSet,
                   TokenizerFactory tokenizerFactory) {
        for (String phrase : phrases) {
            char[] cs = phrase.toCharArray();
            for (String token : tokenizerFactory.tokenizer(cs,0,cs.length))
                 tokenSet.add(token);
        }
    }

    private static String[] toArray(Collection<String> xs) {
        if (xs == null) return EMPTY_STRING_ARRAY;
        String[] result = new String[xs.size()];
        xs.toArray(result);
        return result;
    }

    private static String[] add(String[] xs, String y) {
        for (String x : xs)
            if (x.equals(y))
                return xs;
        String[] result = new String[xs.length + 1];
        for (int i = 0; i < xs.length; ++i)
            result[i] = xs[i];
        result[result.length-1] = y;
        return result;
    }

    private static String[] filter(String[] toKeep, String[] toRemove) {
        Set<String> result = new HashSet<String>((toKeep.length + toRemove.length) * 2);
        for (String alias : toKeep)
            result.add(alias);
        for (String alias : toRemove)
            result.remove(alias);
        return toArray(result);
    }



}
