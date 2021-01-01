package com.lingpipe.cookbook.chapter7.tracker;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

//import com.aliasi.util.XML;

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class InputDocument {

    private final String mTitle;
    private final String mContent;
    private final String mId;

    public InputDocument(String id, String title, String content) {
        mId = id;
        mTitle = title;
        mContent = content;
    }

    public String title() {
        return mTitle;
    }

    public String content() {
        return mContent;
    }

    public String id() {
        return mId;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id());
        sb.append(" ");
        sb.append(title());
        sb.append("\n");
        sb.append(content());
        return sb.toString();
    }

    public static InputDocument[] parse(InputSource in)
        throws IOException, SAXException {
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        //xmlReader.setFeature(XML.VALIDATION_FEATURE,false);

        Handler xmlHandler = new Handler();
        xmlReader.setContentHandler(xmlHandler);
        xmlReader.setDTDHandler(xmlHandler);
        xmlReader.setEntityResolver(xmlHandler);
        try {
            xmlReader.parse(in);
        } catch (IOException e) {
            return xmlHandler.documents();
        } catch (SAXException e) {
            return xmlHandler.documents();
        }
        return xmlHandler.documents();
    }

    static class Handler extends DelegatingHandler {
        List<InputDocument> mInputDocumentList
            = new ArrayList<InputDocument>();
        DocumentHandler mDocumentHandler;

        public Handler() {
            mDocumentHandler = new DocumentHandler(this);
            setDelegate("doc",mDocumentHandler);
        }

        public void startDocument() throws SAXException {
            mInputDocumentList.clear();
            super.startDocument();
        }
        public InputDocument[] documents() {
            return mInputDocumentList
                .<InputDocument>toArray(new InputDocument[mInputDocumentList
                                                          .size()]);
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            if ("doc".equals(qName)) {
                mInputDocumentList.add(mDocumentHandler.getDocument());
            }
        }
        public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException {


            if (INPUT_DOCS_PUBLIC_ID.equals(publicId)) {
                InputStream in
                    = this.getClass().getResourceAsStream(INPUT_DOCS_FILENAME);

                if (in == null)
                    throw new RuntimeException("need to put DTD=" + publicId + " on classpath");

                return new InputSource(in);
            }
            return super.resolveEntity(publicId,systemId);
        }

    }

    static String INPUT_DOCS_PUBLIC_ID = "alias-i-tracker-input-documents";
    static String INPUT_DOCS_FILENAME = "input-documents.dtd";

    static class DocumentHandler extends DelegateHandler {
        private String mId;
        private final TextAccumulatorHandler mTitleHandler;
        private final TextAccumulatorHandler mContentHandler;
        public DocumentHandler(DelegatingHandler parent) {
            super(parent);
            mTitleHandler = new TextAccumulatorHandler();
            mContentHandler = new TextAccumulatorHandler();
            setDelegate("title",mTitleHandler);
            setDelegate("content",mContentHandler);
        }
        public void startDocument()
            throws SAXException {
            mTitleHandler.reset();
            mContentHandler.reset();
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
        public InputDocument getDocument() {
            String title = mTitleHandler.getText();
            String content = mContentHandler.getText();
            return new InputDocument(mId,
                                     nullToEmpty(title),
                                     nullToEmpty(content));
        }
        static String nullToEmpty(String title) {
            return title != null ? title : "";
        }

    }

}