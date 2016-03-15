/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.wtp.server.launchbar.targets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetProvider;
import org.eclipse.launchbar.core.target.TargetStatus;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.tools.wtp.server.launchbar.Activator;

public class ServerLaunchTargetProvider implements ILaunchTargetProvider, IServerLifecycleListener {

	private static final String TYPE = Activator.TARGET_TYPE_ID;
	
	private ILaunchTargetManager manager = null;
	
	public ServerLaunchTargetProvider() {
		ServerCore.addServerLifecycleListener(this);
	}

	@Override
	public synchronized void init(ILaunchTargetManager targetManager) {
		manager = targetManager;
		IServer[] all = ServerCore.getServers();
		ArrayList<ILaunchTarget> existingTargets = new ArrayList<ILaunchTarget>(
				Arrays.asList(targetManager.getLaunchTargetsOfType(TYPE)));
		for( int i = 0; i < all.length; i++ ) {
			ILaunchTarget t = targetManager.getLaunchTarget(TYPE, all[i].getName());
			if( t == null ) {
				targetManager.addLaunchTarget(TYPE, all[i].getName());
			} else {
				existingTargets.remove(t);
			}
		}
		
		// Remaining targets are extra and have no server existing for them, delete them
		Iterator<ILaunchTarget> it = existingTargets.iterator();
		while(it.hasNext()) {
			ILaunchTarget lt = it.next();
			targetManager.removeLaunchTarget(lt);
		}
	}

	@Override
	public TargetStatus getStatus(ILaunchTarget target) {
		return TargetStatus.OK_STATUS;
	}

	@Override
	public synchronized void serverAdded(IServer server) {
		if( manager != null ) {
			if( server != null ) {
				manager.addLaunchTarget(TYPE, server.getName());
			}
		}
	}

	@Override
	public synchronized void serverChanged(IServer server) {
		if( manager != null ) {
			if( server != null ) {
				ILaunchTarget existing = manager.getLaunchTarget(TYPE,  server.getName());
				if( existing == null ) {
					init(manager);
				}
			}
		}
	}

	@Override
	public synchronized void serverRemoved(IServer server) {
		if( manager != null ) {
			if( server != null ) {
				ILaunchTarget existing = manager.getLaunchTarget(TYPE,  server.getName());
				if( existing != null ) {
					manager.removeLaunchTarget(existing);
				}
			}
		}
	}

}
