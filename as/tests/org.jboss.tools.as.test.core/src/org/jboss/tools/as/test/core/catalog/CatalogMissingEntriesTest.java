/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.catalog;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.xml.core.internal.XMLCorePlugin;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalog;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalogEntry;
import org.eclipse.wst.xml.core.internal.catalog.provisional.INextCatalog;
import org.jboss.tools.as.catalog.ServerCatalogCorePlugin;
import org.osgi.framework.Bundle;


public class CatalogMissingEntriesTest extends TestCase {

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public void testMissingEntries() {
		// First get a list of all files in our schema folder
		String[] insidePlugin = findAllSchema();
		String[] insidePluginXML = getAllUris();
		ArrayList<String> missing = new ArrayList<String>();
		for( int i = 0; i < insidePlugin.length; i++ ) {
			if( !fileInPluginXml(insidePlugin[i], insidePluginXML)) {
				missing.add(insidePlugin[i]);
			}
		}
		Iterator<String> i = missing.iterator();
		while(i.hasNext()) {
			System.out.println(i.next() + " is not found in plugin.xml");
		}
		assertEquals(0, missing.size());
	}
	
	public void testMissingFile() {
		// First get a list of all files in our schema folder
		String[] insidePlugin = findAllSchema();
		String[] insidePluginXML = getAllUris();
		ArrayList<String> missing = new ArrayList<String>();
		for( int i = 0; i < insidePluginXML.length; i++ ) {
			if( !uriInPlugin(insidePluginXML[i], insidePlugin)) {
				missing.add(insidePluginXML[i]);
			}
		}
		Iterator<String> i = missing.iterator();
		while(i.hasNext()) {
			System.out.println(i.next() + " is a file not found in plugin");
		}
		assertEquals(0, missing.size());
	}

	private boolean fileInPluginXml(String path, String[] allUri) {
		for( int i = 0; i < allUri.length; i++ ) {
			if( allUri[i].endsWith(path))
				return true;
		}
		return false;
	}
	private boolean uriInPlugin(String uri, String[] insidePlugin) {
		for( int i = 0; i < insidePlugin.length; i++ ) {
			if( uri.endsWith(insidePlugin[i]))
				return true;
		}
		return false;
	}
	
	private String[] findAllSchema() {
		ArrayList<String> list = new ArrayList<String>();
		
		Bundle bundle = Platform.getBundle(ServerCatalogCorePlugin.PLUGIN_ID);
		Enumeration<URL> all = bundle.findEntries("schema", "**", true);
		URL one = null;
		while(all.hasMoreElements()) {
			one = all.nextElement();
			IPath p = new Path(one.getPath());
			if( p.segmentCount() == 3 ) {
				boolean ignored = false;
				if( p.lastSegment().equalsIgnoreCase(".gitignore")) ignored = true;
				if( one.getPath().equalsIgnoreCase("/schema/controller/src/")) ignored = true;
				if( !ignored)
					list.add(one.getPath());
			}
		}
		return (String[]) list.toArray(new String[list.size()]);
	}

	private static void fillCatalogEntryList(ICatalog c, ArrayList<String> list) {
		ICatalogEntry[] all = c.getCatalogEntries();
		for( int i = 0; i < all.length; i++ ) {
			String uri = all[i].getURI();
			if( uri.contains(ServerCatalogCorePlugin.PLUGIN_ID))
				list.add(all[i].getURI());
		}
		INextCatalog[] nextCatalogs = c.getNextCatalogs();
		for( int i = 0; i < nextCatalogs.length; i++ ) {
			fillCatalogEntryList(nextCatalogs[i].getReferencedCatalog(), list);
		}
	}
	
	private String[] getAllUris() {
		ICatalog xmlCatalog = XMLCorePlugin.getDefault().getDefaultXMLCatalog();
		ArrayList<String> uriList = new ArrayList<String>();
		fillCatalogEntryList(xmlCatalog, uriList);
		return (String[]) uriList.toArray(new String[uriList.size()]);
	}
	
// 
	public static IExtension[] findExtension (String extensionId) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(extensionId);
		return extensionPoint.getExtensions();
	}
	
}
