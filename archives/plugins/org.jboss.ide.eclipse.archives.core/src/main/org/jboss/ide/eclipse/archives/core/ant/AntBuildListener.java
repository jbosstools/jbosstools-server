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
package org.jboss.ide.eclipse.archives.core.ant;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.ArchivesCoreMessages;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveBuildListener;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension.FileWrapper;

public class AntBuildListener implements IArchiveBuildListener {
	public void buildFailed(IArchive pkg, IStatus status) {
		ArchivesCore.log(status);
	}
	public void error(IArchiveNode node, IStatus[] multi) {
		for( int i = 0; i < multi.length; i++ ) {
			ArchivesCore.log(multi[i]);
		}
	}

	public void cleanArchive(IArchive pkg) {
		ArchivesCore.log(IStatus.INFO, ArchivesCore.bind(ArchivesCoreMessages.CleanedArchiveTrace,pkg.getName()), null);
	}
	public void cleanProject(IPath project) {
		ArchivesCore.log(IStatus.INFO,ArchivesCore.bind(ArchivesCoreMessages.CleanedProjectTrace, project.toString()),null);
	}
	public void fileRemoved(IArchive topLevelArchive, IArchiveFileSet fileset,
			IPath filePath) {
		FileWrapper[] wrappers = fileset.getMatches(filePath);
		for( int i = 0; i < wrappers.length; i++ ) {
			String s = ArchivesCore.bind(ArchivesCoreMessages.FileRemovedTrace,
					new Object[] {filePath, topLevelArchive.getName(), wrappers[i].getRootArchiveRelative().toString()});
			ArchivesCore.log(IStatus.OK, s, null);
		}
	}
	public void fileUpdated(IArchive topLevelArchive, IArchiveFileSet fileset,
			IPath filePath) {
		FileWrapper[] wrappers = fileset.getMatches(filePath);
		for( int i = 0; i < wrappers.length; i++ ) {
			String s = ArchivesCore.bind(ArchivesCoreMessages.UpdatedFileTrace,
					new Object[] {filePath, topLevelArchive.getName(), wrappers[i].getRootArchiveRelative().toString()});
			ArchivesCore.log(IStatus.OK, s, null);
		}
	}
	public void finishedBuild(IPath project) {
		ArchivesCore.log(IStatus.INFO, ArchivesCore.bind(ArchivesCoreMessages.FinishedBuildingProjectTrace,project.toString()), null);
	}
	public void finishedBuildingArchive(IArchive pkg) {
		ArchivesCore.log(IStatus.INFO, ArchivesCore.bind(ArchivesCoreMessages.FinishedBuildingArchiveTrace,pkg.getRootArchiveRelativePath().toString()), null);
	}
	public void finishedCollectingFileSet(IArchiveFileSet fileset) {
		ArchivesCore.log(IStatus.INFO, ArchivesCore.bind(ArchivesCoreMessages.FinishedCollectingFilesetTrace, fileset.toString()), null);
	}
	public void startedBuild(IPath project) {
		ArchivesCore.log(IStatus.INFO, ArchivesCore.bind(ArchivesCoreMessages.StartedBuildingProjectTrace,project.toString()), null);
	}
	public void startedBuildingArchive(IArchive pkg) {
		ArchivesCore.log(IStatus.INFO, ArchivesCore.bind(ArchivesCoreMessages.StartedBuildingArchiveTrace, pkg.toString()), null);
	}
	public void startedCollectingFileSet(IArchiveFileSet fileset) {
		ArchivesCore.log(IStatus.INFO, ArchivesCore.bind(ArchivesCoreMessages.StartedCollectingFilesetTrace, fileset.toString()), null);
	}

}
