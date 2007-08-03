package org.jboss.ide.eclipse.as.core.packages.types;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.archives.core.model.IArchive;

public class EjbArchiveType extends J2EEArchiveType {

	public final static String ID = "org.jboss.ide.eclipse.as.core.packages.ejbPackageType";
	public String getAssociatedModuleType() {
		return "jst.ejb";
	}

	public String getId() {
		return ID;
	}

	public String getLabel() {
		return "EJB JAR";
	}

	public IArchive createDefaultConfiguration(String projectName, IProgressMonitor monitor) {
		IProject project = getProject(projectName);
		IArchive topLevel = createGenericIArchive(project, null, project.getName() + ".jar");
		return fillDefaultConfiguration(project, topLevel, monitor); 
	}

	public IArchive fillDefaultConfiguration(String projectName, IArchive topLevel, IProgressMonitor monitor) {
		return fillDefaultConfiguration(getProject(projectName), topLevel, monitor);
	}
	public IArchive fillDefaultConfiguration(IProject project, IArchive topLevel, IProgressMonitor monitor) {

		IModule mod = getModule(project.getName());
		
		// TODO:  module artifact to adapt a jboss ejb 30 to a module is MIA. CREATE IT
		if( mod == null ) {
			
		} else {
			if( mod.getModuleType().getId().equals("jst.ejb") ) {
				try {
					IJavaProject proj = JavaCore.create(project);
					IPath outputLoc = proj.getOutputLocation();
					addFileset(project, topLevel, outputLoc.toOSString(), null);
				} catch( Exception e ) {
				}
			}
		}
		return topLevel;
	}

}
