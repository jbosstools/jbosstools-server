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
package org.jboss.ide.eclipse.as.core.server.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IPublishCopyCallbackHandler;

/**
 * This is a stub class for {@link IJBossServerPublishMethod} objects
 * that are unable to handle single-file updates or incremental publishing.
 * 
 * @since 3.0
 */
public class NullPublishCopyCallbackHandler implements
		IPublishCopyCallbackHandler {

	@Override
	public IStatus[] copyFile(IModuleFile mf, IPath path,
			IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IStatus[] deleteResource(IPath path, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFile(IPath path, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IStatus[] makeDirectoryIfRequired(IPath dir, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean shouldRestartModule() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IStatus[] touchResource(IPath path, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

}
