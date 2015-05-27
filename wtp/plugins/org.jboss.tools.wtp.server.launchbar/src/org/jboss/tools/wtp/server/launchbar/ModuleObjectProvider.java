/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.wtp.server.launchbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.ILaunchObjectProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.jboss.tools.wtp.server.launchbar.objects.LaunchedArtifacts;
import org.jboss.tools.wtp.server.launchbar.objects.ModuleArtifactDetailsWrapper;
import org.jboss.tools.wtp.server.launchbar.objects.ModuleArtifactWrapper;
import org.jboss.tools.wtp.server.launchbar.objects.ModuleWrapper;

/**
 * This class provides launchable objects to the framework. 
 * It currently returns three types of objects:
 *    1) Existing modules
 *    2) Module artifact details as pulled from existing launch configs
 *    3) Module artifacts based on the current workspace selection
 */
public class ModuleObjectProvider implements ILaunchObjectProvider, 
	IResourceChangeListener, ILaunchConfigurationListener, ISelectionListener {
	
	private ILaunchBarManager manager;
	private HashMap<IProject, ModuleWrapper[]> knownModules;
	
	/**
	 * Used to store the most recent artifact wrapper corresponding
	 * to the most recent selection from the selection service
	 */
	private ModuleArtifactWrapper mostRecent;
	
	

	
	@Override
	public void init(ILaunchBarManager manager) throws CoreException {
		this.manager = manager;
		
		// Initialize the modulewrapper objects (wrappers with no artifacts)
		knownModules = new HashMap<IProject, ModuleWrapper[]>();
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			resourceChanged(project, null);
		}
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
		
		
		ILaunchConfiguration[] all = getLaunchManager().getLaunchConfigurations();
		for( int i = 0; i < all.length; i++ ) {
			if( all[i].getType().getIdentifier().equals(WTP_LAUNCH_TYPE)) {
				launchConfigurationAdded(all[i]);
			}
		}
		// initialize the artifact adapter launches
		getLaunchManager().addLaunchConfigurationListener(this);
		
		
		delayedAddWorkbenchListener(this);
	}
	
	private void delayedAddWorkbenchListener(final ModuleObjectProvider provider) {
		new Job("Adding Workbench Selection Listener for server artifacts"){
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				long timeLimit = 60*5*1000;
				long current = System.currentTimeMillis();
				long max = current + timeLimit;
				IWorkbenchWindow window = syncGetActiveWorkbenchWindow();
				while( window == null && System.currentTimeMillis() < max) {
					try {
						System.out.println("*** sleeping 500");
						Thread.sleep(500);
					} catch(InterruptedException ie) {
						ie.printStackTrace();
					}
					window = syncGetActiveWorkbenchWindow();
				}
				System.out.println("Loop over:  window=" + window);
				if( window != null ) {
					window.getSelectionService().addPostSelectionListener(provider);
					return Status.OK_STATUS;
				}
				return Status.CANCEL_STATUS; // TODO error status?
			}}.schedule();
	}
	
	private IWorkbenchWindow syncGetActiveWorkbenchWindow() {
		final IWorkbenchWindow[] window = new IWorkbenchWindow[1];
		Display.getDefault().syncExec(
				new Runnable(){
					public void run() {
						window[0] = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					}
				});
		return window[0];
	}
	
	// To allow override by tests
	ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {
				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					IResource res = delta.getResource();
					if (res instanceof IProject) {
						return resourceChanged((IProject)res, delta);
					} else if (res instanceof IFile || res instanceof IFolder) {
						return false;
					}
					return true;
				}
			});
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
	}
	
	private ModuleWrapper[] convert(IModule[] modules) {
		ArrayList<ModuleWrapper> list = new ArrayList<ModuleWrapper>();
		for( int i = 0; i < modules.length; i++ ) {
			list.add(new ModuleWrapper(modules[i]));
		}
		return (ModuleWrapper[]) list.toArray(new ModuleWrapper[list.size()]);
	}
	
	private boolean resourceChanged(IProject project, IResourceDelta delta) throws CoreException {
		if( project == null )
			return false;
		
		IModule[] modules = ServerUtil.getModules((project));
		int kind = delta == null ? IResourceDelta.ADDED : delta.getKind();
		ModuleWrapper[] known = knownModules.get(project);
		if ((kind & IResourceDelta.ADDED) != 0) {
			if( known != null ) {
				alertRemoved(known);
			}
			ModuleWrapper[] newModules = convert(modules);
			alertAdded(newModules);
			knownModules.put(project, newModules);
		} else if ((kind & IResourceDelta.REMOVED) != 0) {
			if( known != null ) {
				alertRemoved(known);
			}
			knownModules.remove(project);
		} else if ((kind & IResourceDelta.CHANGED) != 0) {
			ModuleWrapper[] newModules = convert(modules);
			handleChanged(known, newModules);
		}
		return false;
	}
	
	private void alertRemoved(ModuleWrapper[] all) throws CoreException {
		for( int i = 0; i < all.length; i++ ) {
			manager.launchObjectRemoved(all[i]);
		}
	}
	private void alertAdded(ModuleWrapper[] all) throws CoreException {
		for( int i = 0; i < all.length; i++ ) {
			manager.launchObjectAdded(all[i]);
		}
	}
	private void alertChanged(ModuleWrapper[] all) throws CoreException {
		for( int i = 0; i < all.length; i++ ) {
			manager.launchObjectChanged(all[i]);
		}
	}
	private void handleChanged(ModuleWrapper[] old, ModuleWrapper[] nnew) throws CoreException {
		if( old == null && nnew != null ) {
			// All are added
			alertAdded(nnew);
		} else if( nnew == null && old != null ) {
			// All are removed
			alertRemoved(old);
		} else {
			ModuleWrapper[] missing = findMissing(old, nnew);
			ModuleWrapper[] added = findMissing(nnew, old);
			ArrayList<ModuleWrapper> changed = new ArrayList<ModuleWrapper>(Arrays.asList(nnew));
			changed.removeAll(Arrays.asList(added));
			changed.removeAll(Arrays.asList(missing));
			ModuleWrapper[] changedArray = (ModuleWrapper[]) changed.toArray(new ModuleWrapper[changed.size()]);
			alertAdded(added);
			alertRemoved(missing);
			alertChanged(changedArray);
		}
	}
	
	private ModuleWrapper[] findMissing(ModuleWrapper[] old, ModuleWrapper[] nnew) {
		ArrayList<ModuleWrapper> missing = new ArrayList<ModuleWrapper>();
		for( int i = 0; i < old.length; i++ ) {
			if( !isPresent(old[i], nnew)) {
				missing.add(old[i]);
			}
		}
		return (ModuleWrapper[]) missing.toArray(new ModuleWrapper[missing.size()]);
	}
	
	private boolean isPresent(ModuleWrapper needle, ModuleWrapper[] haystack) {
		for( int i = 0; i < haystack.length; i++ ) {
			if( needle.getModule().getId().equals(haystack[i].getModule().getId())) {
				return true;
			}
		}
		return false;
	}

	
	
	private static String WTP_LAUNCH_TYPE = "org.eclipse.wst.server.ui.launchConfigurationType";
	
	@Override
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		try {
			String typeId = configuration.getType().getIdentifier();
			if(WTP_LAUNCH_TYPE.equals(typeId)) {
				ModuleArtifactDetailsWrapper wrap = getArtifactWrapperFor(configuration); 
				manager.launchObjectAdded(wrap);
			}
		} catch(CoreException ce) {
			ce.printStackTrace();
		}
		
	}
	
	private ModuleArtifactDetailsWrapper getArtifactWrapperFor(ILaunchConfiguration configuration) {
		String ATTR_SERVER_ID = "server-id";
		String ATTR_MODULE_ARTIFACT = "module-artifact";
		String ATTR_MODULE_ARTIFACT_CLASS = "module-artifact-class";

		String ATTR_LAUNCHABLE_ADAPTER_ID = "launchable-adapter-id";
		String ATTR_CLIENT_ID = "client-id";
		
		try {
			String artifact = configuration.getAttribute(ATTR_MODULE_ARTIFACT, (String)null);
			String clazz = configuration.getAttribute(ATTR_MODULE_ARTIFACT_CLASS, (String)null);
			if( artifact != null && clazz != null ) {
				ModuleArtifactDetailsWrapper wrapper = new ModuleArtifactDetailsWrapper(configuration.getName(), artifact, clazz);
				return wrapper;
			}
		} catch(CoreException ce) {
			ce.printStackTrace();
		}
		return null;
	}

	@Override
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		// Can do nothing. Can't even get the type or any attributes
	}

	@Override
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		// Cannot check type
//		try {
//			String typeId = configuration.getType().getIdentifier();
//			if("org.eclipse.wst.server.ui.launchConfigurationType".equals(typeId)) {
//				ModuleArtifactDetailsWrapper w = getArtifactWrapperFor(configuration);
//				if( w != null )
//					manager.launchObjectRemoved(w);
//			}
//		} catch(CoreException ce) {
//			ce.printStackTrace();
//		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if( selection instanceof IStructuredSelection ) {
			Object o = ((IStructuredSelection)selection).getFirstElement();
			final IModuleArtifact[] moduleArtifacts = ServerPlugin.getModuleArtifacts(o);
			if( moduleArtifacts != null && moduleArtifacts.length > 0 ) {
				if( mostRecent != null ) {
					try {
						if( !LaunchedArtifacts.getDefault().hasBeenLaunched(mostRecent))
							manager.launchObjectRemoved(mostRecent);
					} catch(CoreException ce) {
						// TODO log
					}
				}
				mostRecent = new ModuleArtifactWrapper(moduleArtifacts[0]);
				try {
					manager.launchObjectAdded(mostRecent);
				} catch(CoreException ce) {
					// TODO log
				}
			}
		}
	}
}
