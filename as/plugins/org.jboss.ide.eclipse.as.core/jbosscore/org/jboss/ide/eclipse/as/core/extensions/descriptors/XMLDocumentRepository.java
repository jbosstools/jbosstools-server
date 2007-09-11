package org.jboss.ide.eclipse.as.core.extensions.descriptors;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * Parses and potentially stores descriptor files.
 * 
 * @author rstryker
 *
 */
public class XMLDocumentRepository {
	private static XMLDocumentRepository instance = null;
	public static XMLDocumentRepository getDefault() {
		if( instance == null )
			instance = new XMLDocumentRepository();
		return instance;
	}
	
	private HashMap pathToDocument;
	private HashMap pathToTimestamp;
	private XMLDocumentRepository parent;

	XMLDocumentRepository() {
		pathToDocument = new HashMap();
		pathToTimestamp = new HashMap();
	}

	public XMLDocumentRepository(XMLDocumentRepository parent) {
		pathToDocument = new HashMap();
		pathToTimestamp = new HashMap();
		this.parent = parent;
	}

	public Document getDocument(String fullPath) {
		return getDocument(fullPath, true);
	}
	
	public Document getDocument(String fullPath, boolean load) {
		return getDocument(fullPath, load, true);
	}
	
	public Document getDocument(String fullPath, boolean load, boolean save) {
		Document d = (Document)pathToDocument.get(fullPath);
		if( d == null && load ) {
			d = loadDocument(fullPath);
			if( save ) {
				pathToDocument.put(fullPath, d);
				pathToTimestamp.put(fullPath, new Long(new File(fullPath).lastModified()));
			}
		}
		return d;
	}
	
	public boolean refresh(String fullPath) {
		if( new File(fullPath).lastModified() != ((Long)pathToTimestamp.get(fullPath)).longValue()) {
			pathToDocument.put(fullPath, loadDocument(fullPath));
			pathToTimestamp.put(fullPath, new Long(new File(fullPath).lastModified()));
			return true;
		}
		return false;
	}
	
	private Document loadDocument(String fullpath) {
		try {
			URL url = new File(fullpath).toURI().toURL();

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			sp.getXMLReader().setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			
			SAXReader reader = new SAXReader(false);
			reader.setXMLReader(sp.getXMLReader());
			Document document = reader.read(url);
			
			return document;
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return null;
	}

	public static void saveDocument(Document doc, String fullPath) {
		try {
			File outFile = new File(fullPath);
			FileOutputStream os = new FileOutputStream(outFile);
			OutputFormat outformat = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(os, outformat);
			writer.write(doc);
			writer.flush();
		} catch( Exception e ) {
			e.printStackTrace();
		}

	}
}
