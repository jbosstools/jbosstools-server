/******************************************************************************* 
* Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.ui.prompt;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IPromptHandler;
import org.jboss.ide.eclipse.as.core.server.UserPrompter;

public class ServerAlreadyStartedPrompter implements IPromptHandler {

	public Object promptUser(int code, Object... data) {
		if( code == UserPrompter.EVENT_CODE_SERVER_ALREADY_STARTED ) {
			return promptForBehaviour((IServer)data[0], (IStatus)data[1]);
		}
		return null;
	}

	
	public int promptForBehaviour(final IServer server, final IStatus status) {
		final int[] result = new int[1]; 
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				ServerAlreadyStartedDialog d = new ServerAlreadyStartedDialog(server, status,
						Display.getDefault().getActiveShell()); 
				int dResult = d.open();
				if( dResult == Window.CANCEL ) {
					result[0] = UserPrompter.RETURN_CODE_SAS_CANCEL;
				} else {
					result[0] = d.launch ? UserPrompter.RETURN_CODE_SAS_CONTINUE_STARTUP 
							: UserPrompter.RETURN_CODE_SAS_ONLY_CONNECT;
				}
			}
		});
		return result[0];
	}

}
