package com.lingpipe.cookbook.chapter7.tracker;

import com.aliasi.util.Streams;
//import com.aliasi.util.XML;

import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;
import com.aliasi.xml.SAXWriter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.dict.MapDictionary;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;


import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class Dictionary {

    private final Set<Long> mDictionaryIdSet;
    private final DictionaryEntitySpec[] mDictionaryEntitySpecs;
    private final String[] mStopPhrases;


    public Dictionary() {
        mDictionaryEntitySpecs = new DictionaryEntitySpec[0];
        mStopPhrases = new String[0];
        mDictionaryIdSet = new HashSet<Long>();
    }

    public Dictionary(DictionaryEntitySpec[] entitySpecs,
                      String[] stopPhrases) {
        mDictionaryEntitySpecs = entitySpecs;
        mStopPhrases = stopPhrases;
        mDictionaryIdSet = new HashSet<Long>();
        for (DictionaryEntitySpec spec : entitySpecs)
            mDictionaryIdSet.add(spec.id());
    }

    public boolean isDictionaryId(Long id) {
        return mDictionaryIdSet.contains(id);
    }

    public ExactDictionaryChunker chunker(TokenizerFactory tokenizerFactory,
                                          boolean returnAllMatches,
                                          boolean caseSensitive) {
        MapDictionary<String> dictionary 
            = new MapDictionary<String>();
        for (DictionaryEntitySpec entitySpec
                 : mDictionaryEntitySpecs) {
            String[] aliases = entitySpec.aliases();
            boolean[] xdcs = entitySpec.xdcs();
            for (int i = 0; i < aliases.length; ++i)
                if (xdcs[i])
                    dictionary.addEntry(new DictionaryEntry<String>(aliases[i],
                                                                    entitySpec.type()));
        }
        
        ExactDictionaryChunker chunker
            = new ExactDictionaryChunker(dictionary,
                                         tokenizerFactory,
                                         returnAllMatches,caseSensitive);
        return chunker;
    }
    

    public static Dictionary read(InputSource in)
        throws SAXException, IOException {

        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        //xmlReader.setFeature(XML.VALIDATION_FEATURE,false);

        Handler xmlHandler = new Handler();
        xmlReader.setContentHandler(xmlHandler);
        xmlReader.setDTDHandler(xmlHandler);
        xmlReader.setEntityResolver(xmlHandler);
        xmlReader.parse(in);
        return xmlHandler.getDictionary();
    }

    public void writeTo(SAXWriter writer)
        throws IOException, SAXException {

        writer.setDTDString("<!DOCTYPE dictionary PUBLIC \"alias-i-entity-dictionary\" \"http://www.alias-i.com/dtd/entity-dictionary.dtd\">");

        writer.startDocument();

        writer.characters("\n");
        writer.startSimpleElement("dictionary");

        writer.characters("\n");

        for (DictionaryEntitySpec entitySpec : mDictionaryEntitySpecs)
            entitySpec.writeContentTo(writer);

        writer.characters("\n");
        writer.startSimpleElement("stoplist");
        for (String phrase : mStopPhrases) {
            writer.characters("\n  ");
            writer.startSimpleElement("phrase");
            writer.characters(phrase.toCharArray());
            writer.endSimpleElement("phrase");
        }
        writer.characters("\n");
        writer.endSimpleElement("stoplist");
        writer.characters("\n\n");
        writer.endSimpleElement("dictionary");
        writer.characters("\n");
        writer.endDocument();
    }

    public DictionaryEntitySpec[] entitySpecs() {
        return mDictionaryEntitySpecs;
    }

    public String[] stopPhrases() {
        return mStopPhrases;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ENTITIES\n");
        for (DictionaryEntitySpec entitySpec : mDictionaryEntitySpecs)
            sb.append(entitySpec + "\n");
        sb.append("\nSTOP PHRASES\n");
        for (String stopPhrase : mStopPhrases)
            sb.append(stopPhrase + "\n");
        return sb.toString();
    }

    static class Handler extends DelegatingHandler {
        final DictionaryEntitySpec.Handler mEntitySpecHandler;
        final List<DictionaryEntitySpec> mEntitySpecList
            = new ArrayList<DictionaryEntitySpec>();
        final TextAccumulatorHandler mStopPhraseHandler
            = new TextAccumulatorHandler();
        final Set<String> mStopPhraseSet = new HashSet<String>();
        int mTimeoutInDays;

        public Handler() {
            mEntitySpecHandler = new DictionaryEntitySpec.Handler(this);
            setDelegate("entity",mEntitySpecHandler);
            setDelegate("phrase",mStopPhraseHandler);
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            if ("entity".equals(qName))
                mEntitySpecList.add(mEntitySpecHandler
                                    .getDictionaryEntitySpec());
            else if ("phrase".equals(qName))
                mStopPhraseSet.add(mStopPhraseHandler.getText());
        }
        Dictionary getDictionary() {
            DictionaryEntitySpec[] entitySpecs
                = mEntitySpecList
                .<DictionaryEntitySpec>
                toArray(new DictionaryEntitySpec[mEntitySpecList.size()]);
            String[] stopPhrases
                = mStopPhraseSet
                .<String>toArray(new String[mStopPhraseSet.size()]);
            return new Dictionary(entitySpecs,stopPhrases);
        }
        public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException {

            if (DICTIONARY_PUBLIC_ID.equals(publicId)) {
                InputStream in
                    = this.getClass().getResourceAsStream(DTD_FILE_NAME);

                if (in == null)
                    throw new RuntimeException("need to put DTD=" + publicId + " on classpath");

                return new InputSource(in);
            }
            return super.resolveEntity(publicId,systemId);
        }


    }

    static final String DICTIONARY_PUBLIC_ID
        = "alias-i-entity-dictionary";

    static final String DTD_FILE_NAME
        = "entity-dictionary.dtd";


    public static Dictionary read(File dictFile, String charEncoding)
        throws IOException, SAXException {

        FileInputStream in = null;
        BufferedInputStream bufIn = null;
        try {
            in = new FileInputStream(dictFile);
            bufIn = new BufferedInputStream(in);
            InputSource inSource  = new InputSource(bufIn);
            inSource.setEncoding(charEncoding);
            Dictionary dictionary = Dictionary.read(inSource);
            return dictionary;
        } finally {
            Streams.closeInputStream(bufIn);
            Streams.closeInputStream(in);
        }
    }

    public static void main(String[] args) throws IOException, SAXException {
        Dictionary dictionary = Dictionary.read(new File(args[0]), com.aliasi.util.Strings.UTF8);

        System.out.println(dictionary);  // Dictionary.main

        SAXWriter writer = new SAXWriter(System.out,"ISO-8859-1");  // Dictionary.main
        dictionary.writeTo(writer);
    }


}
