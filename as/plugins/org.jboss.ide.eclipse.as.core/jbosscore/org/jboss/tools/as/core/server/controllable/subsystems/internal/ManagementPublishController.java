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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
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
import org.jboss.ide.eclipse.as.management.core.IncrementalDeploymentManagerService;
import org.jboss.ide.eclipse.as.management.core.IncrementalManagementModel;
import org.jboss.ide.eclipse.as.management.core.JBoss7DeploymentState;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerUtil;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.management.core.JBoss7ServerState;
import org.jboss.ide.eclipse.as.wtp.core.modules.filter.patterns.ComponentModuleInclusionFilterUtility;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IModuleStateController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPrimaryPublishController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishControllerDelegate;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.util.PublishControllerUtil;
import org.jboss.ide.eclipse.as.wtp.core.server.launch.AbstractStartJavaServerLaunchDelegate;
import org.jboss.ide.eclipse.as.wtp.core.server.publish.LocalZippedModulePublishRunner;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;
import org.jboss.tools.as.core.server.controllable.internal.DeployableServerBehavior;
import org.jboss.tools.as.core.server.controllable.systems.IModuleDeployPathController;

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
	
	private IJBoss7ManagerService getService() throws JBoss7ManangerException {
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
		return canPublishInternal(false);
	}

	private IStatus canPublishInternal(boolean checkServerResponds) {
		// We can only publish if the server adapter is 'started'
		boolean started = getServer().getServerState() == IServer.STATE_STARTED;
		if( !started )
			return Status.CANCEL_STATUS;
		
		// If we should check if the server is actually running, do it
		if( checkServerResponds ) {
			IStatus isRunningStatus = isRunning();
			if( !isRunningStatus.isOK()) 
				return isRunningStatus;
		}
		
		// If it passed previous 2 checks, just validate we have everything we need
		return validate();
	}
	
	@Override
	public boolean canPublishModule(IModule[] module) {
		return canPublishInternal(false).isOK();
	}

	@Override
	public void publishStart(IProgressMonitor monitor) throws CoreException {
		IStatus canPublish = canPublishInternal(true);
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
				DeployableServerBehavior o = (DeployableServerBehavior)getServer().loadAdapter(DeployableServerBehavior.class, new NullProgressMonitor());
				moduleStateController = (IModuleStateController)o.getController(IModuleStateController.SYSTEM_ID);
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
		IStatus canPublish = canPublishInternal(true);
		if( !canPublish.isOK() && canPublish.getSeverity() != IStatus.CANCEL) {
			IStatus error = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "The server must be started to publish."); //$NON-NLS-1$
			throw new CoreException(error);
		}
		if( canPublish.getSeverity() == IStatus.CANCEL) {
			// Do nothing, server is stopped
			return getServer().getModulePublishState(module);
		}

		((Server)getServer()).setModuleState(module, IServer.STATE_UNKNOWN);
		
		// first see if we need to delegate to another custom publisher, such as bpel / osgi
		// These publishers will need to handle incremental on their own, if they have the ability
		IPublishControllerDelegate delegate = PublishControllerUtil.findDelegatePublishController(getServer(), module, true);
		if( delegate != null ) {
			return delegate.publishModule(kind, deltaKind, module, monitor);
		}

		// Management publisher will only publish on root module request 
		// but will handle all modules. It will ignore all child modules publish requests
		if( module.length > 1 ) {
			return IServer.PUBLISH_STATE_NONE;
		}
		
		int publishType = PublishControllerUtil.getPublishType(getServer(), module, kind, deltaKind);
		if( publishType != PublishControllerUtil.REMOVE_PUBLISH) {
			boolean structureChanged = PublishControllerUtil.structureChanged(getServer(), module[0]);
			if( structureChanged ) {
				// If the structure changed, we need a full publish. Can't go adding / removing child modules willy-nilly!
				publishType = PublishControllerUtil.FULL_PUBLISH;
			}
		}
		
		if( publishType == PublishControllerUtil.INCREMENTAL_PUBLISH || publishType == PublishControllerUtil.NO_PUBLISH) {
			// We should try an incremental publish here. The publishType flag doesn't 
			// know about child modules at all, which may have changed. 
			if( shouldMinimizeRedeployments()) {
				// Do nothing... we are minimizing redeployments at the moment
				return getServer().getModulePublishState(module);
			}
			if( getService().supportsIncrementalDeployment()) {
				return incrementalPublish(module[0], monitor);
			}
		}

		
		if( publishType == PublishControllerUtil.REMOVE_PUBLISH) {
			return removeModule(module, monitor);
		}
		
		// ServerBehaviourDelegate already started this monitor with a 1000 total work to do
		monitor.setTaskName("Publishing " + module[0].getName()); //$NON-NLS-1$
		
		boolean isBinaryObject = ServerModelUtilities.isBinaryModule(module);
		IModuleResource[] resources = ModuleResourceUtil.getMembers(module[0]);
		if( isBinaryObject && resources.length > 1 ) {
			return multiResourceBinaryFullPublish();
		}
		
		
		File toTransfer = getFullPublishFilesToTransfer(module, monitor);
		
		if( toTransfer != null ) {
			String tName = toTransfer.getName();
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FAIL, "Deployment of module " + module[0].getName() + " has failed", null);  //$NON-NLS-1$//$NON-NLS-2$
			IProgressMonitor transferMonitor = ProgressMonitorUtil.submon(monitor, 800);
			transferMonitor.beginTask("Transfering " + tName, 100); //$NON-NLS-1$
			
			// Maybe name will be customized in UI? 
			JBoss7DeploymentState state = getService().getDeploymentState(managementDetails, toTransfer.getName());
			if( state != JBoss7DeploymentState.NOT_FOUND) {
				monitor.setTaskName("Undeploying: " + tName); //$NON-NLS-1$
				IJBoss7DeploymentResult removeResult = getService().undeploySync(managementDetails, tName, true,  ProgressMonitorUtil.submon(transferMonitor, 5));
				ms.add(removeResult.getStatus());
			} else {
				transferMonitor.worked(5);
			}
			
			monitor.setTaskName("Transfering: " + tName); //$NON-NLS-1$
			IJBoss7ManagerService serv = getService();
			IJBoss7DeploymentResult result = null;
			if( serv.supportsIncrementalDeployment()) {
				IncrementalDeploymentManagerService serv2 = (IncrementalDeploymentManagerService)serv;
				String[] explodePaths = (isBinaryObject ? new String[] {} : getExplodePaths(module));
				result = serv2.deploySync(managementDetails, tName, toTransfer, 
						true, explodePaths, ProgressMonitorUtil.submon(transferMonitor, 95));
			} else {
				result = getService().deploySync(managementDetails, tName, toTransfer, 
						true, ProgressMonitorUtil.submon(transferMonitor, 95));
			}
			
			IStatus s = result.getStatus();
			ms.add(s);
			
			if( ms.isOK()) {
				return IServer.PUBLISH_STATE_NONE;
			}
			ServerLogger.getDefault().log(getServer(), ms);
			transferMonitor.done();
			return IServer.PUBLISH_STATE_FULL;
		} else {
			IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Error deploying via management api: No file to deploy: " + module[module.length-1].getName()); //$NON-NLS-1$
			JBossServerCorePlugin.getDefault().getLog().log(s);
		}
		return IServer.PUBLISH_STATE_UNKNOWN;
	}

	
	private File getFullPublishFilesToTransfer(IModule[] module, IProgressMonitor monitor) throws CoreException {
		monitor.setTaskName("Zipping module: " + module[0].getName()); //$NON-NLS-1$
		IProgressMonitor submon = ProgressMonitorUtil.submon(monitor, 200);
		File file = zipLocally(module[0], PublishControllerUtil.FULL_PUBLISH, submon);
		return file;
	}
	
	private int multiResourceBinaryFullPublish() throws CoreException {
//		IModuleResource[] resources = ModuleResourceUtil.getMembers(module[0]);
//		// How to handle binary modules?? 
//		// Single file deployable is easy if its just the 1 file,
//		// but when its a folder, not sure what to do here yet
//		ArrayList<File> fileList = new ArrayList<File>(resources.length);
//		for( int i = 0; i < resources.length; i++ ) {
//			File f = (File)resources[i].getAdapter(File.class);
//			if( f != null ) {
//				fileList.add(f);
//			}
//		}
//		toTransfer = (File[]) fileList.toArray(new File[fileList.size()]);
//		monitor.worked(200);
		return unsupported("Unsupported publish of multi-resource binary module over management"); //$NON-NLS-1$
	}
	
	private int unsupported(String s) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, s));
	}
	
	private String[] getExplodePaths(IModule[] module) throws CoreException {
		String rootModName = getDeploymentOutputName(getServer(), module[0]);
		ArrayList<String> toExplode = new ArrayList<String>();
		toExplode.add(rootModName);
		
		
//		ArrayList<IModule[]> deepModules = ServerModelUtilities.getDeepChildren(getServer(), module);
//		Iterator<IModule[]> deepIt = deepModules.iterator();
//		while(deepIt.hasNext()) {
//			IModule[] m = deepIt.next();
//			IPath rel = ServerModelUtilities.getRootModuleRelativePath(getServer(), m);
//			toExplode.add(new Path(rootModName).append(rel).toString());
//		}
		return (String[]) toExplode.toArray(new String[toExplode.size()]);
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
		monitor.beginTask("", 100); //$NON-NLS-1$
		monitor.done();
	}
	
	
	private File zipLocally(IModule module, int publishType, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Zip module " + module.getName(), 100); //$NON-NLS-1$
		// Zip into a temporary folder, then transfer to the proper location
		IPath localTempLocation = getMetadataTemporaryLocation(getServer());
		String name = getDeploymentOutputName(getServer(), module);
		IPath tmpArchive = localTempLocation.append(name);
		
		LocalZippedModulePublishRunner runner = createZippedRunner(module, tmpArchive); 

		IStatus result = null;
		if( publishType == PublishControllerUtil.FULL_PUBLISH) {
			result = runner.fullPublishModule(ProgressMonitorUtil.submon(monitor, 100));
		} else if( publishType == PublishControllerUtil.INCREMENTAL_PUBLISH) {
			result = runner.incrementalPublishModule(ProgressMonitorUtil.submon(monitor, 100));
		}
		monitor.done();
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
				return ComponentModuleInclusionFilterUtility.findDefaultModuleFilter(module[module.length-1]);
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
	
	private static final IStatus CANCEL_STATUS = new Status(IStatus.CANCEL, "ASWTPToolsPlugin.PLUGIN_ID", "Publish Canceled"); //$NON-NLS-1$ //$NON-NLS-2$
	private static final IStatus[] CANCEL_STATUS_ARR = new IStatus[]{CANCEL_STATUS};

	protected int incrementalPublish(IModule module, IProgressMonitor monitor) throws CoreException {
		IncrementalDeploymentManagerService service = (IncrementalDeploymentManagerService)getService();
		return new IncrementalManagementPublishRunner().incrementalPublish(module, service, monitor);
	}
	
	//Exposed only for tests to override
	protected IModuleResourceDelta[] getDeltaForModule(IModule[] module) {
		IModuleResourceDelta[] deltas = ((Server)getServer()).getPublishedResourceDelta(module);
		return deltas;
	}
	
	
	private class IncrementalManagementPublishRunner {
		
		private class ManagementDouble {
			Map<String, String> changedContent;
			List<String> removedContent;
			public ManagementDouble() {
				changedContent = new HashMap<String, String>();
				removedContent = new ArrayList<String>();
			}
			public void addChangedContent(String relativePath, String absoluteLocalPath) {
				changedContent.put(relativePath, absoluteLocalPath);
			}
			public void addRemovedContent(String relativePath) {
				if( !removedContent.contains(relativePath)) {
					removedContent.add(relativePath);
				}
			}
		}
		
		public int incrementalPublish(IModule module, IncrementalDeploymentManagerService service, IProgressMonitor monitor) throws CoreException {
			// module may be any size > 0 
			monitor.beginTask("Incremental Publish", 200); //$NON-NLS-1$
			
			String rootName = getDeploymentOutputName(getServer(), module);
			
			
			IModule[] asArr = new IModule[] {module};
			IModuleResourceDelta[] delta = getDeltaForModule(asArr);
			ManagementDouble dub = traverseDelta(delta, new SubProgressMonitor(monitor, 100));
			IncrementalManagementModel model = new IncrementalManagementModel();
			model.setDeploymentChanges(rootName, dub.changedContent, dub.removedContent);
			
			ArrayList<IModule[]> deepModules = ServerModelUtilities.getDeepChildren(getServer(), asArr);
			Iterator<IModule[]> it = deepModules.iterator();
			String relative;
			IPath rel;
			while(it.hasNext()) {
				IModule[] m2 = it.next();
				delta = getDeltaForModule(m2);
				dub = traverseDelta(delta, new SubProgressMonitor(monitor, 100));
				
				rel = ServerModelUtilities.getRootModuleRelativePath(getServer(), m2);
				relative = rel.removeTrailingSeparator().toString();
				model.addSubDeploymentChanges(rootName, relative, dub.changedContent, dub.removedContent);
			}
			
			IJBoss7DeploymentResult result = service.incrementalPublish(managementDetails, rootName, model, 
					true, new SubProgressMonitor(monitor, 100));
			IStatus s = result.getStatus();
			if( !s.isOK()) {
				ServerLogger.getDefault().log(getServer(), s);
				return IServer.PUBLISH_STATE_FULL;
			}
			return IServer.PUBLISH_STATE_NONE;
		}
		

		private ManagementDouble traverseDelta(IModuleResourceDelta[] delta, IProgressMonitor monitor) throws CoreException {
			ManagementDouble ret = new ManagementDouble();
			for( int i = 0; i < delta.length; i++ ) {
				traverseDelta(delta[i], ret, monitor);
			}
			return ret;
		}
		
		private void traverseDelta(IModuleResourceDelta delta, ManagementDouble ret, IProgressMonitor monitor) throws CoreException {
			if( monitor.isCanceled())
				return;

			IModuleResource resource = delta.getModuleResource();
			int kind2 = delta.getKind();
			IPath absolutePath = resource.getModuleRelativePath().append(resource.getName());

			// Handle the case of a file
			if (resource instanceof IModuleFile) {
				IModuleFile file = (IModuleFile) resource;
				File ioFile = ModuleResourceUtil.getFile(file);
				if (kind2 == IModuleResourceDelta.REMOVED) {
					ret.addRemovedContent(absolutePath.toString());
				} else if( kind2 != IModuleResourceDelta.NO_CHANGE){
					ret.addChangedContent(absolutePath.toString(), ioFile.getAbsolutePath());
				}
			}
			
			// Handle the case of an added folder
//			if (kind2 == IModuleResourceDelta.ADDED) {
//				//Trace.trace(Trace.STRING_FINER, "      Creating directory resource: " + absolutePath); //$NON-NLS-1$
//				IStatus stat = fsController.makeDirectoryIfRequired(absolutePath, monitor);
//				if( stat != null )
//					status.add( stat);
//			}
			
			// Handle all child deltas. 
			// For added folders, handling children occurs after creating the folder.
			// For removed folders, handling children occurs before removing the folder
			// For NO_CHANGE folders, we traverse the children. API is unclear whether children will have changes.
			// For CHANGED folders we traverse the children.
			IModuleResourceDelta[] childDeltas = delta.getAffectedChildren();
			int size = childDeltas == null ? 0 : childDeltas.length;
			for (int i = 0; i < size; i++) {
				if( monitor.isCanceled())
					return;
				traverseDelta(childDeltas[i], ret, monitor);
			}
			
			// Handle the case where a folder is removed
			if (kind2 == IModuleResourceDelta.REMOVED) {
				ret.addRemovedContent(absolutePath.toString());
			}
			
			return;
		}
	}
	
}
