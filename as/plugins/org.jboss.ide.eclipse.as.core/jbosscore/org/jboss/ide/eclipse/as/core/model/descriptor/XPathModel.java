package org.jboss.ide.eclipse.as.core.model.descriptor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.runtime.server.AbstractJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.ServerAttributeHelper;

public class XPathModel {
	public static final String EMPTY_STRING = "org.jboss.ide.eclipse.as.core.model.descriptor.EmptyString";
	private static final String DELIMITER = ",";
	private static final String CATEGORY_LIST = 
		"org.jboss.ide.eclipse.as.core.model.descriptor.Categories";	
	private static final String QUERY_LIST = 
		"org.jboss.ide.eclipse.as.core.model.descriptor.QueryList";	
	private static final String QUERY = 
		"org.jboss.ide.eclipse.as.core.model.descriptor.Query";	

	public static XPathModel instance;
	public static XPathModel getDefault() {
		if( instance == null )
			instance = new XPathModel();
		return instance;
	}
	
	protected HashMap serverToCategories;
	public XPathModel() {
		serverToCategories = new HashMap();
	}
	
	public XPathQuery getQuery(JBossServer jbs, IPath path) {
		XPathCategory cat = getCategory(jbs, path.segment(0));
		if( cat != null )
			return cat.getQuery(path.segment(1));
		return null;
	}
	
	public ArrayList getCategoryCollection(JBossServer jbs) {
		if( serverToCategories.get(jbs.getServer().getId()) == null ) {
			ArrayList val = new ArrayList(Arrays.asList(load(jbs)));
			if( val.size() == 0 ) {
				val = loadDefaults(jbs);
				serverToCategories.put(jbs.getServer().getId(), val);				
				save(jbs);
			} else {
				serverToCategories.put(jbs.getServer().getId(), val);
			}
		}
		return (ArrayList)serverToCategories.get(jbs.getServer().getId());		
	}

	public XPathCategory[] getCategories(JBossServer jbs) {
		ArrayList val = getCategoryCollection(jbs);
		return (XPathCategory[]) val.toArray(new XPathCategory[val.size()]);
	}
	
	public boolean containsCategory(JBossServer jbs, String name) {
		return getCategory(jbs, name) == null ? false : true;
	}
	
	public XPathCategory getCategory(JBossServer jbs, String name) {
		ArrayList list = getCategoryCollection(jbs);
		Iterator i = list.iterator();
		XPathCategory c;
		while(i.hasNext()) {
			c = (XPathCategory)i.next();
			if( c.getName().equals(name)) 
				return c;
		}
		return null;
	}

	public XPathCategory addCategory(JBossServer jbs, String name) {
		if( !containsCategory(jbs, name)) {
			XPathCategory c = new XPathCategory(name, jbs);
			getCategoryCollection(jbs).add(c);
			return c;
		}
		return getCategory(jbs, name);
	}
	
	public void addCategory(JBossServer jbs, XPathCategory category) {
		if( !containsCategory(jbs, category.getName())) {
			getCategoryCollection(jbs).add(category);
		}
	}
	
	public void removeCategory(JBossServer jbs, String name) {
		ArrayList list = getCategoryCollection(jbs);
		Iterator i = list.iterator();
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
	private XPathCategory[] load(JBossServer jbs) {
		ServerAttributeHelper helper = jbs.getAttributeHelper();
		String list = helper.getAttribute(CATEGORY_LIST, (String)null);
		System.out.println("load " + CATEGORY_LIST + ", got " + list);
		if( list == null )
			return new XPathCategory[] {};
		String[] byName = list.split(DELIMITER);
		XPathCategory[] cats = new XPathCategory[byName.length];
		for( int i = 0; i < byName.length; i++ ) {
			cats[i] = new XPathCategory(byName[i], jbs);
		}
		return cats;
	}
		
	public void save(JBossServer jbs) {
		if( !serverToCategories.containsKey(jbs.getServer().getId()))
			return;
		
		ServerAttributeHelper helper = jbs.getAttributeHelper();
		XPathCategory[] categories = getCategories(jbs);
		String list = "";
		for( int i = 0; i < categories.length; i++ ) {
			if( i != 0 )
				list += DELIMITER;
			list += categories[i].getName();
			saveCategory(categories[i], jbs, helper);
		}
		helper.setAttribute(CATEGORY_LIST, list);
		helper.save();
	}

	public void saveCategory(XPathCategory category, JBossServer jbs, ServerAttributeHelper helper) {
		if( category.queriesLoaded()) {
			XPathQuery[] queries = category.getQueries();
			String val = "";
			for( int i = 0; i < queries.length; i++ ) {
				if( i != 0 )
					val += DELIMITER;
				val += category.getName() + Path.SEPARATOR + queries[i].getName();
				saveQuery(queries[i], category, jbs, helper);
			}
			helper.setAttribute(QUERY_LIST + "." + category.getName().replace(' ', '_'), val);
			System.out.println("save " + QUERY_LIST + "." + category.getName() + " with " + val);
		}
	}
	
	private void saveQuery(XPathQuery query, XPathCategory category, JBossServer jbs, ServerAttributeHelper helper) {
		ArrayList list = new ArrayList();
		list.add(query.getBaseDir());
		list.add(query.getFilePattern() == null ? EMPTY_STRING : query.getFilePattern());
		list.add(query.getXpathPattern() == null ? EMPTY_STRING : query.getXpathPattern());
		list.add(query.getAttribute() == null ? EMPTY_STRING : query.getAttribute());
		helper.setAttribute(QUERY + "." + category.getName().replace(' ', '_') + Path.SEPARATOR + query.getName().replace(' ', '_'), list);
		System.out.println("saved " + QUERY + "." + category.getName() + Path.SEPARATOR + query.getName() + " with " + list);
	}
	
	public XPathQuery[] loadQueries(XPathCategory category, JBossServer jbs) {
		ServerAttributeHelper helper = jbs.getAttributeHelper();
		String list = helper.getAttribute(QUERY_LIST + "." + category.getName().replace(' ', '_'), (String)null);
		System.out.println("load " + QUERY_LIST + "." + category.getName() + " gets " + list);
		if( list == null )
			return new XPathQuery[] {};
		String[] queriesByName = list.split(DELIMITER);
		List queryAsStringValues;
		ArrayList returnList = new ArrayList();
		for( int i = 0; i < queriesByName.length; i++ ) {
			queryAsStringValues = helper.getAttribute(QUERY + "." + queriesByName[i].replace(' ', '_'), (List)null);
			System.out.println("load " + QUERY + "." + queriesByName[i]);
			if( queryAsStringValues != null ) {
				try {
					XPathQuery q =new XPathQuery(queriesByName[i].substring(queriesByName[i].indexOf(Path.SEPARATOR)+1), queryAsStringValues); 
					q.setCategory(category);
					returnList.add(q);
				} catch( Exception e ) {e.printStackTrace(); }
			}
		}
		return (XPathQuery[]) returnList.toArray(new XPathQuery[returnList.size()]);
	}
	
	
	
	/*
	 * Loading the defaults for the server
	 * returns the category created
	 */
	private static HashMap rtToPortsFile;
	private static final String ATTRIBUTE_SUFFIX = "_ATTRIBUTE";
	private static final String FILE_SUFFIX = "_FILE";
	static {
		rtToPortsFile = new HashMap();
		rtToPortsFile.put("3.2", new Path("properties").append("jboss.32.default.ports.properties"));
		rtToPortsFile.put("4.0", new Path("properties").append("jboss.40.default.ports.properties"));
		rtToPortsFile.put("4.2", new Path("properties").append("jboss.42.default.ports.properties"));
		rtToPortsFile.put("5.0", new Path("properties").append("jboss.50.default.ports.properties"));
	}

	protected ArrayList loadDefaults(JBossServer jbs) {
		ArrayList retVal = new ArrayList();
		String configFolder;
		configFolder = jbs.getConfigDirectory(false); // do not check the launch config
		AbstractJBossServerRuntime ajbsr = (AbstractJBossServerRuntime)
			jbs.getServer().getRuntime().loadAdapter(AbstractJBossServerRuntime.class, new NullProgressMonitor());
		Path p = (Path)rtToPortsFile.get(ajbsr.getId());
		URL url = FileLocator.find(JBossServerCorePlugin.getDefault().getBundle(), p, null);
		Properties pr = new Properties();
		try {
			pr.load(url.openStream());
			XPathCategory ports = new XPathCategory("Ports", jbs);
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
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return retVal;
	}
}
