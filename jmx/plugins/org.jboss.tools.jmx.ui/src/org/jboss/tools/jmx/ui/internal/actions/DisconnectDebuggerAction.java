/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.ui.internal.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.action.Action;
import org.jboss.tools.common.jdt.debug.RemoteDebugActivator;
import org.jboss.tools.jmx.core.IDebuggableConnection;
import org.jboss.tools.jmx.ui.JMXUIActivator;

public class DisconnectDebuggerAction extends Action {
	private IDebuggableConnection con;
	public DisconnectDebuggerAction(IDebuggableConnection wrapper) {
		this.con = wrapper;
		setImageDescriptor(JMXUIActivator.getDefault().getSharedImages().descriptor(JMXUIActivator.CONNECT_DEBUGGER_SHARED_IMAGE));
		setEnabled(isDebuggerConnected(wrapper));
		setText("Disconnect Debugger"); //$NON-NLS-1$
	}
	
	/**
	 * Is there already a remote java launch connected to this host/port
	 * @return
	 */
	private boolean isDebuggerConnected(IDebuggableConnection wrapper) {
		return RemoteDebugActivator.isRemoteDebuggerConnected(wrapper.getDebugHost(), wrapper.getDebugPort());
	}

	public void run() {
		ILaunch l = RemoteDebugActivator.getExistingRemoteDebugLaunch(con.getDebugHost(), con.getDebugPort());
		if( !l.isTerminated()) {
			try {
				l.terminate();
			} catch(CoreException ce) {
				JMXUIActivator.log(IStatus.ERROR, "Unable to disconnect debugger", ce); //$NON-NLS-1$
			}
		}
	}
}
