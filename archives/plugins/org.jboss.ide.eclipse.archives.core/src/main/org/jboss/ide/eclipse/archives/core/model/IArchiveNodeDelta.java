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
package org.jboss.ide.eclipse.archives.core.model;

/**
 * Node delta interface
 * @author <a href="rob.stryker@redhat.com">Rob Stryker</a>
 *
 */
public interface IArchiveNodeDelta {
	
	/**
	 * There is no change to this node or any of it's children
	 */
	public static final int NO_CHANGE = 0;
		
	/**
	 * I have been added
	 */
	public static final int ADDED = 0x1;
	
	/**
	 * I have been removed
	 */
	public static final int REMOVED	= 0x2;
	
	
	
	/**
	 * Used to designate that a sub-property within 
	 * a <property> tag has been added.
	 */
	public static final int PROPERTY_ADDED = 0x10;
	
	/**
	 * Used to designate that a sub-property within 
	 * a <property> tag has been removed.
	 */
	public static final int PROPERTY_REMOVED = 0x20;
	
	/**
	 * Used to designate that a sub-property within 
	 * a <property> tag has been changed.
	 */
	public static final int PROPERTY_CHANGED = 0x40;
	
	/**
	 * Used to designate that an primary property of the node, 
	 * such as inWorkspace or exploded, has changed. 
	 */
	public static final int ATTRIBUTE_CHANGED = 0x80;
	
	/**
	 * A child has been added directly to me
	 */
	public static final int CHILD_ADDED		= 0x100;
	
	/**
	 * A child has been removed directly from me
	 */
	public static final int CHILD_REMOVED	= 0x200;
	
	/**
	 * Some other change has occurred, most likely a 
	 * grand-child added or a child's property changed.
	 */
	public static final int DESCENDENT_CHANGED 	= 0x400;
	
	/**
	 * The node was registered with a model
	 */
	public static final int NODE_REGISTERED = 0x800;

	/**
	 * The node was unregistered with a model
	 */
	public static final int NODE_UNREGISTERED = 0x1000;
	
	/**
	 * An unknown change has occurred. This may include
	 * any (or multiple) events and may require an observer
	 * to re-scan the entire node / tree for changes. 
	 */
	public static final int UNKNOWN_CHANGE = 0x1000;

	/**
	 * Return the delta kind
	 * @return
	 */
	public int getKind();

	/**
	 * Return the affected node after changes
	 * @return
	 */
	public IArchiveNode getPostNode();
	
	/**
	 * Return the affected node before changes, or null if the node is an {@link IArchiveModelRootNode}
	 * @return
	 */
	public IArchiveNode getPreNode();
	
	/**
	 * Get a list of property keys for changed properties
	 * @return
	 */
	public String[] getPropertiesWithDeltas();
	
	/**
	 * Get the property node delta for the given property key
	 * @param key
	 * @return
	 */
	public INodeDelta getPropertyDelta(String key);
	
	/**
	 * Get a list of attribute keys for changed attributes
	 * @return
	 */
	public String[] getAttributesWithDeltas();
	
	/**
	 * Get the attribute node delta for a given attribute key
	 * @param key
	 * @return
	 */
	public INodeDelta getAttributeDelta(String key);
	
	/**
	 * Get the array of added children
	 * @return
	 */
	public IArchiveNodeDelta[] getAddedChildrenDeltas();
	
	/**
	 * Get the array of removed Children
	 * @return
	 */
	public IArchiveNodeDelta[] getRemovedChildrenDeltas();
	
	/**
	 * Get only deltas where descendent changed
	 * @return
	 */
	public IArchiveNodeDelta[] getChangedDescendentDeltas();

	/**
	 * Get the children that have been changed
	 * @return
	 */
	public IArchiveNodeDelta[] getAllAffectedChildren();
	
	
	
	public interface INodeDelta {
		public Object getBefore();
		public Object getAfter();
		public int getKind();
	}
	
}
