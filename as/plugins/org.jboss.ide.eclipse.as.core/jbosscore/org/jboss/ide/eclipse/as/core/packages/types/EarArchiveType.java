package org.jboss.ide.eclipse.as.core.packages.types;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.ide.eclipse.archives.core.model.IArchive;

public class EarArchiveType extends J2EEArchiveType {
	public final static String ID = "org.jboss.ide.eclipse.as.core.packages.earPackageType";
	public String getAssociatedModuleType() {
		return "jst.ear";
	}

	public String getId() {
		return ID;
	}

	public String getLabel() {
		return "EAR";
	}

	public IArchive createDefaultConfiguration(IProject project, IProgressMonitor monitor) {
		IArchive topLevel = createGenericIArchive(project, null, project.getName() + ".jar");
		return fillDefaultConfiguration(project, topLevel, monitor);
	}

	public IArchive fillDefaultConfiguration(IProject project, IArchive topLevel, IProgressMonitor monitor) {
		
		return topLevel;
	}

}
