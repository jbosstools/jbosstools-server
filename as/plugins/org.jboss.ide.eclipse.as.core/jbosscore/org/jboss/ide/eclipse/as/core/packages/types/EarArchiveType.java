package org.jboss.ide.eclipse.as.core.packages.types;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.server.core.IEnterpriseApplication;
import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveFolder;
import org.jboss.ide.eclipse.archives.core.model.IArchiveType;
import org.jboss.ide.eclipse.as.core.packages.ModulePackageTypeConverter;

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
		IModule mod = getModule(project);
		if( mod != null ) 
			return createDefaultConfigFromModule(mod, monitor);
		else
			return createDefaultConfiguration2(project, monitor);
	}
	
	public IArchive createDefaultConfiguration2(IProject project,
			IProgressMonitor monitor) {
		IArchive topLevel = createGenericIArchive(project, null, project.getName() + ".ear");
		topLevel.setDestinationPath(project.getLocation());
		topLevel.setInWorkspace(true);
		
		fillDefaultConfiguration(project, topLevel, monitor);
		return topLevel;
	}

	public IArchive createDefaultConfigFromModule(IModule module,
			IProgressMonitor monitor) {
		IProject project = module.getProject();
		IContainer sourceContainer = project.getFolder(EARCONTENT);
		IArchive topLevel = createGenericIArchive(project, null, project.getName() + ".ear", sourceContainer);
		topLevel.setDestinationPath(project.getLocation());
		topLevel.setInWorkspace(true);
		
		fillDefaultConfiguration(project, topLevel, monitor);
		return topLevel;
	}

	public IArchive fillDefaultConfiguration(IProject project, IArchive topLevel, IProgressMonitor monitor) {
		IModule mod = getModule(project);

		if( mod == null ) {
			// add fileset
			IArchiveFolder metainf = addFolder(project, topLevel, METAINF);
			addFileset(project, metainf, new Path(project.getName()).append(METAINF).toOSString(), null);
			
		} else {
			// now add children
			addFileset(project, topLevel, new Path(project.getName()).append(EARCONTENT).toOSString(), "**/*.*");
			IEnterpriseApplication earModule = (IEnterpriseApplication)mod.loadAdapter(IEnterpriseApplication.class, monitor);
			IModule[] childModules = earModule.getModules();
			for( int i = 0; i < childModules.length; i++ ) {
				IModule child = childModules[i];
				IArchiveType type = ModulePackageTypeConverter.getPackageTypeFor(child);
				IArchive childPack;
				if( type == null ) {
					childPack = createGenericIArchive(child.getProject(), null, child.getProject().getName() + ".jar");
				} else {
					childPack = type.createDefaultConfiguration(child.getProject(), new NullProgressMonitor());
				}
				topLevel.addChild(childPack);
			}
		}

		return topLevel;
	}

}
