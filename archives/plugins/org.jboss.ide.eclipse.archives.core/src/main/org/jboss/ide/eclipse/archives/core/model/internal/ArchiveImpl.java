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
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveAction;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveType;
import org.jboss.ide.eclipse.archives.core.model.INamedContainerArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackage;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbPackages;
import org.jboss.ide.eclipse.archives.core.util.PathUtils;

/**
 * An archive
 *
 * @author Rob stryker
 */
public class ArchiveImpl extends ArchiveNodeImpl implements IArchive {

	private XbPackage packageDelegate;

	public ArchiveImpl() {
		this(new XbPackage());
	}
	public ArchiveImpl(XbPackage delegate) {
		super(delegate);
		this.packageDelegate = delegate;
	}

	/*
	 * @see IArchiveNode#getNodeType()
	 */
	public int getNodeType() {
		return TYPE_ARCHIVE;
	}

	/*
	 * @see IArchive#isDestinationInWorkspace()
	 */
	public boolean isDestinationInWorkspace() {
		return packageDelegate.isInWorkspace();
	}

	public String getRawDestinationPath() {
		if( !isTopLevel() )
			return ""; //$NON-NLS-1$
		return packageDelegate.getToDir() == null ? "" : packageDelegate.getToDir(); //$NON-NLS-1$
	}


	/*
	 * @see IArchive#getArchiveFilePath()
	 */
	public IPath getArchiveFilePath() {
		IPath p = PathUtils.getGlobalLocation(this);
		return p == null ? null : p.append(getName());
	}

	/*
	 * @see IArchive#getFileSets()
	 */
	public IArchiveFileSet[] getFileSets() {
		IArchiveNode nodes[] = getChildren(TYPE_ARCHIVE_FILESET);
		IArchiveFileSet filesets[] = new IArchiveFileSet[nodes.length];
		System.arraycopy(nodes, 0, filesets, 0, nodes.length);
		return filesets;
	}

	/*
	 * @see IArchive#getFolders()
	 */
	public IArchiveFolder[] getFolders() {
		IArchiveNode nodes[] = getChildren(TYPE_ARCHIVE_FOLDER);
		IArchiveFolder folders[] = new IArchiveFolder[nodes.length];
		System.arraycopy(nodes, 0, folders, 0, nodes.length);
		return folders;
	}

	/*
	 * @see IArchive#getArchives()
	 */
	public IArchive[] getArchives() {
		IArchiveNode nodes[] = getChildren(TYPE_ARCHIVE);
		IArchive pkgs[] = new IArchive[nodes.length];
		System.arraycopy(nodes, 0, pkgs, 0, nodes.length);
		return pkgs;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchive#getActions()
	 */
	public IArchiveAction[] getActions() {
		IArchiveNode nodes[] = getChildren(TYPE_ARCHIVE_ACTION);
		IArchiveAction actions[] = new IArchiveAction[nodes.length];
		System.arraycopy(nodes, 0, actions, 0, nodes.length);
		return actions;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchive#getPreActions()
	 */
	public IArchiveAction[] getPreActions() {
		return new IArchiveAction[0]; // TODO getPreActions
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchive#getPostActions()
	 */
	public IArchiveAction[] getPostActions() {
		return new IArchiveAction[0]; // TODO getPostActions
	}

	/*
	 * @see IArchive#getName()
	 */
	public String getName() {
		return packageDelegate.getName();
	}

	/*
	 * @see IArchive#getArchiveType()
	 */
	public IArchiveType  getArchiveType() {
		return ArchivesCore.getInstance().getExtensionManager().getArchiveType(packageDelegate.getPackageType());
	}

	/*
	 * @see IArchive#isExploded()
	 */
	public boolean isExploded() {
		return packageDelegate.isExploded();
	}

	/*
	 * @see IArchive#isTopLevel()
	 */
	public boolean isTopLevel() {
		return (packageDelegate.getParent() == null ||
				packageDelegate.getParent() instanceof XbPackages);
	}

	/*
	 * @see IArchive#setDestinationPath(IPath, boolean)
	 */
	public void setDestinationPath(IPath path) {
		String destPath = getRawDestinationPath();
		attributeChanged(DESTINATION_ATTRIBUTE, destPath == null ? null : destPath.toString(), path == null ? null : path.toString());
		packageDelegate.setToDir(path.toString());
	}

	/*
	 * @see IArchive#setInWorkspace(boolean)
	 */
	public void setInWorkspace(boolean inWorkspace) {
		attributeChanged(IN_WORKSPACE_ATTRIBUTE, new Boolean(isDestinationInWorkspace()), new Boolean(inWorkspace));
		packageDelegate.setInWorkspace(inWorkspace);
	}

	/*
	 * @see IArchive#setExploded(boolean)
	 */
	public void setExploded(boolean exploded) {
		attributeChanged(EXPLODED_ATTRIBUTE, new Boolean(isExploded()), new Boolean(exploded));
		packageDelegate.setExploded(exploded);
	}

	/*
	 * @see IArchive#setName(String)
	 */
	public void setName(String name) {
		attributeChanged(NAME_ATTRIBUTE, getName(), name);
		packageDelegate.setName(name);
	}

	/*
	 * @see IArchive#setArchiveType(IArchiveType)
	 */
	public void setArchiveType(IArchiveType type) {
		attributeChanged(PACKAGE_TYPE_ATTRIBUTE, getArchiveTypeId(), type == null ? null : type.getId());
		packageDelegate.setPackageType(type.getId());
	}

	/*
	 * @see IArchive#setArchiveType(String)
	 */
	public void setArchiveType(String type) {
		attributeChanged(PACKAGE_TYPE_ATTRIBUTE, getArchiveTypeId(), type);
		packageDelegate.setPackageType(type);
	}

	public String toString() {
		return getName();
	}

	/*
	 * @see IArchive#getArchiveTypeId()
	 */
	public String getArchiveTypeId() {
		return packageDelegate.getPackageType();
	}

	/*
	 * @see IArchiveNode#getRootArchiveRelativePath()
	 */
	public IPath getRootArchiveRelativePath() {
		if( getParent() == null || getParent().getRootArchiveRelativePath() == null )
			return new Path(getName());
		return getParent().getRootArchiveRelativePath().append(getName());
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.internal.ArchiveNodeImpl#validateChild(org.jboss.ide.eclipse.archives.core.model.IArchiveNode)
	 */
	public boolean validateModel() {
		ArrayList<String> list = new ArrayList<String>();
		IArchiveNode[] children = getAllChildren();
		for( int i = 0; i < children.length; i++ ) {
			if( children[i] instanceof INamedContainerArchiveNode) {
				if( list.contains(((INamedContainerArchiveNode)children[i]).getName()))
						return false;
				else
					list.add(((INamedContainerArchiveNode)children[i]).getName());
			}
		}
		return super.validateModel();
	}

	public boolean canBuild() {
		return PathUtils.getGlobalLocation(this) != null && super.canBuild();
	}
}
