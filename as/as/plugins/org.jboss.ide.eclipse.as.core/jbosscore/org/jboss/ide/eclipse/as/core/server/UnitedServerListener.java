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
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerEvent;

/**
 * This is a stub superclass which can be used
 * to override only those methods you care about.
 * 
 * @author Rob Stryker
 *
 */
public class UnitedServerListener {

	public void init(IServer server) {}
	public void serverAdded(IServer server) {}
	public void serverChanged(IServer server) {}
	public void serverRemoved(IServer server) {}
	public void serverChanged(ServerEvent event) {}
	public void publishStarted(IServer server){}
	public void publishFinished(IServer server, IStatus status){}
	public void cleanUp(IServer server) {}
}
