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
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.archives.core.model.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.types.IArchiveType;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.model.EventLogModel;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.packages.ModulePackageTypeConverter;
import org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger.PublishEvent;
import org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger.PublisherFileUtilListener;
import org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.FileUtil;

/**
 *  This class provides a default implementation for 
 *  packaging different types of flexible projects. It uses 
 *  the built-in heirarchy of the projects to do so. 
 *  
 * @author rob.stryker@jboss.com
 */
public class JstPublisher extends PackagesPublisher {
	
	public static final int BUILD_FAILED_CODE = 100;
	public static final int PACKAGE_UNDETERMINED_CODE = 101;
	
	public JstPublisher(IServer server, EventLogTreeItem context) {
		super(server, context);
	}

	public IStatus publishModule(int kind, int deltaKind, int modulePublishState,
			IModule module, IProgressMonitor monitor) throws CoreException {
		IStatus status = null;
		if(ServerBehaviourDelegate.REMOVED == deltaKind){
        	status = unpublish(server, module, kind, deltaKind, modulePublishState, monitor);
        } else if( ServerBehaviourDelegate.NO_CHANGE != deltaKind || kind == IServer.PUBLISH_FULL || kind == IServer.PUBLISH_CLEAN ){
        	// if there's no change, do nothing. Otherwise, on change or add, re-publish
        	status = publish(server, module, kind, deltaKind, modulePublishState, monitor);
        } else if( ServerBehaviourDelegate.NO_CHANGE != deltaKind && kind == IServer.PUBLISH_INCREMENTAL ){
        	status = publish(server, module, kind, deltaKind, modulePublishState, monitor);
        }
		return status;
	}

	protected IStatus publish(IDeployableServer jbServer, IModule module, int kind, 
						int deltaKind, int modulePublishState, IProgressMonitor monitor) throws CoreException {
		PublishEvent event = PublisherEventLogger.createSingleModuleTopEvent(eventRoot, module, kind, deltaKind);
		EventLogModel.markChanged(eventRoot);
		IArchive topLevel = createTopPackage(module, jbServer.getDeployDirectory(), monitor);
		if( topLevel != null ) {
			try {
				ArchivesCore.buildArchive(topLevel, new NullProgressMonitor());
			} catch( Exception e ) {
				return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, BUILD_FAILED_CODE, "", e);
			}
		} else {
			return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, PACKAGE_UNDETERMINED_CODE, "", null);
		}
		return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, IStatus.OK, "", null);
	}

	protected IStatus unpublish(IDeployableServer jbServer, IModule module, 
						int kind, int deltaKind, int modulePublishKind, IProgressMonitor monitor) throws CoreException {
		PublishEvent event = PublisherEventLogger.createSingleModuleTopEvent(eventRoot, module, kind, deltaKind);
		IArchive topLevel = createTopPackage(module, jbServer.getDeployDirectory(), monitor);
		if( topLevel != null ) {
			IPath path = topLevel.getArchiveFilePath();
			FileUtil.safeDelete(path.toFile(), new PublisherFileUtilListener(event));
		} else {
			return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, PACKAGE_UNDETERMINED_CODE, "", null);
		}
		return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, IStatus.OK, "", null);
	}

	protected IArchive createTopPackage(IModule module, String deployDir, IProgressMonitor monitor) {
		IArchiveType type = ModulePackageTypeConverter.getPackageTypeFor(module);
		if( type != null ) {
    		IArchive topLevel = type.createDefaultConfiguration(module.getProject(), monitor);
    		topLevel.setDestinationPath(new Path(deployDir));
    		topLevel.setInWorkspace(false);
    		return topLevel;
		} 
		return null;
	}
}
