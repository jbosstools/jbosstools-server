/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.core.build;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.jboss.ide.eclipse.archives.core.model.AbstractBuildListener;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.util.PathUtils;

public class PostBuildRefresher extends AbstractBuildListener {

	public void finishedBuildingArchive(final IArchive pkg) {
		// TODO Auto-generated method stub
		if( pkg.isDestinationInWorkspace()) {
			new WorkspaceJob("Refresh Project") { //$NON-NLS-1$
				public IStatus runInWorkspace(IProgressMonitor monitor)
						throws CoreException {
					handlePostBuild(pkg);
					return Status.OK_STATUS;
				}
			}.schedule(1000);
		}
	}
	
	private void handlePostBuild(IArchive pkg) {
		String projectName = pkg.getProjectName();
		IProject p = projectName == null ? null : 
			ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if( p == null || !p.exists()) {
			return;
		}
		
		IPath loc = PathUtils.getGlobalLocation(pkg);
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(URIUtil.toURI(loc.makeAbsolute()));
		for( int i = 0; i < files.length; i++ ) {
			try {
				files[i].refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			} catch( CoreException ce ) {}
		}
		IContainer[] folders = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(URIUtil.toURI(loc.makeAbsolute()));
		for( int i = 0; i < folders.length; i++ ) {
			try {
				folders[i].refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			} catch( CoreException ce ) {}
		}
	}
}
