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
import org.jboss.ide.eclipse.as.core.server.IPublishCopyCallbackHandler;

/**
 * 
 * This is a subsystem for dealing with the filesystem.
 * Initially, it will contain only those methods which are used by publishing, 
 * but, this may be expanded in the future.
 * 
 * It is important to note that this class requires all 
 * remote paths in the signatures to already be absolute
 * for the system to which the resource is to be copied. 
 * 
 * This behavior is different than {@link IPublishCopyCallbackHandler},
 * which takes paths relative to whatever path the object was instantiated with. 
 * 
 * Methods in this class should return an error status or warning status
 * for any important information that should be logged. It should only
 * throw a {@link CoreException} in cases where the publish should be
 * immediately terminated. 
 * 
 * @since 3.0
 */
public interface IFilesystemController extends ISubsystemController {
	
	public static final String SYSTEM_ID = "filesystem";
	
	
	/**
	 * Copy the file to an absolute file path 
	 * 
	 * @param f 	The local file
	 * @param path	The remote path it should be copied to
	 * @param monitor A progress monitor
	 * @return an error status 
	 * @throws CoreException
	 */
	public IStatus copyFile(File f, IPath path, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Delete a directory for this absolute path
	 *  
	 * @param path
	 * @param monitor
	 * @return an error status 
	 */
	public IStatus deleteResource(IPath path, IProgressMonitor monitor) throws CoreException ;
	
	
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
	 * Returns true if the given absolute path exists.
	 * 
	 * @param path
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	public boolean exists(IPath path, IProgressMonitor monitor) throws CoreException;
	/**
	 * Make a directory for this absolute path 
	 * 
	 * @param dir
	 * @param monitor
	 * @return a list of error status objects. 
	 * @throws CoreException
	 */
	public IStatus makeDirectoryIfRequired(IPath dir, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * For touching / updating timestamp
	 * @param path
	 * @return
	 * @throws CoreException
	 */
	public IStatus touchResource(IPath path, IProgressMonitor monitor) throws CoreException;
}
