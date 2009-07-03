package org.jboss.ide.eclipse.as.wtp.override.core.vcf;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualArchiveComponent;

public class JBTVirtualArchiveComponent 
	extends J2EEModuleVirtualArchiveComponent implements IJBTComponent {

	public JBTVirtualArchiveComponent(IProject aComponentProject,
			String archiveLocation, IPath aRuntimePath) {
		super(aComponentProject, archiveLocation, aRuntimePath);
	}
	
}
