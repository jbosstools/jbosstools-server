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

import org.eclipse.core.resources.IProject;
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
import org.jboss.ide.eclipse.as.core.model.EventLogModel;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogRoot;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.packages.ModulePackageTypeConverter;
import org.jboss.ide.eclipse.as.core.publishers.IJBossServerPublisher.PublishEvent;
import org.jboss.ide.eclipse.as.core.publishers.PackagesPublisher.PackagesPublisherMoveEvent;
import org.jboss.ide.eclipse.as.core.publishers.PackagesPublisher.PackagesPublisherRemoveEvent;
import org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer;
import org.jboss.ide.eclipse.packages.core.model.IPackage;
import org.jboss.ide.eclipse.packages.core.model.IPackageFileSet;
import org.jboss.ide.eclipse.packages.core.model.IPackagesBuildListener;
import org.jboss.ide.eclipse.packages.core.model.PackagesCore;
import org.jboss.ide.eclipse.packages.core.model.types.IPackageType;

/**
 *  This class provides a default implementation for packaging different types of projects
 * @author rob.stryker@jboss.com
 */
public class JstPackagesPublisher extends PackagesPublisher {
	
	public JstPackagesPublisher(IDeployableServer server) {
		super(server);
	}

	public void publishModule(int kind, int deltaKind, int modulePublishState,
			IModule[] module, IProgressMonitor monitor) throws CoreException {
        if(ServerBehaviourDelegate.REMOVED == deltaKind){
        	unpublish(server, module, monitor);
        } else if( ServerBehaviourDelegate.NO_CHANGE != deltaKind || kind == IServer.PUBLISH_FULL || kind == IServer.PUBLISH_CLEAN ){
        	// if there's no change, do nothing. Otherwise, on change or add, re-publish
        	publish(server, module, monitor);
        } else if( ServerBehaviourDelegate.NO_CHANGE != deltaKind && kind == IServer.PUBLISH_INCREMENTAL ){
        	publish(server, module, monitor);
        }
	}

	protected IStatus[] publish(IDeployableServer jbServer, IModule[] module, IProgressMonitor monitor) throws CoreException {
		PublishEvent event = new PublishEvent(eventRoot, PackagesPublisher.PUBLISH_TOP_EVENT, module[0]);
		EventLogModel.markChanged(eventRoot);
		IPackage topLevel = createTopPackage(module[0], jbServer.getDeployDirectory(), monitor);
		if( topLevel != null ) {
			Throwable t = null;
			try {
				PackagesCore.buildPackage(topLevel, new NullProgressMonitor());
			} catch( Exception e ) {
				t = e;
			}

			addMoveEvent(event, topLevel, topLevel.isDestinationInWorkspace(), 
					topLevel.getPackageFilePath(), topLevel.getPackageFilePath(), t);
		}
		return null;
	}
	protected void addMoveEvent(EventLogTreeItem parent, IPackage pack, boolean inWorkspace, 
			IPath sourcePath, IPath destPath, Throwable e) {
			String specType = PackagesPublisher.MOVE_PACKAGE_SUCCESS;
		new PackagesPublisherMoveEvent(parent, specType, pack, sourcePath, destPath, e );
		EventLogModel.markChanged(parent);
	}

	
	protected IStatus[] unpublish(IDeployableServer jbServer, IModule[] module, IProgressMonitor monitor) throws CoreException {
		PublishEvent event = new PublishEvent(eventRoot, PackagesPublisher.REMOVE_TOP_EVENT, module[0]);
		EventLogModel.markChanged(eventRoot);

		IPackage topLevel = createTopPackage(module[0], jbServer.getDeployDirectory(), monitor);
		if( topLevel.isDestinationInWorkspace() ) {
			String deployDir = jbServer.getDeployDirectory();
			
			IPath path = topLevel.getPackageResource().getRawLocation();
			IPath p = new Path(deployDir).append(path.lastSegment());
			boolean success = p.toFile().delete();
			addRemoveEvent(event, topLevel, p, success ? SUCCESS : FAILURE);
		} else {
			IPath path = topLevel.getPackageFilePath();
			boolean success = path.toFile().delete();
			addRemoveEvent(event, topLevel, path, success ? SUCCESS : FAILURE);
		}
		return null;
	}

	protected IPackage createTopPackage(IModule module, String deployDir, IProgressMonitor monitor) {
		IPackageType type = ModulePackageTypeConverter.getPackageTypeFor(module);
		if( type != null ) {
    		IPackage topLevel = type.createDefaultConfiguration(module.getProject(), monitor);
    		topLevel.setDestinationPath(new Path(deployDir));
    		//topLevel.setDestinationFolder(new Path("c:\\test"));
    		return topLevel;
		}
		return null;
	}
}
