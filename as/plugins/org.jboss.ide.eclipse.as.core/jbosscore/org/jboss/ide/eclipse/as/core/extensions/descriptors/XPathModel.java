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
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathFileResult.XPathResultNode;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListenerManager;
import org.jboss.ide.eclipse.as.core.util.IConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.IMemento;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.core.util.XMLMemento;

/**
 * The class representing the model for all xpath storage and searching
 * Also API entrance point.
 * @author rob.stryker@redhat.com
 *
 */
public class XPathModel extends UnitedServerListener {
	
	public static final String EMPTY_STRING = "org.jboss.ide.eclipse.as.core.model.descriptor.EmptyString"; //$NON-NLS-1$
	public static final String PORTS_CATEGORY_NAME = Messages.Ports;
	private static final String DELIMITER = ","; //$NON-NLS-1$
	private static final String CATEGORY_LIST = 
		"org.jboss.ide.eclipse.as.core.model.descriptor.Categories";	 //$NON-NLS-1$
	
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
		new Job(Messages.AddXPathDetailsJob) {
			protected IStatus run(IProgressMonitor monitor) {
				if(server2==null || server2.getRuntime()==null) {
					return Status.OK_STATUS; // server has no runtime so we can't set this up.
				}
				
				return handleAddJBossXPaths(server2, getQueryBaseDir(server2));
			}
		}.schedule();
	}
	
	private String getQueryBaseDir(IServer server) {
		// JBoss 6 and below have a default basedir of the config location
		// while as7 has a basedir of the empty string
		if( ServerUtil.isJBoss7(server))
			return ""; //$NON-NLS-1$
		return "${jboss_config_dir}"; //$NON-NLS-1$
	}
	
	private IStatus handleAddJBossXPaths(IServer server2, String baseDir) {
		ArrayList<XPathCategory> defaults = loadDefaultPortQueries(server2, baseDir);
		serverToCategories.put(server2.getId(), defaults);
		save(server2);
		return Status.OK_STATUS;
	}
	
	
	public XPathQuery getQuery(IServer server, IPath path) {
		if( server == null || path == null || path.segmentCount() != 2)
			return null;
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
		return JBossServerCorePlugin.getServerStateLocation(server).append(IJBossToolingConstants.XPATH_FILE_NAME).toFile();
	}
	
	public void save(IServer server) {
		if( !serverToCategories.containsKey(server.getId()))
			return;
		XMLMemento memento = XMLMemento.createWriteRoot("xpaths"); //$NON-NLS-1$
		XPathCategory[] categories = getCategories(server);
		for( int i = 0; i < categories.length; i++ ) {
			XMLMemento child = (XMLMemento)memento.createChild("category");//$NON-NLS-1$
			saveCategory(categories[i], server, child);
		}
		File f = getFile(server);
		if( !f.getParentFile().exists())
			f.getParentFile().mkdirs();
		try {
			memento.save(new FileOutputStream(f));
		} catch( IOException ioe) {
			JBossServerCorePlugin.log("The XPath Model could not be saved", ioe); //$NON-NLS-1$
		}
	}

	public void saveCategory(XPathCategory category, IServer server, XMLMemento memento) {
		memento.putString("name", category.getName());//$NON-NLS-1$
		if( category.queriesLoaded()) {
			XPathQuery[] queries = category.getQueries();
			for( int i = 0; i < queries.length; i++ ) {
				XMLMemento child = (XMLMemento)memento.createChild("query");//$NON-NLS-1$
				saveQuery(queries[i], category, server, child);
			}
		}
	}
	
	private void saveQuery(XPathQuery query, XPathCategory category, 
			IServer server, XMLMemento memento) {
		memento.putString("name", query.getName());//$NON-NLS-1$
		memento.putString("dir", query.getBaseDir());//$NON-NLS-1$
		memento.putString("filePattern", query.getFilePattern());//$NON-NLS-1$
		memento.putString("xpathPattern", query.getXpathPattern());//$NON-NLS-1$
		memento.putString("attribute", query.getAttribute());//$NON-NLS-1$
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
			if( file != null && file.exists() && file.isFile()) {
				XMLMemento memento = XMLMemento.createReadRoot(new FileInputStream(file));
				IMemento[] categoryMementos = memento.getChildren("category");//$NON-NLS-1$
				categories = new XPathCategory[categoryMementos.length];
				for( int i = 0; i < categoryMementos.length; i++ ) {
					categories[i] = new XPathCategory(server, categoryMementos[i]);
				}
			}
		} catch( IOException ioe) {
			// Should never happen. We've verified it exists
			JBossServerCorePlugin.log(ioe);
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
	private static HashMap<String, URL> rtToPortsFile;
	private static final String ATTRIBUTE_SUFFIX = "_ATTRIBUTE";//$NON-NLS-1$
	private static final String FILE_SUFFIX = "_FILE";//$NON-NLS-1$
	static {
		IPath properties = new Path(IJBossToolingConstants.PROPERTIES);
		rtToPortsFile = new HashMap<String, URL>();
		rtToPortsFile.put(IConstants.AS_32, getURLFor(IJBossToolingConstants.DEFAULT_PROPS_32));
		rtToPortsFile.put(IConstants.AS_40, getURLFor(IJBossToolingConstants.DEFAULT_PROPS_40));
		rtToPortsFile.put(IConstants.AS_42, getURLFor(IJBossToolingConstants.DEFAULT_PROPS_42));
		rtToPortsFile.put(IConstants.AS_50, getURLFor(IJBossToolingConstants.DEFAULT_PROPS_50));
		rtToPortsFile.put(IConstants.AS_51, getURLFor(IJBossToolingConstants.DEFAULT_PROPS_51));
		rtToPortsFile.put(IConstants.AS_60, getURLFor(IJBossToolingConstants.DEFAULT_PROPS_60));
		rtToPortsFile.put(IConstants.AS_70, getURLFor(IJBossToolingConstants.DEFAULT_PROPS_70));
		rtToPortsFile.put(IConstants.AS_71, getURLFor(IJBossToolingConstants.DEFAULT_PROPS_71));
		rtToPortsFile.put(IConstants.EAP_43, getURLFor(IJBossToolingConstants.DEFAULT_PROPS_EAP_43));
		rtToPortsFile.put(IConstants.EAP_50, getURLFor(IJBossToolingConstants.DEFAULT_PROPS_EAP_50));
		rtToPortsFile.put(IConstants.EAP_60, getURLFor(IJBossToolingConstants.DEFAULT_PROPS_71));
		// TODO NEW_SERVER_ADAPTER Add the new server ID to port mapping file above this line 
	}
	
	/**
	 * @since 2.3
	 */
	public static void addServerTypeToURLMapping(String serverType, URL mapping) {
		if( !rtToPortsFile.containsKey(serverType)) {
			// ensure nobody tries to overwrite our values
			rtToPortsFile.put(serverType, mapping);
		}
	}
	
	/**
	 * @since 2.3
	 */
	public static URL getUrlFromServerType(String serverType) {
		return rtToPortsFile.get(serverType);
	}
	
	private static URL getURLFor(String props) {
		IPath properties = new Path(IJBossToolingConstants.PROPERTIES).append(props);
		URL url = FileLocator.find(JBossServerCorePlugin.getDefault().getBundle(), properties, null);
		return url;
	}

	/**
	 * @since 2.3
	 */
	public static ArrayList<XPathCategory> loadDefaultPortQueries(IServer server, String baseDir) {
		ArrayList<XPathCategory> retVal = new ArrayList<XPathCategory>();
		URL url = rtToPortsFile.get(server.getRuntime().getRuntimeType().getId());
		if( url == null ) return retVal;

		try {
			XPathCategory ports = new XPathCategory(PORTS_CATEGORY_NAME, server);
			addQueriesToCategoryFromDefaultFile(server, ports, baseDir, url);
			retVal.add(ports);
		} catch (IOException e) {
			JBossServerCorePlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
							Messages.XPathLoadFailure, e));
		}
		return retVal;
	}
	
	/**
	 * @since 2.3
	 */
	public static void addQueriesToCategoryFromDefaultFile(IServer server, XPathCategory category, 
			String baseDir, URL url) throws IOException {
		Properties pr = new Properties();
		pr.load(url.openStream());
		Iterator i = pr.keySet().iterator();
		String name, xpath, attributeName, file;
		XPathQuery query;
		while(i.hasNext()) {
			name = (String)i.next();
			if( !name.endsWith(ATTRIBUTE_SUFFIX) && !name.endsWith(FILE_SUFFIX)) {
				xpath = pr.getProperty(name);
				attributeName = pr.getProperty(name+ATTRIBUTE_SUFFIX);
				file = pr.getProperty(name + FILE_SUFFIX);
				query = new XPathQuery(server, name.replace('_', ' '), baseDir, file, xpath, attributeName);
				category.addQuery(query);
			}
		}
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
		//If nothing's there, load from default
		IPath p = new Path("properties").append("namespaceMap.properties");//$NON-NLS-1$//$NON-NLS-2$
		if( p != null ) {
			URL url = FileLocator.find(JBossServerCorePlugin.getDefault().getBundle(), p, null);
			if( url != null ) {
				Properties pr = new Properties();
				try {
					pr.load(url.openStream());
					namespaceMap = pr;
					return;
				} catch(IOException ioe) {
					// This should never happen, since the file is verified to be in the installation
					JBossServerCorePlugin.log(ioe);
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
	
	
	
	
	public boolean canHandleServer(IServer server) {
		if (!UnitedServerListenerManager.isJBossServer(server))
			return false;
		return true;
	}
	public boolean canHandleRuntime(IRuntime runtime) {
		return findAllServersForRuntime(runtime).length > 0;
	}
	
	public void serverChanged(IServer server) {
		clearCache(server);
	}
	public void runtimeChanged(IRuntime runtime) {
		IServer[] relevent = findAllServersForRuntime(runtime);
		for( int i = 0; i < relevent.length; i++ )
			clearCache(relevent[i]);
	}
	
	protected IServer[] findAllServersForRuntime(IRuntime rt) {
		if( rt == null )
			return new IServer[] {};
		ArrayList<IServer> ret = new ArrayList<IServer>();
		IServer[] all = ServerCore.getServers();
		for( int i = 0; i < all.length; i++ ) {
			if( rt.equals(all[i].getRuntime())) {
				ret.add(all[i]);
			}
		}
		return (IServer[]) ret.toArray(new IServer[ret.size()]);
	}

	public void clearCache(IServer server) {
		XPathCategory[] cats = getCategories(server);
		for( int i = 0; i < cats.length; i++ ) {
			cats[i].clearCache();
		}
	}
	
}
