package org.jboss.ide.eclipse.as.core.module;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.ModuleArtifactAdapterDelegate;

public class PackagedArtifactAdapter extends ModuleArtifactAdapterDelegate {

	public PackagedArtifactAdapter() {
	}

	private PackageModuleFactory factory;
	public IModuleArtifact getModuleArtifact(Object obj) {
		if( obj instanceof IJavaProject ) {
			IProject[] projects2 = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			boolean done = false;
			String jpName = ((IJavaProject)obj).getElementName();
			for( int i = 0; i < projects2.length && !done; i++ ) {
				if( projects2[i].getName().equals(jpName)) {
					done = true;
					obj = projects2[i];
				}
			}
		} 
		if( obj instanceof IProject ) {
			PackageModuleFactory factory = getFactory();
			if( factory != null ) {
				IModule mod = factory.getModuleFromProject((IProject)obj);
				if( mod != null ) {
					return getArtifact(mod);
				}
			}
		}
		return null;
	}
	
	protected PackageModuleFactory getFactory() {
		if( factory != null ) return factory;
		
		ModuleFactory[] factories = ServerPlugin.getModuleFactories();
		System.out.println(PackageModuleFactory.FACTORY_TYPE_ID);
		for( int i = 0; i < factories.length; i++ ) {
			System.out.println("  " + factories[i].getId());
			if( factories[i].getId().equals(PackageModuleFactory.FACTORY_TYPE_ID)) {
				Object o = factories[i].getDelegate(new NullProgressMonitor());
				if( o instanceof PackageModuleFactory ) {
					factory = (PackageModuleFactory)o;
					return factory;
				}
			}
		}
		return null;
	}
	
	protected IModuleArtifact getArtifact(IModule mod) {
		return new PackagedArtifact(mod);
	}
	
	public class PackagedArtifact implements IModuleArtifact{
		protected IModule mod;
		public PackagedArtifact(IModule module) {
			this.mod = module;
		}
		public IModule getModule() {
			return mod;
		}
	}
}
