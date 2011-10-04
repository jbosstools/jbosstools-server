/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
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
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.util.ModuleFile;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
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
	
	/**
	 * This abstract publisher is only suitable for non force-zipped deployments
	 */
	public boolean accepts(String method, IServer server, IModule[] module) {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		if( ds == null ) 
			return false;
		// If this is a root module (not nested) and is binary, let this publisher handle it
		if( module.length == 1 && ServerModelUtilities.isBinaryModule(module[module.length-1]))
			return true;
		return !ds.zipsWTPDeployments();
	}

	public int getPublishState() {
		return publishState;
	}
	
	protected void setPublishState(int state) {
		this.publishState = state;
	}

	public static class CustomSubProgress extends SubProgressMonitor {
		public CustomSubProgress(IProgressMonitor monitor, int ticks, int style) {
			super(monitor, ticks, style);
		}
		public void beginTask(String name, int totalWork) {
			super.beginTask(null, totalWork);
			setTaskName(name);
		}
	}
	
	public static IProgressMonitor getSubMon(IProgressMonitor parent, int ticks) {
		IProgressMonitor subMon = new CustomSubProgress(parent, ticks, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
		return subMon;
	}
	
	public IStatus publishModule(IJBossServerPublishMethod method,
			IServer server, IModule[] module, int publishType,
			IModuleResourceDelta[] delta, IProgressMonitor monitor)
			throws CoreException {
		IStatus status = null;
		this.server = ServerConverter.getDeployableServer(server);
		this.delta = delta;
		this.publishMethod = method;
		
		// Monitor at this point has been begun with 1000 monitor
		IProgressMonitor subMon = getSubMon(monitor, 1000);
		if (publishType == REMOVE_PUBLISH ) {
			status = unpublish(this.server, module, subMon);
		} else {
			if( ServerModelUtilities.isAnyDeleted(module) ) {
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
		return PublishUtil.getDeployPath(publishMethod, moduleTree, server);
	}

	/**
	 * Gets the actual deploy path for this module's parent
	 * Given modules *MUST* be of length 2 or more
	 * 
	 * @param moduleTree
	 * @param server
	 * @return
	 */
	protected IPath getParentDeployPath(IModule[] moduleTree, IDeployableServer server) {
		IModule[] tree2 = new IModule[moduleTree.length -1];
		for( int i = 0; i < moduleTree.length-1; i++ ) {
			tree2[i] = moduleTree[i];
		}
		if( tree2.length == 0 ) 
			return new Path(publishMethod.getPublishDefaultRootFolder(server.getServer()));
		return PublishUtil.getDeployPath(publishMethod, tree2, server);
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
	protected void finishPublish(int publishType, IModule[] moduleTree, IDeployableServer server, IProgressMonitor monitor){
	}
	
	protected IPublishCopyCallbackHandler getCallbackHandler(IPath path) {
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
		return PublishUtil.deployPackaged(moduleTree);
	}
	
	protected IStatus canceledStatus() {
		return new Status(IStatus.CANCEL, JBossServerCorePlugin.PLUGIN_ID, "Publish Canceled"); //$NON-NLS-1$
	}
	
	protected IStatus fullPublish(IModule[] moduleTree, IModule module, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Full Publish: " + moduleTree[moduleTree.length-1].getName(), 1000); //$NON-NLS-1$
		
		IPath deployPath = getDeployPath(moduleTree, server);
		IPublishCopyCallbackHandler callback = getCallbackHandler(getRootPath(deployPath).append(deployPath));
		IModuleResource[] members = PublishUtil.getResources(module, getSubMon(monitor, 200));
 
		if( monitor.isCanceled())
			return canceledStatus();
		
		// First delete it
		// if the module we're publishing is a project, not a binary, clean it's folder
		//if( !(new Path(module.getName()).segmentCount() > 1 ))
		//if( !ServerModelUtilities.isBinaryModule(module))
			callback.deleteResource(new Path("/"), getSubMon(monitor, 100)); //$NON-NLS-1$

		if( monitor.isCanceled())
			return canceledStatus();

		List<IStatus> list = new ArrayList<IStatus>();

		boolean isBinaryObject = ServerModelUtilities.isBinaryModule(module);
		boolean forceZip = forceZipModule(moduleTree);
		
		if( !forceZip && !isBinaryObject) {
			PublishCopyUtil util = new PublishCopyUtil(callback);
			list.addAll(Arrays.asList(util.initFullPublish(members, getSubMon(monitor, 700))));
			JSTPublisherXMLToucher.getInstance().touch(deployPath, module, callback);
		} else if( isBinaryObject )
			list.addAll(Arrays.asList(copyBinaryModule(moduleTree, getSubMon(monitor, 700))));
		else {
			list.addAll(Arrays.asList(transferForceZippedChild(deployPath, module, moduleTree, monitor)));
		}
		
		monitor.done();
		if( list.size() > 0 ) 
			return createMultiStatus(list, module);

		Status status = new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, 
				IEventCodes.JST_PUB_FULL_SUCCESS, 
				NLS.bind(Messages.ModulePublished, module.getName()), null);
		return status;
	}
	
	protected IStatus[] transferForceZippedChild(IPath deployPath, IModule module, IModule[] moduleTree, IProgressMonitor monitor) throws CoreException {
		// A child that must be zipped, forceZip is true
		ArrayList<IStatus> list = new ArrayList<IStatus>();
		IPath deployRoot = JBossServerCorePlugin.getServerStateLocation(server.getServer()).
			append(IJBossToolingConstants.TEMP_DEPLOY).makeAbsolute();
		
		try {
			// Make local jar copy
			File temp = File.createTempFile(module.getName(), ".tmp", deployRoot.toFile()); //$NON-NLS-1$
			IPath tempFile = new Path(temp.getAbsolutePath());
			list.addAll(Arrays.asList(PublishUtil.packModuleIntoJar(moduleTree[moduleTree.length-1], tempFile)));
			
			// TODO !!!!! Transfer it
			IPath deployPathInner = getParentDeployPath(moduleTree, server);
			IPublishCopyCallbackHandler handler = getCallbackHandler(getRootPath(deployPathInner).append(deployPathInner));
			IPath filePath = deployPath.removeFirstSegments(deployPathInner.segments().length);
			IPath parentFolderPath = filePath.removeLastSegments(1);
			handler.makeDirectoryIfRequired(parentFolderPath, getSubMon(monitor, 200));
			ModuleFile mf = new ModuleFile(tempFile.toFile(), tempFile.lastSegment(), tempFile);
			handler.copyFile(mf, filePath, getSubMon(monitor, 500));
			
			// Cleanup
			tempFile.toFile().delete();
		} catch( IOException ioe) {
			list.add( new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, ioe.getMessage(), ioe));
		}
		return list.toArray(new IStatus[list.size()]);
	}
		
	// TODO consider moving to utility class?
	public static Path getRootPath(IPath deployPath) {
		String root = (deployPath.getDevice() == null ? "" : deployPath.getDevice()) + "/";  //$NON-NLS-1$//$NON-NLS-2$
		return new Path(root);
	}

	
	protected IStatus incrementalPublish(IModule[] moduleTree, IModule module, IProgressMonitor monitor) throws CoreException {
		IStatus[] results = new IStatus[] {};
		IPath deployPath = getDeployPath(moduleTree, server);
		IPublishCopyCallbackHandler h1 = getCallbackHandler(deployPath);
		// quick switch to full publish for JBIDE-9112, recent switch from zip to unzipped requires full publish
		if( h1.isFile(new Path("/"), new NullProgressMonitor())) { //$NON-NLS-1$
			return fullPublish(moduleTree, module, monitor);
		}

		
		boolean isBinaryObject = ServerModelUtilities.isBinaryModule(module);
		monitor.beginTask("Incremental Publish: " + moduleTree[moduleTree.length-1].getName(), 100); //$NON-NLS-1$
		boolean forceZip = forceZipModule(moduleTree);
		IPublishCopyCallbackHandler  handler = null;
		if( !forceZip && !isBinaryObject) {
			handler = getCallbackHandler(deployPath);
			results = new PublishCopyUtil(handler).publishDelta(delta, getSubMon(monitor, 100));
		} else if( delta.length > 0 ) {
			if( isBinaryObject)
				results = copyBinaryModule(moduleTree, getSubMon(monitor, 100));
			else {
				// forceZip a child module
				results = transferForceZippedChild(deployPath, module, moduleTree, monitor);
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
		monitor.beginTask("Copying Child Module: " + moduleTree[moduleTree.length-1].getName(), 100); //$NON-NLS-1$
		try {
			IPath destinationPath = getDeployPath(moduleTree, server);
			IPath destinationFolder = destinationPath.removeLastSegments(1);
			IModuleResource[] members = PublishUtil.getResources(moduleTree);
			File source = PublishUtil.getFile(members[0]);
			if( source != null ) {
				IPublishCopyCallbackHandler handler = getCallbackHandler(getRootPath(destinationFolder).append(destinationFolder));
				IPath localFilePath = new Path(source.getAbsolutePath());
				ModuleFile mf = new ModuleFile(localFilePath.toFile(), localFilePath.lastSegment(), localFilePath);
				handler.copyFile(mf, new Path(destinationPath.lastSegment()), new NullProgressMonitor());
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
		return new IStatus[]{};
	}
	
	protected IStatus unpublish(IDeployableServer jbServer, IModule[] module,
			IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Removing Module: " + module[module.length-1].getName(), 100); //$NON-NLS-1$
		IPath remotePath = getDeployPath(module, server);
		IPublishCopyCallbackHandler handler = getCallbackHandler(getRootPath(remotePath).append(remotePath));
		handler.deleteResource(new Path("/"), getSubMon(monitor, 100)); //$NON-NLS-1$
		monitor.done();
		return Status.OK_STATUS;
	}
}
