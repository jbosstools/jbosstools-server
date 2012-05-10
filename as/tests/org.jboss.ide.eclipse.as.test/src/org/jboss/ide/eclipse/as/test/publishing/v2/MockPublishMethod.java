/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.test.publishing.v2;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.jboss.ide.eclipse.as.core.publishers.AbstractPublishMethod;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IPublishCopyCallbackHandler;

public class MockPublishMethod extends AbstractPublishMethod {

	public static final String PUBLISH_METHOD_ID = "mock";
	public static final String MOCK_ROOT = "mockRoot";
	public static ArrayList<IPath> changed = new ArrayList<IPath>();
	public static ArrayList<IPath> removed = new ArrayList<IPath>();
	public static ArrayList<IModuleFile> copiedFiles = new ArrayList<IModuleFile>();
	
	public IPublishCopyCallbackHandler getCallbackHandler(IPath path,
			IServer server) {
		return new MockCopyCallbackHandler(path);
	}

	public String getPublishDefaultRootFolder(IServer server) {
		return "/" + MOCK_ROOT;
	}

	public String getPublishMethodId() {
		return PUBLISH_METHOD_ID;
	}
	public static void reset() {
		changed.clear();
		removed.clear();
		copiedFiles.clear();
	}
	public static IPath[] getRemoved() {
		return (IPath[]) removed.toArray(new IPath[removed.size()]);
	}
	public static IModuleFile[] getChangedFiles() {
		return copiedFiles.toArray(new IModuleFile[copiedFiles.size()]);
	}
	
	public static IPath[] getChanged() {
		return (IPath[]) changed.toArray(new IPath[changed.size()]);
	}


	public class MockCopyCallbackHandler implements IPublishCopyCallbackHandler {
		private IPath root;
		public MockCopyCallbackHandler(IPath root) {
			if( !(new Path(MOCK_ROOT).isPrefixOf(root))) {
				System.out.println("Expected " + new Path(MOCK_ROOT) + " but got: " + root.toString());
				throw new RuntimeException("Unacceptable use of callback handler");
			}
			this.root = root;
		}
		
		public IStatus[] deleteResource(IPath path, IProgressMonitor monitor)
				throws CoreException {
//			System.out.println("deleting " + root.append(path));
			IPath path2 = root.append(path);
			if( !removed.contains(path2.makeRelative()))
				removed.add(path2.makeRelative());
			return new IStatus[]{};
		}

		public IStatus[] makeDirectoryIfRequired(IPath dir,
				IProgressMonitor monitor) throws CoreException {
//			System.out.println("mkdir to " + root.append(dir));

			IPath path2 = root.append(dir);
			if( !changed.contains(path2.makeRelative()))
				changed.add(path2.makeRelative());
			return new IStatus[]{};
		}

		private boolean shouldRestartModule = false;
		public boolean shouldRestartModule() {
			return shouldRestartModule;
		}
		public IStatus[] copyFile(IModuleFile mf, IPath path,
				IProgressMonitor monitor) throws CoreException {
//			System.out.println("copying to " + root.append(path));
			File file = PublishUtil.getFile(mf);
			shouldRestartModule |= checkRestartModule(file);
			IPath path2 = root.append(path);
			if( !changed.contains(path2.makeRelative()))
				changed.add(path2.makeRelative());
			copiedFiles.add(mf);
			return new IStatus[]{};
		}
		
		public IStatus[] touchResource(IPath path, IProgressMonitor monitor) {
//			System.out.println("touching " + root.append(path));
			IPath path2 = root.append(path);
			if( !changed.contains(path2.makeRelative()))
				changed.add(path2.makeRelative());
			return new IStatus[]{};
		}

		public boolean isFile(IPath path, IProgressMonitor monitor)
				throws CoreException {
			IPath path2 = root.append(path);
			return path2.toFile().exists() && path2.toFile().isFile();
		}
		public boolean checkRestartModule(File file) {
			if( file.getName().toLowerCase().endsWith(".jar")) //$NON-NLS-1$
				return true;
			return false;
		}

	}
}
