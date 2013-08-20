/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.archives.scanner;

/**
 * An interface to represent a model which can be used 
 * in a VirtualDirectoryScanner
 */
public interface ITreeNode {
	/**
	 * Get the name for this node
	 * @return
	 */
	public String getName();
	
	/**
	 * Nodes are either leaf nodes, or not leaf nodes. 
	 * Leaf nodes are items that can contain children. 
	 * 
	 * @return
	 */
	public boolean isLeaf();
	
	/**
	 * Get a child of this node with the given name, 
	 * or null if not found or this is a leaf node
	 * @param name
	 * @return
	 */
	public ITreeNode getChild(String name);
	
	
	
	/**
	 * Get a list of children nodes
	 * @return
	 */
	public ITreeNode[] listChildren();
}
