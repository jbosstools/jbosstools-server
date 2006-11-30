/**
 * JBoss, a Division of Red Hat
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
package org.jboss.ide.eclipse.as.core.publishers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.packages.core.model.IPackage;
import org.jboss.ide.eclipse.packages.core.model.PackagesCore;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class PackagesPublisher implements IJBossServerPublisher {
	private JBossServer server;
	public PackagesPublisher(JBossServer jbServer) {
		this.server = jbServer;
	}
	public int getPublishState() {
		return IServer.PUBLISH_STATE_NONE;
	}

    public void publishModule(int kind, int deltaKind, int modulePublishState, 
    		IModule[] module, IProgressMonitor monitor) throws CoreException {
		
    	// if it's being removed
    	if( deltaKind == ServerBehaviourDelegate.REMOVED ) {
    		removeModule(module, monitor);
    		return;
    	}
    	
    	if( deltaKind == ServerBehaviourDelegate.ADDED || deltaKind == ServerBehaviourDelegate.CHANGED) {
    		boolean incremental = (kind == IServer.PUBLISH_INCREMENTAL);
    		publishModule(incremental, module, monitor);
    		return;
    	}
    	
    	if( kind == IServer.PUBLISH_INCREMENTAL ) {
    		boolean incremental = false;
    		if( modulePublishState == IServer.PUBLISH_STATE_NONE ) return;
    		if( modulePublishState == IServer.PUBLISH_STATE_INCREMENTAL ) incremental = true;
    		publishModule(incremental, module, monitor);
    		return;
    	}
    	
    	if( kind == IServer.PUBLISH_FULL ) {
    		publishModule(false, module, monitor);
    		return;
    	}
    	
	}

	protected void removeModule(IModule[] module, IProgressMonitor monitor) {
		// remove all of the deployed items
		System.out.println("removing module " + module[0].getName());
		
		String projectName = module[0].getName();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		PackagesCore.buildProject(project, monitor);
		IPackage[] packages = PackagesCore.getProjectPackages(project, new NullProgressMonitor());
		for( int i = 0; i < packages.length; i++ ) {
			if( packages[i].isDestinationInWorkspace()) {
				IFile file = packages[i].getPackageFile();
				IPath sourcePath = file.getLocation();
				IPath destPath = new Path(server.getDeployDirectory(true)).append(sourcePath.lastSegment());
				boolean deleted = destPath.toFile().delete();
			} else {
				IFile file = packages[i].getPackageFile();
				boolean deleted = file.getLocation().toFile().delete();
			}
		}
	}
	
	protected void publishModule(boolean incremental, IModule[] module, IProgressMonitor monitor) {
		System.out.println("publishing module " + module[0].getName());

		int inc2 = incremental ? IncrementalProjectBuilder.INCREMENTAL_BUILD : IncrementalProjectBuilder.FULL_BUILD;
		String projectName = module[0].getName();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		PackagesCore.buildProject(project, inc2, monitor);
		IPackage[] packages = PackagesCore.getProjectPackages(project, new NullProgressMonitor());
		for( int i = 0; i < packages.length; i++ ) {
			if( packages[i].isDestinationInWorkspace()) {
				try {
					// destination is workspace. Move it. 
					IFile file = packages[i].getPackageFile();
					IPath sourcePath = file.getLocation();
					IPath destPath = new Path(server.getDeployDirectory(true)).append(sourcePath.lastSegment());
					FileUtil.copyFile(sourcePath.toFile(), destPath.toFile());
				} catch( Exception e ) {
					e.printStackTrace();
				}
			}
		}
	}
}
