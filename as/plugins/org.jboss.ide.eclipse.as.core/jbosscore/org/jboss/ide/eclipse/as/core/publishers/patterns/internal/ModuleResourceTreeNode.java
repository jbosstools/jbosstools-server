/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.publishers.patterns.internal;

import java.util.ArrayList;

import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.jboss.tools.archives.scanner.ITreeNode;

/**
 * A tree node class representing an IModuleResource from the 
 * servertools framework. 
 */
public class ModuleResourceTreeNode implements ITreeNode {
	private IModuleResource resource;
	public ModuleResourceTreeNode(IModuleResource resource) {
		this.resource = resource;
	}
	
	@Override
	public String getName() {
		return resource.getName();
	}

	@Override
	public boolean isLeaf() {
		return !(resource instanceof IModuleFolder);
	}

	@Override
	public ITreeNode getChild(String name) {
		if( resource instanceof IModuleFolder) {
			IModuleResource[] children = ((IModuleFolder)resource).members();
			for( int i = 0; i < children.length; i++ ) {
				if( children[i].getName().equals(name)) {
					return new ModuleResourceTreeNode(children[i]);
				}
			}
		}
		return null;
	}

	@Override
	public ITreeNode[] listChildren() {
		ArrayList<ITreeNode> l = new ArrayList<ITreeNode>();
		if( resource instanceof IModuleFolder) {
			IModuleResource[] children = ((IModuleFolder)resource).members();
			for( int i = 0; i < children.length; i++ ) {
				l.add(new ModuleResourceTreeNode(children[i]));	
			}
		}
		return (ITreeNode[]) l.toArray(new ITreeNode[l.size()]);
	}

}
