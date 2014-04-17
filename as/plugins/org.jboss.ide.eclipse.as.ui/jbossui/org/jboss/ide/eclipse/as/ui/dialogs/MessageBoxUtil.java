/******************************************************************************* 
* Copyright (c) 2009 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.dialogs;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.UserPrompter;
import org.jboss.ide.eclipse.as.core.UserPrompter.IPromptHandler;

/**
 * A class for displaying small messages to the user
 * from the core. 
 */
public class MessageBoxUtil {
	public static IPromptHandler zombieProcessHandler() {
		return new IPromptHandler() {
			public Object promptUser(final int code, final Object... data) {
				if( code == UserPrompter.EVENT_CODE_PROCESS_UNTERMINATED) {
					final Object[] ret = new Object[1];
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							ret[0] = promptUserZombieProcess(data);
						}
					});
					return ret[0];
				}
				return null;
			}
		};
	}
	private static Object promptUserZombieProcess(Object... data) {
		IWorkbenchWindow active = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		Shell s = active.getShell();
        MessageBox messageBox = new MessageBox(s,
        		SWT.ICON_WARNING | SWT.YES | SWT.NO);
        
        messageBox.setText("Warning: server process not terminated");
        messageBox.setMessage(NLS.bind(
        		"The process for server {0} has not been terminated. Would you like us to terminate it for you?", 
        		((IServer)data[0]).getName()));
        int buttonID = messageBox.open();
        switch(buttonID) {
          case SWT.YES:
        	  return true;
          case SWT.NO:
        	  return false;
        }
        return null;
	}

}
