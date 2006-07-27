package org.jboss.ide.eclipse.as.core.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerType;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.model.SimpleTreeItem;
import org.jboss.ide.eclipse.as.core.server.runtime.AbstractServerRuntimeDelegate;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

public class ServerAttributeHelper {
	
	public static final String PROP_START_ARGS = "PROP_START_ARGS";
	public static final String PROP_STOP_ARGS = "PROP_STOP_ARGS";
	public static final String PROP_PROG_ARGS = "PROP_PROG_ARGS";
	public static final String PROP_VM_ARGS = "PROP_VM_ARGS";
	public static final String PROP_CONFIG_PATH = "PROP_START_ARGS";
	public static final String PROP_CLASSPATH = "PROP_CLASSPATH";
	
	public static final String PROP_MINIMAL_CONFIG = "MINIMAL_CONFIG";
	
	public static final String JBOSS_SERVER_HOME = "JBOSS_SERVER_HOME";
	public static final String JBOSS_CONFIG = "JBOSS_CONFIG";
	
	public static final String JBOSS_CONFIG_DEFAULT = "default";

	
	public static final String XPATH_CATEGORIES = "_XPATH_CATEGORIES_";
	public static final String XPATH_CATEGORY2_PREFIX = "_XPATH_CATEGORY2_PREFIX_";
	public static final String XPATH_PROPERTY_PREFIX = "_XPATH_PROPERTY_PREFIX_";
	
	
	
	private ServerWorkingCopy server;
	private JBossServer jbServer;
	public ServerAttributeHelper(JBossServer jbServer, IServerWorkingCopy copy) {
		this.server = (ServerWorkingCopy)copy;
		this.jbServer = jbServer;
	}
	

	
	public String getServerHome() {
		return server.getAttribute(JBOSS_SERVER_HOME, (String)null);
	}
	public String getJbossConfiguration() {
		return server.getAttribute(JBOSS_CONFIG, JBOSS_CONFIG_DEFAULT);
	}
	
	public void setServerHome( String home ) {
		server.setAttribute(ServerAttributeHelper.JBOSS_SERVER_HOME, home);
	}
	
	public void setJbossConfiguration( String config ) {
		server.setAttribute(ServerAttributeHelper.JBOSS_CONFIG, config);
	}
	
	public void setProgramArgs(String args) {
		server.setAttribute(PROP_START_ARGS, args);
	}
	
	public void setVMArgs(String args) {
		server.setAttribute(PROP_VM_ARGS, args);
	}
	
	
	public void save() {
		try {
			server.save(true, null);
		} catch( Exception e) {
		}
	}
	
	
	/*
	 * These methods go back to the version delegate for defaults if 
	 * they are not set as attributes here.
	 */
	
	public String getStartArgs() {
		return server.getAttribute(PROP_START_ARGS, getVersionDelegate().getStartArgs(jbServer));
	}

	public String getStopArgs() {
		return server.getAttribute(PROP_STOP_ARGS, getVersionDelegate().getStopArgs(jbServer));
	}

	public String getVMArgs() {
		return server.getAttribute(PROP_VM_ARGS, getVersionDelegate().getVMArgs(jbServer));
	}
	
	public String[] getMinimalConfig() {
		String minimal;
		if( (minimal=server.getAttribute(PROP_MINIMAL_CONFIG, (String)null)) == null ) {
			return getVersionDelegate().getMinimalRequiredPaths();
		}
		return minimal.split("\n");
	}
	
	public String getStartMainType() {
		return getVersionDelegate().getStartMainType();
	}
	
	
	public String getConfigurationPath() {
		return getServerHome() + File.separator + "server" + File.separator + getJbossConfiguration();
	}
	
	
	public String getDeployDirectory() {
		return getConfigurationPath() + File.separator + "deploy";
	}
	
	public AbstractServerRuntimeDelegate getVersionDelegate() {
		return jbServer.getJBossRuntime().getVersionDelegate();
	}

	
	
	public static final String START_TIMEOUT = "_START_TIMEOUT_";
	public static final String STOP_TIMEOUT = "_STOP_TIMEOUT_";
	
	public int getStartTimeout() {
		int prop = server.getAttribute(START_TIMEOUT, -1);
		int max = ((ServerType)server.getServerType()).getStartTimeout();
		
		if( prop <= 0 || prop > max ) return max;
		return prop;
	}
	public int getStopTimeout() {
		int prop = server.getAttribute(STOP_TIMEOUT, -1);
		int max = ((ServerType)server.getServerType()).getStopTimeout();
		
		if( prop <= 0 || prop > max ) return max;
		return prop;
	}
	
	public void setStartTimeout(int time) {
		server.setAttribute(START_TIMEOUT, time);
	}
	public void setStopTimeout(int time) {
		server.setAttribute(STOP_TIMEOUT, time);
	}
	
	
	public IServerWorkingCopy getServer() {
		return this.server;
	}
	
	public SimpleXPathPreferenceTreeItem getXPathPreferenceTree() {
		List categories = server.getAttribute(XPATH_CATEGORIES, new ArrayList());
		if( categories.size() == 0 ) {
			return getDefaultXPathPreferenceTree();
		}
		SimpleXPathPreferenceTreeItem model = new SimpleXPathPreferenceTreeItem(null, XPATH_CATEGORIES);
		Iterator i = categories.iterator();
		while(i.hasNext()) {
			String categoryName = (String)i.next();
			SimpleXPathPreferenceTreeItem categoryItem = new SimpleXPathPreferenceTreeItem(model, categoryName);
			String categoryListKey = XPATH_CATEGORY2_PREFIX + categoryName;
			List categoryElementsAsList = server.getAttribute(categoryListKey, new ArrayList());
			Iterator j = categoryElementsAsList.iterator();
			while(j.hasNext()) {
				String xpathName = (String)j.next();
				String finalKey = XPATH_PROPERTY_PREFIX + categoryName + xpathName;
				List xpathItem = server.getAttribute(finalKey, new ArrayList());
				if( xpathItem.size() == 2 ) {
					XPathPreferenceTreeItem xpi = new XPathPreferenceTreeItem(categoryItem, 
							(String)xpathItem.get(0), (String)xpathItem.get(1));
				} else if( xpathItem.size() == 3 ) {
					XPathPreferenceTreeItem xpi = new XPathPreferenceTreeItem(categoryItem, 
							(String)xpathItem.get(0), (String)xpathItem.get(1), (String)xpathItem.get(2));
				}
			}
		}

		return model;
	}
	
	private SimpleXPathPreferenceTreeItem getDefaultXPathPreferenceTree() {
		SimpleXPathPreferenceTreeItem model = new SimpleXPathPreferenceTreeItem(null, XPATH_CATEGORIES);
		SimpleXPathPreferenceTreeItem ports = new SimpleXPathPreferenceTreeItem(model, "Ports");
		XPathPreferenceTreeItem jndi = new XPathPreferenceTreeItem(ports, 
				"JNDI", "/server/mbean[@name='jboss:service=Naming']/attribute[@name='Port']");
		return model;
	}
	
	public void saveXPathPreferenceTree(SimpleTreeItem tree) {
		
		SimpleTreeItem[] children = tree.getChildren2();
		SimpleTreeItem[] grandChildren;
		ArrayList categoryList = new ArrayList();
		ArrayList xpathNameList, itemAsList;
		XPathPreferenceTreeItem xpathPrefItem;
		HashMap map = new HashMap();
		for( int i = 0; i < children.length; i++ ) {
			xpathNameList = new ArrayList();
			String categoryName = (String)children[i].getData();
			categoryList.add(categoryName);  // add "Ports"
			
			grandChildren = children[i].getChildren2(); // list of xpaths under "Ports"
			for( int j = 0; j < grandChildren.length; j++ ) {
				xpathPrefItem = ((XPathPreferenceTreeItem)grandChildren[j]);  // JNDI
				itemAsList = new ArrayList();
				itemAsList.add(xpathPrefItem.getName());
				itemAsList.add(xpathPrefItem.getXPath());
				itemAsList.add(xpathPrefItem.getAttributeName());
				xpathNameList.add(xpathPrefItem.getName());
				map.put(XPATH_PROPERTY_PREFIX + categoryName + xpathPrefItem.getName(), itemAsList);
			}
			map.put(XPATH_CATEGORY2_PREFIX + categoryName, xpathNameList);
		}
		map.put(XPATH_CATEGORIES, categoryList);
		
		Iterator i = map.keySet().iterator();
		while( i.hasNext()) {
			String key = (String)i.next();
			server.setAttribute(key, (List)map.get(key));
		}

	}
	
	/* Just a label class */
	public static class SimpleXPathPreferenceTreeItem extends SimpleTreeItem {
		public SimpleXPathPreferenceTreeItem(SimpleTreeItem parent, Object data) {
			super(parent, data);
		}
	}
	public static class XPathPreferenceTreeItem extends SimpleXPathPreferenceTreeItem {
		private String name;
		private String elementXPath;
		private String attributeName = null;
		
		public XPathPreferenceTreeItem(SimpleTreeItem parent, String key, String value) {
			super(parent, null);
			this.name = key;
			this.elementXPath = value;
		}
		
		public XPathPreferenceTreeItem(SimpleTreeItem parent, String key, 
										String value, String attributeName ) {
			this(parent, key, value);
			this.attributeName = attributeName;
		}
		public String getName() {
			return name;
		}
		public String getXPath() {
			return elementXPath;
		}
		public void setXPath(String value) {
			this.elementXPath = value;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getAttributeName() {
			return attributeName;
		}
		public void setAttributeName(String attName) {
			this.attributeName = attName;
		}
		
	}
	
}
