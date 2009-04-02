package org.jboss.ide.eclipse.archives.core.build;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveBuildListener;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.core.util.PathUtils;

public class PostBuildRefresher implements IArchiveBuildListener {

	public void buildFailed(IArchive pkg, IStatus status) {
		// TODO Auto-generated method stub

	}

	public void cleanArchive(IArchive pkg) {
		// TODO Auto-generated method stub

	}

	public void cleanProject(IPath project) {
		// TODO Auto-generated method stub

	}

	public void error(IArchiveNode node, IStatus[] multi) {
		// TODO Auto-generated method stub

	}

	public void fileRemoved(IArchive topLevelArchive, IArchiveFileSet fileset,
			IPath filePath) {
		// TODO Auto-generated method stub

	}

	public void fileUpdated(IArchive topLevelArchive, IArchiveFileSet fileset,
			IPath filePath) {
		// TODO Auto-generated method stub

	}

	public void finishedBuild(IPath project) {
		
		// TODO Auto-generated method stub

	}

	public void finishedBuildingArchive(IArchive pkg) {
		// TODO Auto-generated method stub
		if( pkg.isDestinationInWorkspace()) {
			IPath loc = PathUtils.getGlobalLocation(pkg);
			IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(loc);
			for( int i = 0; i < files.length; i++ ) {
				try {
					files[i].refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				} catch( CoreException ce ) {}
			}
			IContainer[] folders = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocation(loc);
			for( int i = 0; i < folders.length; i++ ) {
				try {
					folders[i].refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				} catch( CoreException ce ) {}
			}
		}
	}

	public void finishedCollectingFileSet(IArchiveFileSet fileset) {
		// TODO Auto-generated method stub

	}

	public void startedBuild(IPath project) {
		// TODO Auto-generated method stub

	}

	public void startedBuildingArchive(IArchive pkg) {
		// TODO Auto-generated method stub

	}

	public void startedCollectingFileSet(IArchiveFileSet fileset) {
		// TODO Auto-generated method stub

	}

}
