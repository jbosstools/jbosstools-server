/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.module.factory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.internal.ModuleFile;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;


/**
 * This is the module delegate abstract superclass for any JBoss Module.
 * It is expected that the module-specific delegate class is defined
 * in the factory class itself.
 * 
 * @author rstryker
 *
 */
public abstract class JBossModuleDelegate extends ModuleDelegate {

	private static Integer DNE = new Integer(-1); 
	private static final String TOUCHED_NAME = "Last Touched";
	private static final String TOUCHED_PATH = "tmp/LAST/TOUCHED/DOES/NOT/EXIST";
	

	private JBossModuleFactory factory;
	private int touched = 0;
	
	/**
	 * References to the resource location, in path and URL format
	 */
	private String resourcePath;
	private URL resourceURL;
	
	/**
	 * Map to hold xml documents within our module
	 */
	private HashMap entryToDocument;
	
	public JBossModuleDelegate() {
		entryToDocument = new HashMap();
	}

	
	public abstract void initialize();	
	public abstract IModule[] getChildModules();
	public abstract IStatus validate();


	/**
	 * Some junk implementation to make it easy to "touch" a module
	 * and mark it as changed.
	 */
//	public IModuleResource[] members() throws CoreException {
//		ModuleFile f = new ModuleFile(TOUCHED_NAME, new Path(TOUCHED_PATH).append(""+touched), touched);
//		return new IModuleResource[]{ f };
//	}

	public void touchModule() {
		touched++;
	}
	
	public void setFactory(JBossModuleFactory factory) {
		this.factory = factory;
	}
	
	public JBossModuleFactory getFactory() {
		return this.factory;
	}
	
	public String getResourcePath() {
		return resourcePath;
	}

	public String getResourceName() {
		Path p = new Path(getResourcePath());
		String[] segments = p.segments();
		return segments[segments.length - 1];
	}
	
	public void setResource(IResource resource) {
		try {
			if( factory == null ) {
				this.resourcePath = resource.getLocation().toOSString();
			} else {
				this.resourcePath = factory.getPath(resource);
			}
			this.resourceURL = resource.getLocation().toFile().toURL();
			//ASDebug.p("added resource URL is " + resourceURL, this);
		} catch( Exception e ) {
		}
	}
	
	/**
	 * Return a Document object for some xml descriptor
	 * within this module. If the descriptor hasn't been parsed
	 * yet, parse it now.
	 * 
	 * @param entryName
	 * @return
	 */
	protected Document getDocument(String entryName) {
		Object o = entryToDocument.get(entryName);
		if( o == DNE ) return null;
		if( o != null ) return (Document)o;
		
		// Does the entry exist
		if( !entryExists(entryName)) {
			entryToDocument.put(entryName, DNE);
			return null;
		}
		
		try {
			// Load the entry

			String url = "jar:" + this.resourceURL.toString() + "!/" + entryName;
			URL url2 = new URL(url);
			SAXReader reader = new SAXReader();
			InputStream is = url2.openStream();
			Document document = reader.read(is);
			entryToDocument.put(entryName, document);
			is.close();
			return document;
			
		} catch( Exception e ) {
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	
	/**
	 * Search through a descriptor file jarentry for the 
	 * designated xpath. 
	 * 
	 * If the entry hasn't been parsed into a Document yet, 
	 * parse it now.
	 * 
	 * @param entry
	 * @param xpath
	 * @return
	 */
	public String[] getXpathValues(String entry, String xpath) {
		if( entryExists(entry)) {
			ArrayList strings = new ArrayList();
			try {
				Document d = getDocument(entry);
				List l = d.selectNodes(xpath);
				Iterator i = l.iterator();
				while(i.hasNext() ) {
					DefaultElement el = (DefaultElement)i.next();
					strings.add(el.getText());
				}
			} catch( Throwable e ) {
				e.printStackTrace();
			}
			
			String[] retval = new String[strings.size()];
			strings.toArray(retval);
			return retval;
		} else {
			return new String[] { };
		}
	}
	
	/**
	 * Return whether the entry exists in our module
	 * @param entryName
	 * @return
	 */
	public boolean entryExists(String entryName) {
		try {
			File f = new File(resourcePath);
			JarFile jf = new JarFile(f);
			
			JarEntry entry = jf.getJarEntry(entryName);
			jf.close();
			if( entry == null ) return false;
			return true;
		} catch( IOException ioe ) {
		}
		return false;
	}

	/**
	 * Clear all document references
	 */
	public void clearDocuments() {
		entryToDocument.clear();
	}
}
