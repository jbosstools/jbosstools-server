package org.jboss.ide.eclipse.as.wtp.override.core.modules;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.util.ModuleFile;
import org.eclipse.wst.server.core.util.ModuleFolder;
import org.eclipse.wst.server.core.util.ProjectModule;

public class JBTProjectModuleDelegate extends ProjectModule {
	public JBTProjectModuleDelegate() {
		super();
	}
	public JBTProjectModuleDelegate(IProject project) {
		super(project);
	}
	
	@Override
	public IModule[] getChildModules() {
		return null;
	}

	public IModuleResource[] members() throws CoreException {
		IProject project = getProject();
		final IVirtualComponent c = ComponentCore.createComponent(project);
		IVirtualFolder vf = c.getRootFolder();
		IModuleResource[] children = getResources(vf);
		return children;
	}

	protected IModuleResource[] getResources(IVirtualFolder parent) {
		ArrayList<IModuleResource> members = new ArrayList<IModuleResource>();
		IVirtualResource[] resources = new IVirtualResource[]{};
		try {
			resources = parent.members();
		} catch( CoreException ce ) {}
		for( int i = 0; i < resources.length; i++ ) {
			if( resources[i].getType() == IVirtualResource.FILE) {
				IFile f = (IFile)resources[i].getUnderlyingResource();
				ModuleFile mf = new ModuleFile(f, f.getName(), resources[i].getRuntimePath().removeLastSegments(1));
				members.add(mf);
			} else if( resources[i].getType() == IVirtualResource.FOLDER){
				IFolder folder = (IFolder)resources[i].getUnderlyingResource();
				ModuleFolder mf = new ModuleFolder(folder, folder.getName(), resources[i].getRuntimePath().removeLastSegments(1));
				IModuleResource[] children = getResources((IVirtualFolder)resources[i]);
				mf.setMembers(children);
				members.add(mf);
			}
		}
		return (IModuleResource[]) members
				.toArray(new IModuleResource[members.size()]);
	}
}
