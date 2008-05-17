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

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.modules.PackageModuleFactory.ExtendedModuleFile;
import org.jboss.ide.eclipse.as.core.modules.PackageModuleFactory.IExtendedModuleResource;
import org.jboss.ide.eclipse.as.core.modules.PackageModuleFactory.PackagedModuleDelegate;
import org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger.PublisherFileUtilListener;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class PackagesPublisher implements IJBossServerPublisher {
	
	protected IDeployableServer server;
	protected IModuleResourceDelta[] delta;
	protected EventLogTreeItem eventRoot;
	
	public PackagesPublisher(IDeployableServer server, EventLogTreeItem eventContext) {
		this.server = server;
		eventRoot = eventContext;
	}
	public PackagesPublisher(IServer server, EventLogTreeItem eventContext) {
		this( ServerConverter.getDeployableServer(server), eventContext);
	}
	public void setDelta(IModuleResourceDelta[] delta) {
		this.delta = delta;
	}

	
	public int getPublishState() {
		return IServer.PUBLISH_STATE_NONE;
	}

    public IStatus publishModule(IModule[] module, int publishType, IProgressMonitor monitor) throws CoreException {
		try {
			IModule module2 = module[0];
	    	// if it's being removed
	    	if( publishType == REMOVE_PUBLISH ) {
	    		removeModule(module2, monitor);
	    	} else if( publishType == FULL_PUBLISH ) {
	    		publishModule(module2, false, monitor);
	    	} else if( publishType == INCREMENTAL_PUBLISH ) {
	    		publishModule(module2, true, monitor);
	    	}
		}catch(Exception e) {
			IStatus status = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Error during publish", e);
			JBossServerCorePlugin.getDefault().getLog().log(status);
		}
		return null;
	}

	protected void removeModule(IModule module, IProgressMonitor monitor) {
		IArchive pack = getPackage(module);
		// remove all of the deployed items
		if( pack != null ) {
			IPath sourcePath = pack.getArchiveFilePath();
			IPath destPath = new Path(server.getDeployDirectory()).append(sourcePath.lastSegment());
			// remove the entire file or folder
			PublisherFileUtilListener listener = new PublisherFileUtilListener(eventRoot);
			FileUtil.safeDelete(destPath.toFile(), listener);
		}
	}
	

	
	protected void publishModule(IModule module, boolean incremental, IProgressMonitor monitor) {
		IArchive pack = getPackage(module);
		IPath sourcePath = pack.getArchiveFilePath();
		IPath destPathRoot = new Path(server.getDeployDirectory());
		
		// if destination is deploy directory... no need to re-copy!
		if( destPathRoot.toOSString().equals(pack.getGlobalDestinationPath().toOSString())) {
			// fire null publish event
			return;
		}

		PublisherFileUtilListener listener = new PublisherFileUtilListener(eventRoot);
		if( incremental ) {
			eventRoot.setProperty(PublisherEventLogger.CHANGED_FILE_COUNT, countChanges(delta));
			publishFromDelta(module, destPathRoot, sourcePath.removeLastSegments(1), delta, listener);
		} else {
			// full publish, copy whole folder or file
			eventRoot.setProperty(PublisherEventLogger.CHANGED_FILE_COUNT, countConcreteFiles(module));
			FileUtil.fileSafeCopy(sourcePath.toFile(), destPathRoot.append(sourcePath.lastSegment()).toFile(), listener);
		}
	}

	protected void publishFromDelta(IModule module, IPath destPathRoot, IPath sourcePrefix, 
								IModuleResourceDelta[] delta, PublisherFileUtilListener listener) {
		ArrayList<IPath> changedFiles = new ArrayList<IPath>();
		for( int i = 0; i < delta.length; i++ ) {
			publishFromDeltaHandle(delta[i], destPathRoot, sourcePrefix, changedFiles, listener);
		}
	}
	
	protected int countChanges(IModuleResourceDelta[] deltas) {
		IModuleResource res;
		int count = 0;
		if( deltas == null ) return 0;
		for( int i = 0; i < deltas.length; i++ ) {
			res = deltas[i].getModuleResource();
			if( res != null && res instanceof IModuleFile)
				count++;
			count += countChanges(deltas[i].getAffectedChildren());
		}
		return count;
	}
	
	protected int countConcreteFiles(IModule module) {
		PackagedModuleDelegate delegate = (PackagedModuleDelegate)module.loadAdapter(PackagedModuleDelegate.class, new NullProgressMonitor());
		try {
			ArrayList list = new ArrayList();
			countConcreteFiles(delegate.members()[0], list);
			return list.size();
		} catch( CoreException ce ) {
			
		}
		return -1;
	}
	protected void countConcreteFiles(IModuleResource mr, ArrayList list) {
		if( mr instanceof IExtendedModuleResource) {
			IExtendedModuleResource emr = ((IExtendedModuleResource)mr);
			IPath p = emr.getConcreteDestFile();
			if( mr instanceof IModuleFile && !list.contains(p)) list.add(p);
			if( mr instanceof IModuleFolder) {
				IModuleResource[] children = ((IModuleFolder)mr).members();
				for( int i = 0; i < children.length; i++ )
					countConcreteFiles(children[i], list);
			}
		}
	}
	
	protected void publishFromDeltaHandle(IModuleResourceDelta delta, IPath destRoot, 
			IPath sourcePrefix, ArrayList<IPath> changedFiles, PublisherFileUtilListener listener) {
		switch( delta.getKind()) {
		case IModuleResourceDelta.REMOVED:
			// removed might not be IExtendedModuleResource
			IModuleResource imr = delta.getModuleResource();
			if( imr instanceof IExtendedModuleResource) {
				IExtendedModuleResource emr = ((IExtendedModuleResource)imr);
				IPath concrete = emr.getConcreteDestFile();
				if( !changedFiles.contains(concrete)) {  
					IPath destPath = destRoot.append(concrete.removeFirstSegments(sourcePrefix.segmentCount()));

					// file hasnt been updated yet. 
					// But we don't know whether to delete or copy this file. 
					// depends where it is in the tree and what's exploded. 
					changedFiles.add(concrete);
					IPath concreteRelative = concrete.removeFirstSegments(sourcePrefix.segmentCount()).setDevice(null);
					IPath emrModRelative = emr.getModuleRelativePath();
					boolean delete = concreteRelative.equals(emrModRelative);
					
					if( delete ) {
						FileUtil.safeDelete(destPath.toFile(), listener);
					} else {
						// copy
						FileUtil.fileSafeCopy(concrete.toFile(), destPath.toFile(), listener);
					}
				}
				return;
			} else {
				// TODO
				return;
			}
		case IModuleResourceDelta.ADDED:
			imr = delta.getModuleResource();
			if( imr instanceof IExtendedModuleResource) {
				IPath concrete = ((IExtendedModuleResource)imr).getConcreteDestFile();
				if( !changedFiles.contains(concrete)) {
					changedFiles.add(concrete);
					IPath destPath = destRoot.append(concrete.removeFirstSegments(sourcePrefix.segmentCount()));
					FileUtil.fileSafeCopy(concrete.toFile(), destPath.toFile(), listener);
				}
				return;
			} else {
				// TODO
				return;
			}
		case IModuleResourceDelta.CHANGED:
			imr = delta.getModuleResource();
			if( imr instanceof ExtendedModuleFile ) {
				IPath concrete = ((ExtendedModuleFile)imr).getConcreteDestFile();
				if( !changedFiles.contains(concrete)) {
					changedFiles.add(concrete);
					IPath destPath = destRoot.append(concrete.removeFirstSegments(sourcePrefix.segmentCount()));
					FileUtil.fileSafeCopy(concrete.toFile(), destPath.toFile(), listener);
				}
			}
			break;			
		}
		
		IModuleResourceDelta[] children = delta.getAffectedChildren();
		if( children != null ) {
			for( int i = 0; i < children.length; i++ ) {
				publishFromDeltaHandle(children[i], destRoot, sourcePrefix, changedFiles, listener);
			}
		}
	}
	
	protected IArchive getPackage(IModule module) {
		PackagedModuleDelegate delegate = (PackagedModuleDelegate)module.loadAdapter(PackagedModuleDelegate.class, new NullProgressMonitor());
		return delegate == null ? null : delegate.getPackage();
	}
}
