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
package org.jboss.ide.eclipse.as.core.util;

import java.util.ArrayList;
import java.util.HashMap;

public class SimpleTreeItem {
	
	protected ArrayList<SimpleTreeItem> children;
	protected HashMap<Object, Object> properties;
	protected SimpleTreeItem parent;
	
	protected Object data;
	
	public SimpleTreeItem(SimpleTreeItem parent, Object data) {
		children = new ArrayList<SimpleTreeItem>();
		properties = new HashMap<Object, Object>();
		this.data = data;
		this.parent = parent;
		if( parent != null ) parent.addChild(this);
	}	
	
	public SimpleTreeItem[] getChildren() {
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
		
	public void addChildren(SimpleTreeItem[] kids) {
		for( int i = 0; i < kids.length; i++ ) {
			addChild(kids[i]);
		}
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
	
	public HashMap<Object, Object> getProperties() {
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
		
	public SimpleTreeItem getRoot() {
		SimpleTreeItem item = this;
		while(item.getParent() != null)
			item = item.getParent();
		return item;
	}

}
