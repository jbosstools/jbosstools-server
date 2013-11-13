/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.rse.ui;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFileDialog;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.server.core.IServerAttributes;
import org.jboss.ide.eclipse.as.rse.core.RSEFrameworkUtils;
import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
import org.jboss.ide.eclipse.as.ui.IBrowseBehavior;

public class RSEBrowseBehavior implements IBrowseBehavior {
	public String openBrowseDialog(IServerAttributes server, String original) {
		String current = server.getAttribute(RSEUtils.RSE_SERVER_HOST, (String)null);
		IHost h = RSEFrameworkUtils.findHost(current);
		return browseClicked(new Shell(), h);
	}
	

	public static String browseClicked(Shell s, IHost host) {
		return browseClicked(s,host,null);
	}

	public static String browseClicked(Shell s, IHost host, String path) {
		SystemRemoteFileDialog d = new SystemRemoteFileDialog(
				s, RSEUIMessages.BROWSE_REMOTE_SYSTEM, host);
		if( path != null ) {
			try {
				IRemoteFileSubSystem ss  =	RemoteFileUtility.getFileSubSystem(host);
				IRemoteFile rootFolder = ss.getRemoteFileObject(path, new NullProgressMonitor());
				d.setPreSelection(rootFolder);
			} catch(SystemMessageException sme) {
				// Ignore
			}
		}
		
		if( d.open() == Dialog.OK) {
			Object o = d.getOutputObject();
			if( o instanceof IRemoteFile ) {
				String path2 = ((IRemoteFile)o).getAbsolutePath();
				return path2;
			}
		}
		return null;
	}

}
