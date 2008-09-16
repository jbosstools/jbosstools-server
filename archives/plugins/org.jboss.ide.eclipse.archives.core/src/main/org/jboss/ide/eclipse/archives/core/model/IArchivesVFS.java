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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * 
 * @author rob.stryker <rob.stryker@redhat.com>
 *
 */
public interface IArchivesVFS {
	
	/**
	 * Returns a list of workspace-relative paths
	 * that are children of the parameter. 
	 * @param path
	 * @return
	 */
	public IPath[] getWorkspaceChildren(IPath path);
	
	/**
	 * Translates a workspace path to a global path
	 * @param path
	 * @return
	 */
	public IPath workspacePathToAbsolutePath(IPath path);
	
	/**
	 * Get the project name that matches this path
	 * @param absolutePath
	 * @return
	 */
	public String getProjectName(IPath absolutePath);
	
	/**
	 * Substitute some strings
	 * @param expression
	 * @param projectName
	 * @param reportUndefinedVariables
	 * @return
	 * @throws CoreException
	 */
	public String performStringSubstitution(String expression,	String projectName, boolean reportUndefinedVariables) throws CoreException;
}
