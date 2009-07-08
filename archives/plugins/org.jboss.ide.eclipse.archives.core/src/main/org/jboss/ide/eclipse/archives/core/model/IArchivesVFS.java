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
	 * Translates a global path to any and all workspace path
	 * that match it, if it can
	 */
	public IPath[] absolutePathToWorkspacePath(IPath path);
	
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
