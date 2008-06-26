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

import java.util.HashMap;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;

/**
 * A class representing an XPath Category, which 
 * is owned by a server and has XPath queries in it
 * @author rob.stryker@redhat.com
 *
 */
public class XPathCategory {
	protected String name; // cannot include delimiter from the model, comma
	protected IServer server;
	protected HashMap<String, XPathQuery> children;
	
	public XPathCategory(String name, IServer server) {
		this.name = name;
		this.server = server;
	}
	
	public String getName() { return this.name; }
	
	public boolean queriesLoaded() {
		return children != null;
	}

	/* 
	 * Lazily load the queries upon request 
	 */
	public XPathQuery[] getQueries() {
		if( children == null ) {
			children = new HashMap<String, XPathQuery>();
			XPathQuery[] queries = XPathModel.getDefault().loadQueries(this, server);
			for( int i = 0; i < queries.length; i++ ) {
				children.put(queries[i].getName(), queries[i]);
			}
		}
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
	
	public void removeQuery(XPathQuery query) {
		getQueries();
		children.remove(query.getName());
	}
	
	/*
	 * Save these queries to its server object
	 */
	public void save() {
		ServerAttributeHelper helper = ServerAttributeHelper.createHelper(server);
		XPathModel.getDefault().saveCategory(this, server, helper); 
		helper.save();
	}
}
