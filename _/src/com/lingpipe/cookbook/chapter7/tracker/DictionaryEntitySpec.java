package com.lingpipe.cookbook.chapter7.tracker;

import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.SAXWriter;
import com.aliasi.xml.TextAccumulatorHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class DictionaryEntitySpec {

    final String mCanonicalName;
    final String[] mAliases;
    final boolean[] mXDCs;
    final String mType;
    final long mId;
    final boolean mAllowSpeculativeAliases;

    static String toString(boolean val) {
        return val ? "1" : "0";
    }

    void writeContentTo(SAXWriter writer) 
        throws IOException, SAXException {
        
        Attributes atts
            = SAXWriter
            .createAttributes("id",Long.toString(mId),
                              "type",mType,
                              "canonical",mCanonicalName,
                              "speculativeAliases",toString(mAllowSpeculativeAliases));
        writer.characters("\n");
        writer.startSimpleElement("entity",atts);
        for (int i = 0; i < mAliases.length; ++i) {
            writer.characters("\n  ");
            writer.startSimpleElement("alias","xdc",toString(mXDCs[i]));
            writer.characters(mAliases[i]);
            writer.endSimpleElement("alias");
        }
        writer.characters("\n");
        writer.endSimpleElement("entity");
        writer.characters("\n");
    }

    public DictionaryEntitySpec(String canonicalName,
                                String[] aliases,
                                boolean[] xdcs,
                                String type,
                                long id,
                                boolean allowSpeculativeAliases) {
        mCanonicalName = canonicalName;
        mAliases = aliases;
        mXDCs = xdcs;
        mType = type;
        mId = id;
        mAllowSpeculativeAliases = allowSpeculativeAliases;
    }

    public String canonicalName() {
        return mCanonicalName;
    }

    public String[] aliases() {
        return mAliases;
    }

    public boolean[] xdcs() {
        return mXDCs;
    }

    public String type() {
        return mType;
    }

    public long id() {
        return mId;
    }

    public boolean allowSpeculativeAliases() {
        return mAllowSpeculativeAliases;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<" + mId + "> ");
        sb.append(mCanonicalName);
        sb.append(": " + mType);
        sb.append(" / " + (mAllowSpeculativeAliases ? "+spec" : "-spec"));
        sb.append(" { ");
        for (int i = 0; i < mAliases.length; ++i) {
            if (i > 0) sb.append("; ");
            sb.append(mAliases[i]);
            if (mXDCs[i]) sb.append(" [+xdc]");
        }
        sb.append(" }");
        return sb.toString();
    }

    static class Handler extends DelegateHandler {
        final List<String> mAliasList = new ArrayList<String>();
        final List<Boolean> mXDCList = new ArrayList<Boolean>();
        final AliasHandler mAliasHandler;
        long mId;
        String mType;
        String mCanonicalName;
        boolean mAllowSpeculativeAliases;
        public Handler(DelegatingHandler parent) {
            super(parent);
            mAliasHandler = new AliasHandler();
            setDelegate("alias",mAliasHandler);
        }
        public void startDocument()
            throws SAXException {

            mAliasList.clear();
            mXDCList.clear();
            mId = -1L;
            mType = null;
            mCanonicalName = null;
            super.startDocument();
        }
        public void startElement(String url, String localName,
                                 String qName, Attributes atts)
            throws SAXException {

            if ("entity".equals(qName)) {
                mId = Long.parseLong(atts.getValue("id"));
                mType = atts.getValue("type");
                mCanonicalName = atts.getValue("canonical");
                mAllowSpeculativeAliases = "1".equals(atts.getValue("speculativeAliases"));
            }
            super.startElement(url,localName,qName,atts);
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            if ("alias".equals(qName)) {
                mAliasList.add(mAliasHandler.getText());
                mXDCList.add(Boolean.valueOf(mAliasHandler.mXDC));
            }
        }
        public DictionaryEntitySpec getDictionaryEntitySpec() {
            String[] aliases
                = mAliasList.<String>toArray(new String[mAliasList.size()]);
            boolean[] xdcs
                = new boolean[mXDCList.size()];
            for (int i = 0; i < xdcs.length; ++i)
                xdcs[i] = mXDCList.get(i).booleanValue();
            return new DictionaryEntitySpec(mCanonicalName,
                                            aliases,
                                            xdcs,
                                            mType,
                                            mId,
                                            mAllowSpeculativeAliases);
        }
    }

    static class AliasHandler extends TextAccumulatorHandler {
        boolean mXDC;
        public void startElement(String url, String localName,
                                 String qName, Attributes atts)
            throws SAXException {

            if ("alias".equals(qName)) {
                mXDC = "1".equals(atts.getValue("xdc"));
            }
            super.startElement(url,localName,qName,atts);
        }
    }

}
