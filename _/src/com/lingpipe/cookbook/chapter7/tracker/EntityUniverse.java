package com.lingpipe.cookbook.chapter7.tracker;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.ObjectToSet;
import com.aliasi.util.Streams;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityUniverse {


    /** The <code>EnitityUniverse</code> is an in memory representation of the
     * global entities within a data collection. This is a minimal
     * implementation that contains only the information required for
     * <code>XDocCoref</code> to function.
     * @author Bob Carpenter, Breck Baldwin
     * @version 1.0
     * @since tracker 1.0
     **/


    private long mLastId = FIRST_SYSTEM_ID;

    private final TokenizerFactory mTokenizerFactory;

    private final Map<Long,Entity> mIdToEntity
        = new HashMap<Long,Entity>();

    private final ObjectToSet<String,Entity> mXdcPhraseToEntitySet
        = new ObjectToSet<String,Entity>();

    

    public EntityUniverse(TokenizerFactory tokenizerFactory) {
        mTokenizerFactory = tokenizerFactory;
    }


    // READERS

    public TokenizerFactory tokenizerFactory() {
        return mTokenizerFactory;
    }

    public Entity getEntity(long id) {
        return mIdToEntity.get(id);
    }

    public Set<Entity> xdcEntitiesWithPhrase(String phrase) {
        return mXdcPhraseToEntitySet.getSet(normalPhrase(phrase));
    }

    public Set<Entity> userDefinedEntitySet() {
        Set<Entity> result = new HashSet<Entity>();
        for (Entity entity : mIdToEntity.values())
            if (entity.isUserDefined())
                result.add(entity);
        return result;
    }

    // closes stream being written to
    public void writeTo(OutputStream out, String charset)
        throws IOException {
        Writer writer = null;
        try {
            writer =new OutputStreamWriter(out,charset);
            writer.write("lastId=" + mLastId+"\n");
            Long[] ids = mIdToEntity.keySet().<Long>toArray(new Long[mIdToEntity.size()]);
            Arrays.sort(ids);
            for (Long id : ids) {
                Entity entity = mIdToEntity.get(id);
                writer.write("\n");
                entity.writeTo(writer);
            }
        } finally {
            Streams.closeWriter(writer);
        }
    }

    // WRITERS

    // coming in from re-reading old docs
    public void addHistoricEntity(Long id, String type,
                                  Set<String> aliases) {
        Entity entity = mIdToEntity.get(id);
        if (entity != null) {
            entity.setType(type);
            for (String alias : aliases)
                addXdcPhrase(alias,entity);
        } else if (id < FIRST_SYSTEM_ID) {
            return; // dead user entity not in mIdToEntity map
        } else {
            entity = new Entity(id,type,aliases,null,null,null);
            add(entity);
        }
    }

    public void addPhraseToEntity(String phrase, Entity entity) {
        if (entity.containsPhrase(phrase))
            return;
        Set<String> xdcPhrases = entity.xdcPhrases();
        Set<String> nonXdcPhrases = entity.nonXdcPhrases();
        boolean hasMultiWordPhrases = false;
        for (String entityPhrase : xdcPhrases)
            if (entityPhrase.indexOf(' ') > -1 )
                hasMultiWordPhrases = true;

        for (String entityPhrase : nonXdcPhrases)
            if (entityPhrase.indexOf(' ') > -1 )
                hasMultiWordPhrases = true;

        if (isXdcPhrase(phrase,hasMultiWordPhrases))
            addXdcPhrase(phrase,entity);
        else
            entity.addSpeculativeNonXdcPhrase(phrase);
    }

    //if there is a longer string, then disallow single tokens
    //

    public Entity createEntitySpeculative(Set<String> phrases,
                                          String entityType) {
        Set<String> nonXdcPhrases = new HashSet<String>();
        Set<String> xdcPhrases = new HashSet<String>();
        boolean hasMultiWordPhrases = false;
        for (String phrase : phrases)
            if (phrase.indexOf(' ') > -1 )
                hasMultiWordPhrases = true;
        for (String phrase : phrases) {
            if (isXdcPhrase(phrase,hasMultiWordPhrases))
                xdcPhrases.add(phrase);
            else
                nonXdcPhrases.add(phrase);
        }
        while (mIdToEntity.containsKey(++mLastId)) ; // move up to next untaken ID
        Entity entity = new Entity(mLastId,entityType,
                                   null,null,xdcPhrases,nonXdcPhrases);
        add(entity);
        return entity;
    }

    public boolean isXdcPhrase(String phrase, boolean hasMultiWordPhrase) {

        if (mXdcPhraseToEntitySet.containsKey(normalPhrase(phrase)))
            return false;
        if (phrase.indexOf(' ') == -1 && hasMultiWordPhrase) {
            return false;
        }
        if (PronounChunker.isPronominal(phrase))
            return false;
        return true;
    }

    public Entity createEntityDictionary(DictionaryEntitySpec entitySpec) {
        Set<String> userXdcPhraseSet = new HashSet<String>();
        Set<String> userNonXdcPhraseSet = new HashSet<String>();
        String[] aliases = entitySpec.aliases();
        boolean[] xdcs = entitySpec.xdcs();
        for (int i = 0; i < aliases.length; ++i) {
            if (xdcs[i])
                userXdcPhraseSet.add(aliases[i]);
            else
                userNonXdcPhraseSet.add(aliases[i]);
        }
        long id = entitySpec.id();
        if (mIdToEntity.containsKey(id)) {
            String msg = "Entity ID already taken; must revise instead of create.";
            throw new IllegalArgumentException(msg);
        }
        Entity entity = new Entity(id,
                                   entitySpec.type(),
                                   userXdcPhraseSet,
                                   userNonXdcPhraseSet,
                                   null,
                                   null);
        add(entity);
        return entity;
    }


    public void updateEntitySpec(DictionaryEntitySpec entitySpec) {
        long id = entitySpec.id();
        Entity entity = mIdToEntity.get(id);
        if (entity == null) {
            createEntityDictionary(entitySpec);
            return;
        }
        mergeEntitySpec(entity,entitySpec);
    }


    public void remove(Entity entity) {
        long id = entity.id();
        mIdToEntity.remove(id);

        for (String rawPhrase : entity.xdcPhrases()) {
            String phrase = rawPhrase.toLowerCase();
            removePhraseToXdcToEntitySet(phrase,entity);
            // ?? allow other phrases in non-xdc to become xdc
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Entity Universe: Last id= " + mLastId + "\n");
        sb.append("XDC Phrase to Entity mapping\n");
        for (String phrase : mXdcPhraseToEntitySet.keySet()) {
            sb.append("\n");
            sb.append(phrase);
            sb.append(": ");
            for (Entity entity : mXdcPhraseToEntitySet.get(phrase)) {
                sb.append(" " + entity.id());
            }
        }
        sb.append("\nEntities");
        for (Long key : mIdToEntity.keySet()) {
            sb.append("\n\n");
            sb.append(mIdToEntity.get(key).toString());
        }
        return sb.toString();
    }

    public void add(Entity e) {
        if (e.id() > mLastId)
            mLastId = e.id();
        mIdToEntity.put(new Long(e.id()),e);
        for (String phrase : e.xdcPhrases()) {
            addPhraseToXdcToEntitySet(phrase,e);
        }
    }

    public String normalPhrase(String phrase) {
        return concatenateNormalTokens(normalTokens(phrase));
    }

    public static String normalizeWhitespace(String whitespace) {
        return whitespace.length() > 0 ? " " : "";
    }

    public static String concatenateNormalTokens(String[] toks) {
        if (toks.length < 1) return "";
        if (toks.length == 1) return toks[0];
        StringBuilder sb = new StringBuilder(toks[0]);
        for (int i = 1; i < toks.length; ++i) {
            sb.append(' ');
            sb.append(toks[i]);
        }
        return sb.toString();
    }


    public String[] normalTokens(String phrase) {
        List<String> tokenList = new ArrayList<String>();
        char[] cs = phrase.toCharArray();
        Tokenizer tokenizer
            = mTokenizerFactory
            .tokenizer(cs,0,cs.length);
        for (String token : tokenizer)
            if (!nonEntityToken(token))
                tokenList.add(token);
        return tokenList.<String>toArray(new String[tokenList.size()]);
    }

    private static boolean nonEntityToken(String token) {
        char[] cs = token.toCharArray();
        for (int i = 0; i < cs.length; ++i)
            if (Character.isLetter(cs[i])
                || Character.isDigit(cs[i])) return false;
        return true;
    }

    private void addPhraseToXdcToEntitySet(String phrase,Entity entity) {
        mXdcPhraseToEntitySet.addMember(normalPhrase(phrase),entity);
    }

    private void removePhraseToXdcToEntitySet(String phrase,Entity entity) {
        mXdcPhraseToEntitySet.removeMember(normalPhrase(phrase),entity);
    }

    private void addXdcPhrase(String phrase, Entity entity) {
        addPhraseToXdcToEntitySet(phrase,entity);
        entity.addSpeculativeXdcPhrase(phrase);
    }


    private void mergeEntitySpec(Entity entity, DictionaryEntitySpec entitySpec) {
        if (!entitySpec.allowSpeculativeAliases()) {
            for (String xdcAlias : entity.xdcPhrases())
                removePhraseToXdcToEntitySet(xdcAlias,entity);
            String[] aliases = entitySpec.aliases();
            boolean[] xdcs = entitySpec.xdcs();
            for (int i = 0; i < aliases.length; ++i)
                if (xdcs[i])
                    addPhraseToXdcToEntitySet(aliases[i].toLowerCase(),entity);
        } else {
            String[] aliases = entitySpec.aliases();
            boolean[] xdcs = entitySpec.xdcs();
            for (int i = 0; i < aliases.length; ++i) {
                if (xdcs[i]) {
                    addPhraseToXdcToEntitySet(aliases[i],entity);
                } else {
                    removePhraseToXdcToEntitySet(aliases[i],entity);
                }
            }
        }
        // could just completely re-index after merge instead of above
        entity.merge(entitySpec);
    }
    
    public long getLastId () {
        return mLastId;
    }


    static final long FIRST_SYSTEM_ID = 1000000000;



}
