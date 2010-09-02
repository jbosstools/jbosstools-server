/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 * TODO: Logging and Progress Monitors
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.rse.core;

import org.jboss.ide.eclipse.as.core.server.internal.AbstractJBossBehaviourDelegate;

public class RSEBehaviourDelegate extends AbstractJBossBehaviourDelegate {

	@Override
	public void stop(boolean force) {
		if( force ) {
			getActualBehavior().setServerStopped();
			return;
		}
		RSELaunchDelegate.launchStopServerCommand(getActualBehavior());
	}
}
