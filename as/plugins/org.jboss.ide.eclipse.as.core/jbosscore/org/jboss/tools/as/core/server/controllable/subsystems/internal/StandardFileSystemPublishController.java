/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
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
import java.util.Arrays;
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
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.modules.ResourceModuleResourceUtil;
import org.jboss.ide.eclipse.as.core.publishers.JSTPublisherXMLToucher;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IDeployableServerBehaviour;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilterProvider;
import org.jboss.ide.eclipse.as.core.server.internal.v7.DeploymentMarkerUtils;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ModuleResourceUtil;
import org.jboss.ide.eclipse.as.core.util.ProgressMonitorUtil;
import org.jboss.ide.eclipse.as.core.util.RemotePath;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishController;
import org.jboss.ide.eclipse.as.wtp.core.server.publish.LocalZippedModulePublishRunner;
import org.jboss.ide.eclipse.as.wtp.core.server.publish.PublishModuleFullRunner;
import org.jboss.ide.eclipse.as.wtp.core.server.publish.PublishModuleIncrementalRunner;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentOptionsController;
import org.jboss.tools.as.core.server.controllable.systems.IModuleDeployPathController;
import org.jboss.tools.as.core.server.controllable.systems.IModuleRestartBehaviorController;


/**
 * This class drives the standard publishing operation.
 * It will also delegate to legacy or override controllers if one is found 
 * that matches the given module type. 
 * It has been demonstrated to work with legacy publishers. 
 */
public class StandardFileSystemPublishController extends AbstractSubsystemController
		implements IPublishController {

	// Dependencies
	
	/*
	 * The deployment options gives us access to things like
	 * where the deployment root dir for a server should be,
	 * or whether the server prefers zipped settings
	 */
	private IDeploymentOptionsController deploymentOptions;
	
	/*
	 * The deploy path controller helps us to discover
	 * a module's root deployment directory
	 */
	private IModuleDeployPathController deployPathController;
	
	/*
	 * A filesystem controller gives us access to 
	 * a way to transfer individual files
	 */
	private IFilesystemController filesystemController;
	
	/*
	 * The IModuleRestartBehaviorController helps us determine
	 * when a module requires a restart based (currently) on
	 * whether any of the changed files match a restart regex
	 */
	private IModuleRestartBehaviorController restartController;
	
	/*
	 * publishType and requiresRestart are used to cache deployment state 
	 * until publishFinish is called, at which point we can 
	 * touch descripters or deploy markers appropriately
	 */
	private HashMap<IModule[], Integer> publishType = new HashMap<IModule[], Integer>();
	private HashMap<IModule[], Boolean> requiresRestart = new HashMap<IModule[], Boolean>();
	
	
	
	protected IModuleRestartBehaviorController getModuleRestartBehaviorController() throws CoreException {
		if( restartController == null ) {
			restartController = (IModuleRestartBehaviorController)findDependencyFromBehavior(IModuleRestartBehaviorController.SYSTEM_ID);
		}
		return restartController;
	}
	
	/*
	 * Get the system for deployment options such as zipped or not
	 * We must pass in a custom environment here. 
	 */
	protected IDeploymentOptionsController getDeploymentOptions() throws CoreException {
		if( deploymentOptions == null ) {
			deploymentOptions = (IDeploymentOptionsController)findDependencyFromBehavior(IDeploymentOptionsController.SYSTEM_ID);
		}
		return deploymentOptions;
	}
	
	/*
	 * get the system for deploy path for a given module
	 */
	protected IModuleDeployPathController getDeployPathController() throws CoreException {
		if( deployPathController == null ) {
			Map<String, Object> env = new HashMap<String, Object>(getEnvironment());
			env.put(IModuleDeployPathController.ENV_DEPLOYMENT_OPTIONS_CONTROLLER, getDeploymentOptions());
			deployPathController = (IModuleDeployPathController)findDependency(IModuleDeployPathController.SYSTEM_ID, getServer().getServerType().getId(), env);
		}
		return deployPathController;
	}

	/*
	 * get the filesystem controller for transfering files
	 */
	protected IFilesystemController getFilesystemController() throws CoreException {
		if( filesystemController == null ) {
			filesystemController = (IFilesystemController)findDependencyFromBehavior(IFilesystemController.SYSTEM_ID);
		}
		return filesystemController;
	}

	
	public IStatus validate() {
		try {
			IStatus sup = super.validate();
			if( !sup.isOK())
				return sup;
			getDeploymentOptions();
			getDeployPathController();
			getFilesystemController();
		} catch(CoreException ce) {
			return ce.getStatus();
		}
		return Status.OK_STATUS;
	}
	
	
	@Override
	public IStatus canPublish() {
		IStatus s = validate();
		if( !s.isOK())
			return s;
		return Status.OK_STATUS;
	}

	@Override
	public boolean canPublishModule(IModule[] module) {
		IStatus s = validate();
		if( !s.isOK())
			return false;
		return true;
	}

	@Override
	public void publishStart(IProgressMonitor monitor) throws CoreException {
		validate();
	}

	@Override
	public void publishFinish(IProgressMonitor monitor) throws CoreException {
		validate();
		// handle markers / touch xml files depending on server version
		ensureModulesRestarted();
		IServer s = getServer();
		((Server)s).setServerPublishState(getUpdatedPublishState(s));
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

	
	@Override
	public int publishModule(int kind,
			int deltaKind, IModule[] module, IProgressMonitor monitor)
			throws CoreException {
		monitor = monitor == null ? new NullProgressMonitor() : monitor; // nullsafe
		validate();
		
		// first see if we need to delegate to another custom publisher, such as bpel / osgi
		IPublishController delegate = findDelegatePublishController(module);
		if( delegate != null ) {
			return delegate.publishModule(kind, deltaKind, module, monitor);
		}
		
		/*
		 * A binary module is one in which the contents of the module should be published directly.
		 * For example, given a SingleFileDeployable module with 1 resource (some-ds.xml) which is acting
		 * as a child under an ear,  a normal situation would list the module's root as 
		 * My.ear/folder/to/some-ds.xml, and therefore its actual contents would end up in the archive as
		 * My.ear/folder/to/some-ds.xml/some-ds.xml.
		 * 
		 * To avoid this, we remove the last segment of the module's supposed root. 
		 * This is a quirk of binary modules. 
		 */
		boolean isBinaryObject = ServerModelUtilities.isBinaryModule(module);
		boolean prefersZipped = prefersZipped();
		IPath archiveDestination = getModuleDeployRoot(module);
		int publishType = getPublishType(module, kind, deltaKind);

		
		// If we're a top-level binary module, we don't get zipped. Odds are we're already zipped, or a simple xml file
		// If we're a child binary, we've already been zipped during the parent's pass
		if( !isBinaryObject && prefersZipped) {
			return handleZippedPublish(module, publishType, archiveDestination, false, monitor);
		}
		
		// Handle removals of modules
		if( publishType == IJBossServerPublisher.REMOVE_PUBLISH){
			return removeModule(module, archiveDestination, monitor);
		}
		
		// Skip any wtp-deleted-module for a full or incremental publish
		if( ServerModelUtilities.isAnyDeleted(module) ) {
			Trace.trace(Trace.STRING_FINER, "Handling a wtp 'deleted module' (aka missing/deleted /closed project). No Action Taken. Returning state=unknown "); //$NON-NLS-1$
			return IServer.PUBLISH_STATE_UNKNOWN;
		}
		
		// We'll do full publish or incremental publish now. 
		boolean forceZip = forceZipModule(module);
		boolean forzeZipAnyParent = parentModuleIsForcedZip(module);
		
		if( forceZip ) {
			// if we have to force-zip any of the parents, we already did zip this one as well
			if( forzeZipAnyParent) {
				// We can assume the parent was already published.
				// In the future, we may wish to check the parent's publish state and simply use that one as well
				return IServer.PUBLISH_STATE_NONE;				
			}
			// Otherwise we need to zip this module and its children. 
			return handleZippedPublish(module, publishType, archiveDestination, true, monitor);
		}
		
		IStatus[] ret = null;
		String msgForFailure = null; // This is the message to use in a failure status IF it fails. We set it just in case
		IModulePathFilter filter = ResourceModuleResourceUtil.findDefaultModuleFilter(module[module.length-1]);

		if( isBinaryObject ) {
			return handleBinaryModule(module, archiveDestination);
 		}
		
		
		if( publishType == IJBossServerPublisher.FULL_PUBLISH ) {
			PublishModuleFullRunner runner = new PublishModuleFullRunner(getFilesystemController(), archiveDestination);
			IModuleResource[] filtered = filter == null ? ModuleResourceUtil.getMembers(module[module.length-1]) : filter.getFilteredMembers();
			ret = runner.fullPublish(filtered, monitor);
			requiresRestart.put(module, true);
			msgForFailure = Messages.FullPublishFail; 
		} else if( publishType == IJBossServerPublisher.INCREMENTAL_PUBLISH) {
			PublishModuleIncrementalRunner runner = new PublishModuleIncrementalRunner(getFilesystemController(), archiveDestination);
			IModuleResourceDelta[] cleanDelta = filter != null ? filter.getFilteredDelta(getDeltaForModule(module)) : getDeltaForModule(module);
			requiresRestart.put(module, getModuleRestartBehaviorController().moduleRequiresRestart(module, cleanDelta));
			ret = runner.publish(cleanDelta, monitor);
			msgForFailure = Messages.IncrementalPublishFail;
		}
		monitor.done();
		markModulePublished(module, publishType);
		
		// Accumulate only the failing statuses into a multi-status
		if( ret != null && ret.length > 0 ) {
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_INC_FAIL, 
					NLS.bind(msgForFailure, module[module.length-1].getName()), null);
			for( int i = 0; i < ret.length; i++ ) {
				if( ret[i] != null && !ret[i].isOK())
					ms.add(ret[i]);
			}
			if( ms.getChildren().length > 0 ) {
				ServerLogger.getDefault().log(getServer(),ms);
				return IServer.PUBLISH_STATE_UNKNOWN;
			}
		}
		return IServer.PUBLISH_STATE_NONE;
	}
	
	private int handleBinaryModule(IModule[] module, IPath archiveDestination) throws CoreException {
		// Handle the publish here
		// binary modules should only have 1 resource in them. 
		IModuleResource[] all = ModuleResourceUtil.getMembers(module[module.length-1]);
		if( all != null ) {
			if( all.length > 0 && all[0] != null ) {
				IModuleResource r = all[0];
				File f = ModuleResourceUtil.getFile(r);
				IPath folder = archiveDestination.removeLastSegments(1); 
				IStatus s1 = getFilesystemController().makeDirectoryIfRequired(folder, new NullProgressMonitor());
				IStatus result = null;
				if( s1 != null && !s1.isOK()) {
					result = s1;
				} else {
					IStatus s2 = getFilesystemController().copyFile(f, archiveDestination, new NullProgressMonitor());
					result = s2;
				}
				
				markModulePublished(module, IJBossServerPublisher.FULL_PUBLISH);
				if( result != null && !result.isOK())
			        ServerLogger.getDefault().log(getServer(), result);
				return result == null || result.isOK() ? IServer.PUBLISH_STATE_NONE : IServer.PUBLISH_STATE_FULL;
			}
		}
		return IServer.PUBLISH_STATE_UNKNOWN;
	}
	
	private int removeModule(IModule[] module, IPath remote, IProgressMonitor monitor) throws CoreException {
		// We assume a deletion on a root module will delete its children
		if( module.length > 1 )
			return IServer.PUBLISH_STATE_NONE;
		
		Trace.trace(Trace.STRING_FINER, "Handling an unpublish"); //$NON-NLS-1$
		IStatus results = getFilesystemController().deleteResource(remote, monitor);
		if( !results.isOK()) {
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_REMOVE_FAIL, 
					"Unable to delete module", null); //$NON-NLS-1$
			ms.add(results);
			ServerLogger.getDefault().log(getServer(),ms);
			markModulePublished(module, IJBossServerPublisher.REMOVE_PUBLISH);
			return IServer.PUBLISH_STATE_UNKNOWN;
		}
		
		markModulePublished(module, IJBossServerPublisher.REMOVE_PUBLISH);
		Trace.trace(Trace.STRING_FINER, "Deleted deployment resource: " + remote.toOSString()); //$NON-NLS-1$
		return IServer.PUBLISH_STATE_NONE;
	}
	
	private int handleZippedPublish(IModule[] module, int publishType, IPath archiveDestination, boolean force, IProgressMonitor monitor) throws CoreException{
		if( !force && module.length > 1 ) {
			// We can assume the parent was already published.
			// In the future, we may wish to check the parent's publish state and simply use that one as well
			return IServer.PUBLISH_STATE_NONE;
		}
		
		if( publishType == IJBossServerPublisher.REMOVE_PUBLISH) {
			return removeModule(module, archiveDestination, monitor);
		} else {
			// Skip any wtp-deleted-module for a full or incremental publish
			if( ServerModelUtilities.isAnyDeleted(module) ) {
				Trace.trace(Trace.STRING_FINER, "Handling a wtp 'deleted module' (aka missing/deleted /closed project). No Action Taken. Returning state=unknown "); //$NON-NLS-1$
				return IServer.PUBLISH_STATE_UNKNOWN;
			}

			
			// Zip into a temporary folder, then transfer to the proper location
			IPath localTempLocation = getMetadataTemporaryLocation(getServer());
			IPath tmpArchive = localTempLocation.append(archiveDestination.lastSegment());
			
			LocalZippedModulePublishRunner runner = createZippedRunner(module[0], tmpArchive); 
			monitor.beginTask("Packaging Module", 200); //$NON-NLS-1$
			
			IStatus result = null;
			if( publishType == IJBossServerPublisher.FULL_PUBLISH) {
				result = runner.fullPublishModule(ProgressMonitorUtil.submon(monitor, 100));
			} else if( publishType == IJBossServerPublisher.INCREMENTAL_PUBLISH) {
				result = runner.incrementalPublishModule(ProgressMonitorUtil.submon(monitor, 100));
			}
			if( result != null && result.isOK()) {
				if( tmpArchive.toFile().exists()) {
					result = getFilesystemController().copyFile(tmpArchive.toFile(), archiveDestination, ProgressMonitorUtil.submon(monitor, 100));
				} else {
					result = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Zipped archive not found"); //$NON-NLS-1$
				}
			}
			// If a zipped module has changed, then it requires restart no matter what
			markModulePublished(module, IJBossServerPublisher.FULL_PUBLISH);
			if( result != null )
		        ServerLogger.getDefault().log(getServer(), result);
			return result == null || result.isOK() ? IServer.PUBLISH_STATE_NONE : IServer.PUBLISH_STATE_UNKNOWN;
		}
	}
	
	// If this is a bpel or osgi or some other strange publisher, 
	// allow the delegate to handle the publish
	private IPublishController findDelegatePublishController(IModule[] module) {
		// First try to find a new delegate for this module type
		// TODO , no mechanism yet
		
		// If none are found, lets look for legacy ones. 
		String existingMode = getServer().getAttribute(IDeployableServer.SERVER_MODE, "local"); //$NON-NLS-1$
		IJBossServerPublisher pub = ExtensionManager.getDefault().getPublisher(getServer(), module, existingMode);
		if( pub != null ) {
			// return a legacy wrapper. 
			LegacyPublisherWrapperController c = new LegacyPublisherWrapperController();
			c.initialize(getServer(), null,  null);
			c.setLegacyPublisher(pub);
			// TODO log a warning that this is a legacy publisher and must be converted ??
			return c;
		}
		return null; 
	}

	// Does the given server prefer zipped deployment?
	private boolean prefersZipped() throws CoreException {
		return getDeploymentOptions().prefersZippedDeployments();
	}
	
	private IPath getModuleDeployRoot(IModule[] module) throws CoreException {
		// Find dependency will throw a CoreException if an object is not found, rather than return null
		IDeploymentOptionsController opts = getDeploymentOptions();
		IModuleDeployPathController depPath = getDeployPathController();
		return new RemotePath(depPath.getDeployDirectory(module).toOSString(), 
				opts.getPathSeparatorCharacter());
	}
	
	protected IModulePathFilterProvider getModulePathFilterProvider() {
		return new IModulePathFilterProvider() {
			public IModulePathFilter getFilter(IServer server, IModule[] module) {
				return ResourceModuleResourceUtil.findDefaultModuleFilter(module[module.length-1]);
			}
		};
	}
	
	private void markModulePublished(IModule[] module, int type) {
		publishType.put(module, type);
	}
	
	/**
	 * Given the various flags, return which of the following options 
	 * our publishers should perform:
	 *    1) A full publish
	 *    2) A removed publish (remove the module)
	 *    3) An incremental publish, or
	 *    4) No publish at all. 
	 * @param module
	 * @param kind
	 * @param deltaKind
	 * @return
	 */
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
	
	/**
	 * Check if any parents of this module were force-zipped
	 * @param moduleTree
	 * @return
	 */
	private boolean parentModuleIsForcedZip(IModule[] moduleTree) {
		ArrayList<IModule> tmp = new ArrayList(Arrays.asList(moduleTree)); 
		tmp.remove(tmp.size()-1);
		while( tmp.size() > 0 ) {
			IModule[] tmpArray = tmp.toArray(new IModule[tmp.size()]);
			if( forceZipModule(tmpArray) ) {
				return true;
			}
			tmp.remove(tmp.size()-1);
		}
		return false;
	}

	/**
	 * Get a temporary file location somewhere in metadata
	 * @param module
	 * @param ds
	 * @return
	 */
	private IPath getMetadataTemporaryLocation(IServer server) {
		IPath deployRoot = JBossServerCorePlugin.getServerStateLocation(server).
			append(IJBossToolingConstants.TEMP_REMOTE_DEPLOY).makeAbsolute();
		deployRoot.toFile().mkdirs();
		return deployRoot;
	}
	
	@Override
	public void publishServer(int kind, IProgressMonitor monitor)
			throws CoreException {
		// Do nothing	
		validate();
	}

	// Use xml-touching or deployment markers to restart top-level changed modules
	protected void ensureModulesRestarted() {
		List<IModule> top = getTopLevelModules();
		Iterator<IModule> tit = top.iterator();
		while(tit.hasNext()) {
			IModule m = tit.next();
			if( moduleRequiresRestart(m)) {
				restartModule(m);
			}
		}
	}
	
	/*
	 * This method will, for as6 and below, touch the deployment descriptors
	 * of the root module if they exist. 
	 * For >=7 it will delete old markers and add a .dodeploy marker
	 */
	protected void restartModule(IModule m) {
		try {
			IPath archiveDestination = getModuleDeployRoot(new IModule[]{m});
			IFilesystemController controller = getFilesystemController();
			boolean useAS7Behavior = DeploymentMarkerUtils.supportsJBoss7MarkerDeployment(getServer());
			if( !useAS7Behavior) {
				JSTPublisherXMLToucher.getInstance().touch(archiveDestination, 
						m, controller);
			} else {
				// attempt to remove all markers, and add a .dodeploy
				DeploymentMarkerUtils.removedDeployedMarker(archiveDestination, controller);
				DeploymentMarkerUtils.removedDeployFailedMarker(archiveDestination, controller);
				DeploymentMarkerUtils.createDoDeployMarker(archiveDestination, controller);
			}
		} catch(CoreException ce) {
			JBossServerCorePlugin.log(ce);
		}
	}
	
	// Get only top level modules which have been published
	private List<IModule> getTopLevelModules() {
		ArrayList<IModule> collector = new ArrayList<IModule>();
		Iterator<IModule[]> it = publishType.keySet().iterator();
		while(it.hasNext()) {
			IModule[] m = it.next();
			if( !collector.contains(m[0]))
				collector.add(m[0]);
		}
		return collector;
	}
	
	protected boolean moduleRequiresRestart(IModule m) {
		// If this module or any of its children require a restart, 
		// we restart the root module
		Iterator<IModule[]> it = publishType.keySet().iterator();
		while(it.hasNext()) {
			IModule[] fromMap = it.next();
			// if we're a child module of param m, or are param m, 
			if( fromMap.length > 0 && fromMap[0] == m) {
				int type = publishType.get(fromMap);
				Object t = requiresRestart.get(fromMap);
				boolean matchesPattern = t != null && ((Boolean)t).booleanValue();
				boolean isRemovedChild = (fromMap.length > 1 && type == IJBossServerPublisher.REMOVE_PUBLISH);
				// If we're a child and we're removed, the parent must be restarted
				// if we match a restart pattern, the parent must be restarted
				// if we're full published, the parent must be restarted
				if( matchesPattern || type == IJBossServerPublisher.FULL_PUBLISH
						|| isRemovedChild)
					return true;
			}
		}
		return false;
	}
	
	// Exposed only for unit tests to override
	protected LocalZippedModulePublishRunner createZippedRunner(IModule m, IPath p) {
		return new LocalZippedModulePublishRunner(getServer(), m,p, 
				getModulePathFilterProvider());
	}
	
	//Exposed only for tests to override
	protected IModuleResourceDelta[] getDeltaForModule(IModule[] module) {
		IModuleResourceDelta[] deltas = ((Server)getServer()).getPublishedResourceDelta(module);
		return deltas;
	}

}
