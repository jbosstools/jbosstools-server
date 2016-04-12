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
		public static IPath[] getRemoved() {
			return (IPath[]) removed.toArray(new IPath[removed.size()]);
		}
		public static java.io.File[] getChangedFiles() {
			return copiedFiles.toArray(new java.io.File[copiedFiles.size()]);
		}
		
		public static IPath[] getChanged() {
			return (IPath[]) changed.toArray(new IPath[changed.size()]);
		}
		public static void clearAll() {
			changed.clear();
			removed.clear();
			copiedFiles.clear();
		}
		
	}
	public static StaticModel model = new StaticModel();
	
	

	private LocalFilesystemController delegate;
	private StaticModel myModelRef = model;
	private LocalFilesystemController getDelegate() {
		if( delegate == null ) {
			delegate = new LocalFilesystemController();
			delegate.initialize(getServer(), null, null);
		}
		return delegate;
	}
	
	@Override
	public IStatus copyFile(File file, IPath absoluteRemotePath, IProgressMonitor monitor) throws CoreException {
		if( !model.changed.contains(absoluteRemotePath)) 
			model.changed.add(absoluteRemotePath);
		if( !model.copiedFiles.contains(file))
			model.copiedFiles.add(file);
		getDelegate().copyFile(file, absoluteRemotePath, monitor);
		return null;
	}

	@Override
	public IStatus deleteResource(IPath path, IProgressMonitor monitor)
			throws CoreException {
		if( !model.removed.contains(path))
			model.removed.add(path);
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
		if( !model.changed.contains(dir))
			model.changed.add(dir);
		getDelegate().makeDirectoryIfRequired(dir, monitor);
		return null;
	}

	@Override
	public IStatus touchResource(IPath path, IProgressMonitor monitor)
			throws CoreException {
		if( !model.changed.contains(path))
			model.changed.add(path);
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
