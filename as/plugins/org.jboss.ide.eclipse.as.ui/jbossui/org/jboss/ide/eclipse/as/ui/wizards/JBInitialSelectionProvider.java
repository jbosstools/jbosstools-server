/**
 * JBoss by Red Hat
 * Copyright 2006-2009, Red Hat Middleware, LLC, and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.ui.wizards;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.ui.internal.viewers.InitialSelectionProvider;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.ui.JBossServerUIPlugin;
import org.osgi.service.prefs.BackingStoreException;

/**
 * 
 * @author Rob Stryker <rob.stryker@redhat.com>
 *
 */
public class JBInitialSelectionProvider extends InitialSelectionProvider implements IServerLifecycleListener {

	public JBInitialSelectionProvider() {
	}
	
	public IServerType getInitialSelection(IServerType[] serverTypes) {
		
		if (serverTypes == null)
			return null;
		
		// Find the last-selected one
		IEclipsePreferences prefs = new InstanceScope().getNode(JBossServerUIPlugin.PLUGIN_ID);
		String last = prefs.get(LAST_SERVER_CREATED_KEY, null);
		if( last != null ) {
			IServer s = ServerCore.findServer(last);
			if( s != null ) {
				for( int i = 0; i < serverTypes.length; i++ )
					if( serverTypes[i].getId().equals(s.getServerType().getId()))
						return serverTypes[i];
			}
		}
		
		// return default
		int size = serverTypes.length;
		for (int i = 0; i < size; i++) {
			if( serverTypes[i].getId().equals(IJBossToolingConstants.SERVER_AS_51))
				return serverTypes[i];
		}
		return serverTypes[0];
	}

	private static String LAST_SERVER_CREATED_KEY = "org.jboss.ide.eclipse.as.ui.wizards.LAST_SERVER_CREATED"; //$NON-NLS-1$

	public void serverAdded(IServer server) {
		if( server != null ) {
			IEclipsePreferences prefs = new InstanceScope().getNode(JBossServerUIPlugin.PLUGIN_ID);
			prefs.put(LAST_SERVER_CREATED_KEY, server.getId());
			try {
				prefs.flush();
			} catch(BackingStoreException e) {
			}
		}
	}
	public void serverChanged(IServer server) {
		// Do Nothing
	}

	public void serverRemoved(IServer server) {
		// Do Nothing
	}


}
