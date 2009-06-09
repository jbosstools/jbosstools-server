package org.jboss.ide.eclipse.as.wtp.override.ui.propertypage;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public class AddModulesToEarPropertiesPage extends
		AddModuleDependenciesPropertiesPage {


	public AddModulesToEarPropertiesPage(IProject project,
			J2EEDependenciesPage page) {
		super(project, page);
	}

	protected boolean performOk(IProgressMonitor monitor) {
		return false;
	}
	
}
