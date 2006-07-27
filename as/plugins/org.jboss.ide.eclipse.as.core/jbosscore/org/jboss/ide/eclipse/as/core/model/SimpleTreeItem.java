package org.jboss.ide.eclipse.as.core.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEvent;

public class SimpleTreeItem {
	
	protected ArrayList children;
	protected HashMap properties;
	protected SimpleTreeItem parent;
	
	protected Object data;
	
	public SimpleTreeItem(SimpleTreeItem parent, Object data) {
		children = new ArrayList();
		properties = new HashMap();
		this.data = data;
		this.parent = parent;
		if( parent != null ) parent.addChild(this);
	}	
	
	public SimpleTreeItem[] getChildren2() {
		SimpleTreeItem[] arr = new SimpleTreeItem[children.size()];
		children.toArray(arr);
		return arr;
	}
	
	public SimpleTreeItem getParent() {
		return parent;
	}
	
	public void addChild(SimpleTreeItem item) {
		if( !children.contains(item)) {
			children.add(item);
			item.setParent(this);
		}
	}
	
	public void addChild(int loc, SimpleTreeItem item) {
		children.add(loc, item);
	}
		
	public void deleteChildren() {
		children.clear();
	}

	public void deleteChild(SimpleTreeItem o) {
		children.remove(o);
	}

	
	public void setProperty( Object key, Object val ) {
		properties.put(key, val); 
	}
	
	public Object getProperty(Object key) {
		return properties.get(key);
	}
	
	public HashMap getProperties() {
		return properties;
	}

	public void setParent(SimpleTreeItem parent) {
		this.parent = parent;
	}
	
	public Object getData() {
		return this.data;
	}
	
	public void setData(Object data) {
		this.data = data;
	}
	
	public void addChildren(SimpleTreeItem[] kids) {
		for( int i = 0; i < kids.length; i++ ) {
			addChild(kids[i]);
		}
	}

}
