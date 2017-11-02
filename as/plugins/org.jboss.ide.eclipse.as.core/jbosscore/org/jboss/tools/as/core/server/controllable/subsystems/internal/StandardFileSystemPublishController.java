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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.publishers.JSTPublisherXMLToucher;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilterProvider;
import org.jboss.ide.eclipse.as.core.server.internal.UpdateModuleStateJob;
import org.jboss.ide.eclipse.as.core.server.internal.v7.DeploymentMarkerUtils;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ModuleResourceUtil;
import org.jboss.ide.eclipse.as.core.util.ProgressMonitorUtil;
import org.jboss.ide.eclipse.as.core.util.RemotePath;
import org.jboss.ide.eclipse.as.wtp.core.modules.filter.patterns.ComponentModuleInclusionFilterUtility;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IModuleStateController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPrimaryPublishController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishControllerDelegate;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.util.PublishControllerUtil;
import org.jboss.ide.eclipse.as.wtp.core.server.launch.AbstractStartJavaServerLaunchDelegate;
import org.jboss.ide.eclipse.as.wtp.core.server.publish.BinaryModulePublishRunner;
import org.jboss.ide.eclipse.as.wtp.core.server.publish.LocalZippedModulePublishRunner;
import org.jboss.ide.eclipse.as.wtp.core.server.publish.ModulePublishErrorCache;
import org.jboss.ide.eclipse.as.wtp.core.server.publish.PublishModuleFullRunner;
import org.jboss.ide.eclipse.as.wtp.core.server.publish.PublishModuleIncrementalRunner;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentOptionsController;
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentZipOptionsController;
import org.jboss.tools.as.core.server.controllable.systems.IModuleDeployPathController;
import org.jboss.tools.as.core.server.controllable.systems.IModuleRestartBehaviorController;


/**
 * This class drives the standard publishing operation.
 * It will also delegate to legacy or override controllers if one is found 
 * that matches the given module type. 
 * It has been demonstrated to work with legacy publishers. 
 */
public class StandardFileSystemPublishController extends AbstractSubsystemController
		implements IPublishController, IPrimaryPublishController {

	// Dependencies

	/*
	 * An optional dependency for verifying or modifying the deploy state of a module
	 */
	private IModuleStateController moduleStateController;
	
	/*
	 * A flag so that we can ignore a failure to load and not
	 * try continuously to load a missing optional controller
	 */
	private boolean moduleStateControllerLoadFailed = false;
	
	/*
	 * An optional dependency.
	 * The IModuleRestartBehaviorController helps us determine
	 * when a module requires a restart.
	 * 
	 * The current impl in jbt is based on
	 * whether any of the changed files match a restart regex
	 */
	private IModuleRestartBehaviorController restartController;
	
	/*
	 * A flag so that we can ignore a failure to load and not
	 * try continuously to load a missing optional controller
	 */
	private boolean restartControllerLoadFailed = false;

	
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
	 * publishType and requiresRestart are used to cache deployment state 
	 * until publishFinish is called, at which point we can 
	 * touch descriptors or deploy markers appropriately
	 */
	private HashMap<IModule[], Integer> publishType = new HashMap<>();
	private HashMap<IModule[], Boolean> requiresRestart = new HashMap<>();
	
	
	/**
	 * Access the optional restart controller for 
	 * deciding whether to force a restart after an incremental publish
	 * 
	 * @return
	 * @throws CoreException
	 */
	protected IModuleRestartBehaviorController getModuleRestartBehaviorController() throws CoreException {
		if( restartController == null && !restartControllerLoadFailed) {
			try {
				restartController = (IModuleRestartBehaviorController)findDependencyFromBehavior(IModuleRestartBehaviorController.SYSTEM_ID);
			} catch(CoreException ce) {
				// Do not log; this is optional. But trace
				restartControllerLoadFailed = true;
			}
		}
		return restartController;
	}
	

	/**
	 * Access the optional module state controller. 
	 * 
	 * @return
	 * @throws CoreException 
	 */
	protected IModuleStateController getModuleStateController() throws CoreException {
		if( moduleStateController == null && !moduleStateControllerLoadFailed) {
			try {
				moduleStateController = (IModuleStateController)findDependencyFromBehavior(IModuleStateController.SYSTEM_ID);
			} catch(CoreException ce) {
				// Do not log; this is optional. But trace
				moduleStateControllerLoadFailed = true;
			}
		}
		return moduleStateController;
	}
	
	/**
	 * Get the system for deployment options such as zipped or not
	 * We must pass in a custom environment here. 
	 */
	protected IDeploymentOptionsController getDeploymentOptions() throws CoreException {
		if( deploymentOptions == null ) {
			deploymentOptions = (IDeploymentOptionsController)findDependencyFromBehavior(IDeploymentOptionsController.SYSTEM_ID);
		}
		return deploymentOptions;
	}
	
	/**
	 * get the system for deploy path for a given module
	 */
	protected IModuleDeployPathController getDeployPathController() throws CoreException {
		if( deployPathController == null ) {
			return (IModuleDeployPathController)findDependencyFromBehavior(IModuleDeployPathController.SYSTEM_ID);
		}
		return deployPathController;
	}

	/**
	 * get the filesystem controller for transferring files
	 */
	protected IFilesystemController getFilesystemController() throws CoreException {
		if( filesystemController == null ) {
			filesystemController = (IFilesystemController)findDependencyFromBehavior(IFilesystemController.SYSTEM_ID);
		}
		return filesystemController;
	}

	/**
	 * Validate that all required dependencies load. 
	 */
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
		return s.isOK();
	}

	@Override
	public void publishStart(IProgressMonitor monitor) throws CoreException {
		Trace.trace(Trace.STRING_FINER, "publishStart called on server " + getServer().getName()); //$NON-NLS-1$
		IStatus s = validate();
		if( s != null && !s.isOK()) {
			throw new CoreException(s);
		}
	}

	@Override
	public void publishFinish(IProgressMonitor monitor) throws CoreException {
		Trace.trace(Trace.STRING_FINER, "publishFinish called on server " + getServer().getName()); //$NON-NLS-1$
		validate();
		IServer s = getServer();
		// handle markers / touch xml files depending on server version
		ensureModulesRestarted();
		
		((Server)s).setServerPublishState(getUpdatedPublishState(s));
		

		launchUpdateModuleStateJob();
	}

	protected void launchUpdateModuleStateJob() throws CoreException {
		// update the wtp model with live module state from the server
		IModuleStateController c = getModuleStateController();
		if( c != null && getServer().getServerState() == IServer.STATE_STARTED) {
			Trace.trace(Trace.STRING_FINER, "launching job to update module state for server " + getServer().getName()); //$NON-NLS-1$
			new UpdateModuleStateJob( c, getServer(), true, 15000).schedule(5000);
		}
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

	private String moduleToString(IModule[] mod) throws CoreException {
		StringBuilder sb = new StringBuilder();
		for( int i = 0; i < mod.length; i++ ) {
			if( mod[i] == null ) {
				Status s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
						"Server " + getServer().getName() + " has been asked to publish a null module: " //$NON-NLS-1$//$NON-NLS-2$ 
								+ sb.append((String)null).toString()); 
				throw new CoreException(s);
			}
			sb.append(mod[i].getName());
			if( i < mod.length) 
				sb.append(",   "); //$NON-NLS-1$
		}
		return sb.toString();
	}
	
	@Override
	public int publishModule(int kind,
			int deltaKind, IModule[] module, IProgressMonitor monitor)
			throws CoreException {
		
		Trace.trace(Trace.STRING_FINER, NLS.bind("publishModule called on server {0} with kind={1}, deltaKind={2}, module of size {3}: {4}",  //$NON-NLS-1$
				new Object[]{getServer().getName(), Integer.valueOf(kind), Integer.valueOf(deltaKind), Integer.valueOf(module.length), moduleToString(module)})); 

		SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
		validate();
		
		// first see if we need to delegate to another custom publisher, such as bpel / osgi
		IPublishControllerDelegate delegate = PublishControllerUtil.findDelegatePublishController(getServer(),module, true);
		if( delegate != null ) {
			Trace.trace(Trace.STRING_FINER, "   Publish delegate for module type " + module[module.length-1].getModuleType().getName()); //$NON-NLS-1$
			return delegate.publishModule(kind, deltaKind, module, subMonitor.split(1));
		}
		
		/*
		 * Please see the following method signature for details on what is a 'binary module':
		 * 
		 * private int handleBinaryModule(IModule[] module, IPath archiveDestination) throws CoreException
		 */
		boolean isBinaryObject = treatAsBinaryModule(module);
		Trace.trace(Trace.STRING_FINER, "   Module " + module[module.length-1].getName() + " is binary? " + isBinaryObject); //$NON-NLS-1$ //$NON-NLS-2$

		boolean serverPrefersZipped = prefersZipped();
		Trace.trace(Trace.STRING_FINER, "   Server " + getServer().getName() + " prefers zipped deployment? " + serverPrefersZipped); //$NON-NLS-1$ //$NON-NLS-2$

		IPath archiveDestination = getModuleDeployRoot(module, isBinaryObject);
		Trace.trace(Trace.STRING_FINER, "   Archive destination: " + archiveDestination.toString()); //$NON-NLS-1$

		boolean anyDeleted = ServerModelUtilities.isAnyDeleted(module);
		
		int publishType = PublishControllerUtil.getPublishType(getServer(), module, kind, deltaKind);


		// Handle removals of modules
		if( publishType == PublishControllerUtil.REMOVE_PUBLISH){
			if( !isBinaryObject ) {
				Trace.trace(Trace.STRING_FINER, "   Removing module: " + module[module.length-1].getName()); //$NON-NLS-1$
				return removeModule(module, archiveDestination, subMonitor.split(1));
			} else {
				Trace.trace(Trace.STRING_FINER, "   Removing binary module: " + module[module.length-1].getName()); //$NON-NLS-1$
				return handlePublishBinaryModule(module, archiveDestination, anyDeleted, publishType);
			}
		}
		
		// Skip any wtp-deleted-module for a full or incremental publish
		if( anyDeleted ) {
			Trace.trace(Trace.STRING_FINER, "   Module or parent module is a wtp 'deleted module' (aka missing/deleted /closed project). No Action Taken. Returning state=unknown "); //$NON-NLS-1$
			return IServer.PUBLISH_STATE_UNKNOWN;
		}

		boolean modulePrefersZipped = forceZipModule(module);
		boolean forzeZipAnyParent = parentModuleIsForcedZip(module);

		// if we have to force-zip any of the parents, we already did zip this one as well
		if( forzeZipAnyParent) {
			// We can assume the parent was already published.
			// In the future, we may wish to check the parent's publish state and simply use that one as well
			Trace.trace(Trace.STRING_FINER, "   Parent module already published and zipped. Ignoring this call"); //$NON-NLS-1$
			return IServer.PUBLISH_STATE_NONE;				
		}

		if( !isBinaryObject && modulePrefersZipped ) {
			// Otherwise we need to zip this module and its children. 
			Trace.trace(Trace.STRING_FINER, "   Non-Binary module setting prefers zipped deployment."); //$NON-NLS-1$
			return handleZippedPublish(module, publishType, archiveDestination, true, subMonitor.split(1));
		}
		
		IStatus[] ret = null;
		String msgForFailure = null; // This is the message to use in a failure status IF it fails. We set it just in case
		IModulePathFilter filter = ComponentModuleInclusionFilterUtility.findDefaultModuleFilter(module[module.length-1]);

		if( isBinaryObject ) {
			// Remove situation has been handled
			Trace.trace(Trace.STRING_FINER, "   Publishing binary module."); //$NON-NLS-1$
			return handlePublishBinaryModule(module, archiveDestination, anyDeleted, publishType);
 		}
		
		
		// Exploded deployment logic
		if( publishType == PublishControllerUtil.FULL_PUBLISH ) {
			Trace.trace(Trace.STRING_FINER, "   Executing full publish on module."); //$NON-NLS-1$
			ret = executeFullPublish(module, archiveDestination, filter, subMonitor.split(1));
			msgForFailure = Messages.FullPublishFail; 
		} else if( publishType == PublishControllerUtil.INCREMENTAL_PUBLISH) {
			Trace.trace(Trace.STRING_FINER, "   Executing incremental publish on module."); //$NON-NLS-1$
			PublishModuleIncrementalRunner runner = new PublishModuleIncrementalRunner(getFilesystemController(), archiveDestination);
			IModuleResourceDelta[] cleanDelta = filter != null ? filter.getFilteredDelta(getDeltaForModule(module)) : getDeltaForModule(module);
			IModuleRestartBehaviorController moduleRestartController = getModuleRestartBehaviorController();
			if( moduleRestartController != null ) 
				requiresRestart.put(module, moduleRestartController.moduleRequiresRestart(module, cleanDelta));
			ret = runner.publish(cleanDelta, subMonitor.split(1));
			msgForFailure = Messages.IncrementalPublishFail;
		} else {
			// No publish done
			requiresRestart.put(module, false);
		}
		
		
		// Accumulate only the failing statuses into a multi-status
		IStatus acc = null;
		if( ret != null && ret.length > 0 ) {
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_INC_FAIL, 
					NLS.bind(msgForFailure, module[module.length-1].getName()), null);
			for( int i = 0; i < ret.length; i++ ) {
				if( ret[i] != null && !ret[i].isOK()) {
					ms.add(ret[i]);
				}
			}
			acc = ms;
		}
		markModulePublished(module, publishType, acc, true);
		subMonitor.setWorkRemaining(0);
		return acc == null || acc.isOK() ? IServer.PUBLISH_STATE_NONE : IServer.PUBLISH_STATE_UNKNOWN;
	}
	
	protected boolean treatAsBinaryModule(IModule[] module) {
		return isBinaryModule(module);
	}

	protected boolean isBinaryModule(IModule[] module) {
		return ServerModelUtilities.isBinaryModule(module);		
	}

	/*
	 * Binary modules are non-standard modules that WTP has created, and so must
	 * be handled. Your typical module will follow servertools API, and, the
	 * resources they return will be members "inside" the war. So if a standard
	 * WAR module returns an item "index.html", that index.html lives inside the module,
	 * most likely at the path MyWar.war/index.html 
	 * 
	 * Binary modules are non-standard, consist of a set of files that are to published
	 * as separate items, not zipped or collected into a containing jar file. 
	 * So, for example, if a binary module indicates it has 1 resource,
	 * and that one resource is named "lib.jar",  this should be published
	 * standalone, and should not be packaged in a zip.
	 * 
	 * When handled incorrectly, publishing a binary module with 
	 * one resource named "lib.jar" will output a zip file
	 * named "lib.jar" which has 1 resource inside, also a zip file, 
	 * and also named "lib.jar". To avoid the unnecessary and incorrect
	 * wrapping, binary modules must be handled separately.
	 */
	private int handlePublishBinaryModule(IModule[] module, IPath archiveDestination, boolean anyDeleted, int publishType) throws CoreException {
		if( PublishControllerUtil.NO_PUBLISH == publishType ) {
			markModulePublished(module, PublishControllerUtil.NO_PUBLISH, null, true);
			return IServer.PUBLISH_STATE_NONE;
		}
		IPath archDest2 = getModuleDeployRoot(module, false);
		IFilesystemController fc = getFilesystemController();
		IModuleResource[] all = ModuleResourceUtil.getMembers(module[module.length-1]);
		IModuleResourceDelta[] delta = getDeltaForModule(module);
		BinaryModulePublishRunner runner = new BinaryModulePublishRunner(module, archiveDestination, archDest2, fc, all, delta);
		if( PublishControllerUtil.REMOVE_PUBLISH == publishType) {
			IStatus ms = null;
			if( anyDeleted ) {
				ms = runner.removeDeletedBinaryModule();
			} else {
				ms = runner.removeBinaryModule();
			}
			int ret = ms.isOK() ? IServer.PUBLISH_STATE_NONE : IServer.PUBLISH_STATE_FULL;
			markModulePublished(module, PublishControllerUtil.REMOVE_PUBLISH, ms, true);
			return ret;
		}
		if( PublishControllerUtil.INCREMENTAL_PUBLISH == publishType) {
			IStatus ms = runner.publishModuleIncremental();
			int ret = ms.isOK() ? IServer.PUBLISH_STATE_NONE : IServer.PUBLISH_STATE_FULL;
			markModulePublished(module, PublishControllerUtil.INCREMENTAL_PUBLISH, ms, true);
			return ret;
		} else if( PublishControllerUtil.FULL_PUBLISH == publishType) {
			IStatus ms = runner.publishModuleFull();
			int ret = ms.isOK() ? IServer.PUBLISH_STATE_NONE : IServer.PUBLISH_STATE_FULL;
			markModulePublished(module, PublishControllerUtil.FULL_PUBLISH, ms, true);
			return ret;
		}
		return IServer.PUBLISH_STATE_UNKNOWN;
	}
	
	protected IStatus[] executeFullPublish(IModule[] module, IPath archiveDestination, IModulePathFilter filter, IProgressMonitor monitor) 
			throws CoreException {
		// Mark this module and all child modules as requiring a full publish
		Server s = (Server)getServer();
		ArrayList<IModule[]> kids = ServerModelUtilities.getDeepChildren(getServer(), module);
		Iterator<IModule[]> it = kids.iterator();
		while(it.hasNext()) {
			s.setModulePublishState(it.next(), IServer.PUBLISH_STATE_FULL);
		}
		
		
		PublishModuleFullRunner runner = new PublishModuleFullRunner(getFilesystemController(), archiveDestination);
		IModuleResource[] filtered = filter == null ? ModuleResourceUtil.getMembers(module[module.length-1]) : filter.getFilteredMembers();
		IStatus[] ret = runner.fullPublish(filtered, monitor);
		requiresRestart.put(module, true);
		return ret;
	}

	protected int removeModule(IModule[] module, IPath remote, IProgressMonitor monitor) throws CoreException {
		// We assume a deletion on a root module will delete its children
		if( module.length > 1 )
			return IServer.PUBLISH_STATE_NONE;
		
		Trace.trace(Trace.STRING_FINER, "Handling an unpublish"); //$NON-NLS-1$
		IStatus results = getFilesystemController().deleteResource(remote, monitor);
		boolean error = results != null && !results.isOK();
		if( error) {
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_REMOVE_FAIL, 
					NLS.bind(Messages.DeleteModuleFail, module[module.length-1]), results.getException());
			ms.add(results);
			results = ms;
			Trace.trace(Trace.STRING_FINER, "Failed to delete deployment resource: " + remote.toOSString()); //$NON-NLS-1$
		} else {
			Trace.trace(Trace.STRING_FINER, "Deleted deployment resource: " + remote.toOSString()); //$NON-NLS-1$
		}
		markModulePublished(module, PublishControllerUtil.REMOVE_PUBLISH, results, true);
		return error ? IServer.PUBLISH_STATE_UNKNOWN : IServer.PUBLISH_STATE_NONE;
	}
	
	private int handleZippedPublish(IModule[] module, int publishType, IPath archiveDestination, boolean force, IProgressMonitor monitor) throws CoreException{
		if( !force && module.length > 1 ) {
			// We can assume the parent was already published.
			// In the future, we may wish to check the parent's publish state and simply use that one as well
			return IServer.PUBLISH_STATE_NONE;
		}
		
		if( publishType == PublishControllerUtil.REMOVE_PUBLISH) {
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
			
			LocalZippedModulePublishRunner runner = createZippedRunner(module, tmpArchive); 
			monitor.beginTask("Packaging Module", 200); //$NON-NLS-1$
			
			IStatus result = null;
			boolean rebuiltFull = false;
			boolean rebuiltInc = false;
			if( publishType == PublishControllerUtil.FULL_PUBLISH) {
				rebuiltFull = true;
				result = runner.fullPublishModule(ProgressMonitorUtil.submon(monitor, 100));
			} else {
				// If a child module nested inside this utility requires a full publish, so do we
				// If a child module has been added or removed, or any other structural change, we also require
				// a full publish
				int childPublishType = runner.childPublishTypeRequired();
				if( childPublishType == PublishControllerUtil.FULL_PUBLISH ) {
					rebuiltFull = true;
					result = runner.fullPublishModule(ProgressMonitorUtil.submon(monitor, 100));
				} else if( publishType == PublishControllerUtil.INCREMENTAL_PUBLISH || childPublishType == PublishControllerUtil.INCREMENTAL_PUBLISH) {
					rebuiltInc = true;
					result = runner.incrementalPublishModule(ProgressMonitorUtil.submon(monitor, 100));
				}
			}
			if( (rebuiltFull || rebuiltInc) && (result == null || result.isOK())) {
				if( tmpArchive.toFile().exists()) {
					getFilesystemController().deleteResource(archiveDestination, ProgressMonitorUtil.submon(monitor, 10));
					result = getFilesystemController().copyFile(tmpArchive.toFile(), archiveDestination, ProgressMonitorUtil.submon(monitor, 90));
				} else {
					result = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Zipped archive not found"); //$NON-NLS-1$
				}
			}
			
			boolean error = result != null && !result.isOK();
			int pubTypeRet = -1;
			// If this has been rebuilt fully, it must be restarted
			// If it has been rebuilt incrementally, but is a TOP LEVEL module, it must be restarted
			if( rebuiltFull || ( rebuiltInc && module.length == 1)) {
				pubTypeRet = PublishControllerUtil.FULL_PUBLISH;
			} else if( rebuiltInc) {
				// If it is not top-level, and is incrementally updated, 
				// Only restart the module if it matches the given regex pattern
				IModuleRestartBehaviorController c = getModuleRestartBehaviorController();
				if( c != null && c.moduleRequiresRestart(module, new IModuleResource[0])) {
					// If a zipped module has changed, then it requires restart no matter what
					pubTypeRet = PublishControllerUtil.FULL_PUBLISH;
				}
			} else {
				pubTypeRet = PublishControllerUtil.NO_PUBLISH;
			}
			markModulePublished(module, pubTypeRet, result, true);
			return result == null || result.isOK() ? IServer.PUBLISH_STATE_NONE : IServer.PUBLISH_STATE_UNKNOWN;
		}
	}
	
	// Does the given server prefer zipped deployment?
	private boolean prefersZipped() throws CoreException {
		return getDeploymentOptions().prefersZippedDeployments();
	}

	protected IPath getModuleDeployRoot(IModule[] module) throws CoreException {
		return getModuleDeployRoot(module, treatAsBinaryModule(module));
	}
	
	protected IPath getModuleDeployRoot(IModule[] module, boolean isBinaryObject) throws CoreException {
		// Find dependency will throw a CoreException if an object is not found, rather than return null
		IDeploymentOptionsController opts = getDeploymentOptions();
		IModuleDeployPathController depPath = getDeployPathController();
		return new RemotePath(depPath.getDeployDirectory(module, isBinaryObject).toOSString(), 
				opts.getPathSeparatorCharacter());
	}
	
	protected IModulePathFilterProvider getModulePathFilterProvider() {
		return new IModulePathFilterProvider() {
			public IModulePathFilter getFilter(IServer server, IModule[] module) {
				return ComponentModuleInclusionFilterUtility.findDefaultModuleFilter(module[module.length-1]);
			}
		};
	}
	
	private void markModulePublished(IModule[] module, int type, IStatus result, boolean logError) {
		publishType.put(module, type);
		if( logError && result != null && !result.isOK()) {
			ServerLogger.getDefault().log(getServer(), result);
		}
		ModulePublishErrorCache.getDefault().setPublishErrorState(getServer(), module, type, result);
	}
	
	/**
	 * For certain module trees, some publishers may want to force a child to be zipped.
	 * For example, JST Publisher may want to force utility project children to be zipped.
	 * 
	 * @param moduleTree
	 * @return
	 */
	protected boolean forceZipModule(IModule[] moduleTree) {
		try {
			IDeploymentOptionsController doc = getDeploymentOptions();
			if( doc instanceof IDeploymentZipOptionsController) {
				return ((IDeploymentZipOptionsController)doc).shouldZipDeployment(moduleTree);
			}
		} catch(CoreException ce) {
			JBossServerCorePlugin.log(ce);
		}
		return PublishUtil.deployPackaged(moduleTree, getServer());
	}
	
	/**
	 * Check if any parents of this module were force-zipped
	 * @param moduleTree
	 * @return
	 */
	private boolean parentModuleIsForcedZip(IModule[] moduleTree) {
		ArrayList<IModule> tmp = new ArrayList(Arrays.asList(moduleTree)); 
		tmp.remove(tmp.size()-1);
		while( !tmp.isEmpty() ) {
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
			} else if ( moduleRequiresRemovalCompletion(m)){
				completeRemoval(m);
			}
		}
	}
	protected boolean moduleRequiresRemovalCompletion(IModule m) {
		Iterator<IModule[]> it = publishType.keySet().iterator();
		while(it.hasNext()) {
			IModule[] mit = it.next();
			if( mit.length == 1 && mit[0].equals(m) && publishType.get(mit) == PublishControllerUtil.REMOVE_PUBLISH) {
				return true;
			}
		}
		return false;
	}
	
	protected void completeRemoval(IModule m) {
		try {
			boolean useAS7Behavior = supportsJBoss7Markers();
			// AS7-derived requires the .deployed markers to be removed. Other servers require no action
			if( useAS7Behavior) {
				IPath archiveDestination = getModuleDeployRoot(new IModule[]{m});
				IFilesystemController controller = getFilesystemController();
				DeploymentMarkerUtils.removeDoDeployMarker(archiveDestination, controller);
				DeploymentMarkerUtils.removeDeployedMarker(archiveDestination, controller);
				DeploymentMarkerUtils.removeDeployFailedMarker(archiveDestination, controller);
			}
		} catch(CoreException ce) {
			JBossServerCorePlugin.log(ce);
		}
	}
	
	protected boolean supportsJBoss7Markers() {
		return DeploymentMarkerUtils.supportsJBoss7MarkerDeployment(getServer());
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
			boolean useAS7Behavior = supportsJBoss7Markers();
			if( !useAS7Behavior) {
				JSTPublisherXMLToucher.getInstance().touch(archiveDestination, 
						m, controller);
			} else {
				// attempt to remove all markers, and add a .dodeploy
				DeploymentMarkerUtils.removeDeployedMarker(archiveDestination, controller);
				DeploymentMarkerUtils.removeDeployFailedMarker(archiveDestination, controller);
				DeploymentMarkerUtils.createDoDeployMarker(archiveDestination, controller);
			}
		} catch(CoreException ce) {
			JBossServerCorePlugin.log(ce);
		}
	}
	
	// Get only top level modules which have been published
	private List<IModule> getTopLevelModules() {
		ArrayList<IModule> collector = new ArrayList<>();
		Iterator<IModule[]> it = publishType.keySet().iterator();
		while(it.hasNext()) {
			IModule[] m = it.next();
			if( !collector.contains(m[0]))
				collector.add(m[0]);
		}
		return collector;
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
	
	protected boolean moduleRequiresRestart(IModule m) {
		boolean minimizeRedeployments = shouldMinimizeRedeployments();
		// If this module or any of its children require a restart, 
		// we restart the root module
		Iterator<IModule[]> it = publishType.keySet().iterator();
		while(it.hasNext()) {
			IModule[] fromMap = it.next();
			if( minimizeRedeployments ) {
				// In debug mode, we'll only restart if a full publish is initialized
				if( fromMap.length == 1 && fromMap[0] == m) {
					int type = publishType.get(fromMap);
					return type == PublishControllerUtil.FULL_PUBLISH;
				}
			} else {
				// if we're a child module of param m, or are param m, 
				if( fromMap.length > 0 && fromMap[0] == m) {
					int type = publishType.get(fromMap);
					Object t = requiresRestart.get(fromMap);
					boolean matchesPattern = t != null && ((Boolean)t).booleanValue();
					boolean isRemovedChild = (fromMap.length > 1 && type == PublishControllerUtil.REMOVE_PUBLISH);
					// If we're a child and we're removed, the parent must be restarted
					// if we match a restart pattern, the parent must be restarted
					// if we're full published, the parent must be restarted
					if( matchesPattern || type == PublishControllerUtil.FULL_PUBLISH
							|| isRemovedChild)
						return true;
				}
			}
		}
		return false;
	}
	
	// Exposed only for unit tests to override
	protected LocalZippedModulePublishRunner createZippedRunner(IModule m, IPath p) {
		return createZippedRunner(new IModule[]{m}, p);
	}
	
	protected LocalZippedModulePublishRunner createZippedRunner(IModule[] m, IPath p) {
		return new LocalZippedModulePublishRunner(getServer(), m,p, 
				getModulePathFilterProvider());
	}
	
	//Exposed only for tests to override
	protected IModuleResourceDelta[] getDeltaForModule(IModule[] module) {
		IModuleResourceDelta[] deltas = ((Server)getServer()).getPublishedResourceDelta(module);
		return deltas;
	}

	@Override
	public int transferBuiltModule(IModule[] module, IPath srcFile,
			IProgressMonitor monitor) throws CoreException {
		IPath archiveDestination = getModuleDeployRoot(module);
		IStatus result = getFilesystemController().copyFile(srcFile.toFile(), archiveDestination,monitor);
		// If a zipped module has changed, then it requires restart no matter what
		boolean error = result != null && !result.isOK();
		markModulePublished(module, PublishControllerUtil.FULL_PUBLISH, result, true);
		return error ? IServer.PUBLISH_STATE_UNKNOWN : IServer.PUBLISH_STATE_NONE;
	}

	@Override
	public int removeModule(IModule[] module, IProgressMonitor monitor)
			throws CoreException {
		IPath archiveDestination = getModuleDeployRoot(module);
		return removeModule(module, archiveDestination, monitor);
	}

}
