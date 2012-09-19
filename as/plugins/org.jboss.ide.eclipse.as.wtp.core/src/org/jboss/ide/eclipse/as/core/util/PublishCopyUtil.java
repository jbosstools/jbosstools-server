/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.internal.Messages;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.server.IPublishCopyCallbackHandler;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;
/**
 * Utility class with an assortment of useful file methods.
 * <p>
 * This class provides all its functionality through static members.
 * It is not intended to be subclassed or instantiated.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @since 2.0
 */
public final class PublishCopyUtil {
	private static final IStatus[] EMPTY_STATUS = new IStatus[0];
	private IPublishCopyCallbackHandler handler;
	public PublishCopyUtil(IPublishCopyCallbackHandler handler) {
		this.handler = handler;
	}

	protected IStatus[] canceledStatus() {
		return new IStatus[]{
				new Status(IStatus.CANCEL, ASWTPToolsPlugin.PLUGIN_ID, "Publish Canceled") //$NON-NLS-1$
			}; // todo
	}


	/**
	 * Handle a delta publish.
	 * 
	 * @param delta a module resource delta
	 * @param path the path to publish to
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status
	 * @deprecated
	 */
	public IStatus[] publishDelta(IModuleResourceDelta[] delta, IProgressMonitor monitor) throws CoreException {
		return publishDelta(delta, null, monitor);
	} 
	
	/**
	 * Handle a delta publish.
	 * 
	 * @param delta a module resource delta
	 * @param path the path to publish to
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status
	 * @since 2.3
	 */
	public IStatus[] publishDelta(IModuleResourceDelta[] delta, IModulePathFilter filter, 
			IProgressMonitor monitor) throws CoreException {

		if (delta == null)
			return EMPTY_STATUS;
		
		monitor = ProgressMonitorUtil.getMonitorFor(monitor);
		
		List<IStatus> status = new ArrayList<IStatus>(2);
		int size2 = delta.length;
		for (int i = 0; i < size2; i++) {
			IStatus[] stat = publishDelta(delta[i], new Path("/"), filter, monitor); //$NON-NLS-1$
			addArrayToList(status, stat);
		}
		
		return status.toArray(new IStatus[status.size()]);
	}

	@Deprecated
	public IStatus[] publishDelta(IModuleResourceDelta delta, IPath path, IProgressMonitor monitor) throws CoreException {
		return publishDelta(delta, path, null, monitor);
	}
	
	/**
	 * Handle a delta publish.
	 * 
	 * @param delta a module resource delta
	 * @param path the path to publish to
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status
	 * @since 2.3
	 */
	public IStatus[] publishDelta(IModuleResourceDelta delta, IPath path, 
			IModulePathFilter filter, IProgressMonitor monitor) throws CoreException {
		List<IStatus> status = new ArrayList<IStatus>(2);
		if( monitor.isCanceled())
			return canceledStatus();

		IModuleResource resource = delta.getModuleResource();
		int kind2 = delta.getKind();
		
		if (resource instanceof IModuleFile) {
			IModuleFile file = (IModuleFile) resource;
			if (kind2 == IModuleResourceDelta.REMOVED) {
				IPath path2 = path.append(file.getModuleRelativePath()).append(file.getName());
				handler.deleteResource(path2, monitor);
			}
			else if( filter == null || filter.shouldInclude(resource)){
				IPath path2 = path.append(file.getModuleRelativePath()).append(file.getName());
				handler.makeDirectoryIfRequired(path2.removeLastSegments(1), monitor);
				handler.copyFile(file, path2, monitor);
			}
			return status.toArray(new IStatus[status.size()]);
		}
		
		if (kind2 == IModuleResourceDelta.ADDED) {
			IPath path2 = path.append(resource.getModuleRelativePath()).append(resource.getName());
			IStatus[] s = handler.makeDirectoryIfRequired(path2, monitor);
			if( s != null && s.length > 0 && !s[0].isOK()) {
				return s;
			}
		}
		
		IModuleResourceDelta[] childDeltas = delta.getAffectedChildren();
		int size = childDeltas.length;
		for (int i = 0; i < size; i++) {
			if( monitor.isCanceled())
				return canceledStatus();
			IStatus[] stat = publishDelta(childDeltas[i], path, filter, monitor);
			addArrayToList(status, stat);
		}
		
		if (kind2 == IModuleResourceDelta.REMOVED) {
			IPath path2 = path.append(resource.getModuleRelativePath()).append(resource.getName());
			IStatus[] stat = handler.deleteResource(path2, monitor);
			if( stat.length > 0 && !stat[0].isOK()) {
				status.add(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID,  IEventCodes.JST_PUB_FAIL, NLS.bind(Messages.errorDeleting, path2), null));
			}
		}
		
		IStatus[] stat = new IStatus[status.size()];
		status.toArray(stat);
		return stat;
	}


	/**
	 * @deprecated
	 */
	public IStatus[] initFullPublish(IModuleResource[] resources, IProgressMonitor monitor) throws CoreException  {
		return initFullPublish(resources, null, monitor);
	}
	
	/**
	 * Publish the given module resources to the given path.
	 * 
	 * @param resources an array of module resources
	 * @param path a path to publish to
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @return a possibly-empty array of error and warning status
	 * @since 2.3
	 */
	public IStatus[] initFullPublish(IModuleResource[] resources, IModulePathFilter filter, IProgressMonitor monitor) throws CoreException  {
		int count = ModuleResourceUtil.countMembers(resources);
		monitor = ProgressMonitorUtil.getMonitorFor(monitor);
		monitor.beginTask("Publishing " + count + " resources", //$NON-NLS-1$ //$NON-NLS-2$ 
				(100 * (count)) + 200);
		handler.makeDirectoryIfRequired(new Path("/"),  //$NON-NLS-1$
				ProgressMonitorUtil.getSubMon(monitor, 100)); 
		if( monitor.isCanceled())
			return canceledStatus();
		IStatus[] results = publishFull(resources, new Path("/"), filter, monitor); //$NON-NLS-1$
		monitor.done();
		return results;
	}
	
	protected IStatus[] publishFull(IModuleResource[] resources, IPath relative,
			IModulePathFilter filter, IProgressMonitor monitor) throws CoreException {
		if (resources == null)
			return EMPTY_STATUS;
		List<IStatus> status = new ArrayList<IStatus>(2);
		int size = resources.length;
		for (int i = 0; i < size; i++) {
			if( monitor.isCanceled())
				return canceledStatus();
			if( filter == null || filter.shouldInclude(resources[i])) {
				IStatus[] stat = copy(resources[i], relative, filter, monitor); 
				addArrayToList(status, stat);
			}
		}
		return status.toArray(new IStatus[status.size()]);
	}

	
	protected IStatus[] publishFull(IModuleResource[] resources, IPath relative, 
			IProgressMonitor monitor) throws CoreException {
		return publishFull(resources, relative, null, monitor);
	}

	private IStatus[] copy(IModuleResource resource, IPath path, IModulePathFilter filter, IProgressMonitor monitor) throws CoreException {
		String name = resource.getName();
		//Trace.trace(Trace.PUBLISHING, "Copying: " + name + " to " + path.toString());
		List<IStatus> status = new ArrayList<IStatus>(2);
		if (resource instanceof IModuleFolder) {
			IModuleFolder folder = (IModuleFolder) resource;
			IModuleResource[] children = folder.members();
			if( children.length == 0 )
				handler.makeDirectoryIfRequired(folder.getModuleRelativePath().append(folder.getName()), 
						ProgressMonitorUtil.getSubMon(monitor, 5));
			else {
				IStatus[] stat = publishFull(children, path, filter, monitor);
				addArrayToList(status, stat);
			}
		} else {
			IModuleFile mf = (IModuleFile) resource;
			if( filter == null || filter.shouldInclude(resource)) {
				path = path.append(mf.getModuleRelativePath()).append(name);
				IStatus[] stats = handler.makeDirectoryIfRequired(path.removeLastSegments(1), new NullProgressMonitor());
				if( stats != null && stats.length > 0 && !stats[0].isOK())
					addArrayToList(status, stats);
				addArrayToList(status, handler.copyFile(mf, path, 
						ProgressMonitorUtil.getSubMon(monitor, 100)));
			}
		}
		return status.toArray(new IStatus[status.size()]);
	}

	public static void addArrayToList(List<IStatus> list, IStatus[] a) {
		if (list == null || a == null || a.length == 0)
			return;
		
		int size = a.length;
		for (int i = 0; i < size; i++)
			list.add(a[i]);
	}

}