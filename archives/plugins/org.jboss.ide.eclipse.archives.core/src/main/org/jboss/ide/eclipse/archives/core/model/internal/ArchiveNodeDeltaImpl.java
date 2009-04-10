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
package org.jboss.ide.eclipse.archives.core.model.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelException;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta;

/**
 * This class represents changed nodes in the model
 * @author <a href="rob.stryker@redhat.com">Rob Stryker</a>
 *
 */
public class ArchiveNodeDeltaImpl implements IArchiveNodeDelta {
	
	private IArchiveNodeDelta parentDelta;
	private IArchiveNode postNode, preNode;
	private HashMap attributes, properties, children;
	private int kind;
	private IArchiveNodeDelta[] childrenDeltas;
	
	/**
	 * Primary constructor
	 * @param parentDelta
	 * @param impl
	 * @param attributeChanges
	 * @param propertyChanges
	 * @param childChanges
	 */
	public ArchiveNodeDeltaImpl(IArchiveNodeDelta parentDelta, ArchiveNodeImpl impl, 
			HashMap attributeChanges, HashMap propertyChanges, HashMap childChanges) {
		this.parentDelta = parentDelta;
		postNode = impl;
		kind = 0;
		properties = propertyChanges;
		attributes = attributeChanges; 
		children = childChanges;
		
		// These three lines adjust my "kind" to be accurate
		ensureAccurateKind();
		
		// create *my* pre-node
		// this creates an accurate "old" node but without ANY children at all.
		preNode = ArchivesCore.getInstance().getNodeFactory()
					.createDeltaNode(parentDelta, postNode, 
						attributeChanges, propertyChanges);
		
		// TODO could log if preNode is null here? 
		// This could be null in the other constructor, but *not* here. 
		// A null here would indicate an incomplete implementation for a node type
		
		// The children are expected to be added in the loadAllAffectedChildren
		loadAllAffectedChildren();
	}
	
	/**
	 * Constructor that forces a child to be added or removed, as judged by the parent
	 * @param parentDelta
	 * @param impl
	 * @param forcedKind
	 * @param attributeChanges
	 * @param propertyChanges
	 * @param childChanges
	 */
	public ArchiveNodeDeltaImpl(IArchiveNodeDelta parentDelta, ArchiveNodeImpl impl, 
			int forcedKind, HashMap attributeChanges, 
			HashMap propertyChanges, HashMap childChanges) {
		this(parentDelta, impl, attributeChanges, propertyChanges, childChanges);
		kind = kind | forcedKind; // pre-gaming 
		
		// but if I'm added, I have no pre-node, no changes. All NEW
		if( (kind & IArchiveNodeDelta.ADDED) == IArchiveNodeDelta.ADDED ) {
			preNode = null;
			kind = IArchiveNodeDelta.ADDED;
			attributes.clear();
			properties.clear();
		}

	}
	
	protected IArchiveNodeDelta getParentDelta() {
		return parentDelta;
	}
	
	protected void ensureAccurateKind() {
		
		// Properties First
		Object key;
		NodeDelta val;
		for( Iterator i = properties.keySet().iterator(); i.hasNext(); ) {
			key = i.next();
			val = (NodeDelta)properties.get(key);
			kind = kind | val.getKind();
		}
		
		// Attributes Second
		if( attributes.keySet().size() > 0 )
			kind = kind | ATTRIBUTE_CHANGED;

		/*
		 * Children third.
		 * 
		 * The changed children are saved in a hashmap
		 * Node -> Integer  (where int is one of 
		 * IPackagesModelDelta.CHILD_ADDED or
		 * IPackagesModelDelta.CHILD_REMOVED 
		 */
		Integer val2;
		for( Iterator i = children.keySet().iterator(); i.hasNext(); ) {
			key = i.next();
			val2 = (Integer)children.get(key);
			if( val2 != null )
				kind = kind | val2.intValue();
		}
	}
	
	
	// Forced during constructor, will set the flag for CHILD_CHANGED if a child has changed at all.
	public IArchiveNodeDelta[] getAllAffectedChildren() {
		if( childrenDeltas == null ) {
			loadAllAffectedChildren();
		}
		return childrenDeltas;
	}

	private void loadAllAffectedChildren() {
		ArrayList priorChildren = new ArrayList();

		// first add the deltas for things that are currently our children
		// this includes items that haven't been changed, and items that were added
		IArchiveNode[] children = postNode.getAllChildren();
		IArchiveNodeDelta delta;
		ArrayList deltas = new ArrayList();
		for( int i = 0; i < children.length; i++ ) {
			// create our child delta before evaluating whether or not to add it
			delta = getDelta(children[i]);
			if( delta.getKind() != IArchiveNodeDelta.NO_CHANGE ) {
				deltas.add(delta);
				if( ((delta.getKind() & IArchiveNodeDelta.ADDED) == 0 ) &&
					((delta.getKind() & IArchiveNodeDelta.REMOVED) == 0)){
					kind = kind | DESCENDENT_CHANGED;
				}
			}

			// add ALL current nodes, then later remove the added ones
			priorChildren.add(delta.getPreNode());
		}
		
		// now handle the removed ones
		ArchiveNodeImpl node;
		for(Iterator i = this.children.keySet().iterator(); i.hasNext(); ) {
			node = (ArchiveNodeImpl)i.next();
			int v = ((Integer)this.children.get(node)).intValue();
			
			if( v == IArchiveNodeDelta.CHILD_REMOVED) {
				delta = getDelta(node);
				deltas.add(delta);
				priorChildren.add(delta.getPreNode());
			} else if( v == IArchiveNodeDelta.CHILD_ADDED) {
				delta = getDelta(node);
				priorChildren.remove(delta.getPreNode());
			}
		}
		
		if( preNode != null ) {
			// now we've got our list of current children... set them. 
			for( Iterator i = priorChildren.iterator(); i.hasNext(); ) {
				try {
					preNode.addChild((IArchiveNode)i.next());
				} catch( ArchivesModelException ame) {
					// DO nothing
				}
			}
			// now clear pre-node's deltas so it looks shiny
			preNode.clearDelta();
		}
		
		childrenDeltas = (IArchiveNodeDelta[]) deltas.toArray(new IArchiveNodeDelta[deltas.size()]);
	}

	
	private HashMap deltaMap = new HashMap();
	// local cache
	private IArchiveNodeDelta getDelta(IArchiveNode child) {
		if( !deltaMap.containsKey(child)) {
			deltaMap.put(child, loadDelta(child));
		}
		return (IArchiveNodeDelta)deltaMap.get(child);
	}
	private IArchiveNodeDelta loadDelta(IArchiveNode child) {
		if( child instanceof ArchiveNodeImpl ) {
			int addedOrRemoved = 0;
			if( children.containsKey(child)) {
				addedOrRemoved = ((Integer)children.get(child)).intValue() >> 8;
			}
			ArchiveNodeImpl impl = (ArchiveNodeImpl)child;
			
			// Using a different delta constructor here to force 
			// whether this child is added or removed. 
			return new ArchiveNodeDeltaImpl(this, impl, addedOrRemoved,
					(HashMap)impl.attributeChanges.clone(), 
					(HashMap)impl.propertyChanges.clone(), 
					(HashMap)impl.childChanges.clone());
		}
		return child.getDelta();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta#getKind()
	 */
	public int getKind() {
		return kind;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta#getPostNode()
	 */
	public IArchiveNode getPostNode() {
		return postNode;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta#getPreNode()
	 */
	public IArchiveNode getPreNode() {
		return preNode;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta#getAttributesWithDeltas()
	 */
	public String[] getAttributesWithDeltas() {
		Collection atts = attributes.keySet();
		return (String[]) atts.toArray(new String[atts.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta#getAttributeDelta(java.lang.String)
	 */
	public INodeDelta getAttributeDelta(String key) {
		return (INodeDelta)attributes.get(key);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta#getPropertiesWithDeltas()
	 */
	public String[] getPropertiesWithDeltas() {
		Collection atts = properties.keySet();
		return (String[]) atts.toArray(new String[atts.size()]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta#getPropertyDelta(java.lang.String)
	 */
	public INodeDelta getPropertyDelta(String key) {
		return (INodeDelta)properties.get(key);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta#getAddedChildrenDeltas()
	 */
	public IArchiveNodeDelta[] getAddedChildrenDeltas() {
		return getChangedChildren(IArchiveNodeDelta.ADDED);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta#getRemovedChildrenDeltas()
	 */
	public IArchiveNodeDelta[] getRemovedChildrenDeltas() {
		return getChangedChildren(IArchiveNodeDelta.REMOVED);
	}

	public IArchiveNodeDelta[] getChangedDescendentDeltas() {
		return getChangedChildren(IArchiveNodeDelta.DESCENDENT_CHANGED);
	}

	private IArchiveNodeDelta[] getChangedChildren(int type) {
		ArrayList list = new ArrayList();
		for( int i = 0; i < childrenDeltas.length; i++ ) {
			if( (childrenDeltas[i].getKind() & type) != 0 ) {
				list.add(childrenDeltas[i]);
			}
		}
		return (IArchiveNodeDelta[]) list.toArray(new IArchiveNodeDelta[list.size()]);
	}
	
	/**
	 * A quick and dirty class to keep track of changing
	 * values between saves in a model. 
	 * Used for property changes and attribute changes
	 * @author rstryker
	 *
	 */
	protected static class NodeDelta implements INodeDelta {
		private int kind;
		private Object before, after;
		public NodeDelta(Object before, Object after, int kind) {
			this.before = before;
			this.after = after;
			this.kind = kind;
		}
		public Object getBefore() { return before; }
		public Object getAfter() { return after; }
		public int getKind() {
			return kind;
		}
	}

}
