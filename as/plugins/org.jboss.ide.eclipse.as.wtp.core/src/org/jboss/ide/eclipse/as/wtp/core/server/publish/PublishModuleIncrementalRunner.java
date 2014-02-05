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
package org.jboss.ide.eclipse.as.wtp.core.server.publish;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.internal.Messages;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.ModuleResourceUtil;
import org.jboss.ide.eclipse.as.core.util.ProgressMonitorUtil;
import org.jboss.ide.eclipse.as.core.util.PublishCopyUtil;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;
/**
 * A utility class for the purposes of traversing either a 
 * Module Resource Tree, or a Module Resource Delta tree, 
 * and initiating the correct filesystem operations in terms
 * of copy, mkdir, touch, etc. 
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * <p>
 * This class allows for an {@link IModulePathFilter} for the purposes of 
 * cleaning the module tree or delta. 
 * 
 * </p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * <p>
 * This class re-implements portions of {@link PublishCopyUtil}, which 
 * will one day be removed and marked as deprecated
 * </p>
 * @since 3.0
 */
public final class PublishModuleIncrementalRunner {
	private static final IStatus[] EMPTY_STATUS = new IStatus[0];
	private static final IStatus CANCEL_STATUS = new Status(IStatus.CANCEL, ASWTPToolsPlugin.PLUGIN_ID, "Publish Canceled"); //$NON-NLS-1$
	private static final IStatus[] CANCEL_STATUS_ARR = new IStatus[]{CANCEL_STATUS};
	
	private IFilesystemController fsController;
	private IPath rootDirectory;
	
	
	/**
	 * Construct an instance of the PublishRunner.
	 * The PublishRunner requires a filesystem controller for the purposes 
	 * of adding / removing / resources, marking resources as changed, 
	 * or other filesystem-style operations.
	 * 
	 * The {@link IModulePathFilter} is used to clean the delta
	 * to allow only those files which match the given filter. 
	 * 
	 * All paths acquired from the given module resource (such as its relative path and name)
	 * will be appended to the rootDirectory member variable to get the absolute fs path.
	 * 
	 * @param handler
	 * @param rootDirectory
	 * @param filter
	 * @since 3.0
	 */
	public PublishModuleIncrementalRunner(IFilesystemController fsController, IPath rootDirectory) {
		this.fsController = fsController;
		this.rootDirectory = rootDirectory;
	}

	/**
	 * Handle a delta publish.
	 * 
	 * @param delta a module resource delta
	 * @param path the path to publish to
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status
	 * @since 3.0
	 */
	public IStatus[] publish(IModuleResourceDelta[] delta, IProgressMonitor monitor) throws CoreException {
		if (delta == null)
			return EMPTY_STATUS;
		
		int count = ModuleResourceUtil.countChanges(delta);
		monitor = ProgressMonitorUtil.getMonitorFor(monitor);
		monitor.beginTask("Incremental Publish", count * 100);
		List<IStatus> status = new ArrayList<IStatus>(2);
		int size2 = delta.length;
		for (int i = 0; i < size2; i++) {
			if( monitor.isCanceled())
				return CANCEL_STATUS_ARR;
			
			IStatus[] stat = publishDelta(delta[i], rootDirectory, new SubProgressMonitor(monitor, 100));
			addArrayToList(status, stat);
		}
		return status.toArray(new IStatus[status.size()]);
	}
	
	/**
	 * Handle a delta publish.
	 * 
	 * @param delta a module resource delta
	 * @param path the path to publish to
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status
	 * @since 3.0
	 */
	private IStatus[] publishDelta(IModuleResourceDelta delta, IPath root, 
			IProgressMonitor monitor) throws CoreException {
		List<IStatus> status = new ArrayList<IStatus>(2);
		if( monitor.isCanceled())
			return CANCEL_STATUS_ARR;

		IModuleResource resource = delta.getModuleResource();
		int kind2 = delta.getKind();
		IPath absolutePath = root.append(resource.getModuleRelativePath()).append(resource.getName());
		
		// Handle the case of a file
		if (resource instanceof IModuleFile) {
			IModuleFile file = (IModuleFile) resource;
			File ioFile = ModuleResourceUtil.getFile(file);

			if (kind2 == IModuleResourceDelta.REMOVED) {
				IStatus stat = fsController.deleteResource(absolutePath, monitor);
				if( stat != null )
					status.add( stat);
			} else if( kind2 != IModuleResourceDelta.NO_CHANGE){
				IStatus s1 = fsController.makeDirectoryIfRequired(absolutePath.removeLastSegments(1), new SubProgressMonitor(monitor, 10));
				if( s1 != null )
					status.add( s1);
				if( monitor.isCanceled())
					return CANCEL_STATUS_ARR;
				IStatus s2 = fsController.copyFile(ioFile, absolutePath, new SubProgressMonitor(monitor, 90));
				if( s2 != null )
					status.add( s2);
			}
			return status.toArray(new IStatus[status.size()]);
		}
		
		// Handle the case of an added folder
		if (kind2 == IModuleResourceDelta.ADDED) {
			IStatus stat = fsController.makeDirectoryIfRequired(absolutePath, monitor);
			if( stat != null )
				status.add( stat);
		}
		
		// Handle all child deltas. 
		// For added folders, handling children occurs after creating the folder.
		// For removed folders, handling children occurs before removing the folder
		// For NO_CHANGE folders, we traverse the children. API is unclear whether children will have changes.
		// For CHANGED folders we traverse the children.
		IModuleResourceDelta[] childDeltas = delta.getAffectedChildren();
		int size = childDeltas.length;
		for (int i = 0; i < size; i++) {
			if( monitor.isCanceled())
				return CANCEL_STATUS_ARR;
			IStatus[] stat = publishDelta(childDeltas[i], root, monitor);
			addArrayToList(status, stat);
		}
		
		// Handle the case where a folder is removed
		if (kind2 == IModuleResourceDelta.REMOVED) {
			IStatus stat = fsController.deleteResource(absolutePath, monitor);
			if( stat != null && !stat.isOK()) {
				status.add(new Status(IStatus.ERROR, ASWTPToolsPlugin.PLUGIN_ID,  IEventCodes.JST_PUB_FAIL, 
						NLS.bind(Messages.errorDeleting, absolutePath), stat.getException()));
			}
		}
		
		return (IStatus[]) status.toArray(new IStatus[status.size()]);
	}


	private static void addArrayToList(List<IStatus> list, IStatus[] a) {
		if (a != null && a.length != 0)
			list.addAll(Arrays.asList(a));
	}

}