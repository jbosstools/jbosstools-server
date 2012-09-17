/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.archives.webtools.modules;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.archives.core.util.internal.TrueZipUtil;
import org.jboss.ide.eclipse.archives.webtools.IntegrationPlugin;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.FileUtil.IFileUtilListener;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;

import de.schlichtherle.io.ArchiveDetector;

public class LocalZippedPublisherUtil extends PublishUtil {
	
	private String deployRoot;
	private boolean hasBeenChanged = false;
	private IModule[] module;
	private IServer server;
	public IStatus publishModule(IServer server, String deployRoot, IModule[] module,
			int publishType, IModuleResourceDelta[] delta,
			IProgressMonitor monitor) throws CoreException {
		String name = "Compressing " + module[0].getName(); //$NON-NLS-1$
		monitor.beginTask(name, 200);
		monitor.setTaskName(name);
		this.deployRoot = deployRoot;
		this.module = module;
		this.server = server;
		IStatus[] operationStatus;
		
		
		// Am I a removal? If yes, remove me, and return
		if( publishType == IJBossServerPublisher.REMOVE_PUBLISH)
			operationStatus = removeModule(server, deployRoot, module);
		
		// Am I a full publish? If yes, full publish me, and return
		else if( publishType == IJBossServerPublisher.FULL_PUBLISH)
			operationStatus = fullPublish(server, deployRoot, module); 
		
		else {
			// Am I changed? If yes, handle my changes
			ArrayList<IStatus> results = new ArrayList<IStatus>(); 
			if( countChanges(delta) > 0) {
				results.addAll(Arrays.asList(publishChanges(server, deployRoot, module)));
			}
			
			IModule[] children = server.getChildModules(module, new NullProgressMonitor());
			results.addAll(Arrays.asList(handleChildrenDeltas(server, deployRoot, module, children)));
			
			// Handle the removed children that are not returned by getChildModules
			List<IModule[]> removed = getBehaviour(server).getRemovedModules();
			Iterator<IModule[]> i = removed.iterator();
			while(i.hasNext()) {
				IModule[] removedArray = i.next();
				if( removedArray[0].getId().equals(module[0].getId())) {
					results.addAll(Arrays.asList(removeModule(server, deployRoot, removedArray)));
				}
			}
			operationStatus = (IStatus[]) results.toArray(new IStatus[results.size()]);
		}
		TrueZipUtil.umount();
		
		IStatus finalStatus = createModuleStatus(module, operationStatus);

		monitor.done();
		return finalStatus;
	}


	private IStatus createModuleStatus(IModule[] module, IStatus[] operationStatus) {
		IStatus finalStatus;
		if( operationStatus.length > 0 ) {
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_INC_FAIL, 
					"Publish Failed for module " + module[0].getName(), null); //$NON-NLS-1$
			for( int i = 0; i < operationStatus.length; i++ )
				ms.add(operationStatus[i]);
			finalStatus = ms;
		}  else {
			finalStatus = new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, 
					IEventCodes.JST_PUB_FULL_SUCCESS, 
					NLS.bind(Messages.ModulePublished, module[0].getName()), null);
		}
		return finalStatus;
	}
	

	protected IDeployableServer getDeployableServer(IServer server) {
		return (IDeployableServer)server.loadAdapter(IDeployableServer.class, new NullProgressMonitor());
	}
	protected DeployableServerBehavior getBehaviour(IServer server) {
		return (DeployableServerBehavior)server.loadAdapter(DeployableServerBehavior.class, new NullProgressMonitor());
	}
	
	protected List<IModule[]> getRemovedModules(IServer s) {
		return getBehaviour(s).getRemovedModules();
	}

	public boolean anyChangesRecurse(IServer server, IModule[] module) {
		// Since for zipped, incremental and full are identical, just return a result other than 'no publish'
		if( !getBehaviour(server).hasBeenPublished(module)) 
			return true;
		
		if( isRemoved(server, module))
			return true;
		
		int modulePublishState = server.getModulePublishState(module);
		if( modulePublishState != IServer.PUBLISH_STATE_NONE)
			return true;
		
		IModuleResourceDelta[] deltas = ((Server)server).getPublishedResourceDelta(module);
		if( deltas.length > 0)
			return true;
		
		IModule[] children = server.getChildModules(module, new NullProgressMonitor());
		for( int i = 0; i < children.length; i++ ) {
			IModule[] tmp = combine(module, children[i]);
			if(anyChangesRecurse(server, tmp))
				return true; 
		}
		return false;
	}

	protected IStatus[] handleChildrenDeltas(IServer server, String deployRoot, IModule[] module, IModule[] children) {
		// For each child:
		ArrayList<IStatus> results = new ArrayList<IStatus>(); 
		for( int i = 0; i < children.length; i++ ) {
			IModule[] combinedChild = combine(module, children[i]);
			// if it's new, full publish it
			if( !getBehaviour(server).hasBeenPublished(combinedChild)) {
				results.addAll(Arrays.asList(fullPublish(server, deployRoot, combinedChild)));
			}
			// else if it's removed, full remove it
			else if( isRemoved(server, combinedChild)) {
				results.addAll(Arrays.asList(removeModule(server, deployRoot, combinedChild)));
			}
			// else 
			else {
				// handle changed resources
				results.addAll(Arrays.asList(publishChanges(server, deployRoot, combinedChild)));

				// recurse
				IModule[] children2 = server.getChildModules(combinedChild, new NullProgressMonitor());
				results.addAll(Arrays.asList(handleChildrenDeltas(server, deployRoot, module, children2)));
			}
		}
		return (IStatus[]) results.toArray(new IStatus[results.size()]);
	}
	
	protected boolean isRemoved(IServer server, IModule[] child) {
		List<IModule[]> removed = getBehaviour(server).getRemovedModules();
		Iterator<IModule[]> i = removed.iterator();
		while(i.hasNext()) {
			IModule[] next = i.next();
			if( next.length == child.length) {
				for( int j = 0; j < next.length; j++ ) {
					if( !next[j].getId().equals(child[j].getId())) {
						continue;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	protected IStatus[] removeModule(IServer server, String deployRoot, IModule[] module) {
		IPath deployPath = getOutputFilePath(module);
        final ArrayList<IStatus> status = new ArrayList<IStatus>();
		IFileUtilListener listener = new IFileUtilListener() {
			public void fileCopied(File source, File dest, boolean result,Exception e) {}
			public void fileDeleted(File file, boolean result, Exception e) {
				if( result == false || e != null ) {
					status.add(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FILE_DELETE_FAIL, 
							"Attempt to delete " + file.getAbsolutePath() + " failed",e)); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			public void folderDeleted(File file, boolean result, Exception e) {
				if( result == false || e != null ) {
					status.add(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FILE_DELETE_FAIL,
							"Attempt to delete " + file.getAbsolutePath() + " failed",e)); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} 
		};
		FileUtil.safeDelete(deployPath.toFile(), listener);
		hasBeenChanged = true;
		return (IStatus[]) status.toArray(new IStatus[status.size()]);
	}
	
	protected IStatus[] fullBinaryPublish(IServer server, String deployRoot, IModule[] parent, IModule last) {
		ArrayList<IStatus> results = new ArrayList<IStatus>();
		try {
			IPath path = getOutputFilePath(combine(parent, last));
			path = path.removeLastSegments(1);
			de.schlichtherle.io.File root = TrueZipUtil.getFile(path, TrueZipUtil.getJarArchiveDetector());
			IModuleResource[] resources = getResources(last, new NullProgressMonitor());
			results.addAll(Arrays.asList(copy(root, resources)));
			TrueZipUtil.umount();
			return (IStatus[]) results.toArray(new IStatus[results.size()]);
		} catch( CoreException ce) {
			results.add(generateCoreExceptionStatus(ce));
			return (IStatus[]) results.toArray(new IStatus[results.size()]);
		}
	}
	protected IStatus[] fullPublish(IServer server, String deployRoot, IModule[] module) {
		ArrayList<IStatus> results = new ArrayList<IStatus>();
		try {
			IPath path = getOutputFilePath(module);
			// Get rid of the old
			FileUtil.safeDelete(path.toFile(), null);
			
			TrueZipUtil.createArchive(path);
			de.schlichtherle.io.File root = TrueZipUtil.getFile(path, TrueZipUtil.getJarArchiveDetector());
			IModuleResource[] resources = getResources(module);
			DeployableServerBehavior beh = ServerConverter.getDeployableServerBehavior(server);
			IModulePathFilter filter = beh.getPathFilter(module);
			IModuleResource[] resources2 = filter == null ? resources : filter.getFilteredMembers();
			IStatus[] copyResults = copy(root, resources2);
			results.addAll(Arrays.asList(copyResults));
			
			IModule[] children = server.getChildModules(module, new NullProgressMonitor());
			publishChildren(server, deployRoot, module, results, children);
			TrueZipUtil.umount();
			hasBeenChanged = true;
			return (IStatus[]) results.toArray(new IStatus[results.size()]);
		} catch( CoreException ce) {
			results.add(generateCoreExceptionStatus(ce));
			return (IStatus[]) results.toArray(new IStatus[results.size()]);
		}
	}


	private void publishChildren(IServer server, String deployRoot, IModule[] module, ArrayList<IStatus> results,
			IModule[] children) {
		if( children == null )
			return;
		
		for( int i = 0; i < children.length; i++ ) {
			if( ServerModelUtilities.isBinaryModule(children[i]))
				results.addAll(Arrays.asList(fullBinaryPublish(server, deployRoot, module, children[i])));
			else
				results.addAll(Arrays.asList(fullPublish(server, deployRoot, combine(module, children[i]))));
		}
	}
	
	protected IStatus[] publishChanges(IServer server, String deployRoot, IModule[] module) {
		IPath path = getOutputFilePath(module);
		de.schlichtherle.io.File root = TrueZipUtil.getFile(path, TrueZipUtil.getJarArchiveDetector());
		IModuleResourceDelta[] deltas = ((Server)server).getPublishedResourceDelta(module);
		DeployableServerBehavior beh = ServerConverter.getDeployableServerBehavior(server);
		IModulePathFilter filter = beh.getPathFilter(module);

		return publishChanges(server, deltas, root, filter);
	}
	
	/**
	 * @since 2.3
	 */
	protected IStatus[] publishChanges(IServer server, IModuleResourceDelta[] deltas, 
			de.schlichtherle.io.File root, IModulePathFilter filter) {
		ArrayList<IStatus> results = new ArrayList<IStatus>();
		if( deltas == null || deltas.length == 0 )
			return new IStatus[]{};
		int dKind;
		IModuleResource resource;
		for( int i = 0; i < deltas.length; i++ ) {
			dKind = deltas[i].getKind();
			resource = deltas[i].getModuleResource();
			if( dKind == IModuleResourceDelta.ADDED ) {
				if( filter == null || filter.shouldInclude(resource)) {
					results.addAll(Arrays.asList(copy(root, new IModuleResource[]{resource})));
				}
			} else if( dKind == IModuleResourceDelta.CHANGED ) {
				if( filter == null || filter.shouldInclude(resource)) {
					if( resource instanceof IModuleFile ) 
						results.addAll(Arrays.asList(copy(root, new IModuleResource[]{resource})));
					results.addAll(Arrays.asList(publishChanges(server, deltas[i].getAffectedChildren(), root, filter)));
				}
			} else if( dKind == IModuleResourceDelta.REMOVED) {
				de.schlichtherle.io.File f = getFileInArchive(root, 
						resource.getModuleRelativePath().append(
								resource.getName()));
				boolean b = f.deleteAll();
				if( !b )
					results.add(generateDeleteFailedStatus(f));
				hasBeenChanged = true;
			} else if( dKind == IModuleResourceDelta.NO_CHANGE  ) {
				results.addAll(Arrays.asList(publishChanges(server, deltas[i].getAffectedChildren(), root, filter)));
			}
		}
		
		return (IStatus[]) results.toArray(new IStatus[results.size()]);
	}

	
	protected IStatus[] copy(de.schlichtherle.io.File root, IModuleResource[] children) {
		ArrayList<IStatus> results = new ArrayList<IStatus>();
		for( int i = 0; i < children.length; i++ ) {
			if( children[i] instanceof IModuleFile ) {
				IModuleFile mf = (IModuleFile)children[i];
				java.io.File source = getFile(mf);
				de.schlichtherle.io.File destination = getFileInArchive(root, mf.getModuleRelativePath().append(mf.getName()));
				boolean b = new de.schlichtherle.io.File(source, ArchiveDetector.NULL).archiveCopyAllTo(destination);
				if( !b )
					results.add(generateCopyFailStatus(source, destination));
			} else if( children[i] instanceof IModuleFolder ) {
				de.schlichtherle.io.File destination = getFileInArchive(root, children[i].getModuleRelativePath().append(children[i].getName()));
				destination.mkdirs();
				IModuleFolder mf = (IModuleFolder)children[i];
				results.addAll(Arrays.asList(copy(root, mf.members())));
			}
		}
		hasBeenChanged = true;
		return (IStatus[]) results.toArray(new IStatus[results.size()]);
	}
	
	protected IStatus generateDeleteFailedStatus(java.io.File file) {
		return new Status(IStatus.ERROR, IntegrationPlugin.PLUGIN_ID, "Could not delete file " + file); //$NON-NLS-1$
	}
	protected IStatus generateCoreExceptionStatus(CoreException ce) {
		return new Status(IStatus.ERROR, IntegrationPlugin.PLUGIN_ID, ce.getMessage(), ce);
	}
	protected IStatus generateCopyFailStatus(java.io.File source, java.io.File destination) {
		return new Status(IStatus.ERROR, IntegrationPlugin.PLUGIN_ID, "Copy of " + source + " to " + destination + " has failed");//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}
	
	protected de.schlichtherle.io.File getFileInArchive(de.schlichtherle.io.File root, IPath relative) {
		while(relative.segmentCount() > 0 ) {
			root = new de.schlichtherle.io.File(root, 
					relative.segment(0), ArchiveDetector.NULL);
			relative = relative.removeFirstSegments(1);
		}
		return root;
	}

	public static IModule[] combine(IModule[] module, IModule newMod) {
		return PublishUtil.combine(module, newMod);
	}
	
	public IPath getOutputFilePath() {
		return getOutputFilePath(module);
	}
	
	public IPath getOutputFilePath(IModule[] module) {
		return getOutputFilePath(server,module, deployRoot);
	}
	
	public IPath getOutputFilePath(IServer server, IModule[] module, String deployRoot) {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		return getDeployPath(module, deployRoot, ds);
	}
	
	public boolean hasBeenChanged() {
		return hasBeenChanged;
	}
	public IModule[] getModule() {
		return module;
	}
}
