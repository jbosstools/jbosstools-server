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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.DeletedModule;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.packages.ModulePackageTypeConverter;
import org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer;
import org.jboss.ide.eclipse.packages.core.model.IPackage;
import org.jboss.ide.eclipse.packages.core.model.PackagesCore;
import org.jboss.ide.eclipse.packages.core.model.types.IPackageType;

/**
 *  This class provides a default implementation for packaging different types of projects
 * @author rob.stryker@jboss.com
 */
public class JstPackagesPublisher implements IJBossServerPublisher {
	
	private int state;
	private IDeployableServer server;
	
	public JstPackagesPublisher(IDeployableServer server) {
		this.server = server;
		state = IServer.PUBLISH_STATE_NONE;
	}
	public int getPublishState() {
		return state;
	}


	public void publishModule(int kind, int deltaKind, int modulePublishState,
			IModule[] module, IProgressMonitor monitor) throws CoreException {
    	checkClosed(module);
        if(ServerBehaviourDelegate.REMOVED == deltaKind){
        	unpublish(server, module, monitor);
        } else if( ServerBehaviourDelegate.NO_CHANGE != deltaKind || kind == IServer.PUBLISH_FULL || kind == IServer.PUBLISH_CLEAN ){
        	// if there's no change, do nothing. Otherwise, on change or add, re-publish
        	publish(server, module, monitor);
        }
	}

	protected IStatus[] publish(IDeployableServer jbServer, IModule[] module, IProgressMonitor monitor) throws CoreException {
		IPackage topLevel = createTopPackage(module[0], jbServer.getDeployDirectory(), monitor);
		if( topLevel != null ) {
			PackagesCore.buildPackage(topLevel, new NullProgressMonitor());
		}
		return null;
	}
	protected IStatus[] unpublish(IDeployableServer jbServer, IModule[] module, IProgressMonitor monitor) throws CoreException {
		IPackage topLevel = createTopPackage(module[0], jbServer.getDeployDirectory(), monitor);
		if( topLevel.isDestinationInWorkspace() ) {
			String deployDir = jbServer.getDeployDirectory();
			
			IPath path = topLevel.getPackageFile().getRawLocation();
			IPath p = new Path(deployDir).append(path.lastSegment());
			p.toFile().delete();
		} else {
			IPath path = topLevel.getPackageFilePath();
			path.toFile().delete();
		}
		
		return null;
	}
	
    protected void checkClosed(IModule[] module) throws CoreException {
    	for(int i=0;i<module.length;i++) {
    		if(module[i] instanceof DeletedModule) {	
                IStatus status = new Status(IStatus.ERROR,JBossServerCorePlugin.PLUGIN_ID,0, "Failure", null);
                throw new CoreException(status);
    		}
    	}
    }
    
	protected IPackage createTopPackage(IModule module, String deployDir, IProgressMonitor monitor) {
		IPackageType type = ModulePackageTypeConverter.getPackageTypeFor(module);
		if( type != null ) {
    		IPackage topLevel = type.createDefaultConfiguration(module.getProject(), monitor);
    		topLevel.setDestinationFolder(new Path(deployDir));
    		//topLevel.setDestinationFolder(new Path("c:\\test"));
    		return topLevel;
		}
		return null;
	}
    
}
