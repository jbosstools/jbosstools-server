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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.server.IPromptHandler;
import org.jboss.ide.eclipse.as.wtp.core.server.launch.ServerHotCodeReplaceListener;
import org.jboss.ide.eclipse.as.wtp.ui.WTPOveridePlugin;

public class ServerHotCodeReplacePrompter implements IPromptHandler  {
	public Object promptUser(final int code, Object... data) {
		final IServer s = (IServer)data[0];
		final Exception e = (Exception)data[1];
		
		final ServerHotCodeReplaceDialog[] d = new ServerHotCodeReplaceDialog[1];
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				final ServerHotCodeReplaceDialog dialog = new ServerHotCodeReplaceDialog(s, code,  e);
				dialog.open();
				d[0] = dialog;
			} });
		
		int returnCode = d[0].getReturnCode();
		boolean saveChoice = d[0].getSaveSetting();
		if( saveChoice ) {
			IServerWorkingCopy wc = s.createWorkingCopy();
			wc.setAttribute(ServerHotCodeReplaceListener.PROPERTY_HOTCODE_BEHAVIOR, returnCode);
			try {
				wc.save(true, new NullProgressMonitor());
			} catch(CoreException ce) {
				WTPOveridePlugin.getInstance().getLog().log(new Status(IStatus.ERROR, WTPOveridePlugin.PLUGIN_ID, ce.getMessage(), ce));
			}
		}
		
		return new Integer(returnCode);
	}
	
}
