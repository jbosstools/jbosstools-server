/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.as.test.core.internal;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.LocalFilesystemController;

public class MockPublishMethodFilesystemController extends AbstractSubsystemController implements
		ISubsystemController, IFilesystemController {
	public static class StaticModel {
		public static ArrayList<IPath> changed = new ArrayList<IPath>();
		public static ArrayList<IPath> removed = new ArrayList<IPath>();
		public static ArrayList<java.io.File> copiedFiles = new ArrayList<java.io.File>();		
		
		public static synchronized IPath[] getRemoved() {
			return (IPath[]) removed.toArray(new IPath[removed.size()]);
		}
		public static synchronized java.io.File[] getChangedFiles() {
			return copiedFiles.toArray(new java.io.File[copiedFiles.size()]);
		}
		
		public static synchronized  void markChanged(IPath absoluteRemotePath, File file) {
			System.out.println("Marking changed: " + absoluteRemotePath.toOSString());
			if( !changed.contains(absoluteRemotePath)) 
				changed.add(absoluteRemotePath);
			if( !copiedFiles.contains(file))
				copiedFiles.add(file);
		}
		public static synchronized void dirChanged(IPath dir) {
			if( !changed.contains(dir))
				changed.add(dir);
		}
		
		public static synchronized  void touch(IPath p) {
			System.out.println("Touching: " + p.toOSString());
			if( !changed.contains(p))
				changed.add(p);
		}
		public static synchronized  void delete(IPath p) {
			System.out.println("Deleting: " + p.toOSString());
			if( !removed.contains(p))
				removed.add(p);
		}
		public static synchronized  IPath[] getChanged() {
			return (IPath[]) changed.toArray(new IPath[changed.size()]);
		}
		public static synchronized  void clearAll() {
			System.out.println("[MockPublishMethodFilesystemController] - Clearing all");
			changed.clear();
			removed.clear();
			copiedFiles.clear();
		}
		
	}
	public static StaticModel model = new StaticModel();
	
	

	private LocalFilesystemController delegate;
	private LocalFilesystemController getDelegate() {
		if( delegate == null ) {
			delegate = new LocalFilesystemController();
			delegate.initialize(getServer(), null, null);
		}
		return delegate;
	}
	
	@Override
	public IStatus copyFile(File file, IPath absoluteRemotePath, IProgressMonitor monitor) throws CoreException {
		model.markChanged(absoluteRemotePath, file);
		getDelegate().copyFile(file, absoluteRemotePath, monitor);
		return null;
	}

	@Override
	public IStatus deleteResource(IPath path, IProgressMonitor monitor)
			throws CoreException {
		model.delete(path);
		getDelegate().deleteResource(path, monitor);
		return null;
	}

	@Override
	public boolean isFile(IPath path, IProgressMonitor monitor)
			throws CoreException {
		return path.toFile().exists() && path.toFile().isFile();
	}

	@Override
	public IStatus makeDirectoryIfRequired(IPath dir, IProgressMonitor monitor)
			throws CoreException {
		model.dirChanged(dir);
		getDelegate().makeDirectoryIfRequired(dir, monitor);
		return null;
	}

	@Override
	public IStatus touchResource(IPath path, IProgressMonitor monitor)
			throws CoreException {
		model.touch(path);
		getDelegate().makeDirectoryIfRequired(path.removeLastSegments(1), monitor);
		getDelegate().touchResource(path, monitor);
		return Status.OK_STATUS;
	}

	@Override
	public boolean exists(IPath path, IProgressMonitor monitor)
			throws CoreException {
		return path.toFile().exists();
	}
}
