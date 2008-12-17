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
import java.util.HashMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.ArchivesCoreMessages;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension.FileWrapper;
import org.jboss.ide.eclipse.archives.core.model.internal.xb.XbFileSet;
import org.jboss.ide.eclipse.archives.core.util.PathUtils;

/**
 * An implementation for filesets
 * @author <a href="rob.stryker@redhat.com">Rob Stryker</a>
 *
 */
public class ArchiveFileSetImpl extends ArchiveNodeImpl implements
		IArchiveFileSet {

	private DirectoryScannerExtension scanner;
	private FileWrapper[] matchingPaths;
	private HashMap<String, ArrayList<FileWrapper>> matchingMap;
	private boolean rescanRequired = true;

	public ArchiveFileSetImpl() {
		this(new XbFileSet());
	}

	public ArchiveFileSetImpl (XbFileSet delegate) {
		super(delegate);
	}

	/*
	 * @see IArchiveFileSet#getExcludesPattern()
	 */
	public String getExcludesPattern() {
		return getFileSetDelegate().getExcludes();
	}

	/*
	 * @see IArchiveFileSet#isInWorkspace()
	 */
	public boolean isInWorkspace() {
		return getFileSetDelegate().isInWorkspace();
	}

	/*
	 * @see IArchiveFileSet#getIncludesPattern()
	 */
	public String getIncludesPattern() {
		return getFileSetDelegate().getIncludes();
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet#getRawSourcePath()
	 */
	public String getRawSourcePath() {
		return getFileSetDelegate().getDir();
	}


	/*
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet#isFlattened()
	 */
	public boolean isFlattened() {
		return getFileSetDelegate().isFlattened();
	}

	/*
	 * @see IArchiveFileSet#matchesPath(IPath)
	 */
	public boolean matchesPath(IPath globalPath) {
		return matchesPath(globalPath, false);
	}

	public boolean matchesPath(IPath path, boolean inWorkspace) {
		getScanner();
		IPath globalPath = path;
		if( inWorkspace )
			globalPath = ArchivesCore.getInstance().getVFS().workspacePathToAbsolutePath(path);

		ArrayList<FileWrapper> result = matchingMap.get(globalPath.toFile().getAbsolutePath());
		if( result != null )
			if( result.size() > 0 )
				return true;

		return getScanner() == null ? false : getScanner().couldBeIncluded(path.toString(), inWorkspace);
	}

	public FileWrapper[] getMatches(IPath path) {
		getScanner();
		ArrayList<FileWrapper> l = matchingMap.get(path.toFile().getAbsolutePath());
		if( l != null )
			return (FileWrapper[]) l.toArray(new FileWrapper[l.size()]);
		return new FileWrapper[0];
	}

	/*
	 * @see IArchiveFileSet#findMatchingPaths()
	 */
	public synchronized FileWrapper[] findMatchingPaths () {
		getScanner();
		return matchingPaths;
	}

	/*
	 * Will re-scan if required, or use cached scanner
	 * @return
	 */
	public synchronized DirectoryScannerExtension getScanner() {
		if( scanner == null || rescanRequired) {
			rescanRequired = false;

			try {
				scanner = DirectoryScannerFactory.createDirectoryScanner(this, true);
				if( scanner != null ) {
					matchingPaths = scanner.getMatchedArray();
					matchingMap = scanner.getMatchedMap();
				}
			} catch( IllegalStateException ise ) {
				ArchivesCore.getInstance().getLogger().log(IStatus.WARNING, ArchivesCoreMessages.CouldNotCreateScanner, ise);
				matchingPaths = new FileWrapper[0];
				matchingMap = new HashMap<String, ArrayList<FileWrapper>>();
			}
		}
		return scanner;
	}

	/*
	 * @see IArchiveNode#getNodeType()
	 */
	public int getNodeType() {
		return TYPE_ARCHIVE_FILESET;
	}

	/*
	 * @see IArchiveFileSet#setExcludesPattern(String)
	 */
	public void setExcludesPattern(String excludes) {
		attributeChanged(EXCLUDES_ATTRIBUTE, getExcludesPattern(), excludes);
		getFileSetDelegate().setExcludes(excludes);
		rescanRequired = true;
	}

	/*
	 * @see IArchiveFileSet#setIncludesPattern(String)
	 */
	public void setIncludesPattern(String includes) {
		attributeChanged(INCLUDES_ATTRIBUTE, getIncludesPattern(), includes);
		getFileSetDelegate().setIncludes(includes);
		rescanRequired = true;
	}

	/*
	 * @see IArchiveFileSet#setInWorkspace(boolean)
	 */
	public void setInWorkspace(boolean isInWorkspace) {
		attributeChanged(IN_WORKSPACE_ATTRIBUTE, new Boolean(isInWorkspace()), new Boolean(isInWorkspace));
		getFileSetDelegate().setInWorkspace(isInWorkspace);
		rescanRequired = true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet#setFlattened(boolean)
	 */
	public void setFlattened(boolean flat) {
		attributeChanged(FLATTENED_ATTRIBUTE, new Boolean(isFlattened()), new Boolean(flat));
		getFileSetDelegate().setFlattened(flat);
		rescanRequired = true;
	}

	/*
	 * @see IArchiveFileSet#setSourcePath(IPath, boolean)
	 */
	public void setRawSourcePath (String raw) {
		Assert.isNotNull(raw);
		String src = getRawSourcePath();
		attributeChanged(SOURCE_PATH_ATTRIBUTE,
				src == null ? null : src.toString(),
				raw == null ? null : raw);
		getFileSetDelegate().setDir(raw);
		rescanRequired = true;
	}

	protected XbFileSet getFileSetDelegate () {
		return (XbFileSet)nodeDelegate;
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
		rescanRequired = true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.archives.core.model.internal.ArchiveNodeImpl#validateChild(org.jboss.ide.eclipse.archives.core.model.IArchiveNode)
	 */
	public boolean validateModel() {
		return getAllChildren().length == 0 ? true : false;
	}

	public boolean canBuild() {
		return PathUtils.getGlobalLocation(this) != null && super.canBuild();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{dir="); //$NON-NLS-1$
		sb.append(getFileSetDelegate().getDir());
		sb.append(",includes="); //$NON-NLS-1$
		sb.append(getFileSetDelegate().getIncludes());
		sb.append(",excludes="); //$NON-NLS-1$
		sb.append(getFileSetDelegate().getExcludes());
		sb.append(",inWorkspace="); //$NON-NLS-1$
		sb.append(getFileSetDelegate().isInWorkspace());
		sb.append(",flatten="); //$NON-NLS-1$
		sb.append(getFileSetDelegate().isFlattened());
		return sb.toString();
	}

}
