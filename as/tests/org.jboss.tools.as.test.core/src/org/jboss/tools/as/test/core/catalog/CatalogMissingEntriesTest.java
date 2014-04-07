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

import junit.framework.TestCase;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
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
		for( int i = 0; i < insidePlugin.length; i++ ) {
			if( !fileInPluginXml(insidePlugin[i], insidePluginXML)) {
				fail(insidePlugin[i] + " is not found in plugin.xml");
			}
		}
		
	}
	
	public void testMissingFile() {
		// First get a list of all files in our schema folder
		String[] insidePlugin = findAllSchema();
		String[] insidePluginXML = getAllUris();
		for( int i = 0; i < insidePluginXML.length; i++ ) {
			if( !uriInPlugin(insidePluginXML[i], insidePlugin)) {
				fail(insidePlugin[i] + " a file that is not found in the plugin");
			}
		}
		
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
				list.add(one.getPath());
			}
		}
		return (String[]) list.toArray(new String[list.size()]);
	}
	
	private String[] getAllUris() {
		ArrayList<String> list = new ArrayList<String>();
		String extPt = "org.eclipse.wst.xml.core.catalogContributions";
		IExtension[] extensions = findExtension(extPt);
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				String contrib =  elements[j].getContributor().getName();
				if( contrib.equals("org.jboss.tools.as.catalog")) {
					if( elements[j].getName().equals("catalogContribution")) {
						IConfigurationElement[] children = elements[j].getChildren();
						for( int k = 0; k < children.length; k++ ) {
							if( children[k].getName().equals("public")) {
								String uri = children[k].getAttribute("uri");
								list.add(uri);
							} else if( children[k].getName().equals("uri")) {
								String uri = children[k].getAttribute("uri");
								list.add(uri);
							}
						}
					}
				}
			}
		}
		return (String[]) list.toArray(new String[list.size()]);
	}
	
// 
	public static IExtension[] findExtension (String extensionId) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(extensionId);
		return extensionPoint.getExtensions();
	}
	
}
