/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.server.behavior;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IModule;

/**
 * An IPrimaryPublishController is a publish controller 
 * in control of the operation which may delegate to module-specific
 * publishers, but which is capable of completing a publish operation, 
 * primarily by transferring the compiled deployment where and how it needs to end up finally,
 * or by removing modules if requested.
 */
public interface IPrimaryPublishController {

	/**
	 * Transfer a compiled zip file (or folder) to complete
	 * the publish operation using default behavior.
	 * 
	 * @param module
	 * @param srcFile
	 * @param monitor
	 * @return
	 */
	public int transferBuiltModule(IModule[] module, IPath srcFile, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Remove a given module using default behavior.
	 * 
	 * @param module
	 * @param srcFile
	 * @param monitor
	 * @return
	 */
	public int removeModule(IModule[] module, IProgressMonitor monitor) throws CoreException;

}
