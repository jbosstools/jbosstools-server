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
package org.jboss.ide.eclipse.as.rse.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;

public interface IFileServiceProvider {
	
	/**
	 * Get an rse {@link IFileService} for this object
	 * @return
	 * @throws CoreException
	 */
	public IFileService getFileService() throws CoreException;
	
	/**
	 * Get an rse {@link IFileServiceSubSystem} for this object
	 * @return
	 * @throws CoreException
	 */
	public IFileServiceSubSystem getFileServiceSubSystem() throws CoreException;
}
