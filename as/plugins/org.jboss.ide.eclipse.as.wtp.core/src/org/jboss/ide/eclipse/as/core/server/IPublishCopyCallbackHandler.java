/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.model.IModuleFile;

public interface IPublishCopyCallbackHandler {
	/**
	 * Copy the file (mf) to a file related to the relative path (path). 
	 * 
	 * For example if this path is "/someFolder/file.txt" you may want to 
	 * copy the file to /home/someone/deployfolder/project.war/someFolder/file.txt
	 * 
	 * @param mf
	 * @param path
	 * @param monitor
	 * @return a list of error status objects. 
	 * @throws CoreException
	 */
	public IStatus[] copyFile(IModuleFile mf, IPath path, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Delete a directory for this path relative to where the module belongs.
	 * For example if this path is "/someFolder" you may want to 
	 * delete the folder /home/someone/deployfolder/project.war/someFolder
	 * 
	 * @param dir
	 * @param monitor
	 * @return a list of error status objects. 
	 */
	public IStatus[] deleteResource(IPath path, IProgressMonitor monitor) throws CoreException ;
	
	
	/**
	 * Return true if the given path exists and is a file. 
	 * Return false if hte given path does not exist, or, is a folder.
	 * 
	 * @param path
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	public boolean isFile(IPath path, IProgressMonitor monitor) throws CoreException;

	/**
	 * Make a directory for this path relative to where the module belongs.
	 * For example if this path is "/someFolder" you may want to 
	 * make the folder /home/someone/deployfolder/project.war/someFolder
	 * 
	 * @param dir
	 * @param monitor
	 * @return a list of error status objects. 
	 */
	public IStatus[] makeDirectoryIfRequired(IPath dir, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Verify whether any changes made require a module restart
	 * @return
	 */
	public boolean shouldRestartModule();
	
	/**
	 * For touching / updating timestamp
	 * @param path
	 * @return
	 */
	public IStatus[] touchResource(IPath path, IProgressMonitor monitor);
}