/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jmx.jolokia.internal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.foundation.core.xml.IMemento;
import org.jboss.tools.foundation.core.xml.XMLMemento;
import org.jboss.tools.jmx.core.AbstractConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionCategory;
import org.jboss.tools.jmx.core.IConnectionProvider;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.JMXActivator;
import org.jboss.tools.jmx.core.JMXCoreMessages;
import org.jboss.tools.jmx.jolokia.JolokiaConnectionWrapper;

public class JolokiaConnectionProvider extends AbstractConnectionProvider 
	implements IConnectionProvider, IConnectionCategory {
	public static final String PROVIDER_ID = "org.jboss.tools.jmx.jolokia.JolokiaConnectionProvider"; //$NON-NLS-1$

	private HashMap<String, JolokiaConnectionWrapper> connections;
	public JolokiaConnectionProvider() {
		// Required No-arg constructor
	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public String getName(IConnectionWrapper wrapper) {
		return ((JolokiaConnectionWrapper)wrapper).getName();
	}

	
	@Override
	public IConnectionWrapper[] getConnections() {
		if( connections == null ) {
			loadConnections();
		}
		if( connections != null ) {
			List<IConnectionWrapper> result = new ArrayList<>();
			result.addAll(connections.values());
			// Sort based on name
			Collections.sort(result, new Comparator<IConnectionWrapper>(){
				@Override
				public int compare(IConnectionWrapper o1, IConnectionWrapper o2) {
					String name1 = getName(o1);
					String name2 = getName(o2);
					if( name1 == null )
						return name2 == null ? 0 : -1;
					if( name2 == null )
						return 1;
					return name1.compareTo(name2);
				}
			});
			return result.toArray(new IConnectionWrapper[result.size()]);
		}
		return new IConnectionWrapper[0];
	}

	@Override
	public IConnectionWrapper createConnection(Map map) throws CoreException {
		JolokiaConnectionWrapper con = new JolokiaConnectionWrapper();
		String id = (String)map.get(JolokiaConnectionWrapper.ID);
		String url = (String)map.get(JolokiaConnectionWrapper.URL);
		String type = (String)map.get(JolokiaConnectionWrapper.GET_OR_POST);
		Boolean ignoreSSLError = (Boolean)map.get(JolokiaConnectionWrapper.IGNORE_SSL_ERRORS);
		Map<String, String> headers = (Map<String,String>)map.get(JolokiaConnectionWrapper.HEADERS);
		con.setId(id);
		con.setUrl(url);
		con.setHeaders(headers);
		con.setIgnoreSSLErrors(ignoreSSLError);
		con.setType(type == null ? "POST" : type);
		return con;
	}


	@Override
	public boolean canCreate() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean canDelete(IConnectionWrapper wrapper) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void addConnection(IConnectionWrapper connection) {
		connections.put(((JolokiaConnectionWrapper)connection).getName(), (JolokiaConnectionWrapper)connection);
		try {
			fireAdded(connection);
			save();
		} catch( IOException ioe ) {
			IStatus s = new Status(IStatus.ERROR, JMXActivator.PLUGIN_ID, JMXCoreMessages.DefaultConnection_ErrorAdding, ioe);
			Activator.pluginLog().logStatus(s);
		}
	}

	@Override
	public void removeConnection(IConnectionWrapper connection) {
		connections.remove(((JolokiaConnectionWrapper)connection).getName());
		try {
			fireRemoved(connection);
			save();
		} catch( IOException ioe ) {
			IStatus s = new Status(IStatus.ERROR, JMXActivator.PLUGIN_ID, JMXCoreMessages.DefaultConnection_ErrorAdding, ioe);
			Activator.pluginLog().logStatus(s);
		}
	}

	@Override
	public void connectionChanged(IConnectionWrapper connection) {
		// TODO not sure what to do here
		try {
			fireChanged(connection);
			save();
		} catch( IOException ioe ) {
			IStatus s = new Status(IStatus.ERROR, JMXActivator.PLUGIN_ID, JMXCoreMessages.DefaultConnection_ErrorAdding, ioe);
			Activator.pluginLog().logStatus(s);
		}
	}

	
	
	private static final String XML_FILE_NAME = "jolokiaconnections.xml";
	private static final String XML_ROOT = "JolokiaConnections";
	private static final String XML_CONNECTION = "connection";
	private static final String XML_HEADERS = "headers";
	private static final String XML_HEADER = "header";
	private static final String XML_ID = "id";
	private static final String XML_URL = "url";
	private static final String XML_IGNORE_SSL_ERR = "ignoreSSLError";
	private static final String XML_KEY = "key";
	private static final String XML_VALUE = "value";
	

	private synchronized void loadConnections() {
		if( connections == null ) {
			// TODO load from some model
			connections = new HashMap<>();
			IPath p = Activator.getDefault().getStateLocation().append(XML_FILE_NAME);
			if( p.toFile().exists()) {
				try {
					XMLMemento mem = XMLMemento.createReadRoot(new FileInputStream(p.toFile()));
					IMemento[] mementoConnections = mem.getChildren(XML_CONNECTION);
					for( int i = 0; i < mementoConnections.length; i++ ) {
						String id = mementoConnections[i].getString(XML_ID);
						String url = mementoConnections[i].getString(XML_URL);
						boolean ignoreSSL = mementoConnections[i].getBoolean(XML_IGNORE_SSL_ERR);
						IMemento headers = mementoConnections[i].getChild(XML_HEADERS);
						HashMap<String, String> headerMap = new HashMap<>();
						if( headers != null) {
							IMemento[] individualHeaders = headers.getChildren(XML_HEADER);
							for( int j = 0; j < individualHeaders.length; j++ ) {
								String key = individualHeaders[j].getString(XML_KEY);
								String val = individualHeaders[j].getString(XML_VALUE);
								headerMap.put(key,  val);
							}
						}						
						
						// Pass details to creation method
						Map<String, Object> toCreate = new HashMap<>();
						toCreate.put(JolokiaConnectionWrapper.ID, id);
						toCreate.put(JolokiaConnectionWrapper.URL, url);
						toCreate.put(JolokiaConnectionWrapper.IGNORE_SSL_ERRORS, ignoreSSL);
						toCreate.put(JolokiaConnectionWrapper.HEADERS, headerMap);
						
						try {
							IConnectionWrapper created = createConnection(toCreate);
							if( created != null ) {
								this.connections.put(id, (JolokiaConnectionWrapper)created);
							}
						} catch(CoreException ce) {
							// TODO log?
						}
						
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}


	
	protected void save() throws IOException  {
		// Persist the model, no-op for now
		XMLMemento mem = XMLMemento.createWriteRoot(XML_ROOT);
		Iterator<String> ids = connections.keySet().iterator();
		while(ids.hasNext()) {
			String id = ids.next();
			JolokiaConnectionWrapper con = connections.get(id);
			id = con.getId();
			String url = con.getUrl();
			boolean ignoreSSLErr = con.isIgnoreSSLErrors();
			Map<String, String> headers = con.getHeaders();
			
			
			XMLMemento conMem = (XMLMemento)mem.createChild(XML_CONNECTION);
			conMem.putString(XML_ID, id);
			conMem.putString(XML_URL, url);
			conMem.putBoolean(XML_IGNORE_SSL_ERR, ignoreSSLErr);
			
			XMLMemento headersMem = (XMLMemento)conMem.createChild(XML_HEADERS);
			Iterator<String> headerIt = headers.keySet().iterator();
			while(headerIt.hasNext()) {
				String hKey = headerIt.next();
				String hVal = headers.get(hKey);
				XMLMemento headerMem = (XMLMemento)headersMem.createChild(XML_HEADER);
				headerMem.putString(XML_KEY, hKey);
				headerMem.putString(XML_VALUE, hVal);
			}
		}
		
		IPath p = Activator.getDefault().getStateLocation().append(XML_FILE_NAME);
		p.toFile().getParentFile().mkdirs();
		mem.saveToFile(p.toOSString());
	}
	
	@Override
	public String getCategoryId() {
		return IConnectionCategory.DEFINED_CATEGORY;
	}
	
	public IConnectionWrapper findConnection(String name) {
		return connections.get(name);
	}
}
