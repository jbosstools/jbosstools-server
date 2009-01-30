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

import org.eclipse.core.runtime.IPath;

/**
 * The super type of all package nodes (IPackage, IPackageFileSet, IPackageFolder)
 * 
 * Each node in a package may have arbitrary properties that can be reflected upon by other plug-ins
 * 
 * @author <a href="marshall@jboss.org">Marshall Culpepper</a>
 * @author <a href="rob.stryker@redhat.com">Rob Stryker</a>
 * @version $Revision: 1929 $
 */
public interface IArchiveNode {
	/**
	 * The node type that represents the model
	 */
	public static final int TYPE_MODEL_ROOT = -1;

	/**
	 * The node type that represents an IArchive
	 */
	public static final int TYPE_ARCHIVE = 0;
	
	/**
	 * The node type that represents an IArchiveReference
	 */
	public static final int TYPE_ARCHIVE_REFERENCE = 1;
	
	/**
	 * The node type that represents an IArchiveFileSet
	 */
	public static final int TYPE_ARCHIVE_FILESET = 2;
	
	/**
	 * The node type that represents an IActionFolder
	 */
	public static final int TYPE_ARCHIVE_FOLDER = 3;
	
	/**
	 * The node type that represents an IArchiveAction
	 */
	public static final int TYPE_ARCHIVE_ACTION = 4;

	
	/**
	 * @return The parent of this package node, or null if this node is top level
	 */
	public IArchiveNode getParent();
	
	/**
	 * Set the parent of this package node 
	 * @param parent The new parent of this node
	 */
	public void setParent(IArchiveNode parent);
	
	/**
	 * @param type TYPE_PACKAGE, TYPE_PACKAGE_FILESET, or TYPE_PACKAGE_FOLDER
	 * @return An array of child nodes of the passed in type
	 */
	public IArchiveNode[] getChildren(int type);
	
	/**
	 * @return An array of all children nodes
	 */
	public IArchiveNode[] getAllChildren();
	
	/**
	 * @return Whether or not this node has children
	 */
	public boolean hasChildren();
	
	/**
	 * @param child A possible child node
	 * @return Whether or not the passed-in node is a child of this node
	 */
	public boolean hasChild(IArchiveNode child);
	
	/**
	 * @return The type of this package node
	 */
	public int getNodeType();
	
	/**
	 * @param property The name of the property to fetch
	 * @return The value of the specified property
	 */
	public String getProperty(String property);
	
	/**
	 * Set a property on this package node
	 * @param property The name of the property to set
	 * @param value The new value of the property
	 */
	public void setProperty(String property, String value);
	
	/**
	 * @return The project that this node is defined in (not necessarily the project where this is based if this is a fileset)
	 */
	public IPath getProjectPath();
	
	/**
	 * @return the name of the project this node is defined in
	 */
	public String getProjectName();
	
	/**
	 * Recursively visit the package node tree below this node with the passed-in package node visitor.
	 * @param visitor A package node visitor
	 * @return Whether or not the entire sub-tree was visited
	 */
	public boolean accept(IArchiveNodeVisitor visitor);
	
	/**
	 * Recursively visit the package node tree below this node with the passed-in package node visitor, using depth-first ordering
	 * @param visitor A package node visitor
	 * @return Whether or not the entire sub-tree was visited
	 */
	public boolean accept(IArchiveNodeVisitor visitor, boolean depthFirst);
	
	/**
	 * Add a child node to this node
	 * @param child The child to add
	 */
	public void addChild(IArchiveNode child) throws ArchivesModelException;
	
	/**
	 * Remove a child node from this node
	 * @param child The child to remove
	 */
	public void removeChild(IArchiveNode child);
	
	/**
	 * Get the highest parent that is not null. 
	 * @return
	 */
	public IArchiveNode getRoot();
	
	/**
	 * Get the model this node is attached to, or null if none
	 * @return
	 */
	public IArchiveModelRootNode getModelRootNode();

	/**
	 * Get the descriptor version that this node is using.
	 * If the node is unattached to a descriptor object,
	 * use the latest descriptor version.
	 */
	public double getDescriptorVersion();
		
	/**
	 * Get the path relative to the root archive,
	 * or null if not applicable. 
	 * @return
	 */
	public IPath getRootArchiveRelativePath();
	
	/**
	 * Get the root top-level package for this node
	 * @return
	 */
	public IArchive getRootArchive();

	/**
	 * Get the current delta
	 * @return
	 */
	public IArchiveNodeDelta getDelta();

	/**
	 * Clear the current delta
	 * @return
	 */
	public void clearDelta();
	
	/**
	 * Validating the model
	 * @return true if it's ok, false if it should fail
	 */
	public boolean validateModel();
	
	/**
	 * Are all of the fields here accessible to be built?
	 * @throws AssertionFailedException with what's wrong
	 */
	public boolean canBuild();
}
