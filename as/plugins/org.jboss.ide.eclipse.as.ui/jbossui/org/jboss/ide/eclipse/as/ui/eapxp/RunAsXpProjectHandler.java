/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.eapxp;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jboss.ide.eclipse.as.core.eap.xp.EapXpLaunchConfigurationDelegate;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.actions.DeployHandler;

public class RunAsXpProjectHandler extends AbstractHandler implements ILaunchShortcut2 {

	protected Shell shell;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		shell = HandlerUtil.getActiveShell(event);
		ISelection selection = DeployHandler.getSelectionFromEvent(event);
		IResource r = getLaunchableResource(selection);
		if( r != null ) 
			return executeLaunch(r.getProject(), "run");
		return null;
	}
	
	private ILaunch executeLaunch(IProject p, String mode) throws ExecutionException {
		ILaunchManager mgr = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = mgr.getLaunchConfigurationType(EapXpLaunchConfigurationDelegate.TYPE);
		try {
    		ILaunchConfigurationWorkingCopy lc = type.newInstance(p, p.getName());
    		lc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, p.getName());
			if( lc != null ) {
	    		Job j = new Job("Launch standalone web project") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							ILaunch launch = lc.launch(mode, new NullProgressMonitor());
							return Status.OK_STATUS;
						} catch(CoreException ce) {
							return new Status(IStatus.ERROR, JBossServerUIPlugin.PLUGIN_ID, ce.getMessage(), ce);
						}
					}
	    			
	    		};
	    		j.schedule();
			}
			return null;
		} catch( CoreException ce) {
			throw new ExecutionException(ce.getMessage(), ce);
		}
	}

	private void logError(Exception ee) {
		JBossServerUIPlugin.log("Error running project: " + ee.getMessage(), ee);
	}
	
	@Override
	public void launch(ISelection selection, String mode) {
		IResource r = getLaunchableResource(selection);
		if( r != null ) {
			try {
				executeLaunch(r.getProject(), mode);
			} catch(ExecutionException ee) {
				logError(ee);
			}
		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		IResource launchableResource = getLaunchableResource(editor);
		if( launchableResource != null ) {
			try {
				executeLaunch((IProject)launchableResource, mode);
			} catch( ExecutionException ee) {
				logError(ee);
			}
		}		
	}
	@Override
	public IResource getLaunchableResource(ISelection selection) {
		IStructuredSelection sel2 = (IStructuredSelection)selection;
		Object[] objs = sel2.toArray();
		if( objs.length != 1 ) {
			return null;
		}
		IProject p = (objs[0] instanceof IProject ? ((IProject)objs[0]) : null);
		if( p == null ) {
			return null;
		}
		return p;
	}

	@Override
	public IResource getLaunchableResource(IEditorPart editorpart) {
		if (editorpart == null)
			return null;
		
		// check if the editor input itself can be run. Otherwise, check if
		// the editor has a file input that can be run
		IEditorInput input = editorpart.getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fei = (IFileEditorInput) input;
			IFile file = fei.getFile();
			if( file != null ) {
				return file.getProject();
			}
		}
		return null;
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		// TODO Auto-generated method stub
		return null;
	}

}
