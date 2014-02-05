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
package org.jboss.ide.eclipse.as.core.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;
import org.jboss.ide.eclipse.as.wtp.core.Messages;

/**
 * This class just opens up the get and set attributes for a server,
 * and provides saving mechanisms. 
 * 
 * Since 3.0, it also implements IServerAttributes to allow
 * access to many other APIs. 
 */
public class ServerAttributeHelper extends ServerWorkingCopyWrapper {

	public static ServerAttributeHelper createHelper(IServer iserver) {
		if( iserver instanceof IServer ) {
			IServerWorkingCopy copy = ((IServer)iserver).createWorkingCopy();
			if( copy != null ) {
				return new ServerAttributeHelper(iserver, copy);
			}
		}
		return null;
	}
	
	private IServer server;
	public ServerAttributeHelper(IServer server, IServerWorkingCopy copy) {
		super(copy);
		this.server = server;
	}

	public IServer getServer() {
		return server;
	}
	
	public IServer save() {
		try {
			return save(false, new NullProgressMonitor());
		} catch( CoreException e ) {
			ASWTPToolsPlugin.log(IStatus.ERROR, Messages.ServerSaveFailed, e);
		}
		return null;
	}	
}
