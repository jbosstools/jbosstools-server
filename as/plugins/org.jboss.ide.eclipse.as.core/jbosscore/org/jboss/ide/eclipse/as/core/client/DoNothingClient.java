/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.core.client;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ClientDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

/**
 * This client will only copy to the deploy directory,
 * which is actually a side effect of the run-on-server action.
 * 
 * Therefore, this client does absolutely nothing.
 * 
 * It only shows up on the list for files that are deployable via 
 * copying into a deploy directory. 
 * 
 * @author rstryker
 *
 */
public class DoNothingClient extends ClientDelegate {

	public DoNothingClient() {
		super();
	}

	public boolean supports(IServer server, Object launchable, String launchMode) {
		if( ServerConverter.getDeployableServer(server) == null ) return false;
		return true;
	}

	public IStatus launch(IServer server, Object launchable, String launchMode,
			ILaunch launch) {
		return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, 0, "A-OK", null);
	}

}