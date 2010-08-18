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
package org.jboss.ide.eclipse.as.rse.core;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.publishers.AbstractPublishMethod;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil.IPublishCopyCallbackHandler;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class RSEPublishMethod extends AbstractPublishMethod {

	public static final String RSE_ID = "rse"; //$NON-NLS-1$
	
	private DeployableServerBehavior behaviour;
	
	@Override
	public String getPublishMethodId() {
		return RSE_ID;
	}
	
	private IFileServiceSubSystem fileSubSystem = null;
	private IPath remoteRootFolder;
	private IPath remoteTemporaryFolder;
	public void publishStart(DeployableServerBehavior behaviour,
			IProgressMonitor monitor) throws CoreException {
		this.behaviour = behaviour;
		loadRemoteDeploymentDetails();
		if (fileSubSystem != null && !fileSubSystem.isConnected()) {
		    try {
		    	fileSubSystem.connect(monitor, false);
		    } catch (Exception e) {
		    }
		}
		super.publishStart(behaviour, monitor);
	}
	public IPath getRemoteRootFolder() {
		return remoteRootFolder;
	}
	public IPath getRemoteTemporaryFolder() {
		return remoteTemporaryFolder;
	}
	public IFileServiceSubSystem getFileServiceSubSystem() {
		return fileSubSystem;
	}
	public IFileService getFileService() {
		return fileSubSystem.getFileService();
	}
	
	public int publishFinish(DeployableServerBehavior behaviour,
			IProgressMonitor monitor) throws CoreException {
		return super.publishFinish(behaviour, monitor);
	}
	
	protected void loadRemoteDeploymentDetails() throws CoreException{
		// TODO obviously fix this
//		String homeDir = RSEUtils.getRSEHomeDir(behaviour.getServer());
//		String conf = RSEUtils.getRSEConfigName(behaviour.getServer());
		String connectionName = RSEUtils.getRSEConnectionName(behaviour.getServer());
//		this.remoteRootFolder = new Path("/home/rob/redhat/deploy"); //$NON-NLS-1$
//		this.remoteTemporaryFolder = new Path("/home/rob/redhat/tmp"); //$NON-NLS-1$
		JBossServer jbs = ServerConverter.getJBossServer(behaviour.getServer());
		this.remoteRootFolder = new Path(RSEUtils.getDeployRootFolder(jbs));
		this.remoteTemporaryFolder = new Path("/home/rob/redhat/tmp"); //$NON-NLS-1$
		
		IHost host = findHost(connectionName);
		if( host != null ) {
			fileSubSystem = findFileTransferSubSystem(host);
		} else {
			// TODO error host not found in RSE
		}
	}
	
	protected IHost findHost(String connectionName) {
		IHost[] allHosts = RSECorePlugin.getTheSystemRegistry().getHosts();
		for( int i = 0; i < allHosts.length; i++ ) {
			if( allHosts[i].getAliasName().equals(connectionName))
				return allHosts[i];
		}
		return null;
	}
	
	/*  approved files subsystems *
		ftp.files
		local.files
		ssh.files
	 */
	protected static List<String> APPROVED_FILE_SYSTEMS = 
		Arrays.asList(new String[]{ "ftp.files", "local.files", "ssh.files", "dstore.files"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	protected IFileServiceSubSystem findFileTransferSubSystem(IHost host) {
		ISubSystem[] systems = RSECorePlugin.getTheSystemRegistry().getSubSystems(host);
		for( int i = 0; i < systems.length; i++ ) {
			String tmp = systems[i].getConfigurationId();
			if( APPROVED_FILE_SYSTEMS.contains(systems[i].getConfigurationId()))
				return (IFileServiceSubSystem)systems[i];
		}
		return null;
	}
	
	public static IPath findModuleFolderWithDefault(IModule module, IDeployableServer server, IPath startingPath) {
		IModule[] moduleTree = new IModule[]{module};
		String folder = PublishUtil.getDeployRootFolder(
				moduleTree, server, startingPath.toString(),
				IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC);
		return PublishUtil.getDeployPath(moduleTree, folder).removeLastSegments(1);
	}

	
	public IPublishCopyCallbackHandler getCallbackHandler(IPath path, IServer server) {
		return new RSERemotePublishHandler(path, this);
	}

	public String getPublishDefaultRootFolder(IServer server) {
		return getRemoteRootFolder().toString();
	}

}
