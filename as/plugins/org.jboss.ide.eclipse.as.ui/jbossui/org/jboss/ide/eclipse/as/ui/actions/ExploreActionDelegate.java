/*************************************************************************************
 * Copyright (c) 2008-2009 JBoss by Red Hat and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.jboss.ide.eclipse.as.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.wst.server.core.IServer;

/**
 * @author Snjeza
 *
 */
public class ExploreActionDelegate implements IObjectActionDelegate {

	private ISelection fCurrentSelection;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	
	public void run(IAction action) {
		if (fCurrentSelection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) fCurrentSelection;
			Object object = structuredSelection.getFirstElement();
			if (object instanceof IServer) {
				IServer server = (IServer) object;
				String deployDirectory = ExploreUtils.getDeployDirectory(server);
				if (deployDirectory != null && deployDirectory.length() > 0) {
					ExploreUtils.explore(deployDirectory);
				} 
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	
	public void selectionChanged(IAction action, ISelection selection) {
		fCurrentSelection= selection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		
	}

}
