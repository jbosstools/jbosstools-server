/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 * TODO: Logging and Progress Monitors
 ******************************************************************************/
package org.jboss.ide.eclipse.as.rse.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.IHostShellOutputListener;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.launch.AbstractJBossStartLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.server.internal.launch.IStartLaunchDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.launch.IStartLaunchSetupParticipant;
import org.jboss.ide.eclipse.as.rse.core.RSEHostShellModel.ServerShellModel;

public abstract class AbstractRSELaunchDelegate extends AbstractJBossStartLaunchConfiguration 
	implements IStartLaunchDelegate, IStartLaunchSetupParticipant {

	protected void executeRemoteCommand(String command, DelegatingServerBehavior behavior)
			throws CoreException {
		try {
			ServerShellModel model = RSEHostShellModel.getInstance().getModel(behavior.getServer());
			IHostShell shell = model.createStartupShell("/", command, new String[] {}, new NullProgressMonitor());
			addShellOutputListener(shell);
		} catch (SystemMessageException sme) {
			behavior.setServerStopped(); // Not sure when this comes, but we should
									// try to keep track
			throw new CoreException(new Status(IStatus.ERROR,
					org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID,
					sme.getMessage(), sme));
		}
	}
	// Only for debugging
	private void addShellOutputListener(IHostShell shell) {
		IHostShellOutputListener listener = null;
		listener = new IHostShellOutputListener() {
			public void shellOutputChanged(IHostShellChangeEvent event) {
				IHostOutput[] out = event.getLines();
				for (int i = 0; i < out.length; i++) {
					// TODO listen here for obvious exceptions or failures
					// System.out.println(out[i]);
				}
			}
		};
		// shell.addOutputListener(listener);
	}
}
