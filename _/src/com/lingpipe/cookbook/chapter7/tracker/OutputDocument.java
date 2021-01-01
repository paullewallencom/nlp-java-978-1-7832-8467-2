package com.lingpipe.cookbook.chapter7.tracker;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;

import com.aliasi.util.ObjectToSet;
import com.aliasi.util.Strings;
//import com.aliasi.util.XML;

import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.SAXWriter;
import com.aliasi.xml.TextAccumulatorHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class OutputDocument {

    private final String mId;
    private final Chunking[] mTitleChunkings;
    private final Chunking[] mContentChunkings;

    public OutputDocument(String id,
                          Chunking[] titleChunkings,
                          Chunking[] contentChunkings) {
        mId = id;
        mTitleChunkings = titleChunkings;
        mContentChunkings = contentChunkings;
    }

    public String id() {
        return mId;
    }

    public Chunking[] titleChunkings() {
        return mTitleChunkings;
    }

    public Chunking[] contentChunkings() {
        return mContentChunkings;
    }

    public String toString() {
        try {
            ByteArrayOutputStream bytesOut
                = new ByteArrayOutputStream();
            SAXWriter writer = new SAXWriter(bytesOut,Strings.UTF8);
            writer.startDocument();
            writeTo(writer);
            writer.endDocument();
            byte[] bytes = bytesOut.toByteArray();
            return new String(bytes,Strings.UTF8);
        } catch (Exception e) {
            return "ERROR WRITING";
        }
    }

    public void writeTo(SAXWriter writer)
        throws IOException, SAXException {

        writer.startSimpleElement("doc","id",id());

        writer.startSimpleElement("title");
        int lastSentenceIndex = writeChunkingsTo(mTitleChunkings,writer,0);
        writer.endSimpleElement("title");

        writer.startSimpleElement("content");
        writeChunkingsTo(mContentChunkings,writer,lastSentenceIndex);
        writer.endSimpleElement("content");

        writer.endSimpleElement("doc");
    }

    static int writeChunkingsTo(Chunking[] chunkings,
                                 SAXWriter writer,
                                 int sentenceIndex)
        throws SAXException, IOException {

        for (Chunking chunking : chunkings)
            writeChunkingTo(chunking,writer,sentenceIndex++);
        return sentenceIndex;
    }

    static void writeChunkingTo(Chunking chunking,
                                SAXWriter writer,
                                int sentenceIndex)
        throws SAXException, IOException {

        writer.startSimpleElement("s","index",Integer.toString(sentenceIndex));
        Set<Chunk> chunkSet = chunking.chunkSet();
        char[] cs = Strings.toCharArray(chunking.charSequence());
        Chunk[] chunks = chunkSet.<Chunk>toArray(new Chunk[chunkSet.size()]);
        Arrays.sort(chunks,Chunk.TEXT_ORDER_COMPARATOR);
        int pos = 0;
        for (int i = 0; i < chunks.length; ++i) {
            Chunk chunk = chunks[i];
            int start = chunk.start();
            int end = chunk.end();
            String typeColonId = chunk.type();
            int idx = typeColonId.indexOf(':');
            String entityType = typeColonId.substring(0,idx);
            String id = typeColonId.substring(idx+1);
            writer.characters(cs,pos,start-pos);
            Attributes atts
                = SAXWriter
                .createAttributes("start",Integer.toString(start),
                                  "type",entityType,
                                  "id",id);
            writer.startSimpleElement("entity",atts);
            writer.characters(cs,start,end-start);
            writer.endSimpleElement("entity");
            pos = end;
        }
        writer.characters(cs,pos,cs.length-pos);
        writer.endSimpleElement("s");
    }

    // adds info it finds to ongling collection of entity info
    public static void readMentions(InputSource in,
                                    Map<Long,String> idToType,
                                    ObjectToSet<Long,String> idToAliases)
        throws SAXException, IOException {

        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
	//        xmlReader.setFeature(XML.VALIDATION_FEATURE,false);

        Handler xmlHandler = new Handler(idToType,idToAliases);
        xmlReader.setContentHandler(xmlHandler);
        xmlReader.setDTDHandler(xmlHandler);
        xmlReader.setEntityResolver(xmlHandler);
        xmlReader.parse(in);
    }

    static class Handler extends DelegatingHandler {

        final EntityHandler mEntityHandler;

        final Map<Long,String> mIdToType;

        final ObjectToSet<Long,String> mIdToAliases;

        public Handler(Map<Long,String> idToType,
                       ObjectToSet<Long,String> idToAliases) {
            mEntityHandler = new EntityHandler();
            setDelegate("entity",mEntityHandler);
            mIdToType = idToType;
            mIdToAliases = idToAliases;
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            if ("entity".equals(qName)) {
                Long id = new Long(mEntityHandler.mId);
                String type = mEntityHandler.mType;
                String alias = mEntityHandler.getText();
                mIdToType.put(id,type); // may override; taken in chrono order, so ok
                mIdToAliases.addMember(id,alias);
            }
        }
    }

    static class EntityHandler extends TextAccumulatorHandler {
        long mId = -1L;
        String mType;
        public void startDocument() {
            mType = null;
            super.startDocument();
        }
        public void startElement(String url, String localName,
                                 String qName, Attributes atts)
            throws SAXException {

            if ("entity".equals(qName)) {
                String idString = atts.getValue("id");
                if (idString == null) {
                    String msg = "No id on entity elt.";
                    throw new SAXException(msg);
                }
                mId = Long.parseLong(idString);
                mType = atts.getValue("type");
            }
            super.startDocument();
        }
    }

    /*
    public static OutputDocument read(InputSource in)
        throws IOException, SAXException {

        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setFeature(XML.VALIDATION_FEATURE,false);

        Handler xmlHandler = new Handler();
        xmlReader.setContentHandler(xmlHandler);
        xmlReader.setDTDHandler(xmlHandler);
        xmlReader.setEntityResolver(xmlHandler);
        xmlReader.parse(in);
        return xmlHandler.getDocument();

    }

    static class Handler extends DelegatingHandler {
        ChunksHandler mTitleHandler;
        ChunksHandler mContentHandler;
        String mId;
        public Handler() {
            mTitleHandler = new ChunksHandler(this);
            mContentHandler = new ChunksHandler(this);
            setDelegate("title",mTitleHandler);
            setDelegate("content",mContentHandler);
        }
        public void startDocument()
            throws SAXException {

            mId = null;
            super.startDocument();
        }
        public void startElement(String url, String localName,
                                 String qName, Attributes atts)
            throws SAXException {

            if ("doc".equals(qName)) {
                mId = atts.getValue("id");
            }
            super.startElement(url,localName,qName,atts);
        }
    }

    static class ChunksHandler extends DelegateHandler {

        SentenceHandler mSentenceHandler;
        List<Chunking> mChunkList


        public ChunksHandler(DelgatingHandler parent) {
            super(parent);
            mSentenceHandler = new SentenceHandler();
        }

    }
    */

}