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
package org.jboss.ide.eclipse.archives.core.model.other.internal;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.core.variables.VariablesPlugin;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchivesVFS;
import org.jboss.ide.eclipse.archives.core.model.IVariableManager;

public class WorkspaceVFS implements IArchivesVFS, IDynamicVariableResolver {
	public WorkspaceVFS() {
	}

	public IPath[] getWorkspaceChildren(IPath path) {
		IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		if( res != null || !(res instanceof IContainer)) {
			try {
				IResource[] resources = ((IContainer)res).members();
				IPath[] paths = new IPath[resources.length];
				for( int i = 0; i < resources.length; i++ )
					paths[i] = resources[i].getFullPath();
				return paths;
			} catch( CoreException ce ) {
				return new IPath[0];
			}
		}
		return new IPath[0];
	}

	public IPath workspacePathToAbsolutePath(IPath path) {
		IResource r = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		IPath append = new Path(""); //$NON-NLS-1$
		while( r == null && path.segmentCount() > 0) {
			append = new Path(path.lastSegment()).append(append);
			path = path.removeLastSegments(1);
			r = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		}
		if( r != null )
			return r.getLocation().append(append);
		return null;
	}

	public String getProjectName(IPath absolutePath) {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for( int i = 0; i < projects.length; i++ )
			if( projects[i].getLocation().equals(absolutePath))
				return projects[i].getName();
		return null;
	}


	private String currentProject;
	public synchronized String performStringSubstitution(String expression,
			String projectName, boolean reportUndefinedVariables)
			throws CoreException {
		// set this project name
		if( expression == null )
			return null;

		currentProject = projectName;
		try {
			return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(expression, reportUndefinedVariables);
		} finally {
			currentProject = null;
		}
	}

	// Since the extension point re-instantiates this, we must delegate to the official instance
	public String resolveValue(IDynamicVariable variable, String argument)
			throws CoreException {
		if( this == ArchivesCore.getInstance().getVFS()) {
			if( variable.getName().equals(IVariableManager.CURRENT_PROJECT))
				return currentProject;
		} else {
			return ((WorkspaceVFS)ArchivesCore.getInstance().getVFS()).resolveValue(variable, argument);
		}
		return null;
	}

	public IPath[] absolutePathToWorkspacePath(IPath path) {
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
		IPath[] paths = new IPath[files.length];
		for( int i = 0; i < files.length; i++ ) {
			paths[i] = files[i].getFullPath();
		}
		return paths;
	}
}
