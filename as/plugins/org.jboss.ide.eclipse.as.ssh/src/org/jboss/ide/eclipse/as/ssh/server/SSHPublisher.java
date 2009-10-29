/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ssh.server;

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
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil.IPublishCopyCallbackHandler;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil.LocalCopyCallback;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ssh.SSHDeploymentPlugin;
import org.jboss.ide.eclipse.as.ssh.server.SSHServerBehaviourDelegate.SSHPublishMethod;

import com.jcraft.jsch.Session;

public class SSHPublisher implements IJBossServerPublisher {
	protected IModuleResourceDelta[] delta;
	protected IDeployableServer server;
	protected int publishState = IServer.PUBLISH_STATE_NONE;
	protected SSHPublishMethod publishMethod;
	
	public SSHPublisher() {}

	public boolean accepts(String method, IServer server, IModule[] module) {
		if( !method.equals(SSHPublishMethod.SSH_PUBLISH_METHOD))
			return false;
		if( module == null )
			return true;
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		boolean shouldAccept = ds != null 
			&& ModuleCoreNature.isFlexibleProject(module[0].getProject())
			&& !SSHPublishUtil.getZipsSSHDeployments(server);
		return shouldAccept;
	}

	public int getPublishState() {
		return publishState;
	}

	public IStatus publishModule(IJBossServerPublishMethod method,
			IServer server, IModule[] module, int publishType,
			IModuleResourceDelta[] delta, IProgressMonitor monitor)
			throws CoreException {
		IStatus status = null;
		this.server = ServerConverter.getDeployableServer(server);
		this.delta = delta;
		this.publishMethod = (SSHPublishMethod)method;
		
		boolean deleted = false;
		for( int i = 0; i < module.length; i++ ) {
			if( module[i].isExternal() )
				deleted = true;
		}
		
		if (publishType == REMOVE_PUBLISH ) {
			status = unpublish(this.server, module, monitor);
		} else {
			if( deleted ) {
				publishState = IServer.PUBLISH_STATE_UNKNOWN;
			} else {
				if (publishType == FULL_PUBLISH ) {
					status = fullPublish(module, module[module.length-1], monitor);	
				} else if (publishType == INCREMENTAL_PUBLISH) {
					status = incrementalPublish(module, module[module.length-1], monitor);
				} 
			}
		}
		return status;
	}
	
	protected IStatus fullPublish(IModule[] moduleTree, IModule module, IProgressMonitor monitor) throws CoreException {
		IPath remoteDeployPath = getDeployPath(moduleTree, server);
		IModuleResource[] members = PublishUtil.getResources(module);
 
		// First delete it
		// if the module we're publishing is a project, not a binary, clean it's folder
		if( !(new Path(module.getName()).segmentCount() > 1 ))
			SSHZippedJSTPublisher.launchCommand(publishMethod.getSession(), "rm -rf " + remoteDeployPath.toString(), monitor);

		ArrayList<IStatus> list = new ArrayList<IStatus>();

		if( !PublishUtil.deployPackaged(moduleTree) && !PublishUtil.isBinaryObject(moduleTree)) {
			SSHCopyCallback callback = new SSHCopyCallback(remoteDeployPath, publishMethod);
			PublishCopyUtil util = new PublishCopyUtil(callback);
			list.addAll(Arrays.asList(util.publishFull(members, monitor)));
		}
		else if( PublishUtil.isBinaryObject(moduleTree))
			list.addAll(Arrays.asList(copyBinaryModule(moduleTree, monitor)));
		else {
			IPath deployRoot = JBossServerCorePlugin.getServerStateLocation(server.getServer()).
				append(IJBossServerConstants.DEPLOY).makeAbsolute();
			try {
				File temp = deployRoot.toFile().createTempFile(module.getName(), ".tmp", deployRoot.toFile());
				IPath tempFile = new Path(temp.getAbsolutePath());
				list.addAll(Arrays.asList(PublishUtil.packModuleIntoJar(moduleTree[moduleTree.length-1], tempFile)));
				mkdirAndCopy(publishMethod.getSession(), tempFile.toString(), remoteDeployPath.toString());
			} catch( IOException ioe) {
				list.add(new Status(IStatus.ERROR, SSHDeploymentPlugin.PLUGIN_ID, ioe.getMessage(), ioe));
			}
		}
		
		if( list.size() > 0 ) 
			return createMultiStatus(list, module);
		return Status.OK_STATUS;
	}
	
	protected IStatus incrementalPublish(IModule[] moduleTree, IModule module, IProgressMonitor monitor) throws CoreException {
		IStatus[] results = new IStatus[] {};
		IPath remoteDeployPath = getDeployPath(moduleTree, server);
		if( !PublishUtil.deployPackaged(moduleTree) && !PublishUtil.isBinaryObject(moduleTree)) {
			SSHCopyCallback handler = new SSHCopyCallback(remoteDeployPath, publishMethod);
			results = new PublishCopyUtil(handler).publishDelta(delta, monitor);
		} else if( delta.length > 0 ) {
			if( PublishUtil.isBinaryObject(moduleTree))
				results = copyBinaryModule(moduleTree, monitor);
			else {
				IPath localDeployRoot = JBossServerCorePlugin.getServerStateLocation(server.getServer()).
					append(IJBossServerConstants.DEPLOY).makeAbsolute(); 
				try {
					File temp = localDeployRoot.toFile().createTempFile(module.getName(), ".tmp", localDeployRoot.toFile());
					IPath tempFile = new Path(temp.getAbsolutePath());
					PublishUtil.packModuleIntoJar(moduleTree[moduleTree.length-1], tempFile);
					mkdirAndCopy(publishMethod.getSession(), tempFile.toString(), remoteDeployPath.toString());
				} catch( IOException ioe) {
					IStatus s = new Status(IStatus.ERROR, SSHDeploymentPlugin.PLUGIN_ID, ioe.getMessage(), ioe);
					results = new IStatus[] { s };
				}
			}
		}
		
		if( results != null && results.length > 0 ) {
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_INC_FAIL, 
					NLS.bind(Messages.IncrementalPublishFail, module.getName()), null);
			for( int i = 0; i < results.length; i++ )
				ms.add(results[i]);
			return ms;
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
		try {
			IPath remoteDeployPath = getDeployPath(moduleTree, server);
			IModuleResource[] members = PublishUtil.getResources(moduleTree);
			File source = PublishUtil.getFile(members[0]);
			if( source != null ) {
				SSHZippedJSTPublisher.launchCopyCommand(publishMethod.getSession(), source.toString(), remoteDeployPath.toString(), monitor);
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
		return new IStatus[]{Status.OK_STATUS};
	}
	
	protected IStatus unpublish(IDeployableServer jbServer, IModule[] module,
			IProgressMonitor monitor) throws CoreException {
		IPath remotePath = getDeployPath(module, server);
		SSHZippedJSTPublisher.launchCommand(publishMethod.getSession(), "rm -rf " + remotePath.toString(), monitor);
		return Status.OK_STATUS;
	}
	
	public static IPath getDeployPath(IModule[] moduleTree, IDeployableServer server) {
		return PublishUtil.getDeployPath(moduleTree, getRemoteDeployFolder(server.getServer()));
	}

	public static String getRemoteDeployFolder(IServer server) {
		return ((Server)server).getAttribute(ISSHDeploymentConstants.DEPLOY_DIRECTORY, (String)null);
	}

	public static void mkdirAndCopy(Session session, String localFile, String remoteFile) throws CoreException {
		String parentFolder = new Path(remoteFile).removeLastSegments(1).toString();
		SSHZippedJSTPublisher.launchCommand(session, "mkdir -p " + parentFolder, new NullProgressMonitor());
		SSHZippedJSTPublisher.launchCopyCommand(session, localFile, remoteFile, new NullProgressMonitor());
	}

	public static class SSHCopyCallback implements IPublishCopyCallbackHandler {

		private IPath root;
		private SSHPublishMethod method;
		public SSHCopyCallback(IPath deployRoot, SSHPublishMethod method) {
			this.root = deployRoot;
			this.method = method;
		}
		
		public IStatus[] copyFile(IModuleFile mf, IPath path,
				IProgressMonitor monitor) throws CoreException {
			File sourceFile = PublishUtil.getFile(mf);
			IPath destination = root.append(path);
			mkdirAndCopy(method.getSession(), sourceFile.getAbsolutePath(), destination.toString());
			return new IStatus[]{};
		}

		public IStatus[] deleteResource(IPath path, IProgressMonitor monitor) {
			IPath remotePath = root.append(path);
			try {
				SSHZippedJSTPublisher.launchCommand(method.getSession(), "rm -rf " + remotePath.toString(), monitor);
			} catch( CoreException ce ) {
				return new IStatus[]{ce.getStatus()};
			}
			return new IStatus[] {};
		}

		public IStatus[] makeDirectoryIfRequired(IPath dir, IProgressMonitor monitor) {
			IPath remotePath = root.append(dir);
			try {
				SSHZippedJSTPublisher.launchCommand(method.getSession(), "mkdir -p " + remotePath.toString(), monitor);
			} catch( CoreException ce ) {
				return new IStatus[]{ce.getStatus()};
			}
			return new IStatus[] {};
		}
	}
}
