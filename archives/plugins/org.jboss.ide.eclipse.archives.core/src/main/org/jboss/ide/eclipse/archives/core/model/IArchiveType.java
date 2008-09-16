/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
