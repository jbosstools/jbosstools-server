/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
