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
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.wtp.core.Trace;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.util.ModuleResourceUtil;
import org.jboss.ide.eclipse.as.core.util.ProgressMonitorUtil;
import org.jboss.ide.eclipse.as.core.util.PublishCopyUtil;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;
/**
 * A utility class for the purposes of traversing a 
 * Module Resource Tree, and initiating the correct filesystem 
 * operations in terms of copy, mkdir, touch, etc. 
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * <p>
 * All {@link IModuleResource} and {@link IModuleResourceDelta} arrays
 * that are passed in are expected to be already cleaned in some other fashion.
 * What this means is that only the resources or deltas passed in will be traversed. 
 * If a client wishes for this tree to be filtered, it must be filtered beforehand.
 * This may be done via a {@link IModulePathFilter} object, but it is outside
 * the scope of this class. 
 * </p>
 * <p><b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
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
public final class PublishModuleFullRunner {
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
	 * The {@link IModulePathFilter} is used to clean the delta or module resource tree
	 * to allow only those files which match the given filter. 
	 * 
	 * All paths acquired from the given module resource (such as its relative path and name)
	 * will be appended to the rootDirectory member variable to get the absolute fs path.
	 * 
	 * @param controller a fs controller for doing fs operations
	 * @param rootDirectory
	 * @param filter
	 */
	public PublishModuleFullRunner(IFilesystemController controller, IPath rootDirectory) {
		this.fsController = controller;
		this.rootDirectory = rootDirectory;
	}
	
	/**
	 * Publish the given module resources to the given path.
	 * 
	 * @param resources an array of module resources
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status
	 * @since 2.3
	 */
	public IStatus[] fullPublish(IModuleResource[] resources, IProgressMonitor monitor) throws CoreException  {
		Trace.trace(Trace.STRING_FINER, "      Executing full publish"); //$NON-NLS-1$

		// Initiate the progress monitor stuff
		// A full count is gotten early because otherwise
		// the progress monitor will slow down the deeper in a tree it goes
		int count = ModuleResourceUtil.countMembers(resources, true);
		Trace.trace(Trace.STRING_FINER, "      Items to handle: " + count); //$NON-NLS-1$
		monitor = ProgressMonitorUtil.getMonitorFor(monitor);
		monitor.beginTask("Publishing " + count + " resources", //$NON-NLS-1$ //$NON-NLS-2$ 
				(100 * (count)) + 200);
		
		if( monitor.isCanceled())
			return CANCEL_STATUS_ARR;
		
		Trace.trace(Trace.STRING_FINER, "      Deleting root directory: " + rootDirectory); //$NON-NLS-1$
		fsController.deleteResource(rootDirectory, ProgressMonitorUtil.getSubMon(monitor, 100));
		
		if( monitor.isCanceled())
			return CANCEL_STATUS_ARR;
		
		// Create the remote folder where we'll be copying files to
		Trace.trace(Trace.STRING_FINER, "      Creating folder: " + rootDirectory); //$NON-NLS-1$
		IStatus s = fsController.makeDirectoryIfRequired(rootDirectory,
				ProgressMonitorUtil.getSubMon(monitor, 100)); 
		
		// Last check on progress monitor
		if( monitor.isCanceled())
			return CANCEL_STATUS_ARR;
		
		// Begin to run through the resources
		ArrayList<IStatus> all =new ArrayList<IStatus>();
		IStatus[] results = traverseResources(resources, rootDirectory, monitor); //$NON-NLS-1$
		if( s != null )
			all.add(s);
		all.addAll(Arrays.asList(results));
		monitor.done();
		return results;
	}
	
	// Iterate over the array and call handleSingleResource(etc) on each item
	private IStatus[] traverseResources(IModuleResource[] resources, IPath relative,
			IProgressMonitor monitor) throws CoreException {
		if (resources == null)
			return EMPTY_STATUS;
		List<IStatus> status = new ArrayList<IStatus>(2);
		int size = resources.length;
		for (int i = 0; i < size; i++) {
			if( monitor.isCanceled())
				return CANCEL_STATUS_ARR;
			IStatus[] stat = handleSingleResource(resources[i], relative, monitor); 
			addArrayToList(status, stat);
		}
		return status.toArray(new IStatus[status.size()]);
	}

	
	// Use the fscontroller to make required directories, or copy relevent files
	// Recursively iterate folders
	private IStatus[] handleSingleResource(IModuleResource resource, IPath path, IProgressMonitor monitor) throws CoreException {
		Trace.trace(Trace.STRING_FINER, "      Copying Resource: " + path); //$NON-NLS-1$

		String name = resource.getName();
		IPath rel = resource.getModuleRelativePath();
		IPath absoluteRemote = path.append(rel).append(name);
		
		//Trace.trace(Trace.PUBLISHING, "Copying: " + name + " to " + path.toString());
		List<IStatus> status = new ArrayList<IStatus>(2);
		if (resource instanceof IModuleFolder) {
			IModuleResource[] children = ((IModuleFolder) resource).members();
			if( children.length == 0 ) {
				// Ensure the empty remote folder is still created
				Trace.trace(Trace.STRING_FINER, "      Creating folder: " + path); //$NON-NLS-1$
				fsController.makeDirectoryIfRequired(absoluteRemote, 
						ProgressMonitorUtil.getSubMon(monitor, 100));
			} else {
				// Re-traverse this new folder
				IStatus[] stat = traverseResources(children, path, monitor);
				addArrayToList(status, stat);
			}
		} else {
			// Ensure the directory is created.  This should already be true,
			// so we weight it only 10 in the progmon
			Trace.trace(Trace.STRING_FINER, "      Ensuring folder already created: " + absoluteRemote.removeLastSegments(1)); //$NON-NLS-1$
			IStatus stats = fsController.makeDirectoryIfRequired(absoluteRemote.removeLastSegments(1), 
					ProgressMonitorUtil.getSubMon(monitor, 10));
			status.add( stats);
			
			// Then copy the file
			File file = ModuleResourceUtil.getFile(((IModuleFile)resource));
			Trace.trace(Trace.STRING_FINER, "      Copying file: " + absoluteRemote); //$NON-NLS-1$
			status.add(fsController.copyFile(file, absoluteRemote, 
					ProgressMonitorUtil.getSubMon(monitor, 90)));
		}
		return status.toArray(new IStatus[status.size()]);
	}

	/**
	 * This method assumes the list is non-null
	 * Simply add all elements of the array to the list
	 * @param list
	 * @param a
	 */
	private void addArrayToList(List<IStatus> list, IStatus[] a) {
		if (a != null && a.length != 0)
			list.addAll(Arrays.asList(a));
	}
}