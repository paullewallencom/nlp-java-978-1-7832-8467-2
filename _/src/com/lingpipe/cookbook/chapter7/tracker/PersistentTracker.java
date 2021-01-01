package com.lingpipe.cookbook.chapter7.tracker;

import com.aliasi.chunk.Chunker;

import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.Files;
import com.aliasi.util.ObjectToSet;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import com.aliasi.xml.SAXWriter;
import com.aliasi.hmm.HmmDecoder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PersistentTracker {

    private final TokenizerFactory mTokenizerFactory; // keep so we can make new trackers
    private final Chunker mSentenceChunker;
    private final Chunker mNeSpeculativeChunker;

    private final File mSaveDir;
    private final File mSaveDictFile;
    private final File mSaveDocsDir;

    private boolean mDocumentPersistence = true;

    private final Map<String,Integer> mPhraseCounts;
    private final HmmDecoder mPosTagger;

    private Tracker mTracker; // current tracker


    
    public PersistentTracker(TokenizerFactory tokenizerFactory,
                             Chunker sentenceChunker,
                             Chunker neSpeculativeChunker,
                             HmmDecoder posTagger,
                             Map<String,Integer> phraseCounts,
                             File saveDir,
                             boolean saveDocuments,
                             boolean addSpeculativeEntitiesToEntityUniverse)

        throws SAXException, IOException {
        mTokenizerFactory = tokenizerFactory;
        mSentenceChunker = sentenceChunker;
        mNeSpeculativeChunker = neSpeculativeChunker;
        mPosTagger = posTagger;
        mPhraseCounts = phraseCounts;
        mSaveDir = saveDir;
        mSaveDictFile = new File(mSaveDir,"entity-dictionary.xml");
        mSaveDocsDir = new File(mSaveDir,"docs");
        mDocumentPersistence = saveDocuments;
        try {
            mSaveDocsDir.mkdirs();
        } catch (SecurityException e) {
            String msg ="Persist docs dir=" + mSaveDocsDir
                + " exception=" + e;
            throw new IOException(msg);
        }
        init(addSpeculativeEntitiesToEntityUniverse); // going to reset the tracker
        
    }

    public PersistentTracker(TokenizerFactory tokenizerFactory,
                             Chunker sentenceChunker,
                             Chunker neSpeculativeChunker,
                             HmmDecoder posTagger,
                             Map<String,Integer> phraseCounts,
                             File saveDir)
        throws SAXException, IOException {
        this(tokenizerFactory,
                          sentenceChunker,
                          neSpeculativeChunker,
                          posTagger,
                          phraseCounts,
                          saveDir,
                          true,
                          true);

    }

    // READS

    public Tracker tracker() {
        return mTracker;
    }

    // WRITES

    public synchronized OutputDocument processDocument(InputDocument docIn)
        throws SAXException, IOException {
        OutputDocument docOut = mTracker.processDocument(docIn);
        if (mDocumentPersistence) {
            persist(docOut);
        }
        return docOut;
    }


    public synchronized OutputDocument[] processDocuments(InputSource in)
        throws SAXException, IOException {

        OutputDocument[] docs = mTracker.processDocuments(in);
        for (OutputDocument doc : docs)
            persist(doc);
        return docs;
    }


    public synchronized Dictionary resetDictionary(InputSource in)
        throws SAXException, IOException {

        Dictionary dict = Dictionary.read(in);
        mTracker.getMentionChunker()
            .setDictionaryChunker(dict
                                  .chunker(mTokenizerFactory,
                                    RETURN_ALL_MATCHES_FALSE,
                                    CASE_SENSITIVE_FALSE));
        mTracker.getMentionChunker()
            .setStopPhrases(dict.stopPhrases());

        mTracker.setDictionaryInEntityUniverse(dict);

        persist(dict);
        return dict;
    }


    public synchronized void expireDocsBefore(long msSinceEpoch)
        throws IOException, SAXException {

        expireDocs(mSaveDocsDir,msSinceEpoch);
        init(mTracker.mXDocCoref.mAddSpeculativeEntities);
    }



    private void expireDocs(File file, long msSinceEpoch)
        throws IOException {

        String docsDir = "docs";
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File subFile : files)
                expireDocs(subFile,msSinceEpoch);
            if (file.list().length == 0
                && !docsDir.equals(file.getName()) )
                file.delete();
        } else {
            String name = file.getName();
            int baseNameLen = name.length()-".xml".length();
            String baseName = name.substring(0,baseNameLen);
            int lastCharIndex = baseName.indexOf('_');
            if (lastCharIndex >= 0)
                baseName = baseName.substring(0,lastCharIndex);
            try {
                long t = Long.parseLong(baseName);
                if (t < msSinceEpoch) {
                    System.out.println("Deleting file: " + baseName);
                    file.delete();
                }
            } catch (NumberFormatException e) {
                String msg = "Bombing on parsing file baseName=" + baseName
                    + " file=" + file;
                System.out.println(msg);
                throw e;
            }
        }
    }


    private void init(boolean addSpeculativeEntitiesToEntityUniverse) throws IOException, SAXException {
        Dictionary dictionary = null;
        if (mSaveDictFile.exists()) 
            dictionary = Dictionary.read(mSaveDictFile,Strings.UTF8);
        else {
            dictionary = new Dictionary();
        }
        Map<Long,String> idToType = new HashMap<Long,String>();
        ObjectToSet<Long,String> idToAliases = new ObjectToSet<Long,String>();
        initMentions(mSaveDocsDir,idToType,idToAliases);

        // start-up new tracker with dict, idToType, idToAliases


        TokenizerFactory normTok = new NormalizedTokenizerFactory(mTokenizerFactory);
        EntityPhraseChunker entityPhraseChunker
            = new EntityPhraseChunker(mNeSpeculativeChunker,
                                      mPosTagger,
                                      dictionary.chunker(mTokenizerFactory,RETURN_ALL_MATCHES_FALSE,CASE_SENSITIVE_FALSE),
                                      dictionary.stopPhrases(),
                                      STOP_SUBSTRING_LIST,
                                      mPhraseCounts);

        mTracker = new Tracker(normTok,
                               mSentenceChunker,
                               entityPhraseChunker,
                               dictionary,
                               addSpeculativeEntitiesToEntityUniverse);

        addHistoricEntities(idToType,idToAliases);
    }

    private void addHistoricEntities(Map<Long,String> idToType,
                             ObjectToSet<Long,String> idToAliases) {
        for (Long id : idToType.keySet())
            mTracker
                .xDocCoref()
                .entityUniverse()
                .addHistoricEntity(id,
                                   idToType.get(id),
                                   idToAliases.getSet(id));
    }

    private void initMentions(File file,
                      Map<Long,String> idToType,
                      ObjectToSet<Long,String> idToAliases)
        throws SAXException, IOException {

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            Arrays.sort(files);
            for (File subFile : files)
                initMentions(subFile,idToType,idToAliases);
        } else {
            InputStream in = null;
            BufferedInputStream bufIn = null;
            try {
                in = new FileInputStream(file);
                bufIn = new BufferedInputStream(in);
                InputSource inSource = new InputSource(bufIn);
                inSource.setEncoding(Strings.UTF8);
                OutputDocument.readMentions(inSource,idToType,idToAliases);
            } finally {
                Streams.closeInputStream(bufIn);
                Streams.closeInputStream(in);
            }
        }
    }

    private void persist(Dictionary dict) throws IOException, SAXException {
        OutputStream out = null;
        BufferedOutputStream bufOut = null;
        try {
            out = new FileOutputStream(mSaveDictFile);
            bufOut = new BufferedOutputStream(out);
            SAXWriter writer = new SAXWriter(bufOut,Strings.UTF8);
            dict.writeTo(writer);
        } finally {
            Streams.closeOutputStream(bufOut);
            Streams.closeOutputStream(out);
        }
    }

    private void persist(OutputDocument doc) throws IOException, SAXException {
        long t = System.currentTimeMillis();
        File file = msToFile(mSaveDocsDir,t);
        file.getParentFile().mkdirs();
        OutputStream out = null;
        BufferedOutputStream bufOut = null;
        try {
            out = new FileOutputStream(file);
            bufOut = new BufferedOutputStream(out);
            SAXWriter writer = new SAXWriter(bufOut,Strings.UTF8);
            // only single document, not docs; ok with DTD, but need "doc" as top-level elt
            writer.startDocument();
            doc.writeTo(writer);
            writer.endDocument();
        } catch (Exception e) {
            // log("exception");
            try {
                file.delete();
            } catch (Exception e2) {
                String msg = "Exception persisting doc.=" + e
                    + " Exception deleting doc.=" + e2;
                throw new IOException(msg);
            }
        } finally {
            Streams.closeOutputStream(bufOut);
            Streams.closeOutputStream(out);
        }
    }



    private static File msToFile(File saveDir, long ms) {
        String prefix = Long.toString(ms / HUNDRED_BILLION);
        File dir = new File(saveDir,prefix);
        for (long place = TEN_BILLION; place >= HUNDRED_THOUSAND; place /= 10L)
            dir = new File(dir,Long.toString((ms / place) % 10));
        for (int i = 0; ; ++i) {
            String name = Long.toString(ms % HUNDRED_THOUSAND);
            if (i > 0) name += ("_" + i);
            File file = new File(dir,name+".xml");
            if (!file.exists()) return file;
        }
    }

    // #milliseconds; // time
    private static final long THOUSAND = 1000L;  // 1s
    private static final long TEN_THOUSAND = 10L * THOUSAND; // 10s
    private static final long HUNDRED_THOUSAND = 10L * TEN_THOUSAND; // 1.6m
    private static final long MILLION = 10L * HUNDRED_THOUSAND;  // 16.6m
    private static final long TEN_MILLION = 10L * MILLION;  // 2.75h
    private static final long HUNDRED_MILLION = 10L * TEN_MILLION; // 1.2d
    private static final long BILLION = 10L * HUNDRED_MILLION; // 12d
    private static final long TEN_BILLION = 10L * BILLION; // 120d
    private static final long HUNDRED_BILLION = 10L * TEN_BILLION; // 3.3y
    private static final long TRILLION = 10L * HUNDRED_BILLION; // 33y

    public static final boolean RETURN_ALL_MATCHES_FALSE = false;
    public static final boolean CASE_SENSITIVE_FALSE = false;

    public static String[] STOP_SUBSTRING_LIST = {"www",
                                                 ". com",
                                                 ".com",
                                                 ". net",
                                                 ".net",
                                                 ". edu",
                                                 ".edu",
                                                 ".php",
                                                 ".doc",
                                                 "&lt",
                                                 "&gt",
                                                 "&amp",
                                                 "/",
                                                 "''",
                                                 "?",
                                                 ">",
                                                 "<",
                                                 ":",
                                                 "=",
                                                 "[",
                                                 "]",
                                                 "(",
                                                 ")",
                                                 "\"",
                                                  ";",
                                                  "="};


    public static void main(String args[]) throws Exception {
        for (int i = 0; i < 10; ++i) {
            long time = System.currentTimeMillis();
            File file = msToFile(new File("root"),time);
            System.out.println("time=" + time + " file=" + file); // PersistentTracker.main()
        }
    }


}
