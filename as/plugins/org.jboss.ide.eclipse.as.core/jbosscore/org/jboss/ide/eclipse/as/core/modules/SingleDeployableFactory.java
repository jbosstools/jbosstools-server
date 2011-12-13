/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 

package org.jboss.ide.eclipse.as.core.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.PublishServerJob;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.model.ModuleFactoryDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListenerManager;
import org.jboss.ide.eclipse.as.wtp.core.modules.IJBTModule;
import org.osgi.service.prefs.BackingStoreException;

/**
 * The factory responsible for turning regular files into modules
 * @author Rob Stryker rob.stryker@jboss.com
 *
 */
public class SingleDeployableFactory extends ModuleFactoryDelegate {
	public static final String FACTORY_ID = "org.jboss.ide.eclipse.as.core.singledeployablefactory"; //$NON-NLS-1$
	private static SingleDeployableFactory factDelegate;
	public static final String MODULE_TYPE = "jboss.singlefile"; //$NON-NLS-1$
	public static final String VERSION = "1.0"; //$NON-NLS-1$
	private static final String PREFERENCE_KEY = "org.jboss.ide.eclipse.as.core.singledeployable.deployableList"; //$NON-NLS-1$
	private static final String STORED_IN_PROJECTS_PREF_KEY = "org.jboss.ide.eclipse.as.core.singledeployable.storedInProjectKey"; //$NON-NLS-1$
	private static final String DELIM = "\r"; //$NON-NLS-1$

	
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
	
	/**
	 * @since 2.3
	 */
	public static boolean makeDeployable(IProject project, IPath[] workspaceRelative) {
		boolean ret = true;
		for( int i = 0; i < workspaceRelative.length; i++ ) {
			ret &= getFactory().addModule(workspaceRelative[i]);
		}
		getFactory().saveDeployableList(project.getName());
		return ret;
	}
	public static boolean makeDeployable(IPath workspaceRelative) {
		boolean ret = getFactory().addModule(workspaceRelative);
		getFactory().saveDeployableList(workspaceRelative.segment(0));
		return ret;
	}

	public static void unmakeDeployable(IResource resource) {
		unmakeDeployable(resource.getFullPath());
	}
	
	public static void unmakeDeployable(IPath workspaceRelative) {
		getFactory().removeModule(workspaceRelative);
		getFactory().saveDeployableList(workspaceRelative.segment(0));
	}

	public static void unmakeDeployable(IProject project, IPath[] workspaceRelative) {
		for( int i = 0; i < workspaceRelative.length; i++ ) {
			getFactory().removeModule(workspaceRelative[i]);
		}
		getFactory().saveDeployableList(project.getName());
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
		String storeInProjects = JBossServerCorePlugin.getDefault().getPluginPreferences().getString(STORED_IN_PROJECTS_PREF_KEY);
		if( storeInProjects != null ) {
			projectLoad();
		} else {
			legacyLoad();
		}
	}
	
	protected void projectLoad() {
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		String qualifier = JBossServerCorePlugin.getDefault().getDescriptor().getUniqueIdentifier();
		for( int i = 0; i < allProjects.length; i++) {
			IScopeContext context = new ProjectScope(allProjects[i]);
			IEclipsePreferences node = context.getNode(qualifier);
			String val = node.get(PREFERENCE_KEY, ""); //$NON-NLS-1$
			String[] paths = val.split("\n"); //$NON-NLS-1$
			IPath tmp;
			for( int j = 0; j < paths.length; j++ ) {
				if( !paths[j].trim().equals("")) { //$NON-NLS-1$
					tmp = new Path(allProjects[i].getName()).append(paths[j]);
					addModule(tmp);
				}
			}
		}
	}
	
	protected void legacyLoad() {
		String files = JBossServerCorePlugin.getDefault().getPluginPreferences().getString(PREFERENCE_KEY);
		if( files.equals("")) return; //$NON-NLS-1$
		String[] files2 = files.split(DELIM);
		for( int i = 0; i < files2.length; i++ ) {
			addModule(new Path(files2[i]));
		}
	}

	public void saveDeployableList(String projectName) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		String list = ""; //$NON-NLS-1$
		Set<IPath> allPaths = moduleIdToModule.keySet();
		Iterator<IPath> j = allPaths.iterator();
		IPath tmp;
		while(j.hasNext()) {
			tmp = j.next();
			list += tmp.removeFirstSegments(1).makeRelative() + "\n"; //$NON-NLS-1$
		}
		
		String qualifier = JBossServerCorePlugin.getDefault().getDescriptor().getUniqueIdentifier();
		if( project.exists() && project.isAccessible()) {
			IScopeContext context = new ProjectScope(project);
			IEclipsePreferences node = context.getNode(qualifier);
			if (node != null)
				node.put(PREFERENCE_KEY, list);
			try {
				node.flush();
			} catch (BackingStoreException e) {
				// TODO Log
			}
		}
	}

	/* This is not called but keeping it around for now just in case */
	public void legacySaveDeployableList() {
		Iterator<IPath> i = moduleIdToModule.keySet().iterator();
		String val = ""; //$NON-NLS-1$
		while(i.hasNext()) {
			val += i.next().toString() + DELIM;
		}
		JBossServerCorePlugin.getDefault().getPluginPreferences().setValue(PREFERENCE_KEY, val);
		JBossServerCorePlugin.getDefault().savePluginPreferences();
	}

	
	private SingleDeployableWorkspaceListener workspaceChangeListener;
	protected void registerListener() {
		UnitedServerListenerManager.getDefault().addListener(new UnitedServerListener() { 
			public void publishFinished(IServer server, IStatus status) {
				cleanUnusedModules();
			}
		});
		workspaceChangeListener = new SingleDeployableWorkspaceListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(workspaceChangeListener);
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
				if( !delegate.getGlobalSourcePath().toFile().exists())
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
	
	@Override
	public IModule findModule(String id) {
		IModule s = super.findModule(id);
		if( s == null && new Path(id).isAbsolute()) 
			s = super.findModule(id.substring(1));
		return s;
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

	public class SingleDeployableModuleDelegate extends ModuleDelegate implements IJBTModule {
		private IPath global;
		private IPath workspaceRelative;
		private IResource resource;
		public SingleDeployableModuleDelegate(IPath workspaceRelative) {
			this.workspaceRelative = workspaceRelative;
			resource = ResourcesPlugin.getWorkspace().getRoot().findMember(workspaceRelative);
			global = resource.getLocation();
		}
		public IModule[] getChildModules() {
			return new IModule[0];
		}

		public IModuleResource[] members() throws CoreException {
			if( isBinary() && resource instanceof IFile) {
				IModuleResource resource2 = ResourceModuleResourceUtil.createResource(resource);
				return new IModuleResource[]{resource2};
			}
			/*
			 * Single deployable modules that are folders are not treated as
			 * binary modules, but rather regular modules with no extension. 
			 * This means all module resource paths must be relative to the root folder,
			 * and so we ensure that here. 
			 */
			if( !isBinary() && resource instanceof IContainer) {
				IModuleResource[] resource2 = ResourceModuleResourceUtil.createChildrenResources(
						(IContainer)resource, new Path("/")); //$NON-NLS-1$
				return resource2;
			}
			return new IModuleResource[]{};
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
		public IModule[] getModules() {
			return new IModule[0]; // no children
		}
		public String getURI(IModule module) {
			return null; // never called
		}
		
		// folders are not binary
		public boolean isBinary() {
			boolean b = global.toFile().exists() && global.toFile().isFile();
			return b;
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
	
	public static class UndeployFromServerJob extends Job {
		private ArrayList<IPath> paths;
		private boolean removeFromFactory;
		public UndeployFromServerJob(ArrayList<IPath> paths) {
			this(paths,true);
		}
		public UndeployFromServerJob(ArrayList<IPath> paths, boolean removeFromFactory) {
			super(Messages.UndeploySingleFilesJob);
			this.paths = paths;
			this.removeFromFactory = removeFromFactory;
		}

		protected IStatus run(IProgressMonitor monitor) {
			IPath next;
			IModule mod;
			IServer[] allServers = ServerCore.getServers();
			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IStatus.ERROR, 
					Messages.SingleFileUndeployFailed, null);
			for( Iterator<IPath> i = paths.iterator(); i.hasNext(); ) {
				next = i.next();
				mod = getFactory().getModule(next);
				if( mod != null ) {
					boolean removedFromAllServers = true;
					for( int j = 0; j < allServers.length; j++ ) {
						List l = Arrays.asList(allServers[j].getModules());
						if( l.contains(mod)) {
							try {
								IServerWorkingCopy copy = allServers[j].createWorkingCopy();
								ServerUtil.modifyModules(copy, new IModule[] {}, new IModule[]{mod}, new NullProgressMonitor());
								IServer s = copy.save(false, new NullProgressMonitor());
								new PublishServerJob(s).schedule();
							} catch( CoreException ce ) {
								removedFromAllServers = false;
								IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
										NLS.bind(Messages.SingleFileUndeployFailed2, next, allServers[j].getName()),
										ce);
								ms.add(s);
							}
						}
					}
					if( removeFromFactory && removedFromAllServers )
						SingleDeployableFactory.unmakeDeployable(next);

				}
			}
			return ms.getChildren().length == 0 ? Status.OK_STATUS : ms;
		}
		
	}

}
