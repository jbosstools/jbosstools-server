package org.jboss.ide.eclipse.as.core.modules;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.j2ee.refactor.listeners.ProjectRefactoringListener;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.modules.SingleDeployableFactory.SingleDeployableModuleDelegate;

public class SingleDeployableWorkspaceListener implements
		IResourceChangeListener, IResourceDeltaVisitor {
	private IModule[] changedMods;
	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
			final IProject project = (IProject) event.getResource();

			ArrayList<IModule> shouldChange = new ArrayList<IModule>();
			IModule[] allMods = SingleDeployableFactory.getFactory().getModules();
			for (int i = 0; i < allMods.length; i++) {
				if (allMods[i].getProject().equals(project))
					shouldChange.add(allMods[i]);
			}
			changedMods = shouldChange.toArray(new IModule[shouldChange.size()]);
		} else {
			try {
				if( event.getDelta() != null )
					event.getDelta().accept(this);
			} catch( CoreException ce) {
				JBossServerCorePlugin.log(ce.getStatus());
			}
		}
	}

	@Override
	public boolean visit(final IResourceDelta delta) throws CoreException {
		final IResource resource = delta.getResource();
		if (resource instanceof IWorkspaceRoot) {
			// delta is at the workspace root so keep going
			return true;
		} else if (resource instanceof IProject) {
			processProjectDelta((IProject) resource, delta);
		}
		return false;
	}

	protected void processProjectDelta(IProject resource, IResourceDelta delta) {
		int kind = delta.getKind();
		int flags = delta.getFlags();
		if(kind == IResourceDelta.ADDED && hasRenamedAddedFlags(flags)) {
			processRename(resource, changedMods);
		}
	}
	
	private void processRename(final IProject project, final IModule[] changedMods) {
		WorkspaceJob job = new WorkspaceJob(
				"Renaming a project with deployable artifacts") { //$NON-NLS-1$
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				processRename2(project, changedMods);
				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(final Object family) {
				return ProjectRefactoringListener.PROJECT_REFACTORING_JOB_FAMILY
						.equals(family);
			}
		};
		// XXX note: might want to consider switching to a MultiRule for
		// optimization
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.schedule();
	}

	private void processRename2(IProject project, IModule[] changedMods) {
		IPath[] paths = new IPath[changedMods.length];
		IPath[] paths2 = new IPath[changedMods.length];
		SingleDeployableModuleDelegate del;
		for( int i = 0; i < changedMods.length; i++ ) {
			del = (SingleDeployableModuleDelegate)changedMods[i].loadAdapter(SingleDeployableModuleDelegate.class, null);
			paths[i] = del.getWorkspaceRelativePath();
			paths2[i] = new Path(project.getName()).append(del.getWorkspaceRelativePath().removeFirstSegments(1));
		}
		// create new mods
		SingleDeployableFactory.makeDeployable(project, paths2);
		
		IServer[] allServers = ServerCore.getServers();
		for( int i = 0; i < allServers.length; i++ ) {
			ArrayList<IModule> toAdd = new ArrayList<IModule>();
			ArrayList<IModule> toRemove = new ArrayList<IModule>();
			
			IModule[] modsOnServer = allServers[i].getModules();
			for( int j = 0; j < modsOnServer.length; j++ ) {
				for( int k = 0; k < changedMods.length; k++ ) {
					if( changedMods[k].getId().equals(modsOnServer[j].getId())) {
						toRemove.add(changedMods[k]);
						toAdd.add(SingleDeployableFactory.findModule(paths2[k]));
					}
				}
			}
			if( toAdd.size() > 0 && toRemove.size() > 0 ) {
				IServerWorkingCopy wc = allServers[i].createWorkingCopy();
				try {
					wc.createWorkingCopy().modifyModules(
							toAdd.toArray(new IModule[toAdd.size()]),
							toRemove.toArray(new IModule[toRemove.size()]),
							new NullProgressMonitor());
					wc.save(true, new NullProgressMonitor());
				} catch( CoreException ce) {
					JBossServerCorePlugin.log(ce.getStatus());
				}
			}
		}
	}

	/*
	 * Determines if the added project was renamed based on the IResourceDelta
	 * flags
	 */
	private boolean hasRenamedAddedFlags(final int flags) {
		if ((flags & IResourceDelta.DESCRIPTION) > 0
				&& (flags & IResourceDelta.MOVED_FROM) > 0) {
			return true;
		}
		return false;
	}

}
