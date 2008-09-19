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
import org.eclipse.core.runtime.IStatus;

/**
 * This interface is inteded to be implemented by classes who are
 * interested in receiving callbacks for various IArchive build events
 *
 * @author Marshall
 */
public interface IArchiveBuildListener {


	/**
	 * A project has started being built by the Archives builder
	 * @param project the project being built
	 */
	public void startedBuild (IPath project);

	/**
	 * A project is finished being built by the Archives builder
	 * @param project the project being built
	 */
	public void finishedBuild (IPath project);

	/**
	 * The project is going to be rebuilt from scratch and all state should be discarded
	 * @param project
	 */
	public void cleanProject(IPath project);

	/**
	 * An Archive has started being built by the Archives builder
	 * This can be called from a full build, or from the incremental build
	 * if and only if one of the affected files matches a fileset in this archive.
	 *
	 * @param pkg the Archive being built
	 */
	public void startedBuildingArchive (IArchive pkg);

	/**
	 * An Archive is finished being built by the Archives builder
	 * This can be called from a full build, or from the incremental build
	 * if and only if one of the affected files matches a fileset in this archive.
	 *
	 * @param pkg the Archive being built
	 */
	public void finishedBuildingArchive (IArchive pkg);

	/**
	 * The archive is going to be rebuilt from scratch and all state should be discarded
	 * @param pkg
	 */
	public void cleanArchive(IArchive pkg);


	/**
	 * A fileset has started being collected for copying into a Archive
	 * This is *only* used during  a FULL BUILD or after a MODEL CHANGE
	 * @param fileset the fileset being collected
	 */
	public void startedCollectingFileSet (IArchiveFileSet fileset);

	/**
	 * A fileset has finished being collected for copying into a Archive
	 * This is *only* used during  a FULL BUILD or after a MODEL CHANGE
	 * @param fileset the fileset being collected
	 */
	public void finishedCollectingFileSet (IArchiveFileSet fileset);

	/**
	 * The build for the given project has failed
	 * @param pkg The Archive that failed to build
	 * @param status The status/exception that occurred
	 */
	public void buildFailed (IArchive pkg, IStatus status);

	/**
	 * A file has been updated, with the given IArchive / IArchiveFileSet context
	 * @param topLevelArchive The top level Archive that was updated
	 * @param fileset The fileset that matched the updated file
	 * @param filePath The path to the file that was copied (filesystem/workspace path)
	 */
	public void fileUpdated (IArchive topLevelArchive, IArchiveFileSet fileset, IPath filePath);

	/**
	 * A file has been removed, with the given IArchive / IArchiveFileSet context
	 * @param topLevelArchive The top level Archive that was updated
	 * @param fileset The fileset that matched the removed file
	 * @param filePath The path to the file that was removed (filesystem/workspace path)
	 */
	public void fileRemoved (IArchive topLevelArchive, IArchiveFileSet fileset, IPath filePath);

	/**
	 * An error was happened upon.
	 */
	public void error(IArchiveNode node, IStatus[] multi);

}
