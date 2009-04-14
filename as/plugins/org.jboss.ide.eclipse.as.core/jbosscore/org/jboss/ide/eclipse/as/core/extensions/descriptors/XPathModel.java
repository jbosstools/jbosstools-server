/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.core.extensions.descriptors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult.XPathResultNode;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.server.internal.LocalJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
import org.jboss.tools.jmx.core.IMemento;
import org.jboss.tools.jmx.core.util.XMLMemento;

/**
 * The class representing the model for all xpath storage and searching
 * Also API entrance point.
 * @author rob.stryker@redhat.com
 *
 */
public class XPathModel extends UnitedServerListener {
	
	public static final String EMPTY_STRING = "org.jboss.ide.eclipse.as.core.model.descriptor.EmptyString";
	public static final String PORTS_CATEGORY_NAME = "Ports";
	private static final String DELIMITER = ",";
	private static final String CATEGORY_LIST = 
		"org.jboss.ide.eclipse.as.core.model.descriptor.Categories";	
	private static final IPath STATE_LOCATION = JBossServerCorePlugin.getDefault().getStateLocation();
	private static final String XPATH_FILE_NAME = "xpaths.xml";
	
	/* Singleton */
	private static XPathModel instance;
	public static XPathModel getDefault() {
		if( instance == null )
			instance = new XPathModel();
		return instance;
	}
	
	protected HashMap<String, ArrayList<XPathCategory>> serverToCategories;

	public XPathModel() {
		serverToCategories = new HashMap<String, ArrayList<XPathCategory>>();
	}
	
	public void serverAdded(IServer server) {
		final IServer server2 = server;
		new Job("Add Server XPath Details") {
			protected IStatus run(IProgressMonitor monitor) {
				
				if(server2==null || server2.getRuntime()==null) {
					return Status.OK_STATUS; // server has no runtime so we can't set this up.
				}
				
				LocalJBossServerRuntime ajbsr = (LocalJBossServerRuntime)
				server2.getRuntime().loadAdapter(LocalJBossServerRuntime.class, null);
				if(ajbsr != null ) {
					IPath loc = server2.getRuntime().getLocation();
					IPath configFolder = loc.append(IJBossServerConstants.SERVER).append(ajbsr.getJBossConfiguration());
					ArrayList<XPathCategory> defaults = loadDefaults(server2, configFolder.toOSString());
					serverToCategories.put(server2.getId(), defaults);
					save(server2);
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}
	
	public XPathQuery getQuery(IServer server, IPath path) {
		XPathCategory cat = getCategory(server, path.segment(0));
		if( cat != null )
			return cat.getQuery(path.segment(1));
		return null;
	}
	
	protected ArrayList<XPathCategory> getCategoryCollection(IServer server) {
		if( serverToCategories.get(server.getId()) == null ) {
			ArrayList<XPathCategory> val = new ArrayList<XPathCategory>(Arrays.asList(load(server)));
			serverToCategories.put(server.getId(), val);
		}
		return (ArrayList<XPathCategory>)serverToCategories.get(server.getId());		
	}

	public XPathCategory[] getCategories(IServer jbs) {
		ArrayList<XPathCategory> val = getCategoryCollection(jbs);
		return (XPathCategory[]) val.toArray(new XPathCategory[val.size()]);
	}
	
	public boolean containsCategory(IServer jbs, String name) {
		return getCategory(jbs, name) == null ? false : true;
	}
	
	public XPathCategory getCategory(IServer jbs, String name) {
		ArrayList<XPathCategory> list = getCategoryCollection(jbs);
		Iterator<XPathCategory> i = list.iterator();
		XPathCategory c;
		while(i.hasNext()) {
			c = (XPathCategory)i.next();
			if( c.getName().equals(name)) 
				return c;
		}
		return null;
	}

	public XPathCategory addCategory(IServer jbs, String name) {
		if( !containsCategory(jbs, name)) {
			XPathCategory c = new XPathCategory(name, jbs);
			getCategoryCollection(jbs).add(c);
			return c;
		}
		return getCategory(jbs, name);
	}
	
	public void addCategory(IServer server, XPathCategory category) {
		if( !containsCategory(server, category.getName())) {
			getCategoryCollection(server).add(category);
		}
	}
	
	public void removeCategory(IServer server, String name) {
		ArrayList<XPathCategory> list = getCategoryCollection(server);
		Iterator<XPathCategory> i = list.iterator();
		while(i.hasNext()) {
			XPathCategory cat = (XPathCategory)i.next();
			if( cat.getName().equals(name)) {
				i.remove();
				return;
			}
		}
	}

	/*
	 * Loading and saving is below
	 */
	protected File getFile(IServer server) {
		return STATE_LOCATION.append(server.getId().replace(' ', '_')).append(XPATH_FILE_NAME).toFile();
	}
	
	public void save(IServer server) {
		if( !serverToCategories.containsKey(server.getId()))
			return;
		XMLMemento memento = XMLMemento.createWriteRoot("xpaths");
		XPathCategory[] categories = getCategories(server);
		for( int i = 0; i < categories.length; i++ ) {
			XMLMemento child = (XMLMemento)memento.createChild("category");
			saveCategory(categories[i], server, child);
		}
		try {
			memento.save(new FileOutputStream(getFile(server)));
		} catch( IOException ioe) {
			// TODO LOG
		}
	}

	public void saveCategory(XPathCategory category, IServer server, XMLMemento memento) {
		memento.putString("name", category.getName());
		if( category.queriesLoaded()) {
			XPathQuery[] queries = category.getQueries();
			for( int i = 0; i < queries.length; i++ ) {
				XMLMemento child = (XMLMemento)memento.createChild("query");
				saveQuery(queries[i], category, server, child);
			}
		}
	}
	
	private void saveQuery(XPathQuery query, XPathCategory category, 
			IServer server, XMLMemento memento) {
		memento.putString("name", query.getName());
		memento.putString("dir", query.getBaseDir());
		memento.putString("filePattern", query.getFilePattern());
		memento.putString("xpathPattern", query.getXpathPattern());
		memento.putString("attribute", query.getAttribute());
	}
	
	private XPathCategory[] load(IServer server) {
		if( getFile(server).exists()) 
			return loadXML(server);
		return load_LEGACY(server);
	}
	
	private XPathCategory[] loadXML(IServer server) {
		XPathCategory[] categories = null;
		try {
			File file = getFile(server);
			XMLMemento memento = XMLMemento.createReadRoot(new FileInputStream(file));
			IMemento[] categoryMementos = memento.getChildren("category");
			categories = new XPathCategory[categoryMementos.length];
			for( int i = 0; i < categoryMementos.length; i++ ) {
				categories[i] = new XPathCategory(server, categoryMementos[i]);
			}
		} catch( IOException ioe) {
			// TODO LOG
		}
		return categories == null ? new XPathCategory[] { } : categories;
	}
	
	
	private XPathCategory[] load_LEGACY(IServer server) {
		ServerAttributeHelper helper = ServerAttributeHelper.createHelper(server);
		String list = helper.getAttribute(CATEGORY_LIST, (String)null);
		if( list == null )
			return new XPathCategory[] {};
		String[] byName = list.split(DELIMITER);
		XPathCategory[] cats = new XPathCategory[byName.length];
		for( int i = 0; i < byName.length; i++ ) {
			cats[i] = new XPathCategory(byName[i], server);
		}
		return cats;
	}
	
	/*
	 * Loading the defaults for the server
	 * returns the category created
	 */
	private static HashMap<String, IPath> rtToPortsFile;
	private static final String ATTRIBUTE_SUFFIX = "_ATTRIBUTE";
	private static final String FILE_SUFFIX = "_FILE";
	static {
		rtToPortsFile = new HashMap<String, IPath>();
		rtToPortsFile.put("org.jboss.ide.eclipse.as.runtime.32", new Path("properties").append("jboss.32.default.ports.properties"));
		rtToPortsFile.put("org.jboss.ide.eclipse.as.runtime.40", new Path("properties").append("jboss.40.default.ports.properties"));
		rtToPortsFile.put("org.jboss.ide.eclipse.as.runtime.42", new Path("properties").append("jboss.42.default.ports.properties"));
		rtToPortsFile.put("org.jboss.ide.eclipse.as.runtime.50", new Path("properties").append("jboss.50.default.ports.properties"));
		rtToPortsFile.put("org.jboss.ide.eclipse.as.runtime.eap.43", new Path("properties").append("jboss.eap.43.default.ports.properties"));
	}

	private static ArrayList<XPathCategory> loadDefaults(IServer server, String configFolder) {
		ArrayList<XPathCategory> retVal = new ArrayList<XPathCategory>();
		Path p = (Path)rtToPortsFile.get(server.getRuntime().getRuntimeType().getId());
		if( p == null ) return retVal;
		URL url = FileLocator.find(JBossServerCorePlugin.getDefault().getBundle(), p, null);
		if( url == null ) return retVal;

		Properties pr = new Properties();
		try {
			pr.load(url.openStream());
			XPathCategory ports = new XPathCategory(PORTS_CATEGORY_NAME, server);
			Iterator i = pr.keySet().iterator();
			String name, xpath, attributeName, file;
			XPathQuery query;
			while(i.hasNext()) {
				name = (String)i.next();
				if( !name.endsWith(ATTRIBUTE_SUFFIX) && !name.endsWith(FILE_SUFFIX)) {
					xpath = pr.getProperty(name);
					attributeName = pr.getProperty(name+ATTRIBUTE_SUFFIX);
					file = pr.getProperty(name + FILE_SUFFIX);
					query = new XPathQuery(name.replace('_', ' '), configFolder, file, xpath, attributeName);
					ports.addQuery(query);
				}
			}
			retVal.add(ports);
		} catch (IOException e) {
			JBossServerCorePlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
							"Error loading default xpaths", e));
		}
		return retVal;
	}
	
	
	/*
	 * Namespace map
	 */
	public Properties namespaceMap = null;
	public Properties getNamespaceMap() {
		if( namespaceMap == null )
			loadNamespaceMap();
		return (Properties)namespaceMap.clone();
	}
	protected void loadNamespaceMap() {
		// TODO load from preferenes. 
		//If nothing's there, load from default
		IPath p = new Path("properties").append("namespaceMap.properties");
		if( p != null ) {
			URL url = FileLocator.find(JBossServerCorePlugin.getDefault().getBundle(), p, null);
			if( url != null ) {
				Properties pr = new Properties();
				try {
					pr.load(url.openStream());
					namespaceMap = pr;
					return;
				} catch(IOException ioe) {
				}
			}
		}
		namespaceMap = new Properties();
	}
	public void setNamespaceMap(Properties map) {
		namespaceMap = map;
		// TODO  save to preferences
	}
	
	/* 
	 * Static utility methods
	 */
	

	public static XPathResultNode getResultNode(Object data) {
		// if we are the node to change, change me
		if( data instanceof XPathResultNode ) {
			return (XPathResultNode)data;
		}
		
		// if we're a node which represents a file, but only have one matched node, thats the node.
		if( data instanceof XPathFileResult && ((XPathFileResult)data).getChildren().length == 1 ) {
			return (XPathResultNode) (((XPathFileResult)data).getChildren()[0]);
		}
		
		// if we're a top level tree item (JNDI), with one file child and one mbean grandchild, the grandchild is the node
		if( data instanceof XPathQuery && ((XPathQuery)data).getResults().length == 1 ) {
			XPathFileResult item = ((XPathFileResult) ((XPathQuery)data).getResults()[0]);
			if( item.getChildren().length == 1 ) 
				return (XPathResultNode)item.getChildren()[0];
		}
		return null;
	}
	
	public static XPathResultNode[] getResultNodes(XPathQuery query) {
		ArrayList<XPathResultNode> l = new ArrayList<XPathResultNode>();
		XPathFileResult[] files = query.getResults();
		for( int i = 0; i < files.length; i++ ) {
			l.addAll(Arrays.asList(files[i].getChildren()));
		}
		return l.toArray(new XPathResultNode[l.size()]);
	}
}
