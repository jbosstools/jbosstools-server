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
package org.jboss.ide.eclipse.as.wtp.core.server.publish;

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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.archives.core.util.TrueZipUtil;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilterProvider;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.ModuleResourceUtil;
import org.jboss.ide.eclipse.as.core.util.ProgressMonitorUtil;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;
import org.jboss.ide.eclipse.as.wtp.core.Messages;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.util.PublishControllerUtil;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;



/**
 * This class is a utility class meant to zip a given module
 * and its child modules into 1 zip file. 
 * 
 * Clients may pass in the zip file's intended destination.
 * If an incremental packaging is requested, but the destination
 * path provided does not exist already as an archive, a full packaging
 * will be done instead. 
 * 
 * Resources and deltas are NOT expected to be 'cleaned' already.
 * This is in contrast to {@link PublishModuleFullRunner} and
 * {@link PublishModuleIncrementalRunner}. The reason cleaned
 * variables are not expected in this case is because
 * this utility class acquires the raw deltas and members
 * from the servertools framework, and it is impossible
 * to pass in cleaned resources and deltas for all existing
 * child modules. 
 * 
 * A {@link IModulePathFilterProvider} may be given
 * to assist in cleaning a module and its child modules.
 * An {@link IModulePathFilterProvider} was chosen over a simple
 * {@link IModulePathFilter} because child modules may require
 * different filters than parent modules. 
 * 
 * This class will *not* transfer an archive to any remote system.
 * Clients should do this themselves. 
 * 
 * @since 3.0
 */
public class LocalZippedModulePublishRunner extends ModuleResourceUtil {
	
	private IServer server;
	private IModule[] module;
	private IPath destinationArchive;
	private IModulePathFilterProvider filterProvider;
	public LocalZippedModulePublishRunner(IServer server, IModule module, IPath destinationArchive, IModulePathFilterProvider filterProvider) {
		this(server, new IModule[]{module}, destinationArchive, filterProvider);
	}
	
	public LocalZippedModulePublishRunner(IServer server, IModule[] module, IPath destinationArchive, IModulePathFilterProvider filterProvider) {
		this.server = server;
		this.module = module;
		this.destinationArchive = destinationArchive;
		this.filterProvider = filterProvider;
	}
	
	
	public IStatus fullPublishModule(IProgressMonitor monitor) throws CoreException {
		IStatus[] status = fullPublish(monitor);
		TrueZipUtil.umount();
		IStatus finalStatus = createModuleStatus(module, status);
		return finalStatus;
	}
	
	
	public IStatus incrementalPublishModule(IProgressMonitor monitor) throws CoreException {
		// If the output doesn't exist, don't do an incremental
		// If it exists but it's a folder, don't do incremental
		java.io.File destFile = destinationArchive.toFile();
		if( !destFile.exists() || destFile.isDirectory()) {
			return fullPublishModule(monitor);
		}
		
		
		String name = "Compressing " + lastModule().getName(); //$NON-NLS-1$
		monitor.beginTask(name, 1000);
		monitor.setTaskName(name);
		IModule[] moduleAsArray = module;
		IStatus[] operationStatus;
		
		
		// incremental publish here, or auto publish
		// Am I changed? If yes, handle my changes
		ArrayList<IStatus> results = new ArrayList<IStatus>(); 
		int changeCount = countChanges(getDeltaForModule(module));
		if( changeCount > 0) {
			IProgressMonitor changeMonitor = ProgressMonitorUtil.submon(monitor, 300);
			changeMonitor.beginTask("Copying changed resources", changeCount * 100);
			results.addAll(Arrays.asList(publishChanges(moduleAsArray, changeMonitor)));
			changeMonitor.done();
		}
		
		List<IModule[]> removed = getRemovedChildModules();
		IModule[] children = getChildModules(moduleAsArray);
		if( children !=null)
			results.addAll(Arrays.asList(handleChildrenDeltas(moduleAsArray, children, ProgressMonitorUtil.submon(monitor, 600))));
		
		
		IProgressMonitor removedMonitor = ProgressMonitorUtil.submon(monitor, 100);
		removedMonitor.beginTask("Deleting removed modules", removed.size() * 100);
		// Handle the removed children that are not returned by getChildModules
		Iterator<IModule[]> i = removed.iterator();
		while(i.hasNext() && !monitor.isCanceled()) {
			results.addAll(Arrays.asList(removeModule(i.next())));
			removedMonitor.worked(100);
		}
		operationStatus = (IStatus[]) results.toArray(new IStatus[results.size()]);
		removedMonitor.done();
		
		
		TrueZipUtil.umount();
		
		IStatus finalStatus = createModuleStatus(moduleAsArray, operationStatus);

		monitor.done();
		return finalStatus;
	}

	protected IModule[] getChildModules(IModule[] parent) {
		return server.getChildModules(parent, new NullProgressMonitor());
	}
	
	private IStatus[] removeModule(IModule[] module) {
		// It is assumed that this method is only ever being called when removing CHILD modules, 
		// not a root module. 
		IPath moduleAbsoluteDestination = (module.length == 1 ? destinationArchive : destinationArchive.append(getRootModuleRelativePath(module)));
		boolean success = TrueZipUtil.deleteAll(moduleAbsoluteDestination);
		if( !success) {
			generateDeleteFailedStatus(moduleAbsoluteDestination.toFile());
		}
		return new IStatus[]{Status.OK_STATUS};
	}
	

	// The full publish called on the root module 
	private IStatus[] fullPublish(IProgressMonitor monitor) {
		// Get rid of the old file during a full publish
		FileUtil.safeDelete(destinationArchive.toFile(), null);
		TrueZipUtil.umount();
		return fullPublish(module, null,ProgressMonitorUtil.getMonitorFor(monitor));
	}
	
	// Full publish called on either a root module or any valid module tree (children etc)
	private IStatus[] fullPublish(IModule[] module, File parent, IProgressMonitor monitor) {
		monitor.beginTask("Packaging Module: " + module[module.length-1].getName(), 2000);
		
		// A status collector
		ArrayList<IStatus> results = new ArrayList<IStatus>();
		
		// This is the absolute destination for this module (ex:  /home/user/deploy/Some.ear/Some.war)
		IPath moduleAbsoluteDestination = (module.length == 1 ? destinationArchive : destinationArchive.append(getRootModuleRelativePath(module)));
		de.schlichtherle.io.File moduleRoot = null;
		try {
			if( parent == null ) {
				// We're the root module (no parent) so just create the archive to start
				moduleRoot = TrueZipUtil.getFile(moduleAbsoluteDestination, TrueZipUtil.getJarArchiveDetector());
				// Clear out existing, in case some child modules were removed
				if( TrueZipUtil.pathExists(moduleRoot)) {
					TrueZipUtil.deleteAll(moduleRoot);
				}
				boolean createWorked = TrueZipUtil.createArchive(moduleAbsoluteDestination);
				moduleRoot = TrueZipUtil.getFile(moduleAbsoluteDestination, TrueZipUtil.getJarArchiveDetector());
			} else {
				// We are a child module, so we must get a deeper path for now
				IPath parentPath = new Path(parent.getAbsolutePath());
				IPath addition = moduleAbsoluteDestination.removeFirstSegments(parentPath.segmentCount());
				TrueZipUtil.createArchive(parent,addition);
				moduleRoot = TrueZipUtil.getRelativeArchiveFile(parent, addition);
			}
			monitor.worked(100);
			
			IModuleResource[] resources = getResources(module);
			IModulePathFilter filter = filterProvider == null ? null : filterProvider.getFilter(server, module);
			IModuleResource[] resources2 = filter == null ? resources : filter.getFilteredMembers();
			
			int totalCount = countMembers(resources2, true);
			IProgressMonitor copyMonitor = ProgressMonitorUtil.submon(monitor, 900);
			copyMonitor.beginTask("Copying Resources", totalCount*100);
			IStatus[] copyResults = copy(moduleRoot, resources2, copyMonitor);
			results.addAll(Arrays.asList(copyResults));
			copyMonitor.done();
			
			TrueZipUtil.umount();
			
			IModule[] children = getChildModules(module);
			if( children != null )
				publishChildren(module, results, children, moduleRoot,  ProgressMonitorUtil.submon(monitor, 1000));
			TrueZipUtil.umount();
		} catch( CoreException ce) {
			results.add(generateCoreExceptionStatus(ce));
		}
		monitor.done();
		return (IStatus[]) results.toArray(new IStatus[results.size()]);
	}

	/**
	 * A binary module is one in which the contents of the module should be published directly.
	 * For example, given a SingleFileDeployable module with 1 resource (some-ds.xml) which is acting
	 * as a child under an ear,  a normal situation would list the module's root as 
	 * My.ear/folder/to/some-ds.xml, and therefore its actual contents would end up in the archive as
	 * My.ear/folder/to/some-ds.xml/some-ds.xml.
	 * 
	 * To avoid this, we remove the last segment of the module's supposed root. 
	 * This is a quirk of binary modules. 
	 * 
	 * @param parent
	 * @param last
	 * @return
	 */
	private IStatus[] fullBinaryPublish(IModule[] parent, IModule last, IProgressMonitor monitor) {
		ArrayList<IStatus> results = new ArrayList<IStatus>();
		try {
			IPath tail = getRootModuleRelativePath(combine(parent, last));
			IPath tailLocation = tail.removeLastSegments(1);
			java.io.File root = TrueZipUtil.getFile(destinationArchive.append(tailLocation), TrueZipUtil.getJarArchiveDetector());
			IModuleResource[] resources = getResources(last, new NullProgressMonitor());
			int total = countMembers(resources, true);
			monitor.beginTask("Copying Resources", total*100);
			results.addAll(Arrays.asList(copy(root, resources, monitor)));
			monitor.done();
			TrueZipUtil.umount();
			return (IStatus[]) results.toArray(new IStatus[results.size()]);
		} catch( CoreException ce) {
			results.add(generateCoreExceptionStatus(ce));
			return (IStatus[]) results.toArray(new IStatus[results.size()]);
		}
	}

	private void publishChildren(IModule[] module, ArrayList<IStatus> results, IModule[] children, File parentModule, IProgressMonitor monitor) {
		if( children == null )
			return;
		monitor.beginTask("Assembling child modules", children.length * 100);
		for( int i = 0; i < children.length && !monitor.isCanceled(); i++ ) {
			if( ServerModelUtilities.isBinaryModule(children[i]))
				results.addAll(Arrays.asList(fullBinaryPublish(module, children[i], ProgressMonitorUtil.submon(monitor, 100))));
			else
				results.addAll(Arrays.asList(fullPublish(combine(module, children[i]), parentModule, ProgressMonitorUtil.submon(monitor, 100))));
		}
	}
	
	
	
	private IStatus createModuleStatus(IModule[] module, IStatus[] operationStatus) {
		IStatus finalStatus;
		if( operationStatus.length > 0 ) {
			MultiStatus ms = new MultiStatus(ASWTPToolsPlugin.PLUGIN_ID, IEventCodes.JST_PUB_INC_FAIL, 
					"Publish Failed for module " + module[0].getName(), null); //$NON-NLS-1$
			for( int i = 0; i < operationStatus.length; i++ )
				ms.add(operationStatus[i]);
			finalStatus = ms;
		}  else {
			finalStatus = new Status(IStatus.OK, ASWTPToolsPlugin.PLUGIN_ID, 
					IEventCodes.JST_PUB_FULL_SUCCESS, 
					NLS.bind(Messages.ModulePublished, module[0].getName()), null);
		}
		return finalStatus;
	}
	
	// Get the list of removed modules that are in the heirarchy tree of the root module only
	// ex:  if root mod is SomeEar, ignore removed module []{DifferentEar,DifferentWar};
	private List<IModule[]> getRemovedChildModules() {
		ArrayList<IModule[]> working = new ArrayList<IModule[]>();
		working.addAll(getRemovedModules());
		// Handle the removed children that are not returned by getChildModules
		Iterator<IModule[]> i = working.iterator();
		while(i.hasNext()) {
			IModule[] removedArray = i.next();	
			IModule[] moduleAsArray = module;
			if( removedArray.length == 1 || !removedArray[0].getId().equals(lastModule().getId())) {
				i.remove();
			}
		}
		return working;
	}
	
	private IStatus[] handleChildrenDeltas(IModule[] module, IModule[] children, IProgressMonitor monitor) {
		monitor.beginTask("Handling Child Modules",  children.length * 100);
		// For each child:
		ArrayList<IStatus> results = new ArrayList<IStatus>(); 
		for( int i = 0; i < children.length; i++ ) {
			IModule[] combinedChild = combine(module, children[i]);
			// if it's new, full publish it
			if( !hasBeenPublished(combinedChild)) {
				results.addAll(Arrays.asList(fullPublish(combinedChild, null, ProgressMonitorUtil.submon(monitor, 100))));
			}
			// else if it's removed, full remove it
			else if( isRemoved(combinedChild)) {
				results.addAll(Arrays.asList(removeModule(combinedChild)));
				monitor.worked(100);
			}
			// else 
			else {
				// handle changed resources
				results.addAll(Arrays.asList(publishChanges(combinedChild, ProgressMonitorUtil.submon(monitor, 25))));
				
				// recurse into next level of children
				IModule[] children2 = getChildModules(combinedChild);
				if( children2 != null )
					results.addAll(Arrays.asList(handleChildrenDeltas(module, children2, ProgressMonitorUtil.submon(monitor, 75))));
			}
		}
		monitor.done();
		return (IStatus[]) results.toArray(new IStatus[results.size()]);
	}
	
	private boolean isRemoved(IModule[] child) {
		List<IModule[]> removed = getRemovedModules();
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
	

	private IStatus[] publishChanges(IModule[] module, IProgressMonitor monitor) {
		IPath path = destinationArchive.append(getRootModuleRelativePath(module));
		java.io.File root = TrueZipUtil.getFile(path, TrueZipUtil.getJarArchiveDetector());
		IModuleResourceDelta[] deltas = getDeltaForModule(module);
		IModulePathFilter filter = filterProvider == null ? null : filterProvider.getFilter(server, module);
		return publishChanges(deltas, root, filter, monitor);
	}
	
	/**
	 * Get the path relative to the root module here.
	 * 
	 * For example, if the provided IModule is {WrapEar,TigerWar,ClawUtil},
	 * the returned value should be "TigerWar.war/WEB-INF/lib/ClawUtil.jar" 
	 * @param module
	 * @return
	 */
	private IPath getRootModuleRelativePath(IModule[] module) {
		int start = this.module.length - 1;
		IModule[] toCheck = new IModule[module.length - start];
		for(int i = 0; i < module.length - start; i++ ) {
			toCheck[i] = module[start+i];
		}
		IPath ret = ServerModelUtilities.getRootModuleRelativePath(server, toCheck);
		return ret;
	}
	
	
	private IStatus[] publishChanges(IModuleResourceDelta[] deltas, 
			java.io.File root, IModulePathFilter filter, IProgressMonitor monitor) {
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
					results.addAll(Arrays.asList(copy(root, new IModuleResource[]{resource}, monitor)));
				}
			} else if( dKind == IModuleResourceDelta.CHANGED ) {
				if( filter == null || filter.shouldInclude(resource)) {
					if( resource instanceof IModuleFile ) 
						results.addAll(Arrays.asList(copy(root, new IModuleResource[]{resource}, monitor)));
					results.addAll(Arrays.asList(publishChanges(deltas[i].getAffectedChildren(), root, filter, monitor)));
				}
			} else if( dKind == IModuleResourceDelta.REMOVED) {
				java.io.File f = getDestinationJar(root, 
						resource.getModuleRelativePath().append(
								resource.getName()));
				boolean b = TrueZipUtil.deleteAll(f);
				if( !b )
					results.add(generateDeleteFailedStatus(f));
			} else if( dKind == IModuleResourceDelta.NO_CHANGE  ) {
				results.addAll(Arrays.asList(publishChanges(deltas[i].getAffectedChildren(), root, filter, monitor)));
			}
		}
		
		return (IStatus[]) results.toArray(new IStatus[results.size()]);
	}

	
	private IStatus[] copy(java.io.File root, IModuleResource[] children, IProgressMonitor monitor) {
		ArrayList<IStatus> results = new ArrayList<IStatus>();
		for( int i = 0; i < children.length; i++ ) {
			if( children[i] instanceof IModuleFile ) {
				IModuleFile mf = (IModuleFile)children[i];
				java.io.File source = getFile(mf);
				if( source != null ) {
					java.io.File destination = getDestinationJar(root, mf.getModuleRelativePath().append(mf.getName()));
					boolean b = TrueZipUtil.archiveCopyAllTo(source, TrueZipUtil.getNullArchiveDetector(), destination);
					if( !b )
						results.add(generateCopyFailStatus(source, destination));
				}
				monitor.worked(100);
			} else if( children[i] instanceof IModuleFolder ) {
				java.io.File destination = getDestinationJar(root, children[i].getModuleRelativePath().append(children[i].getName()));
				destination.mkdirs();
				IModuleFolder mf = (IModuleFolder)children[i];
				results.addAll(Arrays.asList(copy(root, mf.members(), monitor)));
			}
		}
		return (IStatus[]) results.toArray(new IStatus[results.size()]);
	}
	
	private IStatus generateDeleteFailedStatus(java.io.File file) {
		return new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, "Could not delete file " + file); //$NON-NLS-1$
	}
	private IStatus generateCoreExceptionStatus(CoreException ce) {
		return new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, ce.getMessage(), ce);
	}
	private IStatus generateCopyFailStatus(java.io.File source, java.io.File destination) {
		return new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID, "Copy of " + source + " to " + destination + " has failed");//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}
	
	private java.io.File getDestinationJar(java.io.File root, IPath relative) {
		return TrueZipUtil.getDestinationJar(root, relative);
	}
	
	private de.schlichtherle.io.File getFileInArchive(de.schlichtherle.io.File root, IPath relative) {
		return TrueZipUtil.getFileInArchive(root, relative);
	}
	
	/*
	 * The following utility methods are here simply for tests to override
	 * when using mock modules that do not actually exist in the workspace. 
	 */
	protected IModuleResourceDelta[] getDeltaForModule(IModule[] module) {
		IModuleResourceDelta[] deltas = ((Server)server).getPublishedResourceDelta(module);
		return deltas;
	}
	
	protected List<IModule[]> getRemovedModules() {
		List<IModule[]> l = ((Server)server).getAllModules();
		int size = l.size();
		((Server)server).getServerPublishInfo().addRemovedModules(l);
		if( l.size() > size ) {
			// if any were added
			List<IModule[]> l2 = l.subList(size, l.size()-1);
			return l2;
		}
		return new ArrayList<IModule[]>();
	}
	
	protected boolean hasBeenPublished(IModule[] mod) {
		return ((Server)server).getServerPublishInfo().hasModulePublishInfo(mod);
	}

	public int childPublishTypeRequired() {
		return childPublishTypeRequired(module);
	}
	
	private IModule lastModule() {
		return module == null ? null : module.length == 0 ? null : module[module.length-1];
	}
	
	protected int childPublishTypeRequired(IModule[] mod) {
		IModule[] children = getChildModules(mod);
		boolean atLeastIncremental = false;
		for( int i = 0; i < children.length; i++ ) {
			IModule[] combinedChild = combine(mod, children[i]);
			// if it's new, full publish it
			if( !hasBeenPublished(combinedChild)) {
				// structural change, new module
				return PublishControllerUtil.FULL_PUBLISH;
			}
			// else if it's removed, full remove it
			else if( isRemoved(combinedChild)) {
				// structural change, removed module
				return PublishControllerUtil.FULL_PUBLISH;
			}
			// else 
			else {
				// Check children
				int childrenRequire = childPublishTypeRequired(combinedChild);
				if( childrenRequire == PublishControllerUtil.FULL_PUBLISH ) {
					return childrenRequire;
				}
				if( childrenRequire == PublishControllerUtil.INCREMENTAL_PUBLISH) {
					atLeastIncremental = true;
				}
				// Otherwise, check if this module has any changes
				IModuleResourceDelta[] delta = getDeltaForModule(combinedChild);
				if( delta.length > 0 )
					atLeastIncremental = true;
			}
		}
		
		if( atLeastIncremental ) 
			return PublishControllerUtil.INCREMENTAL_PUBLISH;
		
		return PublishControllerUtil.NO_PUBLISH;
	}
	
}
