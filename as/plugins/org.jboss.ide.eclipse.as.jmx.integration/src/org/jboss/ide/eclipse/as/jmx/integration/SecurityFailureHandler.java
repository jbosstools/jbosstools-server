/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.jmx.integration;

import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IPollerFailureHandler;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.ide.eclipse.as.ui.dialogs.RequiredCredentialsDialog;

/**
 * 
 * @author rob.stryker <rob.stryker@redhat.com>
 *
 */
public class SecurityFailureHandler implements IPollerFailureHandler {

	public boolean accepts(IServerStatePoller poller, String action,
			List requiredProperties) {
		if( poller.getPollerType().getId().equals(JMXPoller.POLLER_ID))
			return true;
		return false;
	}

	public void handle(final IServerStatePoller poller, String action, List requiredProperties) {
		Display.getDefault().asyncExec(new Runnable() { 
			public void run() {
				IServer server = poller.getServer();
				IServerWorkingCopy copy = server.createWorkingCopy();
				JBossServer jbs = ServerConverter.getJBossServer(copy);
				RequiredCredentialsDialog d = new RequiredCredentialsDialog(new Shell(), jbs);
				if( d.open() == Window.OK) {
					if( d.getSave() ) {
						jbs.setPassword(d.getPass());
						jbs.setUsername(d.getUser());
						try {
							copy.save(false, null);
						} catch( CoreException ce ) {
							JBossServerUIPlugin.log(Messages.ServerSaveFailed, ce);
						}
					}
					
					Properties p = new Properties();
					p.put(JMXPoller.REQUIRED_USER, d.getUser());
					p.put(JMXPoller.REQUIRED_PASS, d.getPass());
					poller.failureHandled(p);
				} else {
					poller.failureHandled(null);
				}
			}
		});
	}
}
