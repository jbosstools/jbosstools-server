/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
