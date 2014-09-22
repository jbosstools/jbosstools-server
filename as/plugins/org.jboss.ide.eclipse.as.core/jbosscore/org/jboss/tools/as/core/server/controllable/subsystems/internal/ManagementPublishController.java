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
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.modules.ResourceModuleResourceUtil;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilterProvider;
import org.jboss.ide.eclipse.as.core.server.internal.UpdateModuleStateJob;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.ModuleResourceUtil;
import org.jboss.ide.eclipse.as.core.util.ProgressMonitorUtil;
import org.jboss.ide.eclipse.as.management.core.IJBoss7DeploymentResult;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7DeploymentState;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerUtil;
import org.jboss.ide.eclipse.as.management.core.JBoss7ServerState;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IModuleStateController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPrimaryPublishController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishControllerDelegate;
import org.jboss.ide.eclipse.as.wtp.core.server.launch.AbstractStartJavaServerLaunchDelegate;
import org.jboss.ide.eclipse.as.wtp.core.server.publish.LocalZippedModulePublishRunner;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;
import org.jboss.tools.as.core.server.controllable.systems.IModuleDeployPathController;
import org.jboss.tools.as.core.server.controllable.util.PublishControllerUtility;

public class ManagementPublishController extends AbstractSubsystemController
		implements IPublishController, IPrimaryPublishController {

	/**
	 * Access the manager service for running remote mgmt commands
	 */
	private IJBoss7ManagerService service;
	
	/**
	 * The details to be passed to the management service. 
	 */
	private AS7ManagementDetails managementDetails;

	/*
	 * An optional dependency for verifying or modifying the deploy state of a module
	 */
	private IModuleStateController moduleStateController;
	
	/*
	 * A boolean to track the failure to load the optional controller
	 */
	private boolean moduleStateControllerLoadFailed = false;
	
	/*
	 * A required controller to determine the output name for 
	 * a module. 
	 */
	private IModuleDeployPathController moduleDeployPathController;
	
	private IJBoss7ManagerService getService() {
		if( service == null ) {
			this.service = JBoss7ManagerUtil.getService(getServer());
			this.managementDetails = new AS7ManagementDetails(getServer());
		}
		return service;
	}
	
	public IStatus validate() {
		try {
			IStatus sup = super.validate();
			if( !sup.isOK())
				return sup;
			getDeployPathController();
		} catch(CoreException ce) {
			return ce.getStatus();
		}
		return Status.OK_STATUS;
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
		IStatus isRunningStatus = isRunning();
		if( !isRunningStatus.isOK()) 
			return isRunningStatus;
		return validate();
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
		IServer s = getServer();
		((Server)s).setServerPublishState(getUpdatedPublishState(s));

		// update the wtp model with live module state from the server
		IModuleStateController c = getModuleStateController();
		if( c != null && getServer().getServerState() == IServer.STATE_STARTED) {
			new UpdateModuleStateJob( c, getServer(), true, 15000).schedule(5000);
		}
	}

	protected IModuleStateController getModuleStateController() throws CoreException {
		if( moduleStateController == null && !moduleStateControllerLoadFailed) {
			try {
				moduleStateController = (IModuleStateController)findDependency(IModuleStateController.SYSTEM_ID);
			} catch(CoreException ce) {
				// Do not log; this is optional. But trace
				moduleStateControllerLoadFailed = true;
			}
		}
		return moduleStateController;
	}

	protected IModuleDeployPathController getDeployPathController() throws CoreException {
		if( moduleDeployPathController == null) {
			moduleDeployPathController = (IModuleDeployPathController)findDependencyFromBehavior(IModuleDeployPathController.SYSTEM_ID);
		}
		return moduleDeployPathController;
	}
	
	/**
	 * Should we be minimizing redeployments at this time. 
	 * Currently, we minimize redeployments when the server is in debug mode, 
	 * and the user has enabled our custom hotcode replace mechanism.
	 * 
	 * @return
	 */
	protected boolean shouldMinimizeRedeployments() {
		boolean debugMode = ILaunchManager.DEBUG_MODE.equals(getServer().getMode());
		Object o = getControllableBehavior().getSharedData(AbstractStartJavaServerLaunchDelegate.HOTCODE_REPLACE_OVERRIDDEN);
		return debugMode && o instanceof Boolean && ((Boolean)o).booleanValue();
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
		((Server)getServer()).setModuleState(module, IServer.STATE_UNKNOWN);
		
		// first see if we need to delegate to another custom publisher, such as bpel / osgi
		IPublishControllerDelegate delegate = PublishControllerUtility.findDelegatePublishController(getServer(), module, true);
		if( delegate != null ) {
			return delegate.publishModule(kind, deltaKind, module, monitor);
		}

		
		int publishType = PublishControllerUtility.getPublishType(getServer(), module, kind, deltaKind);
		if( publishType == PublishControllerUtility.NO_PUBLISH) {
			// Do nothing, no publish is requested
			return getServer().getModulePublishState(module);
		}

		if( publishType == PublishControllerUtility.INCREMENTAL_PUBLISH && shouldMinimizeRedeployments()) {
			// Do nothing... we are minimizing redeployments at the moment
			return getServer().getModulePublishState(module);
		}

		if( publishType == PublishControllerUtility.REMOVE_PUBLISH) {
			return removeModule(module, monitor);
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
			ArrayList<File> fileList = new ArrayList<File>(resources.length);
			for( int i = 0; i < resources.length; i++ ) {
				File f = (File)resources[i].getAdapter(File.class);
				if( f != null ) {
					fileList.add(f);
				}
			}
			toTransfer = (File[]) fileList.toArray(new File[fileList.size()]);
		}
		
		if( toTransfer != null ) {
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FAIL, "Deployment of module " + module[0].getName() + " has failed", null);  //$NON-NLS-1$//$NON-NLS-2$
			IProgressMonitor transferMonitor = ProgressMonitorUtil.submon(monitor, 900);
			transferMonitor.beginTask("Transfering " + module[0].getName(), 100*toTransfer.length); //$NON-NLS-1$
			for( int i = 0; i < toTransfer.length; i++ ) {
				// Maybe name will be customized in UI? 
				JBoss7DeploymentState state = getService().getDeploymentState(managementDetails, toTransfer[i].getName());
				if( state != JBoss7DeploymentState.NOT_FOUND) {
					IJBoss7DeploymentResult removeResult = getService().undeploySync(managementDetails, toTransfer[i].getName(), true,  ProgressMonitorUtil.submon(transferMonitor, 10));
					ms.add(removeResult.getStatus());
				}
				
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

	private String getDeploymentOutputName(IServer server, IModule module) throws CoreException {
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(server);
		if( beh != null ) {
			IModule[] moduleToTest = new IModule[]{module};
			IModuleDeployPathController controller = getDeployPathController();
			return controller.getOutputName(moduleToTest);
		} 
		return null;
	}
	
	@Override
	public void publishServer(int kind, IProgressMonitor monitor)
			throws CoreException {
		// intentionally blank
	}
	
	
	private File zipLocally(IModule module, int publishType, IProgressMonitor monitor) throws CoreException {
		// Zip into a temporary folder, then transfer to the proper location
		IPath localTempLocation = getMetadataTemporaryLocation(getServer());
		String name = getDeploymentOutputName(getServer(), module);
		IPath tmpArchive = localTempLocation.append(name);
		
		LocalZippedModulePublishRunner runner = createZippedRunner(module, tmpArchive); 

		IStatus result = null;
		if( publishType == PublishControllerUtility.FULL_PUBLISH) {
			result = runner.fullPublishModule(ProgressMonitorUtil.submon(monitor, 100));
		} else if( publishType == PublishControllerUtility.INCREMENTAL_PUBLISH) {
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

	@Override
	public int transferBuiltModule(IModule[] module, IPath srcFile,
			IProgressMonitor monitor) throws CoreException {
		String name = getDeploymentOutputName(getServer(), module[0]);
		IJBoss7DeploymentResult removeResult = getService().undeploySync(managementDetails, name, true,  ProgressMonitorUtil.submon(monitor, 10));
		IStatus status1 =(removeResult.getStatus());
		
		IJBoss7DeploymentResult result = getService().deploySync(managementDetails, name, srcFile.toFile(), 
				true, ProgressMonitorUtil.submon(monitor, 100));
		IStatus status2 =(result.getStatus());
		return status1.isOK() && status2.isOK() ? IServer.PUBLISH_STATE_NONE : IServer.PUBLISH_STATE_UNKNOWN;
	}

	@Override
	public int removeModule(IModule[] module, IProgressMonitor monitor)
			throws CoreException {
		String name = getDeploymentOutputName(getServer(), module[0]);
		IJBoss7DeploymentResult removeResult = getService().undeploySync(managementDetails, name, true,  monitor);
		IStatus result = removeResult.getStatus();
		if( result.isOK())
			return IServer.PUBLISH_STATE_NONE;
		return IServer.PUBLISH_STATE_FULL;
	}
	

	private int getUpdatedPublishState(IServer server) {
        IModule[] modules = server.getModules();
        boolean allpublished= true;
        for (int i = 0; i < modules.length; i++) {
        	if(server.getModulePublishState(new IModule[]{modules[i]})!=IServer.PUBLISH_STATE_NONE)
                allpublished=false;
        }
        if(allpublished)
        	return IServer.PUBLISH_STATE_NONE;
        return IServer.PUBLISH_STATE_INCREMENTAL;
	}
}
