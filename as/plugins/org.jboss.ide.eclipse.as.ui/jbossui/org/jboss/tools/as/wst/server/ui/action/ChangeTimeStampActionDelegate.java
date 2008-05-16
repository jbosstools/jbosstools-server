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
package org.jboss.tools.as.wst.server.ui.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;

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
		boolean isWar = J2EEProjectUtilities.isDynamicWebProject(project);
		boolean isEar = J2EEProjectUtilities.isEARProject(project);
		boolean isEJB = J2EEProjectUtilities.isEJBProject(project);
		return isEar || isEJB || isWar;
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
	}

	private static List<IFile> getFilesToTouch(IProject project) {
		List<IFile> fs = new ArrayList<IFile>();
		if(project == null || !project.isAccessible()) return fs;
		boolean isWar = J2EEProjectUtilities.isDynamicWebProject(project);
		boolean isEar = J2EEProjectUtilities.isEARProject(project);

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
			IFile f = project.getFile(path.append("META-INF").append("application.xml"));
			if(f != null && f.exists()) {
				fs.add(f);
			}
		}
		if(isWar && !isReferencedByEar) {
			IVirtualComponent component = ComponentCore.createComponent(project);
			IPath path = component.getRootFolder().getProjectRelativePath();
			IFile f = project.getFile(path.append("WEB-INF").append("web.xml"));
			if(f != null && f.exists()) {
				fs.add(f);
			}
		}
		return fs;
	}	
}