package org.jboss.ide.eclipse.as.egit.internal.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jboss.ide.eclipse.as.egit.core.EGitUtils;

/**
 * 
 * This is a test handler that should be removed in the finaly Milestone!
 * 
 * @author André Dietisheim
 *
 */
public class EGitUtilsCommitAndPushHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		Assert.isTrue(selection instanceof IStructuredSelection);
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		Object selectedElement = structuredSelection.getFirstElement();
		IProject project = (IProject) Platform.getAdapterManager().getAdapter(selectedElement, IProject.class);
		try {
			EGitUtils.commit(project, null);
			EGitUtils.push(EGitUtils.getRepository(project), null);
		} catch (CoreException e) {
			throw new ExecutionException(e.getStatus().getMessage(), e.getStatus().getException());
		}
		return null;
	}
}
