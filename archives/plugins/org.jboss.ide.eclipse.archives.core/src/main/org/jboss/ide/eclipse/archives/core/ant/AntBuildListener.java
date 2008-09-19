package org.jboss.ide.eclipse.archives.core.ant;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveBuildListener;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.model.DirectoryScannerFactory.DirectoryScannerExtension.FileWrapper;

public class AntBuildListener implements IArchiveBuildListener {
	public void buildFailed(IArchive pkg, IStatus status) {
		ArchivesCore.getInstance().getLogger().log(status);
	}
	public void error(IArchiveNode node, IStatus[] multi) {
		for( int i = 0; i < multi.length; i++ ) {
			ArchivesCore.getInstance().getLogger().log(multi[i]);
		}
	}

	public void cleanArchive(IArchive pkg) {
		ArchivesCore.getInstance().getLogger().log(IStatus.INFO, "Cleaned archive " + pkg.getName(), null);
	}
	public void cleanProject(IPath project) {
		ArchivesCore.getInstance().getLogger().log(IStatus.INFO, "Cleaned project " + project, null);
	}
	public void fileRemoved(IArchive topLevelArchive, IArchiveFileSet fileset,
			IPath filePath) {
		FileWrapper[] wrappers = fileset.getMatches(filePath);
		for( int i = 0; i < wrappers.length; i++ ) {
			ArchivesCore.getInstance().getLogger().log(IStatus.OK, "Removed " + filePath + " in " + topLevelArchive.getName()
					+ " at " + wrappers[i].getRootArchiveRelative().toString(), null);
		}
	}
	public void fileUpdated(IArchive topLevelArchive, IArchiveFileSet fileset,
			IPath filePath) {
		FileWrapper[] wrappers = fileset.getMatches(filePath);
		for( int i = 0; i < wrappers.length; i++ ) {
			ArchivesCore.getInstance().getLogger().log(IStatus.OK, "Updated " + filePath + " in " + topLevelArchive.getName()
					+ " at " + wrappers[i].getRootArchiveRelative().toString(), null);
		}
	}
	public void finishedBuild(IPath project) {
		ArchivesCore.getInstance().getLogger().log(IStatus.INFO, "Finished building " + project.toString(), null);
	}
	public void finishedBuildingArchive(IArchive pkg) {
		ArchivesCore.getInstance().getLogger().log(IStatus.INFO, "Finished building archive " + pkg.getRootArchiveRelativePath(), null);
	}
	public void finishedCollectingFileSet(IArchiveFileSet fileset) {
		ArchivesCore.getInstance().getLogger().log(IStatus.INFO, "Finished collecting fileset " + fileset.toString(), null);
	}
	public void startedBuild(IPath project) {
		ArchivesCore.getInstance().getLogger().log(IStatus.INFO, "Started building project " + project.toString(), null);
	}
	public void startedBuildingArchive(IArchive pkg) {
		ArchivesCore.getInstance().getLogger().log(IStatus.INFO, "Started building archive " + pkg.toString(), null);
	}
	public void startedCollectingFileSet(IArchiveFileSet fileset) {
		ArchivesCore.getInstance().getLogger().log(IStatus.INFO, "Started collecting fileset " + fileset.toString(), null);
	}

}
