package org.jboss.ide.eclipse.as.core.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.as.core.modules.PackageModuleFactory.IModuleContributor;
import org.jboss.ide.eclipse.as.core.modules.PackageModuleFactory.PackagedModuleDelegate;

public class ArchivesModelModuleContributor implements IModuleContributor {

	private static ArchivesModelModuleContributor instance;
	public static ArchivesModelModuleContributor getInstance() {
		if( instance == null ) {
			instance = new ArchivesModelModuleContributor(PackageModuleFactory.getFactory());
		}
		return instance;
	}
	
	private PackageModuleFactory factory;
	protected ArrayList modules = null;
	protected HashMap projectToModules = new HashMap(5); //IPath to IModule
	protected HashMap moduleDelegates = new HashMap(5);
	protected HashMap packageToModule = new HashMap(5);	
	
	private ArchivesModelModuleContributor(PackageModuleFactory factory) {
		this.factory = factory;
	}
	
	public IModule[] getModules() {
		if( modules == null ) {
			modules = new ArrayList();
			IProject[] projects2 = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			int size = projects2.length;
			for (int i = 0; i < size; i++) {
				if (projects2[i].isAccessible()) {
					createModules(projects2[i]);
				}
			}
		}
		return (IModule[]) modules.toArray(new IModule[modules.size()]);
	}
	
	protected void createModules(IProject project) {
		IArchive[] packs = ArchivesModelCore.getProjectPackages(project.getLocation(), null, false);
		if( packs != null && packs.length > 0 ) {
			IModule module;
			IArchive[] packages = ArchivesModelCore.getProjectPackages(project.getLocation(), new NullProgressMonitor(), true);
			boolean requiresSave = ensureArchivesHaveIDs(project, packages);
			ArrayList mods = new ArrayList();
			for( int i = 0; i < packages.length; i++ ) {
				module = factory.createModule2(packages[i], project);
				modules.add(module);
				Object moduleDelegate = new PackagedModuleDelegate(packages[i]);
				packageToModule.put(packages[i], module);
				moduleDelegates.put(module, moduleDelegate);
				mods.add(module);
			}
			projectToModules.put(project.getLocation(), mods);
		}
	}
	
	public boolean containsModule(IModule module) {
		return moduleDelegates.containsKey(module);
	}

	public PackagedModuleDelegate getModuleDelegate(IModule module) {
		return (PackagedModuleDelegate)moduleDelegates.get(module);
	}
	
	public void refreshProject(IPath projectLoc) {
		// remove old mods
		ArrayList mods = (ArrayList)projectToModules.get(projectLoc);
		IModule mod;
		IArchive arc;
		if (mods != null) {
			for( Iterator i = mods.iterator(); i.hasNext();) {
				mod = (IModule)i.next();
				if( moduleDelegates.get(mod) != null ) {
					arc = ((PackagedModuleDelegate)moduleDelegates.get(mod)).getPackage();
					packageToModule.remove(arc);
					moduleDelegates.remove(mod);
				}
			}
		}
		createModules(findProject(projectLoc));
		factory.clearModuleCache();
	}

	protected IProject findProject(IPath projectLoc) {
		IProject proj = null;
		IProject[] projects2 = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		int size = projects2.length;
		for (int i = 0; i < size; i++) {
			if( projects2[i].getLocation().equals(projectLoc))
				proj = projects2[i];
		}
		return proj;
	}
	
	/**
	 * Set a property so that each module that's here in the factory
	 * has a unique ID other than it's name (which is not unique)
	 * @param archives
	 * @return  returns whether a save has occurred
	 */
	protected boolean ensureArchivesHaveIDs(IProject project, IArchive[] archives) {
		boolean requiresSave = false;
		for( int i = 0; i < archives.length; i++ ) {
			if( PackageModuleFactory.getID(archives[i]) == null ) {
				requiresSave = true;
				archives[i].setProperty(PackageModuleFactory.MODULE_ID_PROPERTY_KEY, 
										PackageModuleFactory.getID(archives[i], true));
			}
		}
		if( requiresSave ) {
			// save
			ArchivesModel.instance().saveModel(project.getLocation(), new NullProgressMonitor());
		}
		return requiresSave;
	}
}
