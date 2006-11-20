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
package org.jboss.ide.eclipse.as.core.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.model.DescriptorModel.ServerDescriptorModel.XPathTreeItem;
import org.jboss.ide.eclipse.as.core.util.SimpleTreeItem;

public class ServerAttributeHelper {

	private ServerWorkingCopy wch;
	private IServer server;
	public ServerAttributeHelper(IServer server, IServerWorkingCopy copy) {
		this.wch = (ServerWorkingCopy)copy;
		this.server = server;
	}

	
	public IServer getServer() {
		return server;
	}
	
	public void setAttribute(String attributeName, int value) {
		wch.setAttribute(attributeName, value);
	}

	public void setAttribute(String attributeName, boolean value) {
		wch.setAttribute(attributeName, value);
	}

	public void setAttribute(String attributeName, String value) {
		wch.setAttribute(attributeName, value);
	}

	public void setAttribute(String attributeName, List value) {
		wch.setAttribute(attributeName, value);
	}

	public void setAttribute(String attributeName, Map value) {
		wch.setAttribute(attributeName, value);
	}
	
	
	
	

	public String getAttribute(String attributeName, String defaultValue) {
		return wch.getAttribute(attributeName, defaultValue);
	}

	public int getAttribute(String attributeName, int defaultValue) {
		return wch.getAttribute(attributeName, defaultValue);
	}

	public boolean getAttribute(String attributeName, boolean defaultValue) {
		return wch.getAttribute(attributeName, defaultValue);
	}
	
	public List getAttribute(String attributeName, List defaultValue) {
		return wch.getAttribute(attributeName, defaultValue);
	}
	
	public Map getAttribute(String attributeName, Map defaultValue) {
		return wch.getAttribute(attributeName, defaultValue);
	}

	

	public boolean isDirty() {
		return wch.isDirty();
	}

	public IServer save(boolean force, IProgressMonitor monitor) throws CoreException {
		return wch.save(force, monitor);
	}
	
	public IServer save() {
		try {
			return save(false, new NullProgressMonitor());
		} catch( Exception e ) {}
		return null;
	}
	

	
	
	
	
	
	
	
	
	

	
	
	public static final String PORTS_DEFAULT_CATEGORY = "Ports";
	public static final String XPATH_CATEGORIES = "_XPATH_CATEGORIES_";
	public static final String XPATH_CATEGORY2_PREFIX = "_XPATH_CATEGORY2_PREFIX_";
	public static final String XPATH_PROPERTY_PREFIX = "_XPATH_PROPERTY_PREFIX_";
	public static final String PORT_CATEGORY_PREF_KEY = "_PORT_CATEGORY_NAME_";

	public String getDefaultPortCategoryName() {
		return wch.getAttribute(PORT_CATEGORY_PREF_KEY, PORTS_DEFAULT_CATEGORY);
	}
	public void setDefaultPortCategoryName(String s) {
		wch.setAttribute(PORT_CATEGORY_PREF_KEY, s);
	}

	public SimpleXPathPreferenceTreeItem getXPathPreferenceTree() {
		List categories = wch.getAttribute(XPATH_CATEGORIES, (List)null);
		if( categories == null ) {
			return getDefaultXPathPreferenceTree();
		}
		SimpleXPathPreferenceTreeItem model = new SimpleXPathPreferenceTreeItem(null, XPATH_CATEGORIES);
		Iterator i = categories.iterator();
		while(i.hasNext()) {
			String categoryName = (String)i.next();
			SimpleXPathPreferenceTreeItem categoryItem = new SimpleXPathPreferenceTreeItem(model, categoryName);
			String categoryListKey = XPATH_CATEGORY2_PREFIX + categoryName;
			List categoryElementsAsList = wch.getAttribute(categoryListKey, new ArrayList());
			Iterator j = categoryElementsAsList.iterator();
			while(j.hasNext()) {
				String xpathName = (String)j.next();
				String finalKey = XPATH_PROPERTY_PREFIX + categoryName + xpathName;
				List xpathItem = wch.getAttribute(finalKey, new ArrayList());
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
		
		SimpleXPathPreferenceTreeItem ports = new SimpleXPathPreferenceTreeItem(model, PORTS_DEFAULT_CATEGORY);
		
		
		
		XPathPreferenceTreeItem jndi = new XPathPreferenceTreeItem(ports, 
				"JNDI", "/server/mbean[@name='jboss:service=Naming']/attribute[@name='Port']");
		XPathPreferenceTreeItem jndiRMI = new XPathPreferenceTreeItem(ports, 
				"JNDI RMI", "/server/mbean[@name='jboss:service=Naming']/attribute[@name='RmiPort']");
		
		XPathPreferenceTreeItem haJndiStub = new XPathPreferenceTreeItem(ports, 
				"HA JNDI Stub", "//server/mbean[@name='jboss:service=HAJNDI']/attribute[@name='Port']");

		XPathPreferenceTreeItem haJndiRMI = new XPathPreferenceTreeItem(ports, 
				"HA JNDI RMI", "//server/mbean[@name='jboss:service=HAJNDI']/attribute[@name='RmiPort']");

		XPathPreferenceTreeItem jrmpa = new XPathPreferenceTreeItem(ports, 
				"JRMPA RMI Object", "//server/mbean[@name='jboss:service=invoker,type=jrmpha']/attribute[@name='RMIObjectPort']");

		XPathPreferenceTreeItem clusterUDPMcast = new XPathPreferenceTreeItem(ports, 
				"Cluster UDP MCast", "//server/mbean[@name='jboss:service=${jboss.partition.name:DefaultPartition}']/attribute[@name='PartitionConfig']/Config/UDP", "mcast_port");
		
		XPathPreferenceTreeItem webservices = new XPathPreferenceTreeItem(ports, 
				"Web Services", "//server/mbean[@name='jboss:service=WebService']/attribute[@name='Port']");
		
//		XPathPreferenceTreeItem hypersonic = new XPathPreferenceTreeItem(ports, 
//				"HyperSonic", "//server/mbean[@name='jboss:service=Hypersonic']/attribute[@name='Port']");
		
		XPathPreferenceTreeItem jrmpInvoker = new XPathPreferenceTreeItem(ports, 
				"JRMP Invoker", "//server/mbean[@name='jboss:service=invoker,type=jrmp']/attribute[@name='RMIObjectPort']");
		
		XPathPreferenceTreeItem pooledInvoker = new XPathPreferenceTreeItem(ports, 
				"Pooled Invoker", "//server/mbean[@name='jboss:service=invoker,type=pooled']/attribute[@name='ServerBindPort']");
		
		

		
		return model;
	}
	
	public void saveXPathPreferenceTree(SimpleTreeItem tree) {
		
		SimpleTreeItem[] children = tree.getChildren();
		SimpleTreeItem[] grandChildren;
		ArrayList categoryList = new ArrayList();
		ArrayList xpathNameList, itemAsList;
		XPathPreferenceTreeItem xpathPrefItem;
		HashMap map = new HashMap();
		for( int i = 0; i < children.length; i++ ) {
			xpathNameList = new ArrayList();
			String categoryName = (String)children[i].getData();
			categoryList.add(categoryName);  // add "Ports"
			
			grandChildren = children[i].getChildren(); // list of xpaths under "Ports"
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
			wch.setAttribute(key, (List)map.get(key));
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
		
		// actually a forced refresh
		public void ensureLoaded(JBossServer jbServer) {
			String XPATH_PROPERTY_LOADED = "_XPATH_PROPERTY_LOADED_";

			String xpath = getXPath();
			String attribute = getAttributeName();
			XPathTreeItem[] items = new XPathTreeItem[0];
			if( attribute == null || attribute.equals("")) {
				items = jbServer.getDescriptorModel().getXPath(xpath);
			} else {
				items = jbServer.getDescriptorModel().getXPath(xpath, attribute);
			}
			
			if( getProperty(XPATH_PROPERTY_LOADED) != null ) {
				//deleteChildren();
				return;
			}
			for( int i = 0; i < items.length; i++ ) {
				addChild(items[i]);
			}
			setProperty(XPATH_PROPERTY_LOADED, new Boolean(true));
		}
		
	}

	
	
}
