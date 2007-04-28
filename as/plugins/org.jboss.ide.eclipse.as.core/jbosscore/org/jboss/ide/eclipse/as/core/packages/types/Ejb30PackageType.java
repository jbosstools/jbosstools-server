package org.jboss.ide.eclipse.as.core.packages.types;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.as.core.packages.types.J2EEArchiveType;

public class Ejb30PackageType extends J2EEArchiveType {

	public static final String ID = "org.jboss.ide.eclipse.ejb3.wizards.core.ejbPackageType";
	public String getAssociatedModuleType() {
		return "jbide.ejb30";
	}

	public String getId() {
		return ID;
	}
	
	public String getLabel() {
		return "EJB 3.0 JAR";
	}
	
	public IArchive createDefaultConfiguration(IProject project, IProgressMonitor monitor) {
		boolean facetFound = J2EEProjectUtilities.isProjectOfType(project, getAssociatedModuleType()); 
		return createDefaultConfiguration(project, facetFound, monitor);
	}
	
	public IArchive createDefaultConfiguration(IProject project, boolean facetFound, IProgressMonitor monitor) {
		String metaInfDir = (facetFound ? EJBMODULE + Path.SEPARATOR : "") + METAINF;
		
		IArchive topLevel = createGenericIArchive(project, null, project.getName() + ".jar");
		topLevel.setDestinationPath(project.getLocation());
		IArchiveFolder metainf = addFolder(project, topLevel, METAINF);
		IArchiveFolder lib = addFolder(project, metainf, LIB);
		addFileset(project, metainf, metaInfDir, null);
		return topLevel;		
	}
}
