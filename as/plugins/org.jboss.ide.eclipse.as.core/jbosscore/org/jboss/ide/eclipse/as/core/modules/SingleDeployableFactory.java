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
package org.jboss.ide.eclipse.as.core.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ModuleFile;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.model.ModuleFactoryDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListenerManager;

/**
 * The factory responsible for turning regular files into modules
 * @author Rob Stryker rob.stryker@jboss.com
 *
 */
public class SingleDeployableFactory extends ModuleFactoryDelegate {
	public static final String FACTORY_ID = "org.jboss.ide.eclipse.as.core.singledeployablefactory";
	private static SingleDeployableFactory factDelegate;
	public static final String MODULE_TYPE = "jboss.singlefile";
	public static final String VERSION = "1.0";
	private static final String PREFERENCE_KEY = "org.jboss.ide.eclipse.as.core.singledeployable.deployableList";
	private static final String DELIM = "\r";

	
	private static ModuleFactory factory;
	public static SingleDeployableFactory getFactory() {
		if( factDelegate == null ) {
			ModuleFactory[] factories = ServerPlugin.getModuleFactories();
			for( int i = 0; i < factories.length; i++ ) {
				if( factories[i].getId().equals(FACTORY_ID)) {
					Object o = factories[i].getDelegate(new NullProgressMonitor());
					if( o instanceof SingleDeployableFactory ) {
						factory = factories[i];
						factDelegate = (SingleDeployableFactory)o;
						return factDelegate;
					}
				}
			}
		}
		return factDelegate;
	}
	
	public static boolean makeDeployable(IResource resource) {
		return makeDeployable(resource.getFullPath());
	}
	
	public static boolean makeDeployable(IPath workspaceRelative) {
		boolean ret = getFactory().addModule(workspaceRelative);
		getFactory().saveDeployableList();
		return ret;
	}

	public static void unmakeDeployable(IResource resource) {
		unmakeDeployable(resource.getFullPath());
	}
	
	public static void unmakeDeployable(IPath workspaceRelative) {
		getFactory().removeModule(workspaceRelative);
		getFactory().saveDeployableList();
	}

	public static IModule findModule(IResource resource) {
		return findModule(resource.getFullPath());
	}
	public static IModule findModule(IPath workspaceRelative) {
		return getFactory().getModule(workspaceRelative);
	}
	
	
	
	private HashMap<IPath, IModule> moduleIdToModule;
	private HashMap<IModule, SingleDeployableModuleDelegate> moduleToDelegate;
	public SingleDeployableFactory() {
	}
	
	public void initialize() {
		moduleIdToModule = new HashMap<IPath, IModule>();
		moduleToDelegate = new HashMap<IModule, SingleDeployableModuleDelegate>();
		registerListener();
		String files = JBossServerCorePlugin.getDefault().getPluginPreferences().getString(PREFERENCE_KEY);
		if( files.equals("")) return;
		String[] files2 = files.split(DELIM);
		for( int i = 0; i < files2.length; i++ ) {
			addModule(new Path(files2[i]));
		}
	}
	
	protected void registerListener() {
		UnitedServerListenerManager.getDefault().addListener(new UnitedServerListener() { 
			public void publishFinished(IServer server, IStatus status) {
				cleanUnusedModules();
			}
		});
	}
	
	protected void cleanUnusedModules() {
		IModule[] mods = getModules();
		IServer[] servers = ServerCore.getServers();
		ArrayList<IModule> usedMods = new ArrayList<IModule>();
		for( int i = 0; i < servers.length; i++ ) {
			IModule[] modsInner = servers[i].getModules();
			for( int j = 0; j < modsInner.length; j++ ) {
				if( modsInner[j].getModuleType().getId().equals(MODULE_TYPE)) {
					if( !usedMods.contains(modsInner[j]))
						usedMods.add(modsInner[j]);
				}
			}
		}
		
		for( int i = 0; i < mods.length; i++ ) {
			if( !usedMods.contains(mods[i])) {
				SingleDeployableModuleDelegate delegate = (SingleDeployableModuleDelegate)
					mods[i].loadAdapter(SingleDeployableModuleDelegate.class, new NullProgressMonitor());
				unmakeDeployable(delegate.getWorkspaceRelativePath());
			} else {
				usedMods.remove(mods[i]);
			}
		}
		
		IModule tmp;
		for( Iterator<IModule> i = usedMods.iterator(); i.hasNext(); ) {
			tmp = i.next();
			makeDeployable(new Path(tmp.getName()));
		}
		
	}
	
	public IModule getModule(IPath path) {
		return moduleIdToModule.get(path);
	}
	public void saveDeployableList() {
		Iterator<IPath> i = moduleIdToModule.keySet().iterator();
		String val = "";
		while(i.hasNext()) {
			val += i.next().toString() + DELIM;
		}
		JBossServerCorePlugin.getDefault().getPluginPreferences().setValue(PREFERENCE_KEY, val);
		JBossServerCorePlugin.getDefault().savePluginPreferences();
	}
	
	protected boolean addModule(IPath path) {
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		if( resource != null && resource.exists() ) {
			IModule module = createModule(path.toString(), path.toString(), MODULE_TYPE, VERSION, resource.getProject());
			moduleIdToModule.put(path, module);
			moduleToDelegate.put(module, new SingleDeployableModuleDelegate(path));
			return true;
		} else {
			return false;
		}
	}
	
	protected void removeModule(IPath path) {
		IModule mod = moduleIdToModule.get(path);
		moduleIdToModule.remove(path);
		moduleToDelegate.remove(mod);
	}

	public IModule[] getModules() {
		Collection<IModule> c = moduleIdToModule.values();
		return c.toArray(new IModule[c.size()]);
	}

	public ModuleDelegate getModuleDelegate(IModule module) {
		return moduleToDelegate.get(module);
	}

	public class SingleDeployableModuleDelegate extends ModuleDelegate {
		private IPath global;
		private IPath workspaceRelative;
		public SingleDeployableModuleDelegate(IPath workspaceRelative) {
			this.workspaceRelative = workspaceRelative;
			global = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(workspaceRelative);
		}
		public IModule[] getChildModules() {
			return new IModule[0];
		}

		public IModuleResource[] members() throws CoreException {
			return new IModuleResource[] { 
					new ModuleFile(global.lastSegment(), 
					new Path(global.lastSegment()), 
					global.toFile().lastModified()) };
		}

		public IStatus validate() {
			return Status.OK_STATUS;
		}
		
		public IPath getGlobalSourcePath() {
			return this.global;
		}
		
		public IPath getWorkspaceRelativePath() {
			return workspaceRelative;
		}
	}
}
