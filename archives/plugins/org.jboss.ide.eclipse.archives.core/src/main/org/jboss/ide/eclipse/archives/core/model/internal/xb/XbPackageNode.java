/*
 * JBoss, a division of Red Hat
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
package org.jboss.ide.eclipse.archives.core.model.internal.xb;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public abstract class XbPackageNode implements Cloneable {
	
	protected XbPackageNode parent;
	
	/**
	 * The children are a class -> arraylist[child] map hashmap
	 */
	protected Hashtable children;
	
	public XbPackageNode ()
	{
		children = new Hashtable();
	}
	
	public XbPackageNode(XbPackageNode node)
	{
		children = new Hashtable();
		Object key;
		for( Iterator i = node.children.keySet().iterator(); i.hasNext(); ) {
			key = i.next();
			children.put(key, ((ArrayList)node.children.get(key)).clone());
		}
	}
	
	public void addChild (Object object)
	{
		addChild((XbPackageNode)object);
	}
	
	public void addChild (XbPackageNode child)
	{
		if (!children.containsKey(child.getClass()))
		{
			children.put(child.getClass(), new ArrayList());
		}
		getChildren(child.getClass()).add(child);
		child.setParent(this);
	}
	
	public void removeChild (XbPackageNode child)
	{
		if (children.containsKey(child.getClass()))
		{
			getChildren(child.getClass()).remove(child);
		}
	}
	
	public List getChildren(Class type)
	{
		return (List)children.get(type);
	}
	
	public boolean hasChildren ()
	{
		return children != null && children.size() > 0;
	}
	
	public List getAllChildren()
	{
		ArrayList allChildren = new ArrayList();
		
		for (Iterator iter = children.keySet().iterator(); iter.hasNext();)
		{
			Class childType = (Class) iter.next();
			allChildren.addAll(getChildren(childType));
		}
		return allChildren;
	}
	
	public XbPackageNode getParent()
	{
		return parent;
	}
	
	public void setParent (XbPackageNode parent)
	{
		this.parent = parent;
	}
}
