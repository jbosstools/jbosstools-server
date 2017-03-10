/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.extensions.events;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;

/**
 * This class is marked for deletion. There is no alternative. 
 * Please use the official error log. 
 */
@Deprecated
public class ServerLogger {
	private static ServerLogger instance;
	public static ServerLogger getDefault() {
		if( instance == null ) {
			instance = new ServerLogger();
		}
		return instance;
	}
		
	public void log(IServer server, IStatus status) {
		JBossServerCorePlugin.getDefault().getLog().log(status);
	}
}
