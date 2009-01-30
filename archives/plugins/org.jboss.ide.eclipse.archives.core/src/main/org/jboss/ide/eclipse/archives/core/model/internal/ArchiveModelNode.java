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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelException;
import org.jboss.ide.eclipse.archives.core.model.EventManager;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModel;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModelRootNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNodeDelta;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XMLBinding;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackages;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XMLBinding.XbException;

/**
 * 
 * @author rob.stryker <rob.stryker@redhat.com>
 *
 */
public class ArchiveModelNode extends ArchiveNodeImpl implements IArchiveModelRootNode {
	private IPath project;
	private IPath descriptor;
	private IArchiveModel model;
	
	public ArchiveModelNode(IPath project, XbPackages node) {
		this(project, null, node);
	}
	
	public ArchiveModelNode(IPath project, IPath descriptor, XbPackages node) {
		this(project, descriptor, node, null);
	}
	
	public ArchiveModelNode(IPath project, XbPackages node, IArchiveModel model) {
		this(project, null, node, model);
	}
	
	public ArchiveModelNode(IPath project, IPath descriptor,
			XbPackages node, IArchiveModel model) {
		super(node);
		this.project = project;
		this.descriptor = descriptor != null ? descriptor : 
				project.append(IArchiveModel.DEFAULT_PACKAGES_FILE);
		this.model = model;
	}
	
	public IPath getDescriptor() {
		return descriptor;
	}
	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveModelNode#getManager()
	 */
	public IArchiveModel getModel() {
		return model;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.internal.ArchiveNodeImpl#getProject()
	 */
	public IPath getProjectPath() {
		return project;
	}
	
	public XbPackages getXbPackages() {
		return (XbPackages)nodeDelegate;
	}
	
	/**
	 * The model root can only accept IArchive's as children
	 * @see IArchiveNode#addChild(IArchiveNode)
	 */
	protected boolean validateChild(IArchiveNode child) {
		if( child.getNodeType() != IArchiveNode.TYPE_ARCHIVE)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveNode#getNodeType()
	 */
	public int getNodeType() {
		return IArchiveNode.TYPE_MODEL_ROOT;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.internal.ArchiveNodeImpl#getRoot()
	 */
	public IArchiveNode getRoot() {
		return this;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.internal.ArchiveNodeImpl#connectedToModel()
	 */
	public boolean connectedToModel() {
		return getModel() != null;
	}

	/**
	 * No parent allowed for a model node
	 * @see IArchiveNode#setParent(IArchiveNode)
	 */
	public IArchiveNode getParent() {
		return null;
	}

	/**
	 * No parent allowed for a model node
	 * @see IArchiveNode#setParent(IArchiveNode)
	 */
	public void setParent(IArchiveNode parent) {
	}

	public void setModel(IArchiveModel model) {
		this.model = model;
	}
	
	public double getDescriptorVersion() {
		return ((XbPackages)getNodeDelegate()).getVersion();
	}
	
	public void setDescriptorVersion(double d) {
		((XbPackages)getNodeDelegate()).setVersion(d);
	}
	
	/**
	 * I have no relative path. I'm above the root archive
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveNode#getRootArchiveRelativePath()
	 */
	public IPath getRootArchiveRelativePath() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveModelNode#save(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void save(IProgressMonitor monitor) throws ArchivesModelException {
		if( monitor == null )
			monitor = new NullProgressMonitor();
		XbPackages packs = (XbPackages)getNodeDelegate();
		try {
			XMLBinding.marshallToFile(packs, getDescriptor(), monitor);
		} catch( XbException xbe ) {
			throw new ArchivesModelException(xbe);
		}
		IArchiveNodeDelta delta = getDelta();
		clearDelta();
		EventManager.fireDelta(delta);
	}
	
	public boolean validateModel() {
		if( getChildren(IArchiveNode.TYPE_ARCHIVE).length < getAllChildren().length)
			return false;
		ArrayList<IPath> list = new ArrayList<IPath>();
		IArchiveNode[] children = getChildren(IArchiveNode.TYPE_ARCHIVE);
		IArchive child;
		IPath p;
		for( int i = 0; i < children.length; i++ ) {
			child = (IArchive)children[i];
			
			if( child.getArchiveFilePath() != null )
				p = child.getArchiveFilePath();
			else 
				return false;
			
			if( list.contains(p))
				return false;
			else
				list.add(p);
		}

		return super.validateModel();
	}
}
