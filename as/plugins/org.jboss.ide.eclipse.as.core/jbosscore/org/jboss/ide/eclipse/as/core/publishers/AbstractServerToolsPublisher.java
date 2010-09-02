/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.publishers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.common.project.facet.core.util.internal.ProgressMonitorUtil;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.util.ModuleFile;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil.IPublishCopyCallbackHandler;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;

/**
 * Class suitable for parsing any properly formed servertools-api module
 */
public abstract class AbstractServerToolsPublisher implements IJBossServerPublisher {
	protected IModuleResourceDelta[] delta;
	protected IDeployableServer server;
	protected int publishState = IServer.PUBLISH_STATE_NONE;
	protected IJBossServerPublishMethod publishMethod;
	
	public AbstractServerToolsPublisher() {}
	public abstract boolean accepts(String method, IServer server, IModule[] module);

	public int getPublishState() {
		return publishState;
	}
	
	protected void setPublishState(int state) {
		this.publishState = state;
	}

	public IStatus publishModule(IJBossServerPublishMethod method,
			IServer server, IModule[] module, int publishType,
			IModuleResourceDelta[] delta, IProgressMonitor monitor)
			throws CoreException {
		IStatus status = null;
		this.server = ServerConverter.getDeployableServer(server);
		this.delta = delta;
		this.publishMethod = method;
		
		boolean deleted = false;
		for( int i = 0; i < module.length; i++ ) {
			if( module[i].isExternal() )
				deleted = true;
		}

		// Monitor at this point has been begun with 1000 monitor
		IProgressMonitor subMon = ProgressMonitorUtil.submon(monitor, 1000);
		if (publishType == REMOVE_PUBLISH ) {
			status = unpublish(this.server, module, subMon);
		} else {
			if( deleted ) {
				publishState = IServer.PUBLISH_STATE_UNKNOWN;
			} else {
				if (publishType == FULL_PUBLISH ) {
					status = fullPublish(module, module[module.length-1], subMon);	
				} else if (publishType == INCREMENTAL_PUBLISH) {
					status = incrementalPublish(module, module[module.length-1], subMon);
				} 
			}
		}
		return status;
	}
		
	/**
	 * Gets the actual deploy path for this module 
	 * 
	 * @param moduleTree
	 * @param server
	 * @return
	 */
	protected IPath getDeployPath(IModule[] moduleTree, IDeployableServer server) {
		String folder = PublishUtil.getDeployRootFolder(
				moduleTree, server, 
				publishMethod.getPublishDefaultRootFolder(server.getServer()),
				IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC);
		return PublishUtil.getDeployPath(moduleTree, folder);
	}
		
	/**
	 * Finish up the publishing. This may be moving a final zipped entity into the proper
	 * folder or sending it over the wire to a remote machine.
	 * 
	 * Subclasses may override
	 * 
	 * @param publishType
	 * @param moduleTree
	 * @param server
	 * @param monitor
	 */
	protected void finishPublish(int publishType, IModule[] moduleTree, IDeployableServer server, IProgressMonitor monitor){}
	
	private IPublishCopyCallbackHandler getCallbackHandler(IPath path) {
		return publishMethod.getCallbackHandler(path, server.getServer());
	}
	
	/**
	 * For certain module trees, some publishers may want to force a child to be zipped.
	 * For example, JST Publisher may want to force utility project children to be zipped.
	 * 
	 * @param moduleTree
	 * @return
	 */
	protected boolean forceZipModule(IModule[] moduleTree) {
		return false;
	}
	
	protected IStatus canceledStatus() {
		return new Status(IStatus.CANCEL, JBossServerCorePlugin.PLUGIN_ID, "Publish Canceled"); //$NON-NLS-1$
	}
	
	protected IStatus fullPublish(IModule[] moduleTree, IModule module, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Full Publish", 1000); //$NON-NLS-1$
		
		IPath deployPath = getDeployPath(moduleTree, server);
		IPublishCopyCallbackHandler callback = getCallbackHandler(deployPath);
		IModuleResource[] members = PublishUtil.getResources(module, ProgressMonitorUtil.submon(monitor, 200));
 
		if( monitor.isCanceled())
			return canceledStatus();
		
		// First delete it
		// if the module we're publishing is a project, not a binary, clean it's folder
		if( !(new Path(module.getName()).segmentCount() > 1 ))
			callback.deleteResource(new Path("/"), ProgressMonitorUtil.submon(monitor, 100)); //$NON-NLS-1$

		if( monitor.isCanceled())
			return canceledStatus();

		ArrayList<IStatus> list = new ArrayList<IStatus>();

		boolean isBinaryObject = ServerModelUtilities.isBinaryModule(module);
		boolean forceZip = forceZipModule(moduleTree);
		
		if( !forceZip && !isBinaryObject) {
			PublishCopyUtil util = new PublishCopyUtil(callback);
			list.addAll(Arrays.asList(util.initFullPublish(members, ProgressMonitorUtil.submon(monitor, 700))));
		} else if( isBinaryObject )
			list.addAll(Arrays.asList(copyBinaryModule(moduleTree, ProgressMonitorUtil.submon(monitor, 700))));
		else {
			// A child that must be zipped, forceZip is true
			IPath deployRoot = JBossServerCorePlugin.getServerStateLocation(server.getServer()).
				append(IJBossServerConstants.TEMP_DEPLOY).makeAbsolute();
			
			try {
				File temp = deployRoot.toFile().createTempFile(module.getName(), ".tmp", deployRoot.toFile()); //$NON-NLS-1$
				IPath tempFile = new Path(temp.getAbsolutePath());
				list.addAll(Arrays.asList(PublishUtil.packModuleIntoJar(moduleTree[moduleTree.length-1], tempFile)));
				IPublishCopyCallbackHandler handler = getCallbackHandler(new Path("/")); //$NON-NLS-1$
				String parentFolder = deployPath.removeLastSegments(1).toString();
				handler.makeDirectoryIfRequired(new Path(parentFolder), ProgressMonitorUtil.submon(monitor, 200));
				ModuleFile mf = new ModuleFile(tempFile.toFile(), tempFile.lastSegment(), tempFile);
				handler.copyFile(mf, deployPath, ProgressMonitorUtil.submon(monitor, 500));
			} catch( IOException ioe) {
				list.add(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, ioe.getMessage(), ioe));
			}
		}
		
		monitor.done();
		if( list.size() > 0 ) 
			return createMultiStatus(list, module);
		return Status.OK_STATUS;
	}
		
	protected IStatus incrementalPublish(IModule[] moduleTree, IModule module, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Incremental Publish", 100); //$NON-NLS-1$
		IStatus[] results = new IStatus[] {};
		IPath deployPath = getDeployPath(moduleTree, server);
		boolean isBinaryObject = ServerModelUtilities.isBinaryModule(module);
		boolean forceZip = forceZipModule(moduleTree);
		IPublishCopyCallbackHandler  handler = null;
		if( !forceZip && !isBinaryObject) {
			handler = getCallbackHandler(deployPath);
			results = new PublishCopyUtil(handler).publishDelta(delta, ProgressMonitorUtil.submon(monitor, 100));
		} else if( delta.length > 0 ) {
			if( isBinaryObject)
				results = copyBinaryModule(moduleTree, ProgressMonitorUtil.submon(monitor, 100));
			else {
				// forceZip a child module
				IPath localDeployRoot = JBossServerCorePlugin.getServerStateLocation(server.getServer()).
					append(IJBossServerConstants.TEMP_DEPLOY).makeAbsolute(); 
				try {
					File temp = File.createTempFile(module.getName(), ".tmp", localDeployRoot.toFile()); //$NON-NLS-1$
					IPath tempFile = new Path(temp.getAbsolutePath());
					PublishUtil.packModuleIntoJar(moduleTree[moduleTree.length-1], tempFile);
					handler = getCallbackHandler(new Path("/"));		 //$NON-NLS-1$
					String parentFolder = deployPath.removeLastSegments(1).toString();
					handler.makeDirectoryIfRequired(new Path(parentFolder), ProgressMonitorUtil.submon(monitor, 50));
					ModuleFile mf = new ModuleFile(tempFile.toFile(), tempFile.lastSegment(), tempFile);
					handler.copyFile(mf, deployPath, ProgressMonitorUtil.submon(monitor, 50));
				} catch( IOException ioe) {
					IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, ioe.getMessage(), ioe);
					results = new IStatus[] { s };
				}
			}
		}
		
		monitor.done();
		if( results != null && results.length > 0 ) {
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_INC_FAIL, 
					NLS.bind(Messages.IncrementalPublishFail, module.getName()), null);
			for( int i = 0; i < results.length; i++ )
				ms.add(results[i]);
			return ms;
		}
		
		if( handler != null && handler.shouldRestartModule() ) {
			JSTPublisherXMLToucher.getInstance().touch(deployPath, module, handler);
		}

		IStatus ret = new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FULL_SUCCESS, 
				NLS.bind(Messages.CountModifiedMembers, PublishUtil.countChanges(delta), module.getName()), null);
		return ret;
	}
	
	protected IStatus createMultiStatus(List<IStatus> list, IModule module) {
		MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FULL_FAIL, 
				NLS.bind(Messages.FullPublishFail, module.getName()), null);
		for( int i = 0; i < list.size(); i++ )
			ms.add(list.get(i));
		return ms;
	}
	
	protected IStatus[] copyBinaryModule(IModule[] moduleTree, IProgressMonitor monitor) {
		monitor.beginTask("Copy Binary Module", 100); //$NON-NLS-1$
		try {
			IPath destinationPath = getDeployPath(moduleTree, server);
			IModuleResource[] members = PublishUtil.getResources(moduleTree);
			File source = PublishUtil.getFile(members[0]);
			if( source != null ) {
				IPublishCopyCallbackHandler handler = getCallbackHandler(new Path("/"));		 //$NON-NLS-1$
				IPath localFilePath = new Path(source.getAbsolutePath());
				ModuleFile mf = new ModuleFile(localFilePath.toFile(), localFilePath.lastSegment(), localFilePath);
				handler.copyFile(mf, destinationPath, new NullProgressMonitor());
			} else {
//				IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_COPY_BINARY_FAIL,
//						NLS.bind(Messages.CouldNotPublishModule,
//								moduleTree[moduleTree.length-1]), null);
//				return new IStatus[] {s};
				// TODO
			}
		} catch( CoreException ce ) {
			return new IStatus[] {ce.getStatus()};
		}
		monitor.done();
		return new IStatus[]{Status.OK_STATUS};
	}
	
	protected IStatus unpublish(IDeployableServer jbServer, IModule[] module,
			IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Begin Unpublish", 100); //$NON-NLS-1$
		IPath remotePath = getDeployPath(module, server);
		IPublishCopyCallbackHandler handler = getCallbackHandler(new Path("/")); //$NON-NLS-1$
		handler.deleteResource(remotePath, ProgressMonitorUtil.submon(monitor, 100));
		monitor.done();
		return Status.OK_STATUS;
	}
}
