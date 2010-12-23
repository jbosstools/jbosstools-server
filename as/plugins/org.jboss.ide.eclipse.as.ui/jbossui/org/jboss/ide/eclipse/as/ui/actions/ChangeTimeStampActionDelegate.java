/*******************************************************************************
 * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.actions;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.ServerType;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;

public class ChangeTimeStampActionDelegate implements IWorkbenchWindowActionDelegate {

	protected IWorkbenchWindow window;
	String tooltip = null;
	IProject project = null;
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if(project == null && action.isEnabled()) action.setEnabled(false);
		if(tooltip != null && (selection instanceof IStructuredSelection)) {
			Object o = ((IStructuredSelection)selection).getFirstElement();
			IProject p = getProject(o);
			if(p != null) {
				project = p;
				action.setEnabled(computeEnabled());
				action.setToolTipText(NLS.bind(ServerActionMessages.CHANGE_TIME_STAMP,project.getName()));
				return;
			}
		}
		tooltip = ServerActionMessages.CHANGE_TIME_STAMP_DEFAULT; 
		action.setToolTipText(tooltip);
	}

	IProject getProject(Object selection) {
		if(selection instanceof IResource) {
			return ((IResource)selection).getProject();
		} else if(selection instanceof IAdaptable) {
			Object r = ((IAdaptable)selection).getAdapter(IResource.class);
			return r instanceof IResource ? ((IResource)r).getProject() : null;
		}
		return null;
	}

	protected boolean computeEnabled() {
		if(project == null || !project.isAccessible()) return false;
		boolean isWar = JavaEEProjectUtilities.isDynamicWebProject(project);
		boolean isEar = JavaEEProjectUtilities.isEARProject(project);
		boolean isEJB = JavaEEProjectUtilities.isEJBProject(project);
		boolean isEsb = isESBProject(project);
		return isEar || isEJB || isWar || isEsb;
	}

	public void run(IAction action) {
		try {
			changeTimeStamp(project);
		} catch (Exception e) {
			
		}
	}

	public void dispose() {
		window = null;
	}
	
	public static void changeTimeStamp(IProject project) throws CoreException {
		if(project == null || !project.isAccessible()) return;
		List<IFile> fs = getFilesToTouch(project);
		for (int i = 0; i < fs.size(); i++) {
			IFile f = (IFile)fs.get(i);
			f.setLocalTimeStamp(System.currentTimeMillis());
			f.touch(new NullProgressMonitor());	// done so deployers/listeners can detect the actual change.		
		}
		if(fs.isEmpty() && JavaEEProjectUtilities.isDynamicWebProject(project)) {
			List<IServer> servers = getServers(project);
			
			final IServer[] ss = servers.toArray(new IServer[0]);
			
			final Object input = new Object();

			Shell shell = JBossServerUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
			CheckedTreeSelectionDialog dialog = new CheckedTreeSelectionDialog(shell, new LabelProvider(), new ITreeContentProvider() {				
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}
				public void dispose() {
				}
				public boolean hasChildren(Object element) {
					return element == input;
				}
				public Object getParent(Object element) {
					return null;
				}
				public Object[] getElements(Object inputElement) {
					return ss;
				}
				public Object[] getChildren(Object parentElement) {
					return null;
				}
			});
			dialog.setInput(input);
			dialog.setInitialSelections(ss);
			String message = NLS.bind(Messages.ChangeTimestampServerListDialog_Message, project.getName());
			dialog.setMessage(message);
			dialog.create();
			dialog.getShell().setText(Messages.ChangeTimestampServerListDialog_Title);

			dialog.open();
			Object[] os = dialog.getResult();
			
			if(os == null || os.length == 0) {
				return;
			}
			
			servers.clear();
			for (Object s: os) {
				if(s instanceof IServer) {
					servers.add((IServer)s);
				}
			}

			new RegisterServerJob(project, servers.toArray(new IServer[0])).schedule();
		}
	}

	private static List<IFile> getFilesToTouch(IProject project) {
		List<IFile> fs = new ArrayList<IFile>();
		if(project == null || !project.isAccessible()) return fs;
		boolean isWar = JavaEEProjectUtilities.isDynamicWebProject(project);
		boolean isEar = JavaEEProjectUtilities.isEARProject(project);

		boolean isReferencedByEar = false;
		if(!isEar) {
			IProject[] ps = J2EEProjectUtilities.getReferencingEARProjects(project);
			for (int i = 0; i < ps.length; i++) {
				fs.addAll(getFilesToTouch(ps[i]));
				isReferencedByEar = true;
			}
		}
		if(isEar) {
			IVirtualComponent component = ComponentCore.createComponent(project);
			IPath path = component.getRootFolder().getProjectRelativePath();
			IFile f = project.getFile(path.append("META-INF").append("application.xml")); //$NON-NLS-1$ //$NON-NLS-2$
			if(f != null && f.exists()) {
				fs.add(f);
			}
		}
		if(isWar && !isReferencedByEar) {
			IVirtualComponent component = ComponentCore.createComponent(project);
			IPath path = component.getRootFolder().getProjectRelativePath();
			IFile f = project.getFile(path.append("WEB-INF").append("web.xml")); //$NON-NLS-1$ //$NON-NLS-2$
			if(f != null && f.exists()) {
				fs.add(f);
			}
		}
		if(isESBProject(project)) {
			IVirtualComponent component = ComponentCore.createComponent(project);
			IPath path = component.getRootFolder().getProjectRelativePath();
			IFile f = project.getFile(path.append("META-INF").append("jboss-esb.xml")); //$NON-NLS-1$ //$NON-NLS-2$
			if(f != null && f.exists()) {
				fs.add(f);
			}
		}

		return fs;
	}

	private final static String ESB_PROJECT_FACET = "jst.jboss.esb"; //$NON-NLS-1$

	private static boolean isESBProject(IProject project) {
		IFacetedProject facetedProject = null;
		try {
			facetedProject = ProjectFacetsManager.create(project);
		} catch (CoreException e) {
			JBossServerUIPlugin.log(e.getMessage(), e);
			return false;
		}
		if (facetedProject != null && ProjectFacetsManager.isProjectFacetDefined(ESB_PROJECT_FACET)) {
			IProjectFacet projectFacet = ProjectFacetsManager.getProjectFacet(ESB_PROJECT_FACET);
			return projectFacet != null && facetedProject.hasProjectFacet(projectFacet);
		}
		return false;
	}

	static List<IServer> getServers(IProject project) {
		List<IServer> result = new ArrayList<IServer>();
		IServer[] servers = ServerCore.getServers();
		for (IServer s: servers) {
			if(isRegistered(project, s)) {
				result.add(s);
			}
		}
		return result;
	}
	
	public static boolean isRegistered(IProject project, IServer server) {
		if(server == null || project == null) return false;
		IModule[] ms = server.getModules();
		IModule m = findModule(project);
		return (contains(ms, m));
	}

	public static IModule findModule(IProject project) {
		// https://jira.jboss.org/jira/browse/JBIDE-3972
		// There may be a few modules for resources from the same project.
		// Ignore module with jboss.singlefile type if there are other module types.
		IModule[] modules = ServerUtil.getModules(project);
		if(modules != null && modules.length>0) {
			for (int i = 0; i < modules.length; i++) {
				if(!"jboss.singlefile".equals(modules[i].getModuleType().getId())) { //$NON-NLS-1$
					return modules[i];
				}
			}
		}
		return null;
	}

	private static boolean contains(IModule[] modules, IModule module) {
		if(modules == null || module == null) return false;
		for (int i = 0; i < modules.length; i++) {
			if(modules[i].getName() != null && modules[i].getName().equals(module.getName())) return true;
		}
		return false;
	}
	
	public static void register(IProject project, IServer server) {
		if(server == null) return;
		IModule m = findModule(project);
		if(m == null) return;
		if(contains(server.getModules(), m)) return;
		IModule[] add = new IModule[]{m};
		IModule[] remove = new IModule[0];
		try {
			IProgressMonitor monitor = new NullProgressMonitor();
			IServerWorkingCopy copy = server.createWorkingCopy();
			IStatus status = copy.canModifyModules(add, remove, monitor);
			if(status != null && !status.isOK()) return;
			ServerUtil.modifyModules(copy, add, remove, monitor);
			copy.save(true, monitor);
			if(canPublish(server)) {
				server.publish(IServer.PUBLISH_INCREMENTAL, monitor);
			}
		} catch (CoreException e) {
//			WebModelPlugin.getPluginLog().logError(e);
		}
	}
	
	public static boolean canPublish(IServer server) {
		if(server == null || server.getRuntime() == null) return false;
		if (((ServerType)server.getServerType()).startBeforePublish() && 
			(server.getServerState() != IServer.STATE_STARTED)) {
			return false;
		}
		return true;
	}
	
	public static boolean unregister(IProject project, IServer server) {
		if(server == null) return false;
		IModule m = findModule(project);
		if(!contains(server.getModules(), m)) return false;
		IModule[] add = new IModule[0];
		IModule[] remove = new IModule[]{m};
		try {
			IProgressMonitor monitor = new NullProgressMonitor();
			IServerWorkingCopy copy = server.createWorkingCopy();
			ServerUtil.modifyModules(copy, add, remove, monitor);
			copy.save(true, monitor);
			if(canPublish(server)) {
				server.publish(IServer.PUBLISH_INCREMENTAL, monitor);
			}
		} catch (CoreException e) {
			e.printStackTrace();
//			WebModelPlugin.getPluginLog().logError(e);
		}
		return true;
	}
	
	private static class RegisterServerJob extends Job {
		long counter = 100;
		IProject project;
		IServer[] servers;
		public RegisterServerJob(IProject p, IServer[] servers) {
			super("Touch");
			this.project = p;
			this.servers = servers;
		}

		protected IStatus run(IProgressMonitor monitor) {
			try {
				ResourcesPlugin.getWorkspace().run(new WR(), monitor);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return Status.OK_STATUS;
		}

		class WR implements IWorkspaceRunnable {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (IServer s: servers) {
					unregister(project, s);
				}
				for (IServer s: servers) {
					register(project, s);
				}
			}
			
		}
	}

}