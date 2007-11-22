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
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ModuleFile;
import org.eclipse.wst.server.core.internal.PublishServerJob;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.model.ModuleFactoryDelegate;
import org.jboss.ide.eclipse.archives.core.ArchivesCore;
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
	private IResourceChangeListener resourceListener;
	public SingleDeployableFactory() {
		resourceListener = new FileDeletionListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_DELETE);
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
			String projectName = workspaceRelative.segment(0);
			//global = ArchivesCore.getInstance().getVariables().getProjectPath(projectName);
			global = ResourcesPlugin.getWorkspace().getRoot().findMember(workspaceRelative).getLocation();
			global = ArchivesCore.getInstance().getVariables().getProjectPath(projectName).append(workspaceRelative.removeFirstSegments(1));
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
	
	public class FileDeletionListener implements IResourceChangeListener, IResourceDeltaVisitor {
		public ArrayList<IPath> list = new ArrayList<IPath>();
		public void resourceChanged(IResourceChangeEvent event) {
			try {
				if(event.getDelta()!=null) {
					event.getDelta().accept(this);
					ArrayList<IPath> clone;
					if( list.size() > 0 ) {
						synchronized(this) {
							clone = new ArrayList<IPath>();
							clone.addAll(list);
							list.clear();
						}
	
						UndeployFromServerJob job = new UndeployFromServerJob(clone);
						job.schedule();
					}
				}
			} catch( CoreException ce ) {
			}
		}

		public boolean visit(IResourceDelta delta) throws CoreException {
			if( ((delta.getKind() & IResourceDelta.REMOVED) != 0) && delta.getResource() != null && delta.getResource().getFullPath() != null ) {
				IModule module = getModule(delta.getResource().getFullPath());
				if( getModule(delta.getResource().getFullPath()) != null && !list.contains(delta.getResource().getFullPath())) {
					list.add(delta.getResource().getFullPath());
				}
			}
			return true;
		}
	}
	
	public class UndeployFromServerJob extends Job {
		private ArrayList<IPath> paths;
		public UndeployFromServerJob(ArrayList<IPath> paths) {
			super("Undeploy Single Files From Server");
			this.paths = paths;
		}

		protected IStatus run(IProgressMonitor monitor) {
			IPath next;
			IModule mod;
			IServer[] allServers = ServerCore.getServers();
			for( Iterator i = paths.iterator(); i.hasNext(); ) {
				next = (IPath)i.next();
				mod = getModule(next);
				if( mod != null ) {
					for( int j = 0; j < allServers.length; j++ ) {
						List l = Arrays.asList(allServers[j].getModules());
						if( l.contains(mod)) {
							try {
								IServerWorkingCopy copy = allServers[j].createWorkingCopy();
								ServerUtil.modifyModules(copy, new IModule[] {}, new IModule[]{mod}, new NullProgressMonitor());
								IServer s = copy.save(false, new NullProgressMonitor());
								new PublishServerJob(s).schedule();
							} catch( CoreException ce ) {
							}
						}
					}
				}
			}
			return Status.OK_STATUS;
		}
		
	}

}
