package org.jboss.ide.eclipse.as.core.model.descriptor;

import java.util.HashMap;

import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.ServerAttributeHelper;

public class XPathCategory {
	protected String name; // cannot include delimiter from the model, comma
	protected JBossServer server;
	protected HashMap children;
	
	public XPathCategory(String name, JBossServer server) {
		this.name = name;
		this.server = server;
	}
	
	public String getName() { return this.name; }
	
	public boolean queriesLoaded() {
		return children != null;
	}

	public XPathQuery[] getQueries() {
		if( children == null ) {
			children = new HashMap();
			XPathQuery[] queries = XPathModel.getDefault().loadQueries(this, server);
			for( int i = 0; i < queries.length; i++ ) {
				children.put(queries[i].getName(), queries[i]);
			}
		}
		return (XPathQuery[]) children.values().toArray(new XPathQuery[children.size()]);
	}

	public XPathQuery getQuery(String name) {
		getQueries();
		return (XPathQuery)children.get(name);
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
	
	public void save() {
		ServerAttributeHelper helper = server.getAttributeHelper();
		XPathModel.getDefault().saveCategory(this, server, helper); 
		helper.save();
	}
}
