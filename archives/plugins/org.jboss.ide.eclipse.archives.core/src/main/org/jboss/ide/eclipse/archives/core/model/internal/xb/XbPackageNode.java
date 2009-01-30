/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
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
