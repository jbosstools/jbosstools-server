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
package org.jboss.ide.eclipse.as.ui.views.server.extensions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.ui.Messages;

import com.ibm.icu.text.MessageFormat;

public class ShowInJMXConsoleActionProvider extends AbstractOpenBrowserServerAction {
	private static final String CONSOLE_URL_PATTERN = "http://{0}:{1}/jmx-console"; //$NON-NLS-1$
	protected String getActionText() {
		return  Messages.ShowInJMXConsole_Action_Text;
	}
	protected boolean shouldAddForSelection(IStructuredSelection sel) {
		IServer server = getSingleServer(sel);
		if( server != null ) {
			IJBossServer jbs = ServerConverter.getJBossServer(server);
			if( jbs != null ) {
				// TODO fix this so anything BELOW 7 works
				// need new util methods
				return ServerUtil.isJBossServerType(server.getServerType()) && !ServerUtil.isJBoss7(server);
			}
		}
		return false;
	}

	protected boolean accepts(IServer server) {
		return server.getServerState() == IServer.STATE_STARTED;
	}
	protected String getURL(IServer server) throws CoreException {
		JBossServer jbossServer = ServerUtil.checkedGetServerAdapter(server, JBossServer.class);
		String host = jbossServer.getHost();
		int webPort = jbossServer.getJBossWebPort();
		String consoleUrl = MessageFormat.format(CONSOLE_URL_PATTERN, host, String.valueOf(webPort));
		return consoleUrl;
	}
}
