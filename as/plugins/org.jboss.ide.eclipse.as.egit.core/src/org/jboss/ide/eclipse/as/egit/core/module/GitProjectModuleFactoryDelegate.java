package org.jboss.ide.eclipse.as.egit.core.module;

import org.eclipse.core.resources.IProject;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;

public class GitProjectModuleFactoryDelegate extends ProjectModuleFactoryDelegate {

	public GitProjectModuleFactoryDelegate() {
		// TODO Auto-generated constructor stub
		System.out.println("blah");
	}

	protected IModule createModule(IProject project) {
		RepositoryMapping mapping = RepositoryMapping.getMapping(project);
		System.out.println(mapping);
		return null;
	}

	@Override
	public ModuleDelegate getModuleDelegate(IModule module) {
		// TODO Auto-generated method stub
		return null;
	}

}
