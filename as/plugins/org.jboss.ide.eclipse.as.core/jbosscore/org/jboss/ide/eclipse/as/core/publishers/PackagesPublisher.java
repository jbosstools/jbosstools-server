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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
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

	public void publishModule(int kind, int deltaKind, IModule[] module,
			IProgressMonitor monitor) throws CoreException {
		// should create submonitors
		String projectName = module[0].getName();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		PackagesCore.buildProject(project, monitor);
		IPackage[] packages = PackagesCore.getProjectPackages(project, new NullProgressMonitor());
		for( int i = 0; i < packages.length; i++ ) {
			if( packages[i].isDestinationInWorkspace()) {
				// destination is workspace. Move it. 
				IFile file = packages[i].getPackageFile();
				IPath sourcePath = file.getLocation();
				IPath destPath = new Path(server.getDeployDirectory(true)).append(sourcePath.lastSegment());
				System.out.println("copying file: " + sourcePath);
			}
		}
	}

}
