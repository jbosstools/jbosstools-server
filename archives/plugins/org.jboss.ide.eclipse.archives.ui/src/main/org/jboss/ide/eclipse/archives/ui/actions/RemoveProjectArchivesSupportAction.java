package org.jboss.ide.eclipse.archives.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class RemoveProjectArchivesSupportAction implements
		IObjectActionDelegate {

	public RemoveProjectArchivesSupportAction() {
	}

	public void run(IAction action) {
		System.out.println("Removing support"); //$NON-NLS-1$
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub

	}

}
