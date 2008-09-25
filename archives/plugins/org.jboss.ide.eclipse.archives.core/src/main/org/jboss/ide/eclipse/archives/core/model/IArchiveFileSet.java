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
package org.jboss.ide.eclipse.archives.core.model;

import org.eclipse.core.runtime.IPath;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension.FileWrapper;

/**
 * <p>
 * This interface represents a file set inside of a package definition or folder.
 * </p>
 *
 * @author <a href="marshall@jboss.org">Marshall Culpepper</a>
 * @author <a href="rob.stryker@redhat.com">Rob Stryker</a>
 * @version $Revision: 1930 $
 */
public interface IArchiveFileSet extends IArchiveNode {
	public static final String ATTRIBUTE_PREFIX = "org.jboss.ide.eclipse.archives.core.model.IPackageFileSet."; //$NON-NLS-1$
	public static final String INCLUDES_ATTRIBUTE = ATTRIBUTE_PREFIX + "includes"; //$NON-NLS-1$
	public static final String EXCLUDES_ATTRIBUTE = ATTRIBUTE_PREFIX + "excludes"; //$NON-NLS-1$
	public static final String IN_WORKSPACE_ATTRIBUTE = ATTRIBUTE_PREFIX + "inWorkspace"; //$NON-NLS-1$
	public static final String FLATTENED_ATTRIBUTE = ATTRIBUTE_PREFIX + "flattened"; //$NON-NLS-1$
	public static final String SOURCE_PATH_ATTRIBUTE = ATTRIBUTE_PREFIX + "sourcePath"; //$NON-NLS-1$

	/**
	 * @return Whether or not this fileset's basedir is inside the workspace
	 */
	public boolean isInWorkspace();

	/**
	 * @return Whether or not the fileset is flattened
	 */
	public boolean isFlattened();

	/**
	 * @return the source path from the delegate with no translation at all
	 */
	public String getRawSourcePath();

	/**
	 * Force the scanner to check for matched files again
	 */
	public void resetScanner();

	/**
	 * @return The includes pattern for this fileset
	 */
	public String getIncludesPattern();

	/**
	 * @return The excludes pattern for this fileset
	 */
	public String getExcludesPattern();

	/**
	 * Sets the "root" or "source" of this fileset (file-system or workspace relative)
	 * @param path The absolute path that is the source of this fileset
	 */
	public void setRawSourcePath (String raw);

	/**
	 * Set the includes pattern for this fileset. This pattern uses the same syntax as Ant's include pattern.
	 * @param includes The includes pattern for this fileset
	 */
	public void setIncludesPattern(String includes);

	/**
	 * Set the excludes pattern for this fileset. This pattern uses the same syntax as Ant's exclude pattern.
	 * @param excludes The excludes pattern for this fileset
	 */
	public void setExcludesPattern(String excludes);

	/**
	 * Set whether or not this fileset's source is in the workspace. This will automatically be handled if you
	 * use setSingleFile, setSourceProject, setSourceContainer, or setSourceFolder.
	 * @param isInWorkspace Whether or not this fileset's source is in the workspace
	 */
	public void setInWorkspace(boolean isInWorkspace);

	/**
	 * Sets whether or not this fileset is flattened.
	 */
	public void setFlattened(boolean flattened);


	/**
	 * @return An array of matching IPath's in the filesystem (for external filesystem filesets)
	 */
	public FileWrapper[] findMatchingPaths();

	/**
	 * Get the FileWrapper objects that match this path
	 * @param path
	 * @return
	 */
	public FileWrapper[] getMatches(IPath path);

	/**
	 * This method returns whether the global path passed in matches this fileset.
	 * It must give an accurate answer regardless of whether the path is cached or not.
	 *
	 * @param path The absolute path on the filesystem to check.
	 * @return Whether or not this fileset matches the passed-in path
	 */
	public boolean matchesPath(IPath path);

	/**
	 * This method returns whether the path passed in matches this fileset.
	 * It must give an accurate answer regardless of whether the path is cached or not.
	 *
	 * @param path The absolute path on the filesystem to check.
	 * @param inWorkspace Whether this is a workspace path or a global path
	 * @return Whether or not this fileset matches the passed-in path
	 */
	public boolean matchesPath(IPath path, boolean inWorkspace);
}
