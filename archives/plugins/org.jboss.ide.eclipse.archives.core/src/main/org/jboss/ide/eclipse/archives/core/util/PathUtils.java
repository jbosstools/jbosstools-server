package org.jboss.ide.eclipse.archives.core.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFileSet;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;

public class PathUtils {
	public static IPath getGlobalLocation(IArchiveNode node) {
		if( node instanceof IArchive ) {
			IArchive a = (IArchive)node;
			return getGlobalLocation(a.getRawDestinationPath(),
					a.getProjectName(), a.isDestinationInWorkspace(),
					a.getDescriptorVersion());
		}
		else if( node instanceof IArchiveFileSet ) {
			IArchiveFileSet a = (IArchiveFileSet)node;
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
		else if( node instanceof IArchiveFileSet ) {
			IArchiveFileSet a = (IArchiveFileSet)node;
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
			if( inWorkspace && version >= 1.2) {
				IPath p = new Path(translated);
				if( !p.isAbsolute() )
					p = new Path(projectName).append(p).makeAbsolute();
				return p.toString();
			} else {
				// should be workspace relative already
				return new Path(translated).makeAbsolute().toString();
			}
		} catch( CoreException ce ) {
			ce.printStackTrace();
		}
		return null;
	}
}
