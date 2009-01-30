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
import org.eclipse.core.runtime.IStatus;

public class AbstractBuildListener implements IArchiveBuildListener {
	public void buildFailed(IArchive pkg, IStatus status) {
	}
	public void cleanArchive(IArchive pkg) {
	}
	public void cleanProject(IPath project) {
	}
	public void error(IArchiveNode node, IStatus[] multi) {
	}
	public void fileRemoved(IArchive topLevelArchive, IArchiveFileSet fileset,
			IPath filePath) {
	}
	public void fileUpdated(IArchive topLevelArchive, IArchiveFileSet fileset,
			IPath filePath) {
	}
	public void finishedBuild(IPath project) {
	}
	public void finishedBuildingArchive(IArchive pkg) {
	}
	public void finishedCollectingFileSet(IArchiveFileSet fileset) {
	}
	public void startedBuild(IPath project) {
	}
	public void startedBuildingArchive(IArchive pkg) {
	}
	public void startedCollectingFileSet(IArchiveFileSet fileset) {
	}
}
