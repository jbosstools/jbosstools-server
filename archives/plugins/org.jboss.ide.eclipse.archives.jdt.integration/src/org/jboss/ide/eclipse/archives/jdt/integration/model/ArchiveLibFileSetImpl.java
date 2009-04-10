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
package org.jboss.ide.eclipse.archives.jdt.integration.model;

import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.UserLibrary;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension.FileWrapper;
import org.jboss.ide.eclipse.archives.core.model.internal.ArchiveNodeImpl;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbLibFileSet;

/**
 * An implementation for filesets
 * @author <a href="rob.stryker@redhat.com">Rob Stryker</a>
 *
 */
public class ArchiveLibFileSetImpl extends ArchiveNodeImpl implements
		IArchiveLibFileSet {

	private IClasspathEntry[] entries = null;
	private FileWrapper[] wrappers = null;
	public ArchiveLibFileSetImpl() {
		this(new XbLibFileSet());
	}

	public ArchiveLibFileSetImpl (XbLibFileSet delegate) {
		super(delegate);
	}

	public String getId() {
		return getFileSetDelegate().getId();
	}
	
	public void setId(String id) {
		getFileSetDelegate().setId(id);
		resetScanner();
	}
	
	/*
	 * @see IArchiveFileSet#matchesPath(IPath)
	 */
	public boolean matchesPath(IPath globalPath) {
		return matchesPath(globalPath, false);
	}

	/*
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet#matchesPath(org.eclipse.core.runtime.IPath, boolean)
	 */
	public boolean matchesPath(IPath path, boolean inWorkspace) {
		//prime();
		return false;
	}

	/*
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet#getMatches(org.eclipse.core.runtime.IPath)
	 */
	public FileWrapper[] getMatches(IPath path) {
		prime();
		ArrayList<FileWrapper> temp = new ArrayList<FileWrapper>();
		for( int i = 0; i < wrappers.length; i++ ) 
			if( wrappers[i].getWrapperPath().equals(path))
				temp.add(wrappers[i]);
		return (FileWrapper[]) temp.toArray(new FileWrapper[temp.size()]);
	}

	/*
	 * @see IArchiveFileSet#findMatchingPaths()
	 */
	public synchronized FileWrapper[] findMatchingPaths () {
		prime();
		return wrappers == null ? new FileWrapper[]{} : wrappers;
	}

	private synchronized void prime() {
		if( entries == null ) {
			UserLibrary lib = JavaModelManager.getUserLibraryManager().getUserLibrary(getFileSetDelegate().getId());
			if( lib != null ) {
				entries = lib.getEntries();
				ArrayList<FileWrapper> list = new ArrayList<FileWrapper>();
				FileWrapper fw;
				for( int i = 0; i < entries.length; i++ ) {
					fw = new FileWrapper(entries[i].getPath().toFile(), 
							entries[i].getPath(), getRootArchiveRelativePath(),
							entries[i].getPath().lastSegment());
					list.add(fw);
				}
				wrappers = (FileWrapper[]) list.toArray(new FileWrapper[list.size()]);
			} else {
				entries = new IClasspathEntry[]{};
				wrappers = new FileWrapper[]{};
			}
		}
	}
	
	/*
	 * @see IArchiveNode#getNodeType()
	 */
	public int getNodeType() {
		return TYPE_ARCHIVE_FILESET;
	}

	/*
	 * filesets have no path of their own
	 * and should not be the parents of any other node
	 * so the parent is their base location
	 * @see IArchiveNode#getRootArchiveRelativePath()
	 */
	public IPath getRootArchiveRelativePath() {
		return getParent().getRootArchiveRelativePath();
	}

	/*
	 * @see IArchiveFileSet#resetScanner()
	 */
	public void resetScanner() {
		entries = null;
		wrappers = null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.internal.ArchiveNodeImpl#validateChild(org.jboss.ide.eclipse.archives.core.model.IArchiveNode)
	 */
	public boolean validateModel() {
		return getAllChildren().length == 0 ? true : false;
	}

	/*
	 * @see org.jboss.ide.eclipse.archives.core.model.internal.ArchiveNodeImpl#canBuild()
	 */
	public boolean canBuild() {
		return super.canBuild();
	}

	protected XbLibFileSet getFileSetDelegate () {
		return (XbLibFileSet)nodeDelegate;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{id="); //$NON-NLS-1$
		sb.append(getFileSetDelegate().getId());
		return sb.toString();
	}

}
