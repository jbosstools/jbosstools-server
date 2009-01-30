/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.webtools.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
import org.jboss.ide.eclipse.archives.ui.actions.INodeActionDelegate;
import org.jboss.ide.eclipse.archives.webtools.Messages;
import org.jboss.ide.eclipse.archives.webtools.modules.ArchivesModuleModelListener;

public class PublishAction implements INodeActionDelegate {


	public PublishAction() {
	}

	public void run (IArchiveNode node) {
		if (node.getNodeType() == IArchiveNode.TYPE_ARCHIVE
				&& ((IArchive)node).isTopLevel()) {
			final IArchive pkg = (IArchive)node;
			String servers = node.getProperty(ArchivesModuleModelListener.DEPLOY_SERVERS);
			if( servers == null || "".equals(servers) || anyServerDoesntExist(servers)){ //$NON-NLS-1$
				servers = showSelectServersDialog(pkg);
			}
			final String servers2 = servers;
			if( servers != null ) {
				Job j = new Job(Messages.BuildArchive) {
					protected IStatus run(IProgressMonitor monitor) {
						ArchivesModuleModelListener.publish(pkg, servers2, IServer.PUBLISH_FULL);
						return Status.OK_STATUS;
					} };
				j.schedule();
			}
		}
	}

	protected boolean anyServerDoesntExist(String servers) {
		String[] asArray = servers.split(","); //$NON-NLS-1$
		for( int i = 0; i < asArray.length; i++ )
			if( ServerCore.findServer(asArray[i]) == null )
				return true;
		return false;
	}
	public boolean isEnabledFor(IArchiveNode node) {
		if (node.getNodeType() == IArchiveNode.TYPE_ARCHIVE ) {
			IArchive pkg = (IArchive) node;
			if (pkg.isTopLevel()) {
				return true;
			}
		}
		return false;
	}

	protected String showSelectServersDialog(IArchive node) {
		ArchivePublishWizard wiz = new ArchivePublishWizard(node);
		int result = new WizardDialog(new Shell(), wiz).open();
		if( result == Window.OK) {
			return wiz.getServers();
		}
		return null;
	}
}
