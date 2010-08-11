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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
import org.jboss.tools.jmx.core.IMemento;

/**
 * A class representing an XPath Category, which 
 * is owned by a server and has XPath queries in it
 * @author rob.stryker@redhat.com
 *
 */
public class XPathCategory {
	@Deprecated private static final String DELIMITER = ","; //$NON-NLS-1$
	@Deprecated private static final String QUERY_LIST = 
		"org.jboss.ide.eclipse.as.core.model.descriptor.QueryList";	 //$NON-NLS-1$
	@Deprecated private static final String QUERY = 
		"org.jboss.ide.eclipse.as.core.model.descriptor.Query";	 //$NON-NLS-1$

	
	protected String name; // cannot include delimiter from the model, comma
	protected IServer server;
	protected IMemento memento;
	protected HashMap<String, XPathQuery> children;
	
	@Deprecated
	public XPathCategory(String name, IServer server) {
		this.name = name;
		this.server = server;
		children = new HashMap<String, XPathQuery>();
		XPathQuery[] queries = loadQueries_LEGACY(this, server);
		for( int i = 0; i < queries.length; i++ ) {
			children.put(queries[i].getName(), queries[i]);
		}
	}
	
	private static XPathQuery[] loadQueries_LEGACY(XPathCategory category, IServer server) {
		ServerAttributeHelper helper = ServerAttributeHelper.createHelper(server);
		String list = helper.getAttribute(QUERY_LIST + '.' + category.getName().replace(' ', '_'), (String)null);
		if( list == null )
			return new XPathQuery[] {};
		String[] queriesByName = list.split(DELIMITER);
		List<String> queryAsStringValues;
		ArrayList<XPathQuery> returnList = new ArrayList<XPathQuery>();
		for( int i = 0; i < queriesByName.length; i++ ) {
			queryAsStringValues = helper.getAttribute(QUERY + '.' + queriesByName[i].replace(' ', '_'), (List)null);
			if( queryAsStringValues != null ) {
				XPathQuery q =new XPathQuery(queriesByName[i].substring(queriesByName[i].indexOf(Path.SEPARATOR)+1), queryAsStringValues); 
				q.setCategory(category);
				returnList.add(q);
			}
		}
		return (XPathQuery[]) returnList.toArray(new XPathQuery[returnList.size()]);
	}
	
	public XPathCategory(IServer server, IMemento memento) {
		this.server = server;
		this.name = memento.getString("name"); //$NON-NLS-1$
		IMemento[] queryMementos = memento.getChildren("query");//$NON-NLS-1$
		children = new HashMap<String, XPathQuery>();
		for( int i = 0; i < queryMementos.length; i++ ) {
			String name = queryMementos[i].getString("name");//$NON-NLS-1$
			XPathQuery child = new XPathQuery(queryMementos[i]);
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
	
	/*
	 * Save these queries to its server object
	 */
	public void save() {
		XPathModel.getDefault().save(server); 
	}
}
