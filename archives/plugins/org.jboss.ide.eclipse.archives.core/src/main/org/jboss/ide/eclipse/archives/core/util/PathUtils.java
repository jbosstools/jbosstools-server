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
package org.jboss.ide.eclipse.archives.core.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveModelRootNode;
import org.jboss.ide.eclipse.archives.core.model.IArchiveStandardFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;

public class PathUtils {
	public static IPath getGlobalLocation(IArchiveNode node) {
		if( node instanceof IArchive ) {
			IArchive a = (IArchive)node;
			return getGlobalLocation(a.getRawDestinationPath(),
					a.getProjectName(), a.isDestinationInWorkspace(),
					a.getDescriptorVersion());
		}
		else if( node instanceof IArchiveStandardFileSet ) {
			IArchiveStandardFileSet a = (IArchiveStandardFileSet)node;
			return getGlobalLocation(a.getRawSourcePath(),
					a.getProjectName(), a.isInWorkspace(),
					a.getDescriptorVersion());
		}
		return null;
	}

	public static String getAbsoluteLocation(IArchiveNode node) {
		if( node instanceof IArchive ) {
			IArchive a = (IArchive)node;
			return getAbsoluteLocation(a.getRawDestinationPath(), a.getProjectName(), a.isDestinationInWorkspace(), a.getDescriptorVersion());
		}
		else if( node instanceof IArchiveStandardFileSet ) {
			IArchiveStandardFileSet a = (IArchiveStandardFileSet)node;
			return getAbsoluteLocation(a.getRawSourcePath(), a.getProjectName(), a.isInWorkspace(), a.getDescriptorVersion());
		}
		return null;
	}

	// Get the actual file located on the filesystem
	public static IPath getGlobalLocation(String expression,
			String projectName, boolean inWorkspace, double version) {
		String absolute = getAbsoluteLocation(expression, projectName, inWorkspace, version);
		if( absolute != null ) {
			if( inWorkspace ) {
				IPath p = ArchivesCore.getInstance().getVFS()
					.workspacePathToAbsolutePath(new Path(absolute));
				return p;
			}
			return new Path(absolute);
		}
		return null;
	}

	// Get an absolute path, workspace-absolute or fs-absolute
	public static String getAbsoluteLocation(String expression,
			String projectName, boolean inWorkspace, double version) {
		if( inWorkspace && ("".equals(expression) || ".".equals(expression))) //$NON-NLS-1$ //$NON-NLS-2$
			return new Path(projectName).makeAbsolute().toString();

		try {
			String translated = ArchivesCore.getInstance().getVFS().
				performStringSubstitution(expression, projectName, true);
			if( inWorkspace ) {
				if( version >= IArchiveModelRootNode.DESCRIPTOR_VERSION_1_2) {
					IPath p = new Path(translated);
					if( !p.isAbsolute() )
						p = new Path(projectName).append(p).makeAbsolute();
					return p.toString();
				} else {
					// should be workspace relative already
					return new Path(translated).makeAbsolute().toString();
				}
			} else {
				if( new Path(translated).isAbsolute())
					return new Path(translated).toString();
				// not absolute... hrmm
				IPath projectPath = ArchivesCore.getInstance().getVFS().workspacePathToAbsolutePath(new Path(projectName));
				return projectPath.append(translated).toString();
			}
		} catch( CoreException ce ) {
			// ignore, just return null ce.printStackTrace();
		}
		return null;
	}
}
