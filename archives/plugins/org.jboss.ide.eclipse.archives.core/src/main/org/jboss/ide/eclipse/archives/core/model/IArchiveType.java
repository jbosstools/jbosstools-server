/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.core.model;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This interface represents a package type (i.e. JAR,WAR,SAR etc).
 * 
 * A package type's main focus right now is to provide a default "template" Package for a given Project, making it easier
 * for users and adopters to automatically adapt projects into a deployable package type.
 * 
 * @author Marshall
 */
public interface IArchiveType {

	/**
	 * @return The ID for this PackageType, i.e. "jar", "war" etc
	 */
	public String getId();
	
	/** 
	 * @return The label for this PackageType (usually shown in UI)
	 */
	public String getLabel();
	
	/**
	 * This will create a "default" packaging configuration for this project using this package type.
	 * 
	 * If the passed-in project does not support this package type, a null IPackage should be returned.
	 * 
	 * @param projectName The project to create the packages configuration for
	 * @return The top level package that was created
	 */
	public IArchive createDefaultConfiguration(String projectName, IProgressMonitor monitor);
	
	/**
	 * Fill an archive type with some filesets and folders that are required
	 */ 
	public IArchive fillDefaultConfiguration(String projectName, IArchive topLevel, IProgressMonitor monitor);
	
}
