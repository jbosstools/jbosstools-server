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

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.IHostShellOutputListener;
import org.jboss.ide.eclipse.as.core.server.IDelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.IDeployableServerBehaviour;
import org.jboss.ide.eclipse.as.core.server.IJBossBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.server.IJBossLaunchDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.launch.AbstractJBossStartLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.util.ThreadUtils;
import org.jboss.ide.eclipse.as.rse.core.RSEHostShellModel.ServerShellModel;

public abstract class AbstractRSELaunchDelegate extends AbstractJBossStartLaunchConfiguration 
	implements IJBossLaunchDelegate {

	public static final String DELIMETER = ":";
	public static final String ECHO_KEY_DISCOVER_PID = "JBTOOLS_SERVER_START_CMD";
	protected void executeRemoteCommand(String command, IDelegatingServerBehavior behavior)
			throws CoreException {
		try {
			ServerShellModel model = RSEHostShellModel.getInstance().getModel(behavior.getServer());
			IHostShell shell = model.createStartupShell("/", command, new String[] {}, new NullProgressMonitor());
			addShellOutputListener(shell, behavior);
			String getPidCommand = "echo \"" + ECHO_KEY_DISCOVER_PID + DELIMETER + behavior.getServer().getId() + DELIMETER + "\"$!";
			shell.writeToShell(getPidCommand);
		} catch (SystemMessageException sme) {
			// could not connect to remote system
			((DelegatingServerBehavior)behavior).setServerStopped(); 
			throw new CoreException(new Status(IStatus.ERROR,
					org.jboss.ide.eclipse.as.rse.core.RSECorePlugin.PLUGIN_ID,
					MessageFormat.format("Could not execute command on remote server {0}. Please ensure the server is reachable.", behavior.getServer().getName()), sme));
		}
	}
	
	private void addShellOutputListener(final IHostShell shell, 
			final IDelegatingServerBehavior behavior) {
		if( shell == null ) 
			return; // No listener needed for a null shell. 
		IHostShellOutputListener listener = null;
		listener = new IHostShellOutputListener() {
			public void shellOutputChanged(IHostShellChangeEvent event) {
				IHostOutput[] out = event.getLines();
				for (int i = 0; i < out.length; i++) {
					if( out[i].toString().startsWith(ECHO_KEY_DISCOVER_PID)) {
						// pid found
						int lastColon = out[i].toString().lastIndexOf(DELIMETER);
						String pid = out[i].toString().substring(lastColon+1);
						IJBossBehaviourDelegate del = behavior.getDelegate();
						if( del instanceof AbstractRSEBehaviourDelegate) {
							((AbstractRSEBehaviourDelegate)del).setPid(pid);
						}
					}
				}
			}
		};
		shell.addOutputListener(listener);
	}
	
	protected void launchPingThread(IDeployableServerBehaviour beh) {
		// TODO do it properly here
		ThreadUtils.sleepFor(30000);
		((DeployableServerBehavior)beh).setServerStarted();
	}
}
