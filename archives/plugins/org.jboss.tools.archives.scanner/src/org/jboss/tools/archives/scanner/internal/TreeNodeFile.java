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
package org.jboss.tools.archives.scanner.internal;

import java.io.File;

import org.jboss.tools.archives.scanner.ITreeNode;

/**
 * A wrapper for java.io.File to force it to implement ITreeNode.
 * 
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TreeNodeFile extends java.io.File implements ITreeNode {

	private static final long serialVersionUID = 60592058953093602L;

	public TreeNodeFile(File parent) {
		super(parent.getAbsolutePath());
	}
	
	public TreeNodeFile(File parent, String child) {
		super(parent, child);
	}

	public boolean isLeaf() {
		return !isDirectory();
	}

	public ITreeNode getChild(String name) {
		return new TreeNodeFile(this, name);
	}

	public ITreeNode[] listChildren() {
		String[] children = list();
		TreeNodeFile[] ret = new TreeNodeFile[children.length];
		for( int i = 0; i < children.length; i++ ) {
			ret[i] = new TreeNodeFile(this, children[i]);
		}
		return ret;
	}
}
