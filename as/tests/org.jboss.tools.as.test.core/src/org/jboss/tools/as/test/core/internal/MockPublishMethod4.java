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
package org.jboss.tools.as.test.core.internal;

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
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IPublishCopyCallbackHandler;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;

public class MockPublishMethod4 extends AbstractPublishMethod {

	public static final String PUBLISH_METHOD_ID = "mock4";
	public static ArrayList<IPath> changed = new ArrayList<IPath>();
	public static ArrayList<IPath> removed = new ArrayList<IPath>();
	public static ArrayList<IPath> tempFiles = new ArrayList<IPath>();
	public static ArrayList<IModuleFile> copiedFiles = new ArrayList<IModuleFile>();
	
	
	protected static String expectedRoot = null;
	protected static String expectedTempRoot = null;
	
	protected static RuntimeException error = null;
	
	public IPublishCopyCallbackHandler getCallbackHandler(IPath path,
			IServer server) {
		return new MockCopyCallbackHandler(path, null);
	}

	public IPublishCopyCallbackHandler getCallbackHandler(IPath deployPath,
			IPath tmpFolder, IServer server) {
		return new MockCopyCallbackHandler(deployPath, tmpFolder);
	}

	
	public String getPublishDefaultRootFolder(IServer server) {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		return ds.getDeployFolder();
	}

	public String getPublishDefaultRootTempFolder(IServer server) {
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		return ds.getTempDeployFolder();
	}
	
	public String getPublishMethodId() {
		return PUBLISH_METHOD_ID;
	}
	public static void resetPublish() {
		changed.clear();
		removed.clear();
		tempFiles.clear();
		copiedFiles.clear();
		error = null;
	}
	
	/* Reset Everything */
	public static void reset() {
		changed.clear();
		removed.clear();
		tempFiles.clear();
		copiedFiles.clear();
		expectedRoot = null;
		expectedTempRoot = null;
		error = null;
	}
	
	public static void setExpectedRoot(String s) {
		expectedRoot = s;
	}

	public static void setExpectedTempRoot(String s) {
		expectedTempRoot = s;
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
	public static IPath[] getTempPaths() {
		return (IPath[]) tempFiles.toArray(new IPath[tempFiles.size()]);
	}
	public static Exception getError() {
		return error;
	}


	public class MockCopyCallbackHandler implements IPublishCopyCallbackHandler {
		private IPath root, tempRoot;
		public MockCopyCallbackHandler(IPath root, IPath tempRoot) {
			if( !(new Path(expectedRoot).isPrefixOf(root))) {
				String e = "Expected " + new Path(expectedRoot) + " but got: " + root.toString();
				System.out.println(e);
				System.out.println(expectedRoot);
				error = new RuntimeException("Unacceptable use of callback handler: " + e);
				throw error;
			}
			if( tempRoot != null && !(new Path(expectedTempRoot).isPrefixOf(tempRoot))) {
				String e = "Expected " + new Path(expectedTempRoot) + " but got: " + tempRoot.toString();
				System.out.println(e);
				error = new RuntimeException("Unacceptable use of callback handler: " + e);
				throw error;
			}
			this.root = root;
			this.tempRoot = tempRoot;
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
			IPath tmpFile = (tempRoot == null ? root : tempRoot).append(path);
			if( !tempFiles.contains(tmpFile.makeRelative()))
				tempFiles.add(tmpFile.makeRelative());
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
