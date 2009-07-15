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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
	private static final String QUERY_LIST = 
		"org.jboss.ide.eclipse.as.core.model.descriptor.QueryList";	
	private static final String QUERY = 
		"org.jboss.ide.eclipse.as.core.model.descriptor.Query";	
	private static final String DEFAULTS_SET = "org.jboss.ide.eclipse.as.core.model.descriptor.DefaultsSet";

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
		final ServerAttributeHelper helper = new ServerAttributeHelper(server, server.createWorkingCopy());
		if( !helper.getAttribute(DEFAULTS_SET, false)) {
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
						loadDefaults(server2, configFolder.toOSString());
						helper.setAttribute(DEFAULTS_SET, true);
						helper.save();
						save(server2);
					}
					return Status.OK_STATUS;
				}
				
			}.schedule();
		}
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
	private XPathCategory[] load(IServer server) {
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
		
	public void save(IServer server) {
		if( !serverToCategories.containsKey(server.getId()))
			return;
		
		ServerAttributeHelper helper = ServerAttributeHelper.createHelper(server);
		XPathCategory[] categories = getCategories(server);
		String list = "";
		for( int i = 0; i < categories.length; i++ ) {
			if( i != 0 )
				list += DELIMITER;
			list += categories[i].getName();
			saveCategory(categories[i], server, helper);
		}
		helper.setAttribute(CATEGORY_LIST, list);
		helper.save();
	}

	public void saveCategory(XPathCategory category, IServer server, ServerAttributeHelper helper) {
		if( category.queriesLoaded()) {
			XPathQuery[] queries = category.getQueries();
			String val = "";
			for( int i = 0; i < queries.length; i++ ) {
				if( i != 0 )
					val += DELIMITER;
				val += category.getName() + Path.SEPARATOR + queries[i].getName();
				saveQuery(queries[i], category, server, helper);
			}
			helper.setAttribute(QUERY_LIST + "." + category.getName().replace(' ', '_'), val);
		}
	}
	
	private void saveQuery(XPathQuery query, XPathCategory category, IServer server, ServerAttributeHelper helper) {
		ArrayList<String> list = new ArrayList<String>();
		list.add(query.getBaseDir());
		list.add(query.getFilePattern() == null ? EMPTY_STRING : query.getFilePattern());
		list.add(query.getXpathPattern() == null ? EMPTY_STRING : query.getXpathPattern());
		list.add(query.getAttribute() == null ? EMPTY_STRING : query.getAttribute());
		helper.setAttribute(QUERY + "." + category.getName().replace(' ', '_') + Path.SEPARATOR + query.getName().replace(' ', '_'), list);
	}
	
	public XPathQuery[] loadQueries(XPathCategory category, IServer server) {
		ServerAttributeHelper helper = ServerAttributeHelper.createHelper(server);
		String list = helper.getAttribute(QUERY_LIST + "." + category.getName().replace(' ', '_'), (String)null);
		if( list == null )
			return new XPathQuery[] {};
		String[] queriesByName = list.split(DELIMITER);
		List<String> queryAsStringValues;
		ArrayList<XPathQuery> returnList = new ArrayList<XPathQuery>();
		for( int i = 0; i < queriesByName.length; i++ ) {
			queryAsStringValues = helper.getAttribute(QUERY + "." + queriesByName[i].replace(' ', '_'), (List)null);
			if( queryAsStringValues != null ) {
				XPathQuery q =new XPathQuery(queriesByName[i].substring(queriesByName[i].indexOf(Path.SEPARATOR)+1), queryAsStringValues); 
				q.setCategory(category);
				returnList.add(q);
			}
		}
		return (XPathQuery[]) returnList.toArray(new XPathQuery[returnList.size()]);
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
		rtToPortsFile.put("org.jboss.ide.eclipse.as.runtime.51", new Path("properties").append("jboss.51.default.ports.properties"));
		rtToPortsFile.put("org.jboss.ide.eclipse.as.runtime.eap.43", new Path("properties").append("jboss.eap.43.default.ports.properties"));
		rtToPortsFile.put("org.jboss.ide.eclipse.as.runtime.eap.50", new Path("properties").append("jboss.eap.50.default.ports.properties"));
	}

	public void loadDefaults(IServer server, String configFolder) {
		ArrayList<XPathCategory> retVal = new ArrayList<XPathCategory>();
		Path p = (Path)rtToPortsFile.get(server.getRuntime().getRuntimeType().getId());
		if( p == null ) return;
		URL url = FileLocator.find(JBossServerCorePlugin.getDefault().getBundle(), p, null);
		if( url == null ) return;

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
		
		serverToCategories.put(server.getId(), retVal);
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
