package org.jboss.ide.eclipse.archives.ui.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.jboss.ide.eclipse.archives.core.build.ArchiveBuildDelegate;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;

public class BuildAction implements IWorkbenchWindowActionDelegate {
	private IProject selectedProject;
	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

	public void run(IAction action) {
		if( selectedProject != null )
			buildSelectedNode(selectedProject);
	}

	public void run(Object node) {
		buildSelectedNode(node);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if( !selection.isEmpty() && selection instanceof IStructuredSelection ) {
			Object o = ((IStructuredSelection)selection).getFirstElement();
			if( o instanceof IAdaptable ) {
				IResource res = (IResource)  ((IAdaptable)o).getAdapter(IResource.class);
				if( res != null ) {
					selectedProject = res.getProject();
					return;
				}
			}
		}
		selectedProject = null;
	}

	private void buildSelectedNode(final Object selected) {
		new Job("Build Archive Node") {
			// TODO actually get the status object
			protected IStatus run(IProgressMonitor monitor) {
				if( selected == null ) return Status.OK_STATUS;
				if (selected instanceof IArchiveNode &&
						((IArchiveNode)selected).getNodeType() == IArchiveNode.TYPE_ARCHIVE) {
					new ArchiveBuildDelegate().fullArchiveBuild((IArchive)selected, monitor);
				} else if( selected != null && selected instanceof IProject ){
					new ArchiveBuildDelegate().fullProjectBuild(((IProject)selected).getProject().getLocation(), monitor);
				} else {
					new ArchiveBuildDelegate().fullArchiveBuild(((IArchiveNode)selected).getRootArchive(), monitor);
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

}