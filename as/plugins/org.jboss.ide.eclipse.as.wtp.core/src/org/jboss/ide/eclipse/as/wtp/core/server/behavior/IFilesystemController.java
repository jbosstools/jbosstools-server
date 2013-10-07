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
package org.jboss.ide.eclipse.as.wtp.core.server.behavior;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * 
 * This is a subsystem for dealing with the filesystem.
 * Initially, it will contain only those methods which are used by publishing, 
 * but, this will be expanded in the future! 
 * 
 * @since 3.0
 */
public interface IFilesystemController extends ISubsystemController {
	/**
	 * Copy the file to an absolute file path 
	 * 
	 * @param mf
	 * @param path
	 * @param monitor
	 * @return a list of error status objects. 
	 * @throws CoreException
	 */
	public IStatus[] copyFile(File f, IPath path, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Delete a directory for this absolute path
	 *  
	 * @param path
	 * @param monitor
	 * @return a list of error status objects. 
	 */
	public IStatus[] deleteResource(IPath path, IProgressMonitor monitor) throws CoreException ;
	
	
	/**
	 * Return true if the given absolute path exists and is a file. 
	 * Return false if the given absolute path does not exist, or, is a folder.
	 * 
	 * @param path
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	public boolean isFile(IPath path, IProgressMonitor monitor) throws CoreException;

	/**
	 * Make a directory for this absolute path 
	 * 
	 * @param dir
	 * @param monitor
	 * @return a list of error status objects. 
	 */
	public IStatus[] makeDirectoryIfRequired(IPath dir, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * For touching / updating timestamp
	 * @param path
	 * @return
	 */
	public IStatus[] touchResource(IPath path, IProgressMonitor monitor);
}
