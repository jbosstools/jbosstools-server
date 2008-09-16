/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.archives.webtools.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModel;
import org.jboss.ide.eclipse.archives.core.model.ArchivesModelException;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.util.ModelUtil;
import org.jboss.ide.eclipse.archives.webtools.IntegrationPlugin;
import org.jboss.ide.eclipse.archives.webtools.modules.PackageModuleFactory.IModuleContributor;
import org.jboss.ide.eclipse.archives.webtools.modules.PackageModuleFactory.PackagedModuleDelegate;

/**
 * 
 * @author Rob Stryker rob.stryker@jboss.com
 *
 */
public class ArchivesModelModuleContributor implements IModuleContributor {

	private static ArchivesModelModuleContributor instance;
	public static ArchivesModelModuleContributor getInstance() {
		if( instance == null ) {
			instance = new ArchivesModelModuleContributor(PackageModuleFactory.getFactory());
		}
		return instance;
	}
	
	private PackageModuleFactory factory;
	protected ArrayList<IModule> modules = null;
	protected HashMap<IPath, ArrayList<IModule>> projectToModules = new HashMap<IPath, ArrayList<IModule>>(5); //IPath to IModule
	protected HashMap<IModule, Object> moduleDelegates = new HashMap<IModule, Object>(5);
	protected HashMap<IArchive, IModule> packageToModule = new HashMap<IArchive, IModule>(5);	
	
	private ArchivesModelModuleContributor(PackageModuleFactory factory) {
		this.factory = factory;
	}
	
	public IModule[] getModules() {
		if( modules == null ) {
			modules = new ArrayList<IModule>();
			IProject[] projects2 = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			int size = projects2.length;
			for (int i = 0; i < size; i++) {
				if (projects2[i].isAccessible()) {

					if( !ArchivesModel.instance().isProjectRegistered(projects2[i].getLocation())) {
						if( ArchivesModel.instance().canReregister(projects2[i].getLocation()))
							// registration should also add this to the factory manually, so do not create the module
							ArchivesModel.instance().registerProject(projects2[i].getLocation(), new NullProgressMonitor());
					} else {
						try {
							// project is already registered. create the module
							createModules(projects2[i]);
						} catch(ArchivesModelException ame) {
							IStatus status = new Status(IStatus.ERROR, IntegrationPlugin.PLUGIN_ID, ame.getMessage(), ame);
							IntegrationPlugin.getDefault().getLog().log(status);
						}
					}
				}
			}
		}
		return modules.toArray(new IModule[modules.size()]);
	}
	
	protected void createModules(IProject project) throws ArchivesModelException {
		if( project == null )
			return;
		IArchive[] packages = ModelUtil.getProjectArchives(project.getLocation());
		if( packages != null && packages.length > 0 ) {
			IModule module;
			boolean requiresSave = ensureArchivesHaveIDs(project, packages);
			ArrayList<IModule> mods = new ArrayList<IModule>();
			for( int i = 0; i < packages.length; i++ ) {
				module = factory.createModule2(packages[i], project);
				modules.add(module);
				Object moduleDelegate = new PackagedModuleDelegate(packages[i]);
				packageToModule.put(packages[i], module);
				moduleDelegates.put(module, moduleDelegate);
				mods.add(module);
			}
			projectToModules.put(project.getLocation(), mods);
			if( requiresSave ) {
				try {
					ArchivesModel.instance().save(project.getLocation(), 
							new NullProgressMonitor());
				} catch( ArchivesModelException ame ) {
				}
			}
		}
	}
	
	public boolean containsModule(IModule module) {
		return moduleDelegates.containsKey(module);
	}

	public PackagedModuleDelegate getModuleDelegate(IModule module) {
		return (PackagedModuleDelegate)moduleDelegates.get(module);
	}
	
	public void refreshProject(IPath projectLoc) {
		// prime, make sure all are found
		if( modules == null ) 
			getModules();
		
		// remove old mods
		ArrayList<IModule> mods = projectToModules.get(projectLoc);
		IModule mod;
		PackagedModuleDelegate delegate;
		if (mods != null) {
			for( Iterator<IModule> i = mods.iterator(); i.hasNext();) {
				mod = (IModule)i.next();
				if( modules.contains(mod)) {
					delegate = ((PackagedModuleDelegate)moduleDelegates.get(mod));
					moduleDelegates.remove(mod);
					modules.remove(mod);
					if( delegate != null ) 
						packageToModule.remove(delegate.getPackage());
				}
			}
		}
		try {
			createModules(findProject(projectLoc));
		} catch( ArchivesModelException ame ) {
			IStatus status = new Status(IStatus.ERROR, IntegrationPlugin.PLUGIN_ID, ame.getMessage(), ame);
			IntegrationPlugin.getDefault().getLog().log(status);
		}
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
	 * 
	 * This must set ONLY the timestamp as the property in the file.
	 * @param archives
	 * @return  returns whether a save has occurred
	 */
	protected boolean ensureArchivesHaveIDs(IProject project, IArchive[] archives) {
		boolean requiresSave = false;
		for( int i = 0; i < archives.length; i++ ) {
			if( PackageModuleFactory.getStamp(archives[i]) == null ) {
				requiresSave = true;
				archives[i].setProperty(PackageModuleFactory.MODULE_ID_PROPERTY_KEY, 
										PackageModuleFactory.getStamp(archives[i], true));
			}
		}
		return requiresSave;
	}
}
