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
package org.jboss.ide.eclipse.archives.core.model.internal;

import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.INamedContainerArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbFolder;

/**
 * A PackageFolderImpl.
 * 
 * @author <a href="marshall@jboss.org">Marshall Culpepper</a>
 * @author <a href="rob.stryker@redhat.com">Rob Stryker</a>
 * @version $Revision: 1930 $
 */
public class ArchiveFolderImpl extends ArchiveNodeImpl implements
		IArchiveFolder {

	private XbFolder folderDelegate;
	
	public ArchiveFolderImpl() {
		this(new XbFolder());
	}
	public ArchiveFolderImpl(XbFolder delegate) {
		super(delegate);
		this.folderDelegate = delegate;
	}

	/*
	 * @see IArchiveFolder#getName()
	 */
	public String getName() {
		return folderDelegate.getName();
	}

	/*
	 * @see IArchiveFolder#getFileSets()
	 */
	public IArchiveFileSet[] getFileSets() {
		IArchiveNode nodes[] = getChildren(TYPE_ARCHIVE_FILESET);
		IArchiveFileSet filesets[] = new IArchiveFileSet[nodes.length];
		System.arraycopy(nodes, 0, filesets, 0, nodes.length);
		return filesets;
	}

	/*
	 * @see IArchiveFolder#getFolders()
	 */
	public IArchiveFolder[] getFolders() {
		IArchiveNode nodes[] = getChildren(TYPE_ARCHIVE_FOLDER);
		IArchiveFolder folders[] = new IArchiveFolder[nodes.length];
		System.arraycopy(nodes, 0, folders, 0, nodes.length);
		return folders;
	}

	/*
	 * @see IArchiveFolder#getArchives()
	 */
	public IArchive[] getArchives() {
		IArchiveNode nodes[] = getChildren(TYPE_ARCHIVE);
		IArchive pkgs[] = new IArchive[nodes.length];
		System.arraycopy(nodes, 0, pkgs, 0, nodes.length);
		return pkgs;
	}

	/*
	 * @see IArchiveNode#getNodeType()
	 */
	public int getNodeType() {
		return TYPE_ARCHIVE_FOLDER;
	}

	/*
	 * @see IArchiveFolder#setName(String)
	 */
	public void setName(String name) {
		attributeChanged(NAME_ATTRIBUTE, getName(), name);
		folderDelegate.setName(name);
	}

	protected XbFolder getFolderDelegate () {
		return folderDelegate;
	}
	
	public String toString() {
		return "folder[" + getName() + "]";
	}
	
	/*
	 * @see IArchiveNode#getRootArchiveRelativePath()
	 */
	public IPath getRootArchiveRelativePath() {
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

}
