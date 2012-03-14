/*******************************************************************************
 * Copyright (c) 2010 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.wizards;

import java.util.ArrayList;
import java.util.Arrays;

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
		ArrayList<IServerType> types = new ArrayList<IServerType>();
		types.addAll(Arrays.asList(serverTypes));
		
		// Find the last-selected one
		IEclipsePreferences prefs =  InstanceScope.INSTANCE.getNode(JBossServerUIPlugin.PLUGIN_ID);
		String last = prefs.get(LAST_SERVER_CREATED_KEY, null);
		IServer lastServer = last == null ? null : ServerCore.findServer(last);
		IServerType lastType = lastServer == null ? null : lastServer.getServerType();
		if( lastType != null && types.contains(lastType))
			return lastType;
		
		// return default server type
		IServerType defaultType = getDefaultServerType();
		if( types.contains(defaultType))
			return defaultType;
		
		// Else, just choose whatever they give us
		return serverTypes[0];
	}
	
	public IServerType getDefaultServerType() {
		String newestJBoss = IJBossToolingConstants.SERVER_AS_71;
		return ServerCore.findServerType(newestJBoss);
	}

	private static String LAST_SERVER_CREATED_KEY = "org.jboss.ide.eclipse.as.ui.wizards.LAST_SERVER_CREATED"; //$NON-NLS-1$

	public void serverAdded(IServer server) {
		if( server != null ) {
			IEclipsePreferences prefs =  InstanceScope.INSTANCE.getNode(JBossServerUIPlugin.PLUGIN_ID);
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
