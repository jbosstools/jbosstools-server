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

import java.io.IOException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.view.servers.AbstractServerAction;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.JBossServerUISharedImages;

/**
 * @author snjeza
 * 
 */

public class ExploreAction extends AbstractServerAction {

	public ExploreAction(Shell shell, ISelectionProvider selectionProvider) {
		super(shell, selectionProvider, ExploreUtils.EXPLORE);
		setToolTipText(ExploreUtils.EXPLORE_DESCRIPTION);
		setImageDescriptor(JBossServerUISharedImages.getImageDescriptor(JBossServerUISharedImages.EXPLORE_IMAGE));
		try {
			selectionChanged((IStructuredSelection) selectionProvider
					.getSelection());
		} catch (Exception ignore) {
		}
	}

	@Override
	public void perform(IServer server) {
		String deployDirectory = ExploreUtils.getDeployDirectory(server);
		if (deployDirectory != null && deployDirectory.length() > 0) {
			ExploreUtils.explore(deployDirectory);
		} 

	}

	@Override
	public boolean accept(IServer server) {
		return ExploreUtils.canExplore(server);
	}

}
