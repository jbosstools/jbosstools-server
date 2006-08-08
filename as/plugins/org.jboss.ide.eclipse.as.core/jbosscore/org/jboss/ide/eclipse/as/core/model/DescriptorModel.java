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
package org.jboss.ide.eclipse.as.core.model;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.Server;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

/**
 * This class is intended to represent the actual mbeans, 
 * configuration descriptors, jars, etc enabled, disabled, or 
 * available in a server's on-disk configuration.
 * 
 * By configuration, here, I mean the directory under jboss' 
 * install directory which contains deployed jars and -service.xml files,
 * mbeans, etc. 
 * 
 * This is not yet implemented. All code here is forward-looking.
 * 
 * @author rstryker
 *
 */

public class DescriptorModel {
	
	/* Static Portion */
	private static DescriptorModel model = null;
	
	public static DescriptorModel getDefault() {
		if( model == null ) {
			model = new DescriptorModel();
		}
		return model;
	}
	
	
	/* Member variables  / methods */
	private HashMap map;
	
	public DescriptorModel() {
		map = new HashMap();
	}
	
	public ServerDescriptorModel getServerModel(IServer server) {
		String key = server.getId();
		Object o = map.get(key);
		if( o == null ) {
			o = createEntity(server);
		}
		return (ServerDescriptorModel)o;
	}
	
	private ServerDescriptorModel createEntity(IServer server) {
		if( server.getServerType().getId().startsWith("org.jboss.ide.eclipse.as.")) {
			String id = server.getId();
			ServerDescriptorModel val = new ServerDescriptorModel(id);
			map.put(id, val);
			return val;
		}
		return null;
	}
	
	
	public class ServerDescriptorModel {
		private String serverId;
		private String configPath;
		
		private HashMap pathToDocument;
		private HashMap pathToLastRead;
		
		public ServerDescriptorModel(String id) {
			this.serverId = id;
			ServerAttributeHelper helper = getJBossServer().getAttributeHelper();
			configPath = helper.getConfigurationPath();
			pathToDocument = new HashMap();
			pathToLastRead = new HashMap();
		}
		
		public JBossServer getJBossServer() {
			return JBossServerCore.getServer(ServerCore.findServer(serverId));
		}
		
		
		/**
		 * Discover all descriptors in the configuration
		 * @return
		 */
		
		private File[] getAllDescriptors() {
			ArrayList list = new ArrayList();
			File config = new File(configPath);
			getAllDescriptorsRecurse(config, list);
			File[] ret = new File[list.size()];
			list.toArray(ret);
			return ret;
		}
		
		private void getAllDescriptorsRecurse(File parent, ArrayList collector) {
			if( parent.isDirectory() ) {
				File[] children = parent.listFiles();
				for( int i = 0; i < children.length; i++ ) {
					if( children[i].isDirectory()) {
						getAllDescriptorsRecurse(children[i], collector);
					} else if( children[i].getAbsolutePath().endsWith(".xml")) {
						collector.add(children[i]);
					}
				}
			}
		}
		
		
		private static final String IGNORED_DESCRIPTOR_FOLDERS = "_IGNORED_DESCRIPTOR_FOLDERS_";
		
		/*
		 * Do preference stuff here to see if the directory is ignored.
		 */
		private boolean isIgnoredDirectory(File f) {
			boolean found = false;
			List ignored = Arrays.asList(getIgnoredDirectories());
			while( f != null && !found ) {
				if( ignored.contains(f.getAbsoluteFile())) 
					return true;
				f = f.getParentFile();
			}
			return false;
		}

		public String[] getIgnoredDirectories() {
			List list = ((Server)getJBossServer().getAttributeHelper().getServer()).getAttribute(IGNORED_DESCRIPTOR_FOLDERS, new ArrayList());
			String[] ignoredDirs = (String[]) list.toArray(new String[list.size()]);
			return ignoredDirs;
		}
		
		private Document getDocument(String path) {
			// First get last time loaded.
			if( !isCurrent(path)) {
				loadDocument(path);
			}
			return (Document)pathToDocument.get(path);
		}
		
		private boolean isCurrent( String path ) {
			if( pathToLastRead.get(path) == null ) return false;
			if( !(pathToLastRead.get(path) instanceof Long)) return false;
			if( pathToDocument.get(path) == null ) return false;
			long lastModified = ((Long)pathToLastRead.get(path)).longValue();
			File f = new File(path);
			if( f.lastModified() > lastModified ) return false;
			
			return true;
		}
		
		private void loadDocument(String path) {
			try {
				File file = new File(path);
				long lastModified = file.lastModified();
				URL url = new File(path).toURL();
				SAXReader reader = new SAXReader(false);
				reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
				Document document = reader.read(url);
				
				// add to maps
				pathToDocument.put(path, document);
				pathToLastRead.put(path, new Long(lastModified));
			} catch( Exception e ) {
				ASDebug.p("file " + path + ", Exception: " + e.getMessage(), this);
			}
		}
		
		/**
		 * Searches in the given file for the given xpath
		 * @param path
		 * @param xpath
		 * @return A list of DefaultElements
		 */
		public List getXPathFromFile(String path, String xpath) {
			try {
				Document d = getDocument(path);
				if( d == null ) return new ArrayList();
				return d.selectNodes(xpath);
			} catch( Exception e ) {
				return new ArrayList();
			}
		}
		
		/**
		 * Searches all descriptors for the provided xpath
		 * @param xpath
		 * @return A list of file / DefaultElement pairs
		 */
		public XPathTreeItem[] getXPath(String xpath) {
			return getXPath(xpath, null);
		}
		
		public XPathTreeItem[] getXPath(String xpath, String attributeName) {
			return getXPath(xpath, attributeName, true);
		}
		
		public XPathTreeItem[] getXPath(String xpath, String attributeName, boolean filter ) {
			return getXPath(xpath, attributeName, filter, true);
		}
		
		public XPathTreeItem[] getXPath(String xpath, String attributeName, boolean filter, boolean refresh ) {
			if( refresh ) 
				refreshDescriptors(new NullProgressMonitor());

			ArrayList list = new ArrayList();
			Set documentPathSet = pathToDocument.keySet();
			Iterator i = documentPathSet.iterator();
			String p;
			List tmp;
			XPathTreeItem newItem;
			while(i.hasNext()) {
				p = (String)i.next();
				
				if( !filter || !isIgnoredDirectory(new File(p))) {
					tmp = getXPathFromFile(p, xpath);
					if( tmp.size() > 0 ) { 
						if( attributeName == null || attributeName.equals("")) {
							newItem = new XPathTreeItem(null, new File(p), tmp);
							list.add(newItem);
						} else {
							// Remove any that match the path but not the attribute
							Iterator j = tmp.iterator();
							while( j.hasNext()) {
								DefaultElement el = (DefaultElement)j.next();
								if( el.attribute(attributeName) == null ) {
									j.remove();
								}
							}
							if( tmp.size() > 0 ) {
								newItem = new XPathTreeItem(null, new File(p), tmp, attributeName);
								list.add(newItem);
							}
						}
					}
				}
			}
			return (XPathTreeItem[]) list.toArray(new XPathTreeItem[list.size()]);
		}

		
		
		public class XPathTreeItem extends SimpleTreeItem {
			public XPathTreeItem(SimpleTreeItem parent, File data, List nodes) {
				super(parent, data);
				addXPathsAsChildren(nodes);
			}
			
			public XPathTreeItem(SimpleTreeItem parent, File data, List nodes, String attribute) {
				super(parent, data);
				addXPathsAsChildren(nodes, attribute);
			}
			
			public File getFile() {
				return ((File)getData());
			}
			
//			 just add the list of nodes as children
			private void addXPathsAsChildren(List nodes) {
				Iterator i = nodes.iterator();
				int z = 0;
				while(i.hasNext()) {
					Node o = (Node)i.next();
					addChild(new XPathTreeItem2(this, o, z++));
				}
			}
			
//			 just add the list of nodes as children with an attribute
			private void addXPathsAsChildren(List nodes, String attribute) {
				Iterator i = nodes.iterator();
				int z = 0;
				while(i.hasNext()) {
					Node o = (Node)i.next();
					addChild(new XPathTreeItem2(this, o, attribute, z++));
				}
			}
			
			
		}
		
		public class XPathTreeItem2 extends SimpleTreeItem {
			// which match inside the file am i?
			private int index;
			private String attribute;
			private boolean hasAttribute, isDirty;
			
			public XPathTreeItem2(XPathTreeItem parent, Node data, int index) {
				super(parent, data);
				this.index = index;
				hasAttribute = false;
				isDirty = false;
			}
			public XPathTreeItem2(XPathTreeItem parent, Node data, String attribute, int index) {
				super(parent, data);
				this.index = index;
				this.attribute = attribute;
				hasAttribute = true;
				isDirty = false;
			}

			public int getIndex() {
				return index;
			}
			
			public boolean hasAttribute() {
				return hasAttribute;
			}
			public boolean isDirty() {
				return isDirty;
			}
			
			public String getAttribute() {
				return attribute;
			}
			
			public String getAttributeValue() {
				if( getData() instanceof DefaultElement ) {
					return ((DefaultElement)getData()).attributeValue(attribute);
				}
				return "";
			}
			
			public Document getDocument() {
				if( getData() instanceof DefaultElement ) {
					return ((DefaultElement)getData()).getDocument();
				}
				return null;
			}
			
			public String getText() {
				try {
				if( getData() instanceof DefaultElement ) {
					if( !hasAttribute()) {
						return ((DefaultElement)getData()).getText();
					} else {
						Attribute att = ((DefaultElement)getData()).attribute(attribute);
						return att.getValue();
					}
				}
				} catch( NullPointerException npe ) {
					npe.printStackTrace();
				}
				return "";
			}
			
			public void setText(String newValue) {
				if( getData() instanceof DefaultElement ) {
					if( !hasAttribute()) {
						((DefaultElement)getData()).setText(newValue);
					} else {
						((DefaultElement)getData()).attribute(attribute).setValue(newValue);
					}
					isDirty = true;
				}
			}
			
			public String elementAsXML() {
				DefaultElement element = ((DefaultElement)getData());
				return ((DefaultElement)getData()).asXML();
			}
			
			public void saveDescriptor() {
				if( getParent() instanceof XPathTreeItem ) {
					try {
						File outFile = ((XPathTreeItem)getParent()).getFile();
						FileOutputStream os = new FileOutputStream(outFile);
	
						Document doc = ((DefaultElement)getData()).getDocument();
	
						OutputFormat outformat = OutputFormat.createPrettyPrint();
						XMLWriter writer = new XMLWriter(os, outformat);
						writer.write(doc);
						writer.flush();
					} catch( Exception e ) {
						e.printStackTrace();
					}
				}
			}
			
			public void reloadDescriptorFromDisk() {
				if( getParent() instanceof XPathTreeItem ) {
					try {
						loadDocument(((XPathTreeItem)getParent()).getFile().getAbsolutePath());
					} catch( Exception e ) {
						e.printStackTrace();
					}
				}
			}
		}
		
		public void refreshDescriptors(IProgressMonitor monitor) {
			File[] files = getAllDescriptors();
			monitor.beginTask("Refresh", files.length);
			for( int i = 0; i < files.length; i++ ) {
				getDocument(files[i].getAbsolutePath());
				monitor.worked(1);
			}
			monitor.done();
		}
		
		public void refreshDescriptorsClean(IProgressMonitor monitor) {
			pathToDocument.clear();
			pathToLastRead.clear();
			refreshDescriptors(monitor);
 		}
		
		/**
		 * JNDI specific
		 * @return
		 */
		public int getJNDIPort() {
			try {
				String jbossServicePath = configPath + File.separator + "conf" + 
						File.separator + "jboss-service.xml";
				String xpath = "/server/mbean[@name='jboss:service=Naming']/attribute[@name='Port']";
				List l = getXPathFromFile(jbossServicePath, xpath);
				if( l.size() > 0 ) {
					DefaultElement el = (DefaultElement)l.get(0);
					int jndi = Integer.parseInt(el.getText());
					
					return jndi;
				}
			} catch( Exception e ) {
			}
			return 1099;
		}
	}
}
