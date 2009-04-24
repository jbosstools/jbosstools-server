/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.extensions.descriptors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.xml.sax.SAXException;

/**
 * Parses and potentially stores descriptor files.
 * 
 * @author rstryker@redhat.com
 * 
 */
public class XMLDocumentRepository {
	/** singleton instance */
	private static XMLDocumentRepository instance = null;
	private static final String LOAD_EXTERNAL_DTD_KEY = 
		"http://apache.org/xml/features/nonvalidating/load-external-dtd"; //$NON-NLS-1$

	/** singleton getter */
	public static XMLDocumentRepository getDefault() {
		if (instance == null)
			instance = new XMLDocumentRepository();
		return instance;
	}

	/** maps a path to its actual document object */
	private HashMap<String, Document> pathToDocument;
	
	/** maps a path to the last time that path's file was changed */
	private HashMap<String, Long> pathToTimestamp;
	
	/** a link to a parent repository which may already contain the document 
	 * and may prevent a costly reparse. */
	private XMLDocumentRepository parent;

	/** package-private constructor */
	XMLDocumentRepository() {
		pathToDocument = new HashMap<String, Document>();
		pathToTimestamp = new HashMap<String, Long>();
	}

	/** public constructor with a parent repository
	 * @param parent The parent repository
	 */
	public XMLDocumentRepository(XMLDocumentRepository parent) {
		pathToDocument = new HashMap<String, Document>();
		pathToTimestamp = new HashMap<String, Long>();
		this.parent = parent;
	}

	/**
	 * get the document for a full path
	 * @param fullPath
	 * @return the document
	 */
	public Document getDocument(String fullPath) {
		return getDocument(fullPath, true);
	}

	/**
	 * get the document for a full path.
	 * @param fullPath  The path of the file
	 * @param load      Whether to load if the document has not already been loaded
	 * @return the document
	 */
	public Document getDocument(String fullPath, boolean load) {
		return getDocument(fullPath, load, true);
	}

	/**
	 * get the document for a full path
	 * @param fullPath the path of the file
	 * @param load whether to load the document if not already loaded
	 * @param save whether to save this document in the repository or just return it
	 * @return the document
	 */
	public Document getDocument(String fullPath, boolean load, boolean save) {
		Document d = pathToDocument.get(fullPath);
		if( d == null && parent != null)
			d = parent.getDocument(fullPath, false, save);
		if (d == null && load) {
			d = loadDocument(fullPath);
			if (save) {
				pathToDocument.put(fullPath, d);
				pathToTimestamp.put(fullPath, new Long(new File(fullPath)
						.lastModified()));
			}
		}
		return d;
	}

	/**
	 * refresh the document for a given file 
	 * @param fullPath the path to the file
	 * @return whether the document was re-read
	 */
	public boolean refresh(String fullPath) {
		boolean found = pathToTimestamp.get(fullPath) != null;
		if (!found || new File(fullPath).lastModified() != pathToTimestamp
				.get(fullPath).longValue()) {
			pathToDocument.put(fullPath, loadDocument(fullPath));
			pathToTimestamp.put(fullPath, new Long(new File(fullPath)
					.lastModified()));
			return true;
		}
		return false;
	}

	/*
	 * Load the document for some path and return it. 
	 * Upon exception, log the exception and return null
	 */
	private Document loadDocument(String fullpath) {
		Exception ex = null;
		try {
			URL url;
			url = new File(fullpath).toURI().toURL();

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			sp.getXMLReader().setFeature(
					LOAD_EXTERNAL_DTD_KEY,
					false);

			SAXReader reader = new SAXReader(false);
			reader.setXMLReader(sp.getXMLReader());
			Document document = reader.read(url);

			return document;
		} catch (MalformedURLException e) {
			ex = e;
		} catch (ParserConfigurationException e) {
			ex = e;
		} catch (SAXException e) {
			ex = e;
		} catch (DocumentException e) {
			ex = e;
		}
		if (ex != null) {
			JBossServerCorePlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
							NLS.bind(Messages.loadXMLDocumentFailed,fullpath), ex));
		}
		return null;
	}

	/*
	 * Save some document "doc" to some full file path
	 * Upon error, log it
	 */
	public static void saveDocument(Document doc, String fullPath) {
		Exception ex = null;
		try {
			File outFile = new File(fullPath);
			FileOutputStream os = new FileOutputStream(outFile);
			OutputFormat outformat = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(os, outformat);
			writer.write(doc);
			writer.flush();
		} catch (MalformedURLException e) {
			ex = e;
		} catch (FileNotFoundException e) {
			ex = e;
		} catch (UnsupportedEncodingException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		}
		if (ex != null) {
			JBossServerCorePlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
							NLS.bind(Messages.saveXMLDocumentFailed ,fullPath), ex));
		}
	}
}
