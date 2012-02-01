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

import java.util.HashMap;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.util.IMemento;

/**
 * A class representing an XPath Category, which 
 * is owned by a server and has XPath queries in it
 * @author rob.stryker@redhat.com
 *
 */
public class XPathCategory {
	protected String name; // cannot include delimiter from the model, comma
	protected IServer server;
	protected IMemento memento;
	protected HashMap<String, XPathQuery> children;
	
	/**
	 * Create a stub empty category
	 * @param name
	 * @param server
	 */
	public XPathCategory(String name, IServer server) {
		this.name = name;
		this.server = server;
		children = new HashMap<String, XPathQuery>();
	}
	
	public XPathCategory(IServer server, IMemento memento) {
		this.server = server;
		this.name = memento.getString("name"); //$NON-NLS-1$
		IMemento[] queryMementos = memento.getChildren("query");//$NON-NLS-1$
		children = new HashMap<String, XPathQuery>();
		for( int i = 0; i < queryMementos.length; i++ ) {
			String name = queryMementos[i].getString("name");//$NON-NLS-1$
			XPathQuery child = new XPathQuery(queryMementos[i], server);
			children.put(name, child);
			child.setCategory(this);
		}
	}

	public String getName() { return this.name; }
	public IServer getServer() { return this.server; }

	public boolean queriesLoaded() {
		return children != null;
	}

	/* 
	 * Lazily load the queries upon request 
	 */
	public XPathQuery[] getQueries() {
		return children.values().toArray(new XPathQuery[children.size()]);
	}

	public XPathQuery getQuery(String name) {
		getQueries();
		return children.get(name);
	}
	public void addQuery(XPathQuery query) {
		getQueries();
		children.put(query.getName(), query);
		query.setCategory(this);
	}
	protected void renameQuery(String oldName, String newName) {
		getQueries();
		XPathQuery q = children.get(oldName);
		if( q != null ) {
			children.remove(oldName);
			children.put(newName, q);
		}
	}
	public void removeQuery(XPathQuery query) {
		getQueries();
		children.remove(query.getName());
	}
	
	public boolean isLoaded() {
		return children == null ? false : true;
	}
	
	public void clearCache() {
		if( isLoaded()) {
			XPathQuery[] allQueries = getQueries();
			for( int i = 0; i < allQueries.length; i++ ) {
				allQueries[i].clearCache();
			}
		}
	}

	/*
	 * Save these queries to its server object
	 */
	public void save() {
		XPathModel.getDefault().save(server); 
	}
}
