/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.as.core.server.controllable.subsystems.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerShutdownController;

public class NoOpShutdownController extends AbstractSubsystemController
		implements IServerShutdownController {

	public NoOpShutdownController() {
	}

	@Override
	public IStatus canStop() {
		return Status.OK_STATUS;
	}

	@Override
	public void stop(boolean force) {
		IControllableServerBehavior beh = getControllableBehavior();
		if( beh != null ) {
			((ControllableServerBehavior)beh).setServerStopping();
			((ControllableServerBehavior)beh).setServerStopped();
		}
	}
}
