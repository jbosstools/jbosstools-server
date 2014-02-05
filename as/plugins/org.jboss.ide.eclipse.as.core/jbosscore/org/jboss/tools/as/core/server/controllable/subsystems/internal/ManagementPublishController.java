/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.as.core.server.controllable.subsystems.internal;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.modules.ResourceModuleResourceUtil;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilterProvider;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ModuleResourceUtil;
import org.jboss.ide.eclipse.as.core.util.ProgressMonitorUtil;
import org.jboss.ide.eclipse.as.management.core.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerUtil;
import org.jboss.ide.eclipse.as.management.core.JBoss7ServerState;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishController;
import org.jboss.ide.eclipse.as.wtp.core.server.publish.LocalZippedModulePublishRunner;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;

public class ManagementPublishController extends AbstractSubsystemController
		implements IPublishController {

	private IJBoss7ManagerService service;
	private AS7ManagementDetails managementDetails;
	private IJBoss7ManagerService getService() {
		if( service == null ) {
			this.service = JBoss7ManagerUtil.getService(getServer());
			this.managementDetails = new AS7ManagementDetails(getServer());

		}
		return service;
	}
	
	private IStatus isRunning() {
		if( getServer().getServerState() != IServer.STATE_STARTED )
			//return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "The server must be started to publish."); //$NON-NLS-1$
			return Status.CANCEL_STATUS;
		
		Exception e = null;
		try {
			JBoss7ServerState serverState = null;
			serverState = getService().getServerState(managementDetails);
			if(serverState == JBoss7ServerState.RUNNING)
				return Status.OK_STATUS;
		} catch (Exception e2) {
			e = e2;
		}
		return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "JBossTools is unable to verify that the server is up and responsive.", e); //$NON-NLS-1$
	}
	
	
	@Override
	public IStatus canPublish() {
		return isRunning();
	}

	@Override
	public boolean canPublishModule(IModule[] module) {
		return isRunning().isOK();
	}

	@Override
	public void publishStart(IProgressMonitor monitor) throws CoreException {
		// intentionally blank
		IStatus canPublish = canPublish();
		if( !canPublish.isOK() && canPublish().getSeverity() != IStatus.CANCEL) {
			throw new CoreException(canPublish);
		}
	}

	@Override
	public void publishFinish(IProgressMonitor monitor) throws CoreException {
		// intentionally blank
	}

	@Override
	public int publishModule(int kind, int deltaKind, IModule[] module,
			IProgressMonitor monitor) throws CoreException {
		IStatus canPublish = canPublish();
		if( !canPublish.isOK() && canPublish.getSeverity() != IStatus.CANCEL) {
			IStatus error = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "The server must be started to publish."); //$NON-NLS-1$
			throw new CoreException(error);
		}
		if( canPublish.getSeverity() == IStatus.CANCEL) {
			// Do nothing, server is stopped
			return getServer().getModulePublishState(module);
		}
		
		if( module.length > 1 ) {
			return IServer.PUBLISH_STATE_NONE;
		}
		
		int publishType = getPublishType(module, kind, deltaKind);
		if( publishType == IJBossServerPublisher.NO_PUBLISH) {
			// Do nothing, server is stopped
			return getServer().getModulePublishState(module);
		}
		if( publishType == IJBossServerPublisher.REMOVE_PUBLISH) {
			String name = module[0].getName() + getDefaultSuffix(module[0]);
			IJBoss7DeploymentResult removeResult = getService().undeploySync(managementDetails, name, true,  monitor);
			IStatus result = removeResult.getStatus();
			if( result.isOK())
				return IServer.PUBLISH_STATE_NONE;
			return IServer.PUBLISH_STATE_FULL;
		}
		
		
		monitor.beginTask("Publishing " + module[0].getName(), 1000); //$NON-NLS-1$

		
		boolean isBinaryObject = ServerModelUtilities.isBinaryModule(module);
		File[] toTransfer = null;
		if( !isBinaryObject ) {
			File file = zipLocally(module[0], publishType, ProgressMonitorUtil.submon(monitor, 100));
			toTransfer = new File[]{file};
		} else {
			IModuleResource[] resources = ModuleResourceUtil.getMembers(module[0]);
			// How to handle binary modules?? 
			// Single file deployable is easy if its just the 1 file,
			// but when its a folder, not sure what to do here yet
		}
		
		if( toTransfer != null ) {
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FAIL, "Deployment of module " + module[0].getName() + " has failed", null);  //$NON-NLS-1$//$NON-NLS-2$
			IProgressMonitor transferMonitor = ProgressMonitorUtil.submon(monitor, 900);
			transferMonitor.beginTask("Transfering " + module[0].getName(), 100*toTransfer.length); //$NON-NLS-1$
			for( int i = 0; i < toTransfer.length; i++ ) {
				// Maybe name will be customized in UI? 
				IJBoss7DeploymentResult removeResult = getService().undeploySync(managementDetails, toTransfer[i].getName(), true,  ProgressMonitorUtil.submon(transferMonitor, 10));
				ms.add(removeResult.getStatus());
				
				IJBoss7DeploymentResult result = getService().deploySync(managementDetails, toTransfer[i].getName(), toTransfer[i], 
						true, ProgressMonitorUtil.submon(transferMonitor, 100));
				IStatus s = result.getStatus();
				ms.add(s);
			}
			if( ms.isOK()) {
				return IServer.PUBLISH_STATE_NONE;
			}
			ServerLogger.getDefault().log(getServer(), ms);
			return IServer.PUBLISH_STATE_FULL;
		} else {
			// TODO error?
		}
		
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void publishServer(int kind, IProgressMonitor monitor)
			throws CoreException {
		// intentionally blank
	}
	
	
	private File zipLocally(IModule module, int publishType, IProgressMonitor monitor) throws CoreException {
		// Zip into a temporary folder, then transfer to the proper location
		IPath localTempLocation = getMetadataTemporaryLocation(getServer());
		String name = module.getName() + getDefaultSuffix(module);
		IPath tmpArchive = localTempLocation.append(name);
		
		LocalZippedModulePublishRunner runner = createZippedRunner(module, tmpArchive); 

		IStatus result = null;
		if( publishType == IJBossServerPublisher.FULL_PUBLISH) {
			result = runner.fullPublishModule(ProgressMonitorUtil.submon(monitor, 100));
		} else if( publishType == IJBossServerPublisher.INCREMENTAL_PUBLISH) {
			result = runner.incrementalPublishModule(ProgressMonitorUtil.submon(monitor, 100));
		}
		if( result != null && result.isOK()) {
			if( tmpArchive.toFile().exists()) {
				return tmpArchive.toFile();
			}
		}
		throw new CoreException(result);
	}

	private IPath getMetadataTemporaryLocation(IServer server) {
		IPath deployRoot = JBossServerCorePlugin.getServerStateLocation(server).
			append(IJBossToolingConstants.TEMP_REMOTE_DEPLOY).makeAbsolute();
		deployRoot.toFile().mkdirs();
		return deployRoot;
	}
	
	
	// Duplicated code. Unify with StandardFileSystemPublishController
	private int getPublishType(IModule[] module, int kind, int deltaKind) {
		int modulePublishState = getServer().getModulePublishState(module);
		if( deltaKind == ServerBehaviourDelegate.ADDED ) 
			return IJBossServerPublisher.FULL_PUBLISH;
		else if (deltaKind == ServerBehaviourDelegate.REMOVED) {
			return IJBossServerPublisher.REMOVE_PUBLISH;
		} else if (kind == IServer.PUBLISH_FULL 
				|| modulePublishState == IServer.PUBLISH_STATE_FULL 
				|| kind == IServer.PUBLISH_CLEAN ) {
			return IJBossServerPublisher.FULL_PUBLISH;
		} else if (kind == IServer.PUBLISH_INCREMENTAL 
				|| modulePublishState == IServer.PUBLISH_STATE_INCREMENTAL 
				|| kind == IServer.PUBLISH_AUTO) {
			if( ServerBehaviourDelegate.CHANGED == deltaKind ) 
				return IJBossServerPublisher.INCREMENTAL_PUBLISH;
		} 
		return IJBossServerPublisher.NO_PUBLISH;
	}
	
	private String getDefaultSuffix(IModule module) {
		return ServerModelUtilities.getDefaultSuffixForModule(module);
		
	}

	private IModulePathFilterProvider getModulePathFilterProvider() {
		return new IModulePathFilterProvider() {
			public IModulePathFilter getFilter(IServer server, IModule[] module) {
				return ResourceModuleResourceUtil.findDefaultModuleFilter(module[module.length-1]);
			}
		};
	}
	
	private LocalZippedModulePublishRunner createZippedRunner(IModule m, IPath p) {
		return new LocalZippedModulePublishRunner(getServer(), m,p, getModulePathFilterProvider());
	}
}
