package com.lingpipe.cookbook.chapter7.tracker;

import com.aliasi.coref.Mention;
import com.aliasi.coref.MentionChain;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;


//import com.aliasi.util.Collections;
import com.aliasi.util.ObjectToSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XDocCoref {

    protected final EntityUniverse mEntityUniverse;

    private final TokenizerFactory mTokenizerFactory;
    protected boolean mAddSpeculativeEntities;

    
    public XDocCoref(EntityUniverse entitySet) {
        this(entitySet, true);
    }

    public XDocCoref(EntityUniverse entitySet,
                     boolean addSpeculativeEntitiesToEntityUniverse) {
        mEntityUniverse = entitySet;
        mTokenizerFactory = entitySet.tokenizerFactory();
        mAddSpeculativeEntities = addSpeculativeEntitiesToEntityUniverse;
    }

    // READERS

    public EntityUniverse entityUniverse() {
        return mEntityUniverse;
    }

    /*    // should use this to normalize everywhere, but need Xdoc handle
    private String normalPhrase(String phrase) {
        return concatenateNormalTokens(normalTokens(phrase));
    }

    private String[] normalTokens(String phrase) {
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

    */

    // WRITERS

    // returns map from TTMention to ids
    // massive side-effects on entity set
    public Entity[] xdocCoref(MentionChain[] chains) {
        Long localId = mEntityUniverse.getLastId();
        Entity[] entities = new Entity[chains.length];

        Map<MentionChain,Entity> chainToEntity
            = new HashMap<MentionChain,Entity>();
        ObjectToSet<Entity,MentionChain> entityToChainSet
            = new ObjectToSet<Entity,MentionChain>();

        for (MentionChain chain : chains)
            resolveMentionChain((TTMentionChain) chain,
                                chainToEntity, entityToChainSet);

        for (int i = 0; i < chains.length; ++i) {
            TTMentionChain chain = (TTMentionChain) chains[i];
            Entity entity = chainToEntity.get(chain);

            if (entity != null) {
                 if (Tracker.DEBUG) {
                   System.out.println("XDOC: resolved to " + entity);  //
                   Set chainSetForEntity = entityToChainSet.get(entity);
                   if (chainSetForEntity.size() > 1) {
                     System.out.println("XDOC: multiple chains resolved to same entity " + entity.id());
                     
                   }
                 }

                entities[i] = entity;
                if (entity.allowSpeculativeAliases())
                    addMentionChainToEntity(chain,entity);
            } else {

                Entity newEntity 
                    = mAddSpeculativeEntities  
                    ? promote(chain) 
                    :promoteButDoNotAddToEntityUniverse(chain, ++localId);
                entities[i] = newEntity;
            }
        }
        return entities;
    }
    
    public void setAddSpeculativeEntities (boolean value) {
        mAddSpeculativeEntities = value;
    }

    private void resolveMentionChain(TTMentionChain chain,
                                     Map<MentionChain,Entity> chainToEntity,
                                     ObjectToSet<Entity,MentionChain> entityToChainSet) {
         if (Tracker.DEBUG)
             System.out.println("XDOC: resolving mention chain " + chain);
        int maxLengthAliasOnMentionChain = 0;
        int maxLengthAliasResolvedToEntityFromMentionChain = -1;
        Set<String> tokens = new HashSet<String>();
        Set<Entity> candidateEntities = new HashSet<Entity>();
        for (String phrase : chain.normalPhrases()) {
            String[] phraseTokens = mEntityUniverse.normalTokens(phrase);
            String normalPhrase = EntityUniverse.concatenateNormalTokens(phraseTokens);
            for (int i = 0; i < phraseTokens.length; ++i)
                    tokens.add(phraseTokens[i]);
            int length = phraseTokens.length;
            if (length > maxLengthAliasOnMentionChain)
                maxLengthAliasOnMentionChain = length;
            Set<Entity> matchingEntities
                = mEntityUniverse.xdcEntitiesWithPhrase(phrase);

            for (Entity entity : matchingEntities) {
                //System.out.println("Candidate Entity: " + entity);
                if (null != TTMatchers.unifyEntityTypes(chain.entityType(),
                                                        entity.type())) {
                    if (maxLengthAliasResolvedToEntityFromMentionChain < length)
                        maxLengthAliasResolvedToEntityFromMentionChain = length;
                    candidateEntities.add(entity);
                }
            }
        }
        resolveCandidates(chain,
                          tokens,
                          candidateEntities,
                          maxLengthAliasResolvedToEntityFromMentionChain == maxLengthAliasOnMentionChain,
                          chainToEntity,
                          entityToChainSet);
    }


    private void resolveCandidates(TTMentionChain chain,
                                   Set<String> tokens,
                                   Set<Entity> candidateEntities,
                                   boolean resolvedAtMaxLength,
                                   Map<MentionChain,Entity> chainToEntity,
                                   ObjectToSet<Entity,MentionChain> entityToChainSet) {
        filterCandidates(chain,tokens,candidateEntities,resolvedAtMaxLength);
        if (candidateEntities.size() == 0)
            return;
        if (candidateEntities.size() == 1) {
            Entity entity = candidateEntities.iterator().next();
            chainToEntity.put(chain,entity);
            entityToChainSet.addMember(entity,chain);
            return;
        }
        if (Tracker.DEBUG) {
            System.out.println("Blown UP; candidateEntities.size()=" + candidateEntities.size());
            for (Entity entity : candidateEntities ) 
                System.out.println(entity);
        }
    }



    protected void filterCandidates(TTMentionChain chain,
                                  Set<String> tokens,
                                  Set<Entity> candidateEntities,
                                  boolean resolvedAtMaxLength) {
        if (candidateEntities.size() < 1) return;
        if (resolvedAtMaxLength) return;
        List<Entity> filteredEntities = new ArrayList<Entity>();
        Iterator<Entity> candidateIterator = candidateEntities.iterator();
        while (candidateIterator.hasNext()) {
            Entity entity = candidateIterator.next();
            Set<String> entityTokens = entity.tokens(mTokenizerFactory);
            if (entityTokens.size() == 1) continue;
            Set<String> intersection = new HashSet<String>(tokens);
            intersection.retainAll(entityTokens);
            int entityTokensSize = entityTokens.size();
            int intersectionSize = intersection.size();
            if ((entityTokensSize <= 2 && intersectionSize < 2)
                || (entityTokensSize > 2
                    && intersectionSize < tokens.size() - 1)
                || !TTMentionFactory.genderMatch(chain.entityType(),
                                                 entity.type())) {
                candidateIterator.remove();
            }
        }
    }

    private Entity promoteButDoNotAddToEntityUniverse(TTMentionChain chain, Long localId) {
        Entity entity = new Entity(localId,chain.entityType(),
                                   null,null,chain.normalPhrases(),new HashSet<String>());
        return entity;
    }

    private Entity promote(TTMentionChain chain) {
        Entity entity
            = mEntityUniverse.createEntitySpeculative(chain.normalPhrases(),
                                                      chain.entityType());
         if (Tracker.DEBUG)
         System.out.println("XDOC: promoted " + entity);
        return entity;
    }


    private void addMentionChainToEntity(TTMentionChain chain, Entity entity) {
        for (String phrase : chain.normalPhrases()) {
            mEntityUniverse.addPhraseToEntity(phrase,entity);
        }
    }

    private static boolean nonEntityToken(String token) {
        char[] cs = token.toCharArray();
        for (int i = 0; i < cs.length; ++i)
            if (Character.isLetter(cs[i])
                || Character.isDigit(cs[i])) return false;
        return true;
    }

    int tokenLength(String phrase) {
        int length = 1;
        for (int i = 0; i < phrase.length(); ++i)
            if (phrase.charAt(i) == ' ')
                ++length;
        return length;
    }
    /*
    private static String normalizeWhitespace(String whitespace) {
        return whitespace.length() > 0 ? " " : "";
    }

    private static String concatenateNormalTokens(String[] toks) {
        if (toks.length < 1) return "";
        if (toks.length == 1) return toks[0];
        StringBuilder sb = new StringBuilder(toks[0]);
        for (int i = 1; i < toks.length; ++i) {
            sb.append(' ');
            sb.append(toks[i]);
        }
        return sb.toString();
    }
    */

}
