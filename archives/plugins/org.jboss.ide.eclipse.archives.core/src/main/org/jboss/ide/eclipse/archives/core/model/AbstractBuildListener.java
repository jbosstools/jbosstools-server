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
