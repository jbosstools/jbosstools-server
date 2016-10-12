/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
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
import java.util.List;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeLifecycleListener;
import org.eclipse.wst.server.core.IRuntimeType;
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
public class JBInitialSelectionProvider extends InitialSelectionProvider implements IServerLifecycleListener, IRuntimeLifecycleListener {
	private static final String LAST_SERVER_CREATED_KEY = "org.jboss.ide.eclipse.as.ui.wizards.LAST_SERVER_CREATED"; //$NON-NLS-1$
	private static final String LAST_RUNTIME_CREATED_KEY = "org.jboss.ide.eclipse.as.ui.wizards.LAST_RUNTIME_CREATED"; //$NON-NLS-1$
	private static final String LAST_SERVER_TYPE_CREATED_KEY = "org.jboss.ide.eclipse.as.ui.wizards.LAST_SERVER_TYPE_CREATED"; //$NON-NLS-1$
	private static final String LAST_RUNTIME_TYPE_CREATED_KEY = "org.jboss.ide.eclipse.as.ui.wizards.LAST_RUNTIME_TYPE_CREATED"; //$NON-NLS-1$
	
	
	private static final String DEFAULT_INITIAL_SERVER_TYPE = "DEFAULT_SERVER_TYPE"; //$NON-NLS-1$
	private static final String DEFAULT_INITIAL_RUNTIME_TYPE = "DEFAULT_RUNTIME_TYPE"; //$NON-NLS-1$

	private static final String LATEST_JBT_SERVER = IJBossToolingConstants.SERVER_WILDFLY_80;
	private static final String LATEST_JBT_RUNTIME = IJBossToolingConstants.WILDFLY_80;
	
	public JBInitialSelectionProvider() {
	}


	public IServerType getDefaultServerType() {
		IEclipsePreferences defaults = DefaultScope.INSTANCE.getNode(JBossServerUIPlugin.PLUGIN_ID);
		String newestJBoss = defaults.get(DEFAULT_INITIAL_SERVER_TYPE, LATEST_JBT_SERVER);
		return ServerCore.findServerType(newestJBoss);
	}

	public IRuntimeType getDefaultRuntimeType() {
		IEclipsePreferences defaults = DefaultScope.INSTANCE.getNode(JBossServerUIPlugin.PLUGIN_ID);
		String newestJBoss = defaults.get(DEFAULT_INITIAL_RUNTIME_TYPE, LATEST_JBT_RUNTIME);
		return ServerCore.findRuntimeType(newestJBoss);
	}
	
	@Override
	public IRuntimeType getInitialSelection(IRuntimeType[] runtimeTypes) {
		return (IRuntimeType)getInitialSelection(runtimeTypes, LAST_RUNTIME_TYPE_CREATED_KEY, true, getDefaultRuntimeType());
	}
	
	@Override
	public IServer getInitialSelection(IServer[] servers) {
		IEclipsePreferences prefs =  InstanceScope.INSTANCE.getNode(JBossServerUIPlugin.PLUGIN_ID);
		String last = prefs.get(LAST_SERVER_CREATED_KEY, null);
		IServer lastServer = last == null ? null : ServerCore.findServer(last);
		List<IServer> all = Arrays.asList(servers);
		return lastServer == null ? null : all.contains(lastServer) ? lastServer : null;
	}
	

	@Override
	public IServerType getInitialSelection(IServerType[] serverTypes) {
		return (IServerType)getInitialSelection(serverTypes, LAST_SERVER_TYPE_CREATED_KEY, true, getDefaultServerType());
	}
	
	private Object getInitialSelection(Object[] types, String lastKey, boolean isServer, Object defaultType) {
		ArrayList<Object> typesList = new ArrayList<Object>();
		typesList.addAll(Arrays.asList(types));

		// Find the last-selected one
		IEclipsePreferences prefs =  InstanceScope.INSTANCE.getNode(JBossServerUIPlugin.PLUGIN_ID);
		String last = prefs.get(lastKey, null);
		
		Object lastObject = null;
		if( isServer ) {
			lastObject = (last == null ? null : ServerCore.findServerType(last));
		} else {
			lastObject = (last == null ? null : ServerCore.findRuntimeType(last));
		}
		
		if( lastObject != null && typesList.contains(lastObject))
			return lastObject;
		
		if( typesList.contains(defaultType))
			return defaultType;

		return types == null || types.length == 0 ? null : types[0];
	}
	
	private void store(String id, String key) {
		if( id != null ) {
			IEclipsePreferences prefs =  InstanceScope.INSTANCE.getNode(JBossServerUIPlugin.PLUGIN_ID);
			prefs.put(key, id);
			try {
				prefs.flush();
			} catch(BackingStoreException e) {
			}
		}
		
	}
	
	public void serverAdded(IServer server) {
		store( server == null ? null : server.getId(), LAST_SERVER_CREATED_KEY);
		store( server == null ? null : server.getServerType().getId(), LAST_SERVER_TYPE_CREATED_KEY);
	}
	
	public void runtimeAdded(IRuntime runtime) {
		store( runtime == null ? null : runtime.getId(), LAST_RUNTIME_CREATED_KEY);
		store( runtime == null ? null : runtime.getRuntimeType().getId(), LAST_RUNTIME_TYPE_CREATED_KEY);
	}
	
	public void serverChanged(IServer server) {
		// Do Nothing
	}

	public void serverRemoved(IServer server) {
		// Do Nothing
	}

	public void runtimeChanged(IRuntime runtime) {
		// Do Nothing
	}

	public void runtimeRemoved(IRuntime runtime) {
		// Do Nothing
	}


}
