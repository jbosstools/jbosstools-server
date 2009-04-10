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

	/**
	 * Force the scanner to check for matched files again
	 */
	public void resetScanner();
	
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
